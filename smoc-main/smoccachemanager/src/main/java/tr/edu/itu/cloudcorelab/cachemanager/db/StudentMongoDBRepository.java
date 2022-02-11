package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentMongoDBRepository extends MongoRepository<StudentsDBObject, String> {
	
	@Query("{'id' : ?0}")
	StudentsDBObject findById(Integer id);
	
	@Query("{'advisorId' : ?0}")
	List<StudentsDBObject> findByAdvisorId(Integer advisorId);

}