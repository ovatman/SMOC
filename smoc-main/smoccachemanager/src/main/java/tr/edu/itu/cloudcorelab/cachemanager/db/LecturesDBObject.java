package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

@Document(collection=Utils.DB_COLLECTION_SUB)
public class LecturesDBObject {

    @Id
    public String studentIdwithCRN;
    @Field("studentId")
    private Integer studentId;
    @Field("crn")
    private Integer crn;
    @Field("code")
    private String code;
    @Field("name")
    private String name;
    @Field("grade")
    private String grade;
    @Field("lecturerId")
    private Integer lecturerId;

    @PersistenceConstructor
	public LecturesDBObject(Integer studentId, Integer crn, String code, String name, String grade, Integer lecturerId) {
		this.studentIdwithCRN = Utils.concatStudentIdWithCrn(studentId, crn);
		this.studentId = studentId;
		this.crn = crn;
		this.code = code;
		this.name = name;
		this.grade = grade;
		this.lecturerId = lecturerId;
	}
    
    public String toString()
    {
    	return "[studentId:" + this.studentId
    			+ ", crn:" + this.crn
    			+ ", code:" + this.code
    			+ ", name:" + this.name
    			+ ", grade:" + this.grade
    			+ ", lecturerId:" + this.lecturerId
    			+ "]";
    }
	
	public Integer getStudentId() {
		return studentId;
	}
	public Integer getCrn() {
		return crn;
	}
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String getGrade() {
		return grade;
	}
	public Integer getLecturerId() {
		return lecturerId;
	}
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
	public void setCrn(Integer crn) {
		this.crn = crn;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public void setLecturerId(Integer lecturerId) {
		this.lecturerId = lecturerId;
	}
    
}
