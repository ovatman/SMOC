package tr.edu.itu.cloudcorelab.cachemanager.db;

/** DTO = Data Transfer Object */

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;


@Document(collection=Utils.DB_COLLECTION)
public class StudentsDBObject {

    @Id
    public Integer id;
    @Field("name")
    protected String name;
    @Field("email")
    protected String email;
    @Field("term")
    protected Integer term;
    @Field("advisorId")
    protected Integer advisorId;
    
    public StudentsDBObject(Students s) {
        this.id = s.id;
        this.name = s.name;
        this.email= s.email;
        this.term = s.term;
        this.advisorId = s.advisorId;
    }  
    
    @PersistenceConstructor
    public StudentsDBObject(Integer id, String name, String email, Integer term, Integer advisorId) {
        this.id = id;
        this.name = name;
        this.email=email;
        this.term = term;
        this.advisorId = advisorId;
    }   

    
    public String toString()
    {
    	return "{id:" + this.id
    			+ ", name:" + this.name
    			+ ", email:" + this.email
    			+ ", term:" + this.term
    			+ ", advisorId:" + this.advisorId
    			+ "}";
    }
    
	public void setTerm(Integer term) {
		this.term = term;
	}
	
	public Integer getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getEmail() {
		return email;
	}
	public Integer getTerm() {
		return term;
	}
	public Integer getAdvisorId() {
		return advisorId;
	}
}
