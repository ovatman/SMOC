package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureMongoDBRepository extends MongoRepository<LecturesDBObject, String> {
	
	@Query("{'_id' : ?0}")
	LecturesDBObject findByStudentIdwithCRN(String id);
	
	@Query("{'studentId' : ?0}")
	List<LecturesDBObject>  findByStudentId(Integer studentId);
	
	@Query("{'crn' : ?0}")
	List<LecturesDBObject>  findByCrn(Integer crn);
	
	@Query("{'lecturerId' : ?0}")
	List<LecturesDBObject>  findByLecturerId(Integer lecturerId);
}