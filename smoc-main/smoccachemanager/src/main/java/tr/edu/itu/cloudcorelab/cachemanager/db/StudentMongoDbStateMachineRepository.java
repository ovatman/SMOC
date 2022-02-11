package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentMongoDbStateMachineRepository  extends MongoDbStateMachineRepository {

}
