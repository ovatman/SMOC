package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

@Document(collection=Utils.DB_COLLECTION_INFO)
public class StudentInfoDBObject {

    @Id
    public Integer id;
    @Field("fatherName")
    private String fatherName;
    @Field("motherName")
    private String motherName;
    @Field("birthDay")
    private String birthDay;
    @Field("address")
    private String address;

    @PersistenceConstructor
	public StudentInfoDBObject(Integer id, String fatherName, String motherName, String birthDay, String address) {
		this.id = id;
		this.fatherName = fatherName;
		this.motherName = motherName;
		this.birthDay = birthDay;
		this.address = address;
	}
    
    public String toString()
    {
    	return "[studentId:" + id
    			+ ", motherName:" + this.motherName
    			+ ", fatherName:" + this.fatherName
    			+ ", birthDay:" + this.birthDay
    			+ ", address:" + this.address
    			+ "]";
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getMotherName() {
		return motherName;
	}

	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}

	public String getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(String birthDay) {
		this.birthDay = birthDay;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
