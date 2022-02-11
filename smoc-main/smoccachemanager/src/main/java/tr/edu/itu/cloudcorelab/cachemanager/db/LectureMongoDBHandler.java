package tr.edu.itu.cloudcorelab.cachemanager.db;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import tr.edu.itu.cloudcorelab.cachemanager.utils.Utils;

import java.util.List;


@Component
public class LectureMongoDBHandler {

    @Autowired
    private LectureMongoDBRepository LectureMongoDBRepository;

    // INSERT
    public LecturesDBObject insertLecture(@NotNull LecturesDBObject lecturesDBObject) {
        //System.out.println("LectureMongoDBHandler - DBOBJECTHANDLER::INSERT Lecture");
        /*try {
        	return LectureMongoDBRepository.insert(lecturesDBObject);
        } catch(DuplicateKeyException ex) {
        	return updateLecture(lecturesDBObject);
        } catch(Exception ex) {
            //System.out.println("LectureMongoDBHandler - Can not insert :(");
            //System.out.println("LectureMongoDBHandler - Exception...");
            ex.printStackTrace();
            return null;
        }*/
    	return lecturesDBObject;
    }

    // UPDATE
    public LecturesDBObject updateLecture(LecturesDBObject lecturesDBObject) {
        //System.out.println("LectureMongoDBHandler - UPDATE Lecture");
        return LectureMongoDBRepository.save(lecturesDBObject);
    }

    //GET ALL
    public List<LecturesDBObject> getAllLectures(){
        //System.out.println("LectureMongoDBHandler.getAllLectures():: GET ALL LectureS");
        return LectureMongoDBRepository.findAll();
    }

    //GET depending on events
    public List<LecturesDBObject> findByStudentId(Integer studentId){
        //System.out.println("LectureMongoDBHandler.findByStudentId():: {"+studentId+ "}");
        return LectureMongoDBRepository.findByStudentId(studentId);
    }

    //GET depending on events
    public List<LecturesDBObject> findByLecturerId(Integer lecturertId){
        //System.out.println("LectureMongoDBHandler.findByLecturerId():: {"+lecturertId+ "}");
        return LectureMongoDBRepository.findByLecturerId(lecturertId);
    }

    //GET depending on events
    public List<LecturesDBObject> findByCrn(Integer crn){
        //System.out.println("LectureMongoDBHandler.findByCrn():: {"+crn+ "}");
        return LectureMongoDBRepository.findByCrn(crn);
    }

    //GET depending on events
    public LecturesDBObject findByStudentIdwithCRN(Integer crn, Integer studentId){
        //System.out.println("LectureMongoDBHandler.findByStudentIdwithCRN():: {"+ Utils.concatStudentIdWithCrn(studentId, crn) + "}");
        return LectureMongoDBRepository.findByStudentIdwithCRN(Utils.concatStudentIdWithCrn(studentId, crn));
    }


}
