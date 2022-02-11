package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherMongoDBRepository extends MongoRepository<TeachersDBObject, String> {

	@Query("{'id' : ?0}")
	TeachersDBObject  findById(Integer id);
}