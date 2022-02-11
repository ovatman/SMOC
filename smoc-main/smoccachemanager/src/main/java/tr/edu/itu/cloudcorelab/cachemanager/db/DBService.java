package tr.edu.itu.cloudcorelab.cachemanager.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

@Service
public class DBService {

    static final Logger logger = LoggerFactory.getLogger(DBService.class);
    private Queue<EventDirections> m_DirectionQueue;
    
    Random rand = new Random(); 

    @Autowired
    private StudentMongoDBHandler studentDbObjectHandler;

    @Autowired
    private StudentInfoMongoDBHandler studentInfoDbObjectHandler;

    @Autowired
    private LectureMongoDBHandler lectureDbObjectHandler;

    @Autowired
    private TeacherMongoDBHandler teacherDbObjectHandler;

    private Map<Integer, Students> m_stdntMap;
    private Integer fifoIndex = 0;
    private Integer cashMissCounter;
    private ReplacementPolicy m_ReplacementPolicy = Utils.CASH_TYPE;

    @PostConstruct
    public void init() {
        logger.info("Initializing a new list in order to store s in memory ...");
        m_stdntMap = new HashMap<Integer,Students>();
        cashMissCounter = 0;
    	m_DirectionQueue = new LinkedList<>();
    }

    public void setStudent(Students student, boolean withUpdatingLessonDB, boolean withUpdatingInfoDB) {
    	studentDbObjectHandler.insertStudent(student);
    	addStudentToCash(student, withUpdatingLessonDB, withUpdatingInfoDB);
    }
	
    private Students addStudentDBToCash(StudentsDBObject newStudentDB, boolean withGettingLessonsFromDB, boolean withGettingStudentInfo) {
    	Students newStudent = new Students(newStudentDB);

    	if(withGettingStudentInfo) {
    		StudentInfoDBObject sInfo = studentInfoDbObjectHandler.findById(newStudentDB.getId());
    		newStudent.setInfo(sInfo);
    	}
    	
    	if(withGettingLessonsFromDB) {
    		List<LecturesDBObject> lessons = lectureDbObjectHandler.findByStudentId(newStudent.getId());
    		Students updatedStudent = newStudent.setLessonsGetStudent(lessons);
    		addStudentToCash(updatedStudent, false, false);
    		return updatedStudent;
    	}
    	else {
    		addStudentToCash(newStudent, false, false);
    		return newStudent;
    	}
    }
	
    private void addStudentToCash(Students newStudent, boolean withUpdatingLessonDB, boolean withUpdatingInfoDB) {
    	if(newStudent != null) {
    		if(withUpdatingLessonDB) {
        		for(LecturesDBObject l : newStudent.getLessons()) {
        			lectureDbObjectHandler.insertLecture(l);
        		}
    		}
    		
    		if(withUpdatingInfoDB) {
    			StudentInfoDBObject sInfo = newStudent.getInfo();
    			if(sInfo != null) {
    				studentInfoDbObjectHandler.insertStudentInfo(newStudent.getInfo());
    			}
    		}
    		
            if(m_stdntMap.containsKey(newStudent.getId())) {
            	newStudent.updateTimestamp();
            	newStudent.setCounter(m_stdntMap.get(newStudent.getId()).getCounter() + 1);
                newStudent.setIndex(m_stdntMap.get(newStudent.getId()).getIndex()); 
                //logger.info("Same item will be removed = {}",newStudent.getId());
            	m_stdntMap.remove(newStudent.getId());
            }
            else {
            	removePolicy();
                newStudent.setIndex(++fifoIndex); 
            }
            m_stdntMap.put(newStudent.getId(), newStudent);
    	}
    }
	
    private void removePolicy() {
    	if(m_stdntMap.size() == Utils.CASH_SIZE) {
    		switch(m_ReplacementPolicy) {
    		case RANDOM:
    			removeNInMemory_Random();
    			break;
    		case FIFO:
    			removeNInMemory_FIFO();
    			break;
    		case LRU:
    			removeNInMemory_LRU();
    			break;
    		case LFU:
    			removeNInMemory_LFU();
    			break;
    		default:
    			// code block
    		}
    	}
    }
    
    private void removeNInMemory_FIFO(){
    	Entry<Integer, Students> minEntry = getFIFOkey();
    	int key =  minEntry.getKey();
    	//logger.info("FIFO item will be removed = {}",key);
    	m_stdntMap.remove(key);
    }   
    public Entry<Integer, Students> getFIFOkey() {
    	Entry<Integer, Students> minEntry = Collections.min(m_stdntMap.entrySet(), new Comparator<Entry<Integer, Students>>() {
    	    public int compare(Entry<Integer, Students> entry1, Entry<Integer, Students> entry2) {
    	    	return entry1.getValue().getIndex().compareTo(entry2.getValue().getIndex());
    	    }
    	});
        return minEntry;
    }
    
    private void removeNInMemory_Random(){
    	Object[] keyArrays =  m_stdntMap.keySet().toArray();
    	int randKey = rand.nextInt(keyArrays.length);
    	//logger.info("Random item will be removed = {}",keyArrays[randKey]);
    	m_stdntMap.remove(keyArrays[randKey]);
    }
    
    private void removeNInMemory_LRU(){
    	int key =  getLRUkey();
    	//logger.info("LRU item will be removed = {}",key);
    	m_stdntMap.remove(key);
    }
    private int getLRUkey() {
    	Entry<Integer, Students> minEntry = Collections.min(m_stdntMap.entrySet(), new Comparator<Entry<Integer, Students>>() {
    	    public int compare(Entry<Integer, Students> entry1, Entry<Integer, Students> entry2) {
    	    	return Utils.firstTimeStampOccursAfterSecond(entry1.getValue().getTimestamp(), entry2.getValue().getTimestamp());
    	    }
    	});
        return minEntry.getKey();
    }

    private void removeNInMemory_LFU(){
    	Entry<Integer, Students> minEntry = getLFUkey();
    	int key =  minEntry.getKey();
    	//logger.info("LFU item will be removed = {}",key);
    	m_stdntMap.remove(key);
    }
    public Entry<Integer, Students> getLFUkey() {
    	Entry<Integer, Students> minEntry = Collections.min(m_stdntMap.entrySet(), new Comparator<Entry<Integer, Students>>() {
    	    public int compare(Entry<Integer, Students> entry1, Entry<Integer, Students> entry2) {
    	    	return entry1.getValue().getCounter().compareTo(entry2.getValue().getCounter());
    	    }
    	});
        return minEntry;
    }
    
   /* public void saveLocal_All() {
		List<StudentsDBObject> studentsDB = studentDbObjectHandler.getAllStudents();
    	for(StudentsDBObject stDB : studentsDB) {
    		addStudentDBToCash(stDB, true);
    	}
    	
    }*/
    
    public String listLocalStorage() {
		String temp = "\n---------------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!---------------------";
		/*for (Integer i : m_stdntMap.keySet()) {
			temp += "\n"+ "key: " + i + " value: \n" + m_stdntMap.get(i);
		}*/
		temp += "\n \n \n cashMissCounter:["+cashMissCounter+"]   \n \n";
		temp += "\n---------------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!---------------------\n";
		return temp;
	}
	
	public void updateLectureOfStudent(LecturesDBObject lecture, int studentId) {
		if(lecture!= null && m_stdntMap.containsKey(studentId)) {
			Students st = m_stdntMap.get(studentId);
			st.setLesson(lecture);
			lectureDbObjectHandler.insertLecture(lecture);
			addStudentToCash(st, false, false);
        }
	}
	
	public void updateStudentInfo(StudentInfoDBObject sInfo) {
		if(m_stdntMap.containsKey(sInfo.getId())) {
			Students st = m_stdntMap.get(sInfo.getId());
			st.setInfo(sInfo);
			addStudentToCash(st, false, true);
        }
	}
	
	public StudentInfoDBObject getStudentInfo(int studentId) {
		StudentInfoDBObject sInfo = null;
		Students st = null;
		if(m_stdntMap.containsKey(studentId)) {
			st = m_stdntMap.get(studentId);
        }
		else {
			StudentsDBObject newStudentDB = studentDbObjectHandler.findById(studentId);
			if(newStudentDB != null) {
				increaseCashMissCounter();
				st = new Students(newStudentDB);
			}
		}
		if(st != null) {
			sInfo = st.getInfo();
			if(sInfo == null) {
				sInfo = studentInfoDbObjectHandler.findById(studentId);
				if(sInfo != null) {		
					increaseCashMissCounter();			
					st.setInfo(sInfo);
					addStudentToCash(st, false, false);
				}
			}
		}
		return sInfo;
	}
	
	public LecturesDBObject getLecture(int crn, int studentId) {
		LecturesDBObject lec = null;
		Students st = null;
		if(m_stdntMap.containsKey(studentId)) {
			st = m_stdntMap.get(studentId);
        }
		else {
			StudentsDBObject newStudentDB = studentDbObjectHandler.findById(studentId);
			if(newStudentDB != null) {
				increaseCashMissCounter();
				st = new Students(newStudentDB);
			}
		}
		if(st != null) {
			lec = st.getLesson(crn);
			if(lec == null) {
				lec = lectureDbObjectHandler.findByStudentIdwithCRN(crn, studentId);
				if(lec != null) {		
					increaseCashMissCounter();			
					st.setLesson(lec);
					addStudentToCash(st, false, false);
				}
			}
		}
		return lec;
	}
	
	public Students getStudentById(int id, boolean withGettingLessonsFromDB, boolean withGettingStudentInfo, int teacherId) {
		if(!withGettingLessonsFromDB && m_stdntMap.containsKey(id)) {
        	return m_stdntMap.get(id);
        }
		else {
			StudentsDBObject newStudent = studentDbObjectHandler.findById(id);
			if(newStudent == null) {
				newStudent = new StudentsDBObject(id, "Unkonwn", "Unkonwn", 0, teacherId);
			}
			increaseCashMissCounter();
	    	return addStudentDBToCash(newStudent, withGettingLessonsFromDB, withGettingStudentInfo);
		}
	}
	
	public void getTeacherFromDBWithPercentage(int advisorId, boolean isHalf, EventDirections notIncluded) {
		List<StudentsDBObject> studentDBs = studentDbObjectHandler.findByAdvisorId(advisorId);
		boolean stInsert = true;
		for(StudentsDBObject stDB : studentDBs) {
			if(stInsert) {
				Students newStudent = new Students(stDB);

				boolean isStudentInfoNotIncluded = isHalf && notIncluded == EventDirections.ONLY_STUDENT;
				if(!isStudentInfoNotIncluded) {
					StudentInfoDBObject sInfo = studentInfoDbObjectHandler.findById(stDB.getId());
					newStudent.setInfo(sInfo);
				}

				List<LecturesDBObject> lessons = lectureDbObjectHandler.findByStudentId(newStudent.getId());
				boolean lecInsert = true;
				for(LecturesDBObject l: lessons) {
					if(lecInsert) {
						newStudent.setLesson(l);
						lecInsert = isStudentInfoNotIncluded;
					}
					else {
						lecInsert = true;
					}
				}

				addStudentToCash(newStudent, false, false);
				stInsert = false;
			}
			else {
				stInsert = true;
			}
		}
	}
	
	public Teachers getTeacherFromDB(int id, boolean withGettingLessonsFromDB, boolean withGettingStudentInfo, AssumptionRate aR) {
		Teachers teacher = new Teachers (teacherDbObjectHandler.findById(id));
		if(withGettingStudentInfo) {
			teacher.setStudent(getStudentsByTeacherIdFromDB(id, withGettingLessonsFromDB, !withGettingLessonsFromDB, aR));
		} 
		else if(withGettingLessonsFromDB) {
			teacher.setStudent(getLecturesStudentsByAdvisorId(id, aR));
		}
		return teacher;
	}
	
	public List<Students> getStudentsByTeacherIdFromDB(int advisorId, boolean withGettingLessonsFromDB, boolean withGettingStudentInfo, AssumptionRate aR) {
		List<StudentsDBObject> studentDBs = studentDbObjectHandler.findByAdvisorId(advisorId);

    	List<Students> students = new ArrayList<Students>();
    	
    	int effectedSize = Utils.getEffectedSize(aR, studentDBs.size());
    	
    	for(int i = 0; i < effectedSize; i++) {
    		StudentsDBObject stDB = studentDBs.get(i);
    		students.add(addStudentDBToCash(stDB, withGettingLessonsFromDB, withGettingStudentInfo));
    	}
    	
		return students;
	}
	
	public List<Students> getLecturesStudentsByAdvisorId(int advisorId, AssumptionRate aR) {
    	Map<Integer, Students> students = new HashMap<Integer, Students>();
		List<LecturesDBObject> lessons = lectureDbObjectHandler.findByLecturerId(advisorId);

    	int effectedSize = Utils.getEffectedSize(aR, lessons.size());
    	
    	for(int i = 0; i < effectedSize; i++) {
    		LecturesDBObject l = lessons.get(i);
    		int studentId = l.getStudentId();
    		
    		if(!m_stdntMap.containsKey(studentId)) {
    			StudentsDBObject stDB = studentDbObjectHandler.findById(studentId);
    			if(stDB == null) {
    				stDB = new StudentsDBObject(studentId, "Unknown", "Unknown", 0, l.getLecturerId());
    			}        		
    			Students newStudent = new Students(stDB);
        		newStudent.setLesson(l);
        		addStudentToCash(newStudent, false, false);
        		students.put(studentId, newStudent);
    		}
    		else {
    			updateLectureOfStudent(l, studentId);
    			if(students.containsKey(studentId)) {
    				students.remove(studentId);
    			}
    			Students updatedStudent =  m_stdntMap.get(studentId);
        		students.put(studentId, updatedStudent);
    		}
    	}
		return students.values().stream()
                .collect(Collectors.toList());
	}
	
	public Queue<EventDirections> getDirectionQueue() {
		return m_DirectionQueue;
	}

	public void setDirectionQueue(Queue<EventDirections> directionQueue) {
		this.m_DirectionQueue = directionQueue;
	}
	
	public void increaseCashMissCounter()
	{
		cashMissCounter++;
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void test_addDB() {

		int studentId = Utils.TEST_CASES_START_DB_STUDENTS_ID;
		int studentId2 = studentId + Utils.TEST_CASES_MAX_TH_PERSON;
		int studentId3 = studentId2 + Utils.TEST_CASES_MAX_TH_PERSON;
		int teacherId = Utils.TEST_CASES_START_DB_TEACHER_ID;
		int crn = Utils.TEST_CASES_MIN_DB_LESSON_CRN;
		
		TeachersDBObject t0 =  new TeachersDBObject(teacherId, "Unkown_"+teacherId , "Dr.");
		teacherDbObjectHandler.insertTeacher(t0);
		
		LecturesDBObject l0 = new LecturesDBObject(studentId, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
		lectureDbObjectHandler.insertLecture(l0);
		LecturesDBObject l02 = new LecturesDBObject(studentId2, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
		lectureDbObjectHandler.insertLecture(l02);
		LecturesDBObject l03 = new LecturesDBObject(studentId3, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
		lectureDbObjectHandler.insertLecture(l03);
		
		StudentsDBObject s0= new StudentsDBObject(studentId, "Name_"+studentId, "empty", 5, teacherId);
		studentDbObjectHandler.insertStudent(s0);
		StudentsDBObject s02= new StudentsDBObject(studentId2, "Name_"+studentId2, "empty", 5, teacherId);
		studentDbObjectHandler.insertStudent(s02);
		StudentsDBObject s03= new StudentsDBObject(studentId3, "Name_"+studentId3, "empty", 5, teacherId);
		studentDbObjectHandler.insertStudent(s03);
		
		StudentInfoDBObject si0 = new StudentInfoDBObject(studentId, "father_"+studentId, "mother_"+studentId, "DD.MM.YYYY", "XYZ, Turkey");
		studentInfoDbObjectHandler.insertStudentInfo(si0);
		StudentInfoDBObject si02 = new StudentInfoDBObject(studentId2, "father_"+studentId2, "mother_"+studentId2, "DD.MM.YYYY", "XYZ, Turkey");
		studentInfoDbObjectHandler.insertStudentInfo(si02);
		StudentInfoDBObject si03 = new StudentInfoDBObject(studentId3, "father_"+studentId3, "mother_"+studentId3, "DD.MM.YYYY", "XYZ, Turkey");
		studentInfoDbObjectHandler.insertStudentInfo(si03);
		
		for(int i = 1; i<Utils.TEST_CASES_MAX_TH_PERSON+1; i++) {
			studentId = i + Utils.TEST_CASES_START_DB_STUDENTS_ID;
			studentId2 = studentId + Utils.TEST_CASES_MAX_TH_PERSON;
			studentId3 = studentId2 + Utils.TEST_CASES_MAX_TH_PERSON;
			teacherId = i + Utils.TEST_CASES_START_DB_TEACHER_ID;
			crn = i + Utils.TEST_CASES_MIN_DB_LESSON_CRN;

			TeachersDBObject t =  new TeachersDBObject(teacherId, "Unkown_"+teacherId , "Dr.");
			teacherDbObjectHandler.insertTeacher(t);
			
			LecturesDBObject l1 = new LecturesDBObject(studentId, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
			lectureDbObjectHandler.insertLecture(l1);
			crn = crn + 1000;
			LecturesDBObject l2 = new LecturesDBObject(studentId - 1, crn, "BLG5XXX", "Unknown_"+crn, "G", teacherId + 1);
			lectureDbObjectHandler.insertLecture(l2);
			crn = crn + 1000;
			LecturesDBObject l3 = new LecturesDBObject(studentId + 1, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId + 2);
			lectureDbObjectHandler.insertLecture(l3);
			crn = crn + 1000;
			LecturesDBObject l4 = new LecturesDBObject(studentId + 2, crn, "BLG5XXX", "Unknown_"+crn, "G", teacherId + 3);
			lectureDbObjectHandler.insertLecture(l4);
			
			LecturesDBObject l12 = new LecturesDBObject(studentId2, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
			lectureDbObjectHandler.insertLecture(l12);
			crn = crn + 1000;
			LecturesDBObject l22 = new LecturesDBObject(studentId2 - 1, crn, "BLG5XXX", "Unknown_"+crn, "G", teacherId + 1);
			lectureDbObjectHandler.insertLecture(l22);
			crn = crn + 1000;
			LecturesDBObject l32 = new LecturesDBObject(studentId2 + 1, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId + 2);
			lectureDbObjectHandler.insertLecture(l32);
			crn = crn + 1000;
			LecturesDBObject l42 = new LecturesDBObject(studentId2 + 2, crn, "BLG5XXX", "Unknown_"+crn, "G", teacherId + 3);
			lectureDbObjectHandler.insertLecture(l42);
			
			LecturesDBObject l13 = new LecturesDBObject(studentId3, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
			lectureDbObjectHandler.insertLecture(l13);
			crn = crn + 1000;
			LecturesDBObject l23 = new LecturesDBObject(studentId3 - 1, crn, "BLG5XXX", "Unknown_"+crn, "G", teacherId + 1);
			lectureDbObjectHandler.insertLecture(l23);
			crn = crn + 1000;
			LecturesDBObject l33 = new LecturesDBObject(studentId3 + 1, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId + 2);
			lectureDbObjectHandler.insertLecture(l33);
			crn = crn + 1000;
			LecturesDBObject l43 = new LecturesDBObject(studentId3 + 2, crn, "BLG5XXX", "Unknown_"+crn, "G", teacherId + 3);
			lectureDbObjectHandler.insertLecture(l43);
			
			StudentsDBObject s1= new StudentsDBObject(studentId, "Name_"+studentId, "empty", 5, teacherId);
			studentDbObjectHandler.insertStudent(s1);
			StudentsDBObject s12= new StudentsDBObject(studentId2, "Name_"+studentId2, "empty", 5, teacherId);
			studentDbObjectHandler.insertStudent(s12);
			StudentsDBObject s13= new StudentsDBObject(studentId3, "Name_"+studentId3, "empty", 5, teacherId);
			studentDbObjectHandler.insertStudent(s13);
			
			StudentInfoDBObject si = new StudentInfoDBObject(studentId, "father_"+studentId, "mother_"+studentId, "DD.MM.YYYY", "XYZ, Turkey");
			studentInfoDbObjectHandler.insertStudentInfo(si);
			StudentInfoDBObject si2 = new StudentInfoDBObject(studentId2, "father_"+studentId2, "mother_"+studentId2, "DD.MM.YYYY", "XYZ, Turkey");
			studentInfoDbObjectHandler.insertStudentInfo(si2);
			StudentInfoDBObject si3 = new StudentInfoDBObject(studentId3, "father_"+studentId3, "mother_"+studentId3, "DD.MM.YYYY", "XYZ, Turkey");
			studentInfoDbObjectHandler.insertStudentInfo(si3);
		}
	}

	public void test_addDB2() {

		int studentId = Utils.TEST_CASES_START_DB_STUDENTS_ID;
		int studentId2 = studentId + Utils.TEST_CASES_MAX_TH_PERSON;
		int studentId3 = studentId2 + Utils.TEST_CASES_MAX_TH_PERSON;
		int teacherId = Utils.TEST_CASES_START_DB_TEACHER_ID;
		int crn = Utils.TEST_CASES_MIN_DB_LESSON_CRN;
		
		TeachersDBObject t0 =  new TeachersDBObject(teacherId, "Unkown_"+teacherId , "Dr.");
		teacherDbObjectHandler.insertTeacher(t0);
		
		LecturesDBObject l0 = new LecturesDBObject(studentId, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
		lectureDbObjectHandler.insertLecture(l0);
		LecturesDBObject l02 = new LecturesDBObject(studentId2, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
		lectureDbObjectHandler.insertLecture(l02);
		LecturesDBObject l03 = new LecturesDBObject(studentId3, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
		lectureDbObjectHandler.insertLecture(l03);
		
		StudentsDBObject s0= new StudentsDBObject(studentId, "Name_"+studentId, "empty", 5, teacherId);
		studentDbObjectHandler.insertStudent(s0);
		StudentsDBObject s02= new StudentsDBObject(studentId2, "Name_"+studentId2, "empty", 5, teacherId);
		studentDbObjectHandler.insertStudent(s02);
		StudentsDBObject s03= new StudentsDBObject(studentId3, "Name_"+studentId3, "empty", 5, teacherId);
		studentDbObjectHandler.insertStudent(s03);
		
		StudentInfoDBObject si0 = new StudentInfoDBObject(studentId, "father_"+studentId, "mother_"+studentId, "DD.MM.YYYY", "XYZ, Turkey");
		studentInfoDbObjectHandler.insertStudentInfo(si0);
		StudentInfoDBObject si02 = new StudentInfoDBObject(studentId2, "father_"+studentId2, "mother_"+studentId2, "DD.MM.YYYY", "XYZ, Turkey");
		studentInfoDbObjectHandler.insertStudentInfo(si02);
		StudentInfoDBObject si03 = new StudentInfoDBObject(studentId3, "father_"+studentId3, "mother_"+studentId3, "DD.MM.YYYY", "XYZ, Turkey");
		studentInfoDbObjectHandler.insertStudentInfo(si03);
		
		for(int i = 1; i<Utils.TEST_CASES_MAX_TH_PERSON+1; i++) {
			studentId = i + Utils.TEST_CASES_START_DB_STUDENTS_ID;
			studentId2 = studentId + Utils.TEST_CASES_MAX_TH_PERSON;
			studentId3 = studentId2 + Utils.TEST_CASES_MAX_TH_PERSON;
			teacherId = i + Utils.TEST_CASES_START_DB_TEACHER_ID;
			crn = i + Utils.TEST_CASES_MIN_DB_LESSON_CRN;

			TeachersDBObject t =  new TeachersDBObject(teacherId, "Unkown_"+teacherId , "Dr.");
			teacherDbObjectHandler.insertTeacher(t);
			
			for(int j = 0; j<10; j++) {
				int studentIx = j * Utils.TEST_CASES_MAX_TH_PERSON;
				LecturesDBObject l1 = new LecturesDBObject(studentId + studentIx, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
				lectureDbObjectHandler.insertLecture(l1);
				crn = crn + 1000;
				LecturesDBObject l12 = new LecturesDBObject(studentId2 + studentIx, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
				lectureDbObjectHandler.insertLecture(l12);
				crn = crn + 1000;
				LecturesDBObject l13 = new LecturesDBObject(studentId3 + studentIx, crn, "BLG5XXXE", "Unknown_"+crn, "G", teacherId);
				lectureDbObjectHandler.insertLecture(l13);
				crn = crn + 1000;
						
			}
			
			StudentsDBObject s1= new StudentsDBObject(studentId, "Name_"+studentId, "empty", 5, teacherId);
			studentDbObjectHandler.insertStudent(s1);
			StudentsDBObject s12= new StudentsDBObject(studentId2, "Name_"+studentId2, "empty", 5, teacherId);
			studentDbObjectHandler.insertStudent(s12);
			StudentsDBObject s13= new StudentsDBObject(studentId3, "Name_"+studentId3, "empty", 5, teacherId);
			studentDbObjectHandler.insertStudent(s13);
			
			StudentInfoDBObject si = new StudentInfoDBObject(studentId, "father_"+studentId, "mother_"+studentId, "DD.MM.YYYY", "XYZ, Turkey");
			studentInfoDbObjectHandler.insertStudentInfo(si);
			StudentInfoDBObject si2 = new StudentInfoDBObject(studentId2, "father_"+studentId2, "mother_"+studentId2, "DD.MM.YYYY", "XYZ, Turkey");
			studentInfoDbObjectHandler.insertStudentInfo(si2);
			StudentInfoDBObject si3 = new StudentInfoDBObject(studentId3, "father_"+studentId3, "mother_"+studentId3, "DD.MM.YYYY", "XYZ, Turkey");
			studentInfoDbObjectHandler.insertStudentInfo(si3);
		}
	}


	public void test_addDB3() {

		TeachersDBObject t =  new TeachersDBObject(21, "Unkown_"+21 , "Dr.");
		teacherDbObjectHandler.insertTeacher(t);

		for(int i = 1; i<500; i++) {
			StudentsDBObject s1= new StudentsDBObject(i, "Name_"+i, "empty", 5, 21);
			studentDbObjectHandler.insertStudent(s1);

			StudentInfoDBObject si = new StudentInfoDBObject(i, "father_"+i, "mother_"+i, "DD.MM.YYYY", "XYZ, Turkey");
			studentInfoDbObjectHandler.insertStudentInfo(si);
			
			for(int j = 1; j<500; j++) {
				LecturesDBObject l1 = new LecturesDBObject(i, j, "BLG5XXXE", "Unknown_"+j, "G", 21);
				lectureDbObjectHandler.insertLecture(l1);
			}
		}
	}

}

