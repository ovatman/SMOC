package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

import java.util.List;

@Component
public class StudentMongoDBHandler {

    @Autowired
    private StudentMongoDBRepository studentMongoDBRepository;

    // INSERT
    public StudentsDBObject insertStudent(@NotNull Students student) {
    	StudentsDBObject studentsDBObject = new StudentsDBObject(student);
    	return insertStudent(studentsDBObject);
    }
    // INSERT
    public StudentsDBObject insertStudent(@NotNull StudentsDBObject studentsDBObject) {
        //System.out.println("StudentMongoDBHandler - DBOBJECTHANDLER::INSERT Student");
        try {
        	return studentMongoDBRepository.insert(studentsDBObject);
        } catch(DuplicateKeyException ex) {
        	return updateStudent(studentsDBObject);
        } catch(Exception ex) {
            //System.out.println("StudentMongoDBHandler - Can not insert :(");
            //System.out.println("StudentMongoDBHandler - Exception...");
            ex.printStackTrace();
            return null;
        }
    }

    // UPDATE
    public StudentsDBObject updateStudent(StudentsDBObject studentsDBObject) {
        //System.out.println("StudentMongoDBHandler - UPDATE Student");
        return studentMongoDBRepository.save(studentsDBObject);
    }

    //GET ALL
    public List<StudentsDBObject> getAllStudents(){
        //System.out.println("StudentMongoDBHandler.getAllStudents():: GET ALL StudentS");
        return studentMongoDBRepository.findAll();
    }

    //GET depending on events
    public StudentsDBObject findById(Integer id){
        //System.out.println("StudentMongoDBHandler.findById():: {"+id+ "}");
        return studentMongoDBRepository.findById(id);
    }

    //GET depending on events
    public List<StudentsDBObject> findByAdvisorId(Integer advisorId){
        //System.out.println("StudentMongoDBHandler.findByAdvisorId():: {"+advisorId+ "}");
        return studentMongoDBRepository.findByAdvisorId(advisorId);
    }

}
