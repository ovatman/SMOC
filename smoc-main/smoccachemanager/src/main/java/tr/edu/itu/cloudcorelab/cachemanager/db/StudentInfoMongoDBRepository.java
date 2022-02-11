package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentInfoMongoDBRepository extends MongoRepository<StudentInfoDBObject, String> {

	@Query("{'id' : ?0}")
	StudentInfoDBObject findById(Integer id);
}