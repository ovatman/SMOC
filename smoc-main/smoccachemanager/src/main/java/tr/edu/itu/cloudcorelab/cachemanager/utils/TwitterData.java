package tr.edu.itu.cloudcorelab.cachemanager.utils;

public class TwitterData {

	private Integer studentId;
	private Integer teacherId;
	private Integer crn;
	private EventDirections ed;
	
	public TwitterData(Integer studentId, Integer teacherId, Integer crn, EventDirections ed) {
		this.studentId = studentId;
		this.teacherId = teacherId;
		this.crn = crn;
		this.ed = ed;
	}
	public Integer getStudentId() {
		return studentId;
	}
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
	public Integer getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}
	public Integer getCrn() {
		return crn;
	}
	public void setCrn(Integer crn) {
		this.crn = crn;
	}
	public EventDirections getEd() {
		return ed;
	}
	public void setEd(EventDirections ed) {
		this.ed = ed;
	}
	
}
