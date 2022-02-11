package tr.edu.itu.cloudcorelab.geocoordinator;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import tr.edu.itu.cloudcorelab.geocoordinator.communication.RPCServer;
import tr.edu.itu.cloudcorelab.geocoordinator.config.ClusterConfig;
import tr.edu.itu.cloudcorelab.geocoordinator.sm.StateMachineConfig;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.statemachine.StateMachine;

public class Application {

	public static void main(String[] args) throws Exception {
		int port = 8980;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		ObjectMapper mapper = new ObjectMapper();
		
		StateMachineConfig.conf = mapper.readValue(new File("config.json"), ClusterConfig.class);

		System.out.println(StateMachineConfig.conf.me.toString());

		StateMachine<String, String> machine = new AnnotationConfigApplicationContext(StateMachineConfig.class).getBean(StateMachine.class);
		machine.start();

		RPCServer server = new RPCServer(port, machine);
		server.start();

		server.blockUntilShutdown();

	}
}