package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import tr.edu.itu.cloudcorelab.cachemanager.utils.Utils;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Component
public class TeacherMongoDBHandler {

    @Autowired
    private TeacherMongoDBRepository TeacherMongoDBRepository;

    // INSERT
    public TeachersDBObject insertTeacher(@NotNull TeachersDBObject TeachersDBObject) {
        //System.out.println("TeacherMongoDBHandler - DBOBJECTHANDLER::INSERT Teacher");
        try {
        	return TeacherMongoDBRepository.insert(TeachersDBObject);
        } catch(DuplicateKeyException ex) {
        	return updateTeacher(TeachersDBObject);
        } catch(Exception ex) {
            //System.out.println("TeacherMongoDBHandler - Can not insert :(");
            //System.out.println("TeacherMongoDBHandler - Exception...");
            ex.printStackTrace();
            return null;
        }
    }

    // UPDATE
    public TeachersDBObject updateTeacher(TeachersDBObject TeachersDBObject) {
        //System.out.println("TeacherMongoDBHandler - UPDATE Teacher");
        return TeacherMongoDBRepository.save(TeachersDBObject);
    }

    //GET ALL
    public List<TeachersDBObject> getAllTeachers(){
        //System.out.println("TeacherMongoDBHandler.getAllTeachers():: GET ALL TeacherS");
        return TeacherMongoDBRepository.findAll();
    }

    //GET depending on events
    public TeachersDBObject findById(Integer id){
        //System.out.println("TeacherMongoDBHandler.findById():: {"+id+ "}");
        return TeacherMongoDBRepository.findById(id);
    }
    


}
