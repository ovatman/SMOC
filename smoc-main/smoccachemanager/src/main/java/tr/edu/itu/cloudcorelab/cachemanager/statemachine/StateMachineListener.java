package tr.edu.itu.cloudcorelab.cachemanager.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;

import tr.edu.itu.cloudcorelab.cachemanager.comm.CheckpointMessage;
import tr.edu.itu.cloudcorelab.cachemanager.comm.RabbitMQSender;
import tr.edu.itu.cloudcorelab.cachemanager.db.*;
import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@WithStateMachine(name = Utils.STATE_MACHINE)
public class StateMachineListener {
    private File logfile;
    private Queue<EventDirections> m_DirectionQueue;
    
    private Integer m_TeacherId;
    private Integer m_StudentId;
    private Integer m_LessonCrn;

    @Autowired
    private DBService dBService;

	@Autowired
	private RabbitMQSender rabbitMQSender;

    /** Default Constructor **/
    public StateMachineListener(){
    	createLogFile();
    	m_DirectionQueue = new LinkedList<>();
    	storeLocalVariables();
    }
    
    public void storeLocalVariables() {
		TestMesajData msj = Utils.getTestData();
		m_TeacherId = msj.getTeacherId();
		m_StudentId = msj.getStudentId();
		m_LessonCrn = msj.getCrn();
		//System.out.println("m_TeacherId:["+m_TeacherId+"]   m_StudentId:["+m_StudentId+"]   m_LessonCrn:["+m_LessonCrn+"] ");
    }

    public void createLogFile(){
        String path = Paths.get("").toAbsolutePath().toString();
        System.out.println("PATH ---> " + path);
        
        String fd = path + "/logs/log_" + Utils.getTimeStamp() + ".txt" ;
        try
        {
            logfile = new File(fd);
            if (!logfile.exists()) {
                System.out.println("Logfile does not exist. Create new one.");
                logfile.createNewFile();
            }
            else if (logfile.exists()){
                System.out.println("Logfile exists on filesystem. Deletes previous one & creates new one.");
                logfile.delete();
                logfile.createNewFile();
            }
        }
        catch (Exception e){
            System.out.println("Exception occured during creating log file: " + e);
        }

    }

    public void storeEvents(String log){
        try {
            FileWriter fileWriter = new FileWriter(logfile,true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(log);  //New line
            printWriter.close();
        }
        catch (Exception e) {System.out.println("Exception occured during flushing into log file: " + e);}
    }
    
    private void updateAssumptionQueue(EventDirections ed) {
    	if(Utils.ASSUMPTION_AVAILABLITY) {
    		if(Utils.ASSUMPTION_IS_LOCAL) {
    			if(m_DirectionQueue.size() == Utils.ASSUMPTION_LENGTH) {
    				m_DirectionQueue.poll();
    			}
    			m_DirectionQueue.add(ed);
    			
    			if(m_DirectionQueue.size() == Utils.ASSUMPTION_LENGTH) {
    				InetAddress inetAddress;
    				try {
    					inetAddress = InetAddress.getLocalHost();
    					final CheckpointMessage helloMessage = new CheckpointMessage(inetAddress.getHostAddress(),"1" ,m_DirectionQueue);
    					rabbitMQSender.convertAndSend(helloMessage);
    					//rabbitMQSender.convertAndSend_2(helloMessage);
    					//rabbitMQSender.convertAndSend_4(helloMessage);
    					//rabbitMQSender.convertAndSend_5(helloMessage);
    					storeEvents(Utils.getTimeStamp() + " -> Sending Message: " + helloMessage.toString());
    					
    				} catch (UnknownHostException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		}
    		else {
    			m_DirectionQueue = dBService.getDirectionQueue();
				String temp = Utils.printDirectionQueue(m_DirectionQueue);
				storeEvents(Utils.getTimeStamp() + " -> Receiving Message: " + temp);
    			/*
    			System.out.println("--------------##############################################---------");
    			System.out.println(temp);
    			System.out.println("--------------##############################################---------");*/
    		}
    	}
    }

    public boolean calculateAssumptionProbability(EventDirections ed){    	
    	if(!Utils.ASSUMPTION_AVAILABLITY)
    		return false;
    	
    	Map<EventDirections, Integer> map = m_DirectionQueue.stream().
    			collect(Collectors.toConcurrentMap(
    					w -> w, w -> 1, Integer::sum));

    	List<EventDirections> keys = map.entrySet().stream()
    			.filter(e-> e.getValue() >= Utils.ASSUMPTION_THRESHOLD)
    			.map(e -> e.getKey())
    			.collect(Collectors.toList());

    	return (keys.size() > 0) && (ed == keys.get(0));
    }

    public void selectAssumptionProbability(){
    	storeLocalVariables();
    	
    	if(Utils.ASSUMPTION_AVAILABLITY && m_DirectionQueue.size() == Utils.ASSUMPTION_LENGTH){

    		Map<EventDirections, Integer> map = m_DirectionQueue.stream().
    				collect(Collectors.toConcurrentMap(
    						w -> w, w -> 1, Integer::sum));
    		
    		Map<EventDirections, Integer> sortedMap = map.entrySet().stream()
    				.filter(e-> e.getValue() >= Utils.ASSUMPTION_THRESHOLD)
    				.sorted((e1,e2) ->  e2.getValue().compareTo(e1.getValue()))
    				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    		
    		/*for(EventDirections ed : sortedMap.keySet())
    			System.out.println("selectAssumptionProbability    "+ed + ":["+sortedMap.get(ed)+"]");*/
    		
    		if(sortedMap.size() > 0) {
    			selectAssumption(sortedMap);
    		}
    	}
    }
    
    public void selectAssumption(Map<EventDirections, Integer> sortedMap){
    	switch(Utils.ASSUMPTION_TYPE) {
    	case BIGGER_WIN:
    		selectAssumptionBiggerWin(sortedMap);
    		break;
    	case PERCENTAGE_PROBABILITY:
    		selectAssumptionPercentage(sortedMap);
    		break;
    	default:
    		// code block
    	}
    }
    
    public void selectAssumptionPercentage(Map<EventDirections, Integer> sortedMap){
		//System.out.println("selectAssumptionPercentage  size:["+sortedMap.size()+"]");
    	int sortedMapSize = sortedMap.size();
		if(sortedMapSize == 3) {
			dBService.getTeacherFromDB(m_TeacherId, false, true, AssumptionRate.ONE_IN_THREE);
			dBService.getTeacherFromDB(m_TeacherId, true, false, AssumptionRate.ONE_IN_THREE);
			dBService.getTeacherFromDB(m_TeacherId, true, true, AssumptionRate.ONE_IN_THREE);
		}
		else if(sortedMapSize == 2) {
			EventDirections ed = findMissingDirection();
	    	switch(ed) {
	    	case ONLY_STUDENT:
	        	dBService.getTeacherFromDB(m_TeacherId, true, false, AssumptionRate.HALF);
	        	dBService.getTeacherFromDB(m_TeacherId, true, true, AssumptionRate.HALF);
	    		break;
	    	case ONLY_CRN:
	        	dBService.getTeacherFromDB(m_TeacherId, true, true, AssumptionRate.HALF);
	        	dBService.getTeacherFromDB(m_TeacherId, false, true, AssumptionRate.HALF);
	    		break;
	    	case BOTH_OF_THEM:
	        	dBService.getTeacherFromDB(m_TeacherId, false, true, AssumptionRate.HALF);
	        	dBService.getTeacherFromDB(m_TeacherId, true, false, AssumptionRate.HALF);
	    		break;
	    	default:
	    	}

		}
		else {
			selectAssumptionBiggerWin(sortedMap);
		}
    }

    public void selectAssumptionBiggerWin(Map<EventDirections, Integer> sortedMap){
    	EventDirections ed = sortedMap.keySet().iterator().next();
		//System.out.println("selectAssumption     "+ed + ":["+sortedMap.get(ed)+"]");
    	switch(ed) {
    	case ONLY_STUDENT:
        	dBService.getTeacherFromDB(m_TeacherId, false, true, AssumptionRate.FULL);
    		break;
    	case ONLY_CRN:
        	dBService.getTeacherFromDB(m_TeacherId, true, false, AssumptionRate.FULL);
    		break;
    	case BOTH_OF_THEM:
        	dBService.getTeacherFromDB(m_TeacherId, true, true, AssumptionRate.FULL);
    		break;
    	default:
    		// code block
    	}
    }
    
    public EventDirections findMissingDirection() {
    	
    	Map<EventDirections, Integer> map = m_DirectionQueue.stream().
				collect(Collectors.toConcurrentMap(
						w -> w, w -> 1, Integer::sum));
    	List<EventDirections> edList = new ArrayList<EventDirections>();
    	edList.add(EventDirections.BOTH_OF_THEM);
    	edList.add(EventDirections.ONLY_CRN);
    	edList.add(EventDirections.ONLY_STUDENT);

    	for(EventDirections ed : map.keySet()) {
    		if(map.get(ed) < Utils.ASSUMPTION_THRESHOLD)
    			return ed;
    		else if (edList.contains(ed))
    			edList.remove(ed);
    	}
    	
    	return edList.get(0);
    }

    @OnTransition(source = "INITIAL", target = "TEACHER_LESSONS")
    public boolean teacherToLessons_EventTransition(StateContext<States, Events> context) {
    	System.out.println("INITIAL STATE...TEACHER_TO_LESSONS event...TEACHER_TO_LESSONS STATE>>>>>>>>>> teacherId:"+m_TeacherId);
    	dBService.getTeacherFromDB(m_TeacherId, false, false, AssumptionRate.FULL);

        storeEvents(Utils.getTimeStamp() + " >>>>> " +"INITIAL STATE...LESSONS_TO_LECTURE event...TEACHER_TO_LESSONS STATE");
        return true;
    }

    @OnTransition(source = "TEACHER_LESSONS", target = "TEACHER_LESSONS_LS")
    public boolean teacherTo1Lesson_EventTransition(StateContext<States, Events> context) {
        System.out.println("TEACHER_LESSONS STATE...LESSONS_TO_LECTURE event...TEACHER_LESSONS_LS STATE");
        
    	LecturesDBObject lec = dBService.getLecture(m_LessonCrn, m_StudentId);
    	if(lec == null) {
    		lec = new LecturesDBObject(m_StudentId, m_LessonCrn, "BLGXXX", "Dummy Class", "XY", m_TeacherId);
    	}
    	else {
    		lec.setGrade("AA");
    	}
    	dBService.updateLectureOfStudent(lec, m_StudentId);
    	
        storeEvents(Utils.getTimeStamp() + " >>>>> " +"TEACHER_LESSONS STATE...LESSONS_TO_LECTURE event...TEACHER_LESSONS_LS STATE");
        return true;
    }

    @OnTransition(source = "TEACHER_LESSONS_LS", target = "INITIAL")
    public boolean startfromscratch1_EventTransition() {
        System.out.println("TEACHER_LESSONS_LS STATE...startfromscratch event...INITIAL STATE");
        updateAssumptionQueue(EventDirections.ONLY_CRN);
        storeEvents(Utils.getTimeStamp() + " >>>>> " + "TEACHER_LESSONS_LS STATE...startfromscratch event...INITIAL STATE");
        selectAssumptionProbability();
        
        System.out.println(dBService.listLocalStorage());
        return true;
    }

    @OnTransition(source = "INITIAL", target = "TEACHER_STUDENTS")
    public boolean teacherToStudents_EventTransition(StateContext<States, Events> context) {
    	System.out.println("INITIAL STATE...TEACHER_TO_STUDENTS event...TEACHER_STUDENTS STATE  >>>>>>>>>> teacherId:"+m_TeacherId); 
    	dBService.getTeacherFromDB(m_TeacherId, false, false, AssumptionRate.FULL);

        storeEvents(Utils.getTimeStamp() + " >>>>> " +"INITIAL STATE...TEACHER_TO_STUDENTS event..TEACHER_STUDENTS STATE");
        return true;
    }

    @OnTransition(source = "TEACHER_STUDENTS", target = "TEACHER_STUDENTS_ST")
    public boolean teacherTo1Student_EventTransition(StateContext<States, Events> context) {
        System.out.println("TEACHER_STUDENTS STATE...STUDENTS_TO_STUDENT event...TEACHER_STUDENTS_ST STATE  m_StudentId:"+m_StudentId);
        
        dBService.getStudentById(m_StudentId, false, false, m_TeacherId);
        
        storeEvents(Utils.getTimeStamp() + " >>>>> " +"TEACHER_STUDENTS STATE...STUDENTS_TO_STUDENT event..TEACHER_STUDENTS_ST STATE");
        return true;
    }

    @OnTransition(source = "TEACHER_STUDENTS_ST", target = "TEACHER_STUDENTS_ST_INFO")
    public boolean studentInfo_EventTransition(StateContext<States, Events> context) {
        System.out.println("TEACHER_STUDENTS_ST STATE...STUDENT_TO_INFO event...TEACHER_STUDENTS_ST_INFO STATE m_StudentId:"+m_StudentId);
        
    	StudentInfoDBObject sInfo = dBService.getStudentInfo(m_StudentId);
    	if(sInfo == null) {
    		sInfo = new StudentInfoDBObject(m_StudentId, "father_"+m_StudentId, "mother_"+m_StudentId, "DD.MM.YYYY", "XYZ, Turkey");
		}
    	else {
    		sInfo.setFatherName("FATHER_"+m_StudentId);
    	}
        dBService.updateStudentInfo(sInfo);
        
        storeEvents(Utils.getTimeStamp() + " >>>>> " +"TEACHER_STUDENTS_ST STATE...STUDENTS_TO_STUDENT event..TEACHER_STUDENTS_ST_INFO STATE");
        return true;
    }

    @OnTransition(source = "TEACHER_STUDENTS_ST_INFO", target = "INITIAL")
    public boolean startfromscratch2_EventTransition() {
        System.out.println("TEACHER_STUDENTS_ST_INFO STATE...startfromscratch event...INITIAL STATE");
        updateAssumptionQueue(EventDirections.ONLY_STUDENT);
        storeEvents(Utils.getTimeStamp() + " >>>>> " + "TEACHER_STUDENTS_ST_INFO STATE...startfromscratch event...INITIAL STATE");
        selectAssumptionProbability();

        System.out.println(dBService.listLocalStorage());
        return true;
    }


    @OnTransition(source = "TEACHER_STUDENTS_ST", target = "TEACHER_STUDENTS_ST_LECTURES")
    public boolean studentToLectures_EventTransition(StateContext<States, Events> context) {
        System.out.println("TEACHER_STUDENTS_ST STATE...STUDENT_TO_LECTURES event...TEACHER_STUDENTS_ST_LECTURES STATE");

        storeEvents(Utils.getTimeStamp() + " >>>>> " +"TEACHER_STUDENTS_ST STATE...STUDENT_TO_LECTURES event..TEACHER_STUDENTS_ST_LECTURES STATE");
        return true;
    }

    @OnTransition(source = "TEACHER_STUDENTS_ST_LECTURES", target = "TEACHER_STUDENTS_ST_LECTURES_LS")
    public boolean student1Lecture_EventTransition(StateContext<States, Events> context) {
        System.out.println("TEACHER_STUDENTS_ST_LECTURES STATE...LECTURES_TO_LECTURE event...TEACHER_STUDENTS_ST_LECTURES_LS STATE");
        
    	LecturesDBObject lec = dBService.getLecture(m_LessonCrn, m_StudentId);
    	if(lec == null) {
    		lec = new LecturesDBObject(m_StudentId, m_LessonCrn, "BLGXXX", "Dummy Class", "XY", m_TeacherId);
    	}
    	else {
    		lec.setGrade("AA");
    	}
    	dBService.updateLectureOfStudent(lec, m_StudentId);
    	
        storeEvents(Utils.getTimeStamp() + " >>>>> " +"TEACHER_STUDENTS_ST_LECTURES STATE...LECTURES_TO_LECTURE event..TEACHER_STUDENTS_ST_LECTURES_LS STATE");
        return true;
    }

    @OnTransition(source = "TEACHER_STUDENTS_ST_LECTURES_LS", target = "INITIAL")
    public boolean startfromscratch3_EventTransition(StateContext<States, Events> context) {
        System.out.println("TEACHER_STUDENTS_ST_LECTURES_LS STATE...startfromscratch event...INITIAL STATE");
        updateAssumptionQueue(EventDirections.BOTH_OF_THEM);
        storeEvents(Utils.getTimeStamp() + " >>>>> " + "TEACHER_STUDENTS_ST_LECTURES_LS STATE...startfromscratch event...INITIAL STATE");
        selectAssumptionProbability();
        
        System.out.println(dBService.listLocalStorage());
        return true;
    }   

}
