package tr.edu.itu.cloudcorelab.cachemanager.utils;

public class TestMesajData {

	private Integer studentId;
	private Integer teacherId;
	private Integer crn;
	
	public TestMesajData(Integer studentId, Integer teacherId, Integer crn) {
		this.studentId = studentId;
		this.teacherId = teacherId;
		this.crn = crn;
	}
	
	public Integer getStudentId() {
		return studentId;
	}
	public Integer getTeacherId() {
		return teacherId;
	}
	public Integer getCrn() {
		return crn;
	}
	
}
