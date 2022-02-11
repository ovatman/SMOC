package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

@Document(collection=Utils.DB_COLLECTION_PARENT)
public class TeachersDBObject {

    @Id
    public Integer id;
    @Field("name")
    protected String name;
    @Field("job")
    protected String job; //title

    @PersistenceConstructor
	public TeachersDBObject(Integer id, String name, String job) {
		this.name = name;
		this.id = id;
		this.job = job;
	}
	
	public Integer getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getJob() {
		return job;
	}
	public void setid(Integer id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setJob(String job) {
		this.job = job;
	}
    
}
