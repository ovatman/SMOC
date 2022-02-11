package tr.edu.itu.cloudcorelab.cachemanager.db;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;


public class Students extends StudentsDBObject{

    private List<LecturesDBObject> lessons;
    private StudentInfoDBObject info;
    private String timestamp;
    private Integer counter;
    private Integer index;

    public Students(StudentsDBObject s) {
    	super(s.id, s.name, s.email, s.term, s.advisorId);

        this.lessons = new ArrayList<LecturesDBObject>();
        this.info = new StudentInfoDBObject(s.id, "", "", "", "");
        this.timestamp = Utils.getTimeStampForDB();
        this.counter = 1;
        this.index = 0;
    }
    

    public Students(Integer id, String name, String email, Integer term, Integer advisorId) {
    	super(id, name, email, term, advisorId);

        this.lessons = new ArrayList<LecturesDBObject>();
        this.info = new StudentInfoDBObject(id, "", "", "", "");
        this.timestamp = Utils.getTimeStampForDB();
        this.counter = 1;
        this.index = 0;
    }
    
    public String toString()
    {
    	return "{id:" + this.id
    			+ ", timestamp:" + this.timestamp
    			+ ", name:" + this.name
    			+ ", email:" + this.email
    			+ ", term:" + this.term
    			+ ", advisorId:" + this.advisorId
    			+ ", lessons:" + lessonsToString()
    			+ ", info:" + this.info.toString()
    			+ ", counter: " + this.counter
    			+ ", index: " + this.index
    			+ "}";
    }
    
    public String lessonsToString()
    {
    	String temp = "{";
    	for(LecturesDBObject l : this.lessons)
    		temp = temp + ", \n \t"+ l.toString();
    	temp += "\n}";
    	return temp;
    }

	public Integer getCounter() {
		return counter;
	}
	public void setCounter(Integer counter) {
		this.counter = counter;
	}	
	public void incrementCounter() {
		this.counter++;
	}	

	public void setIndex(Integer index) {
		this.index = index;
	}	
	public Integer getIndex() {
		return index;
	}	

	public List<LecturesDBObject> getLessons() {
		return lessons;
	}

	public LecturesDBObject getLesson(int crn) {
		List<LecturesDBObject> tempList = lessons
				.stream()
				.filter(w-> w.getCrn()==crn)
				.collect(Collectors.toList());
		return tempList.size() > 0 ? tempList.get(0) : null;
	}
	public void clearUpdateLessons(List<LecturesDBObject> lessons) {
		this.lessons = lessons;
	}
	public void setLessons(List<LecturesDBObject> lessons) {
		if(lessons.size() < Utils.LESSON_CASH_SIZE) {
			clearUpdateLessons(lessons);
		} 
		else {
			for(int i=0; i < Utils.LESSON_CASH_SIZE; i++) {

				this.lessons.add(lessons.get(i));
			}
		}
	}
	public Students setLessonsGetStudent(List<LecturesDBObject> lessons) {
		setLessons(lessons);
		return this;
	}
	public void setLesson(LecturesDBObject lesson) {
		if(this.lessons != null && this.lessons.contains(lesson)) {
			this.lessons.remove(lesson);
		}
		else if(this.lessons.size() == Utils.LESSON_CASH_SIZE) {
			this.lessons.remove(0);
		}
		this.lessons.add(lesson);
	}

	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public void updateTimestamp() {
		this.timestamp = Utils.getTimeStampForDB();
	}

	public StudentInfoDBObject getInfo() {
		return info;
	}
	public void setInfo(StudentInfoDBObject info) {
		this.info = info;
	}
}
