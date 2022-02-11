package tr.edu.itu.cloudcorelab.cachemanager.db;

import java.util.ArrayList;
import java.util.List;

public class Teachers extends TeachersDBObject{

    private List<Students> students;

	public Teachers(TeachersDBObject t) {
		super(t.id, t.name, t.job);
        this.students = new ArrayList<Students>();
	}
	
	public Teachers(Integer id, String name, String job) {
		super(id, name, job);
        this.students = new ArrayList<Students>();
	}
    
    public String toString()
    {
    	return "[id:" + id
    			+ ", name:" + this.name
    			+ ", title:" + this.job
    			+ ", students:" + studentsToString()
    			+ "]";
    }
    
    public String studentsToString()
    {
    	String temp = "{";
    	for(Students s : this.students)
    		temp = temp + ", \n \t"+ s.toString();
    	temp += "\n}";
    	return temp;
    }
    
	public List<Students> getStudent() {
		return students;
	}
	public void setStudent(List<Students> students) {
		this.students = students;
	}
	public Teachers setStudentGetTeacher(List<Students> students) {
		this.students = students;
		return this;
	}
	public void setStudent(Students student) {
		if(this.students.contains(student)) {
			this.students.remove(student);
			this.students.add(student);
		}
	}
}
