package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class StudentInfoMongoDBHandler {

    @Autowired
    private StudentInfoMongoDBRepository StudentInfoMongoDBRepository;

    // INSERT
    public StudentInfoDBObject insertStudentInfo(@NotNull StudentInfoDBObject StudentInfoDBObject) {
        //System.out.println("StudentInfoMongoDBHandler - DBOBJECTHANDLER::INSERT StudentInfo");
        try {
        	return StudentInfoMongoDBRepository.insert(StudentInfoDBObject);
        } catch(DuplicateKeyException ex) {
        	return updateStudentInfo(StudentInfoDBObject);
        } catch(Exception ex) {
            //System.out.println("StudentInfoMongoDBHandler - Can not insert :(");
            //System.out.println("StudentInfoMongoDBHandler - Exception...");
            ex.printStackTrace();
            return null;
        }
    }

    // UPDATE
    public StudentInfoDBObject updateStudentInfo(StudentInfoDBObject StudentInfoDBObject) {
        //System.out.println("StudentInfoMongoDBHandler - UPDATE StudentInfo");
        return StudentInfoMongoDBRepository.save(StudentInfoDBObject);
    }

    //GET ALL
    public List<StudentInfoDBObject> getAllStudentInfo(){
        //System.out.println("StudentInfoMongoDBHandler.getAllStudentInfo():: GET ALL StudentInfo");
        return StudentInfoMongoDBRepository.findAll();
    }

    //GET depending on events
    public StudentInfoDBObject findById(Integer id){
        //System.out.println("StudentInfoMongoDBHandler.findById():: {"+studentId+ "}");
        return StudentInfoMongoDBRepository.findById(id);
    }

}
