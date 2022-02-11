package tr.edu.itu.cloudcorelab.cachemanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Utils {
	/* State machine */
	public static final String STATE_MACHINE = "DistributedStateMachine";

	/* Database */
	public static final String DB_COLLECTION = "Students";
	public static final String DB_COLLECTION_INFO = "StudentInfo";
	public static final String DB_COLLECTION_SUB = "Lessons";
	public static final String DB_COLLECTION_PARENT = "Teachers";
	public static final int TEST_CASES_MAX_ST_PERSON = 4800;
	public static final int TEST_CASES_MAX_TH_PERSON = 1600;
	public static final int TEST_CASES_START_DB_STUDENTS_ID = 111111111;
	public static final int TEST_CASES_START_DB_TEACHER_ID = 21;
	public static final int TEST_CASES_MIN_DB_LESSON_CRN = 15001;

	/* Rabbit MQ */
	public final static String QUEUE = "my-test-queue";
	public final static String ROUTE_KEY = "my-route-key";
	public static final String EXCHANGE = "my-direct-exchange";
	public final static String ROUTE_KEY_2 = "second-route";
	public static final String EXCHANGE_2 = "second-exchange";
	public final static String ROUTE_KEY_5 = "fifth-route";
	public static final String EXCHANGE_5 = "fifth-exchange";

	public final static String ROUTE_KEY_4 = "forth-route";
	public static final String EXCHANGE_4 = "forth-exchange";


	/* Generate random integers in range 0 to 999  */
	public static final int RANDOM_RANGE= 1000;

	/* Cash size for DB operations*/
	public static final int CASH_SIZE = 20;
	public static final int LESSON_CASH_SIZE = 5;
	public static final ReplacementPolicy CASH_TYPE = ReplacementPolicy.RANDOM;

	/* Assumption for State Machine Transitions */
	public static final boolean ASSUMPTION_AVAILABLITY = true;
	public static final boolean ASSUMPTION_IS_LOCAL = true;
	public static final int ASSUMPTION_LENGTH = 20;
	public static final int ASSUMPTION_THRESHOLD = 13;
	public static final AssumptionType ASSUMPTION_TYPE = AssumptionType.PERCENTAGE_PROBABILITY;
	public static final String fileEvents = "D:\\Thesis_Projects\\thesis\\utils\\ex8\\data\\e501-1.txt";

	public static EventDirections getRandomDirection(Random rand) {
		int randKey = rand.nextInt(4);
		if(randKey == 1) {
			return EventDirections.ONLY_STUDENT;
		}
		else if(randKey == 2) {
			return EventDirections.ONLY_CRN;
		}
		else {
			return EventDirections.BOTH_OF_THEM;
		}
	}
	
	public static Integer getEffectedSize(AssumptionRate aR, Integer size) {
		if(aR == AssumptionRate.HALF) {
			return size / 2;
		}
		else if(aR == AssumptionRate.ONE_IN_THREE) {
			return size / 3;
		}
		else {
			return size;
		}
	}

	public static String concatStudentIdWithCrn(int studentId, int crn){
		return studentId + "_" + crn;
	}

	public static String getTimeStamp(){
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int ms = now.get(Calendar.MILLISECOND);

		String ts = year + "." + month + "." +  day + "_" + hour + "." + minute + "." + second + "." + ms;
		return ts;
	}

	public static String getTimeStampForDB(){
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int ms = now.get(Calendar.MILLISECOND);

		String ts = year + "-" + (month<10 ? "0": "" ) + month + "-" +  (day<10 ? "0": "" ) + day + "_" 
				+ (hour<10 ? "0": "" ) + hour + ":" + (minute<10 ? "0": "" ) + minute + ":" + (second<10 ? "0": "" ) + second + "_" + ms;
		return ts;
	}

	public static int firstTimeStampOccursAfterSecond(String timeStamp1, String timeStamp2){
		String sDate1 = timeStamp1.split("_")[0];
		String sDate2 = timeStamp1.split("_")[0];
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
		Date d1;
		try {
			d1 = sdformat.parse(sDate1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		Date d2;
		try {
			d2 = sdformat.parse(sDate2);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 1;
		}
		int comparedValue = d1.compareTo(d2);
		if(comparedValue > 0) {
			return 1;
		} 
		else if(comparedValue < 0) {
			return -1;
		} 
		else {
			String sTime1 = timeStamp1.split("_")[1];
			String sTime2 = timeStamp1.split("_")[1];
			LocalTime time1 = LocalTime.parse(sTime1); 
			LocalTime time2  = LocalTime.parse(sTime2); 
			comparedValue = time1.compareTo(time2); 
			if(comparedValue > 0) {
				return 1;
			} 
			else if(comparedValue < 0) {
				return -1;
			} 
			else {
				Integer ms1 = Integer.parseInt(timeStamp1.split("_")[2]);
				Integer ms2 = Integer.parseInt(timeStamp2.split("_")[2]);
				return ms1.compareTo(ms2); 
			}
		}
	}
	
	public static TestMesajData getTestData() {
		Random rand = new Random(); 
		int th = rand.nextInt(Utils.TEST_CASES_MAX_TH_PERSON);
		int teacherId = th + Utils.TEST_CASES_START_DB_TEACHER_ID;

		int stPoss = rand.nextInt(3);
		int studentId = teacherId + TEST_CASES_START_DB_STUDENTS_ID - TEST_CASES_START_DB_TEACHER_ID 
				+ (stPoss * Utils.TEST_CASES_MAX_TH_PERSON);
		
		int crnPoss = rand.nextInt(3);
		int crnPoss2 = rand.nextInt(10);
		int crn = teacherId + TEST_CASES_MIN_DB_LESSON_CRN - TEST_CASES_START_DB_TEACHER_ID 
				+ (crnPoss * Utils.TEST_CASES_MAX_TH_PERSON)
				+ (crnPoss2 * Utils.TEST_CASES_MAX_TH_PERSON);

		TestMesajData msj = new TestMesajData(studentId, teacherId, crn);
		return msj;
	}
	
	public static String printDirectionQueue(Queue<EventDirections> directionQueue) 
	{
		String temp = "{";
		for(EventDirections ed : directionQueue)
			temp = temp + ed + ",";
		temp += "}";

		return temp;
	}
}
