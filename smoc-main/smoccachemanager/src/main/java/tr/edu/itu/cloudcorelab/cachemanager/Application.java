package tr.edu.itu.cloudcorelab.cachemanager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;
import tr.edu.itu.cloudcorelab.cachemanager.db.*;
import tr.edu.itu.cloudcorelab.cachemanager.statemachine.*;
import tr.edu.itu.cloudcorelab.cachemanager.comm.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Paths;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
/*@ImportResource({"classpath*:channel-config.xml"})
@PropertySource(value={"classpath:application.properties"})
@ComponentScan(basePackages = {"com.example.orderservice"})*/
@EnableMongoRepositories(basePackageClasses=StudentMongoDBRepository.class)
public class Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private RabbitMQSender rabbitMQSender;

	@Autowired
	private StateMachine<States, Events> paymentStateMachine;
	
	private Map<EventDirections, Integer> m_TestCounter;
	private Random m_Random = new Random(); 
	private Map<Integer,TwitterData> m_ReadingEvents;

    @Autowired
    private DBService dBService;
	@Override
	public void run(String... args) throws Exception {
		long startTime = System.currentTimeMillis();
		
		m_ReadingEvents = new HashMap<Integer,TwitterData>();
		readingEventFromFiles();
		
		m_TestCounter = new HashMap<EventDirections, Integer>();
		m_TestCounter.put(EventDirections.ONLY_STUDENT, 0);
		m_TestCounter.put(EventDirections.ONLY_CRN, 0);
		m_TestCounter.put(EventDirections.BOTH_OF_THEM, 0);
		int timeSleep = 100;
		stateMachineTester(timeSleep);
		
		long endTime = System.currentTimeMillis();
		long totalTime = (endTime - startTime) / 1000;
		System.out.println("---------------------------total time: " + totalTime);

		//rabbitMQtest();
	}

	private void rabbitMQtest() throws Exception {
		Queue<EventDirections> directionQueue  = new LinkedList<>();
		for(int i= 0; i<20; i++) {
			if(i%3 == 0)
				directionQueue.add(EventDirections.ONLY_CRN);
			else if(i%2 == 0)
				directionQueue.add(EventDirections.ONLY_STUDENT);
			else
				directionQueue.add(EventDirections.BOTH_OF_THEM);
		}
		
		InetAddress inetAddress = InetAddress.getLocalHost();
		final CheckpointMessage helloMessage = new CheckpointMessage(inetAddress.getHostAddress(),inetAddress.getHostName() ,directionQueue);
		rabbitMQSender.convertAndSend(helloMessage);
		final CheckpointMessage helloMessage2 = new CheckpointMessage(inetAddress.getHostAddress(),inetAddress.getHostName() );
		rabbitMQSender.convertAndSend_2(helloMessage2);
		final CheckpointMessage helloMessage1 = new CheckpointMessage(inetAddress.getHostAddress(),inetAddress.getHostName() ,directionQueue);
		rabbitMQSender.convertAndSend(helloMessage1);
		final CheckpointMessage helloMessage22 = new CheckpointMessage(inetAddress.getHostAddress(),inetAddress.getHostName() );
		rabbitMQSender.convertAndSend_2(helloMessage22);
	}

	public void stateMachineTester(int timeSleep) throws Exception {
		paymentStateMachine.start();

		for(int i = 0; i<m_ReadingEvents.size(); i++)
			testCases(timeSleep,i);

		System.out.println("ASSUMPTION_AVAILABLITY:["+Utils.ASSUMPTION_AVAILABLITY+ "]  ASSUMPTION_THRESHOLD:["+Utils.ASSUMPTION_THRESHOLD+"]  ASSUMPTION_TYPE:["+Utils.ASSUMPTION_TYPE+"]" );
		
		System.out.println("counter of " + EventDirections.BOTH_OF_THEM + " is " + m_TestCounter.get(EventDirections.BOTH_OF_THEM));
		System.out.println("counter of " + EventDirections.ONLY_STUDENT + " is " + m_TestCounter.get(EventDirections.ONLY_STUDENT));
		System.out.println("counter of " + EventDirections.ONLY_CRN + " is " + m_TestCounter.get(EventDirections.ONLY_CRN));
		System.out.println("Total test steps::["+m_ReadingEvents.size()+"]");

			
		paymentStateMachine.stop();
	}
	public void readingEventFromFiles() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(Utils.fileEvents));
			String sline = reader.readLine();
			int index = 0;
			while (sline != null ) {
				String[] arr = sline.split(" ");
				//System.out.println(index+":" + Integer.valueOf(arr[0]) + "," + Integer.valueOf(arr[1]) + "," + arr[2]+ "," + arr[3] );
				
				EventDirections eds = EventDirections.BOTH_OF_THEM;;
				// read next line
				/*if(arr[3].contains("RE"))
					eds = EventDirections.BOTH_OF_THEM;
				else */if(arr[3].contains("MT"))
					eds = EventDirections.ONLY_CRN;
				else if(arr[3].contains("RT"))
					eds = EventDirections.ONLY_STUDENT;
				
				TwitterData td = new TwitterData(Integer.valueOf(arr[0])%500, 21, Integer.valueOf(arr[1])%500, eds);

				m_ReadingEvents.put(index, td);
				index++;
				sline = reader.readLine();
			}
			reader.close();
			System.out.println("-------------------------------------index:["+index+"]  size:["+m_ReadingEvents.size()+"]");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void testCases(int timeSleep, int index) {
		TwitterData td = m_ReadingEvents.get(index);
		EventDirections ed = td.getEd();
		System.out.println(">>>>>>>>> TestCase : " + ed);
		m_TestCounter.merge(ed, 1, Integer::sum);
		switch(ed) {
		case ONLY_STUDENT:
			testCase_onlyStudentDirection(timeSleep, td);
			break;
		case ONLY_CRN:
			testCase_onlyCrnDirection(timeSleep, td);
			break;
		case BOTH_OF_THEM:
			testCase_bothDirection(timeSleep, td);
			break;
		default:
			// code block
		}
	}

	public void testCase_onlyStudentDirection(int timeSleep, TwitterData td) {
		int eventNumber = 0;

		Message<Events> message1 = MessageBuilder
				.withPayload(Events.TEACHER_TO_STUDENTS)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "TEACHER_TO_STUDENTS")
				.setHeader("source", "INITIAL")
				.setHeader("target", "TEACHER_STUDENTS")
				.setHeader("twitter", td)
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message1);

		Message<Events> message2 = MessageBuilder
				.withPayload(Events.STUDENTS_TO_STUDENT)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "STUDENTS_TO_STUDENT")
				.setHeader("source", "TEACHER_STUDENTS")
				.setHeader("target", "TEACHER_STUDENTS_ST")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message2);

		Message<Events> message3 = MessageBuilder
				.withPayload(Events.STUDENT_TO_INFO)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "STUDENT_TO_INFO")
				.setHeader("source", "TEACHER_STUDENTS_ST")
				.setHeader("target", "TEACHER_STUDENTS_ST_INFO")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message3);

		Message<Events> message5 = MessageBuilder
				.withPayload(Events.STARTFROMSCRATCH)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "STARTFROMSCRATCH")
				.setHeader("source", "TEACHER_STUDENTS_ST_INFO")
				.setHeader("target", "INITIAL")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message5);
	}

	public void testCase_onlyCrnDirection(int timeSleep, TwitterData td) {
		int eventNumber = 0;

		Message<Events> message1 = MessageBuilder
				.withPayload(Events.TEACHER_TO_LESSONS)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "TEACHER_TO_LESSONS")
				.setHeader("source", "INITIAL")
				.setHeader("target", "TEACHER_LESSONS")
				.setHeader("twitter", td)
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message1);

		Message<Events> message3 = MessageBuilder
				.withPayload(Events.LESSONS_TO_LECTURE)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "LESSONS_TO_LECTURE")
				.setHeader("source", "TEACHER_LESSONS")
				.setHeader("target", "TEACHER_LESSONS_LS")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message3);

		Message<Events> message5 = MessageBuilder
				.withPayload(Events.STARTFROMSCRATCH)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "STARTFROMSCRATCH")
				.setHeader("source", "TEACHER_LESSONS_LS")
				.setHeader("target", "INITIAL")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message5);
	}

	public void testCase_bothDirection(int timeSleep, TwitterData td) {
		int eventNumber = 0;
		Message<Events> message1 = MessageBuilder
				.withPayload(Events.TEACHER_TO_STUDENTS)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "TEACHER_TO_STUDENTS")
				.setHeader("source", "INITIAL")
				.setHeader("target", "TEACHER_STUDENTS")
				.setHeader("twitter", td)
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message1);

		Message<Events> message2 = MessageBuilder
				.withPayload(Events.STUDENTS_TO_STUDENT)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "STUDENTS_TO_STUDENT")
				.setHeader("source", "TEACHER_STUDENTS")
				.setHeader("target", "TEACHER_STUDENTS_ST")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message2);

		Message<Events> message3 = MessageBuilder
				.withPayload(Events.STUDENT_TO_LECTURES)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "STUDENT_TO_LECTURES")
				.setHeader("source", "TEACHER_STUDENTS_ST")
				.setHeader("target", "TEACHER_STUDENTS_ST_LECTURES")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message3);

		Message<Events> message4 = MessageBuilder
				.withPayload(Events.LECTURES_TO_LECTURE)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "LECTURES_TO_LECTURE")
				.setHeader("source", "TEACHER_STUDENTS_ST_LECTURES")
				.setHeader("target", "TEACHER_STUDENTS_ST_LECTURES_LS")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message4);

		Message<Events> message5 = MessageBuilder
				.withPayload(Events.STARTFROMSCRATCH)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", paymentStateMachine.getUuid())
				.setHeader("processedEvent", "STARTFROMSCRATCH")
				.setHeader("source", "TEACHER_STUDENTS_ST_LECTURES_LS")
				.setHeader("target", "INITIAL")
				.setHeader("context", "CONTEXT_TEST" + eventNumber)
				.setHeader("eventNumber",++eventNumber)
				.build();
		paymentStateMachine.sendEvent(message5);
	}

}
