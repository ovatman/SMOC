package tr.edu.itu.cloudcorelab.pathpredictor.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.stereotype.Service;

import tr.edu.itu.cloudcorelab.twittersm.Events;
import tr.edu.itu.cloudcorelab.twittersm.States;

@Service
public class StateMachineWorker
{
	@Autowired
	@Qualifier("zk_sm_factory")
	private StateMachineFactory<States, Events> factory_with_zk;
	
	@Autowired
	private StateMachineEnsemble<States, Events> stateMachineEnsemble;
	
	private List<StateMachine<States, Events>> stateMachines = new ArrayList<>();
	/*
	@SuppressWarnings("rawtypes")
	private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() ->
	{
		final Kryo kryo = new Kryo();
		kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
		kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
		kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
		return kryo;
	});
	*/
	@Autowired
	private Environment environment;
	
	private static final Logger logger = LogManager.getLogger();
	
	@PostConstruct
	public void init()
	{
		final int stateMachineQuantity = Integer.parseInt(environment.getProperty("tsm.state_machine_quantity"));
		
		for (int i = 0; i < stateMachineQuantity; i++)
		{
			StateMachine<States, Events> stateMachine = factory_with_zk.getStateMachine();
			stateMachine.start();
			stateMachineEnsemble.join(stateMachine);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			logger.info("Gracefully stopping...");
			
			this.stateMachines.forEach(StateMachine::stop);
		}));
		
	}
	
	public void sendTweetEvent(Events event, int subjectId, int associatedId, long timestamp, int eventNumber, int timeSleep)
	{
		sendEvent(States.IDLE, States.PROCESSING_TWEET, event, subjectId, associatedId, timestamp, eventNumber, timeSleep);
	}
	
	public void sendFollowEvent(Events event, int subjectId, int associatedId, long timestamp, int eventNumber, int timeSleep)
	{
		sendEvent(States.IDLE, States.PROCESSING_FOLLOW, event, subjectId, associatedId, timestamp, eventNumber, timeSleep);
	}
	
	public void sendProcessEvent(Events event, long timestamp, int eventNumber, int timeSleep)
	{
		sendEvent(event == Events.PROCESSED_FOLLOW ? States.PROCESSING_FOLLOW : States.PROCESSING_TWEET, States.IDLE, event, -1, -1, timestamp, eventNumber, timeSleep);
	}
	
	private void sendEvent(States source, States target, Events event, int subjectId, int associatedId, long timestamp, int eventNumber, int timeSleep)
	{
		for (StateMachine<States, Events> stateMachine : stateMachines)
		{
			final MessageBuilder<Events> builder =
				MessageBuilder.withPayload(event)
				.setHeader("timeSleep", timeSleep)
				.setHeader("machineId", stateMachine.getUuid())
				.setHeader("source", source.toString())
				.setHeader("processedEvent", event.toString())
				.setHeader("target", target.toString())
				.setHeader("subject", subjectId)
				.setHeader("associated", associatedId)
				.setHeader("eventTimestamp", timestamp);
			
			stateMachine.sendEvent(builder.build());
		}
	}
	
/*	public String serializeStateMachineContext()
	{
		final StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
		
		if (context == null)
		{
			logger.warn("+++++++++ CONTEXT IS NULL, SERIALIZATION FAILED ++++++++++");
			return "NULL_SMOC_CONTEXT";
		}
		
		final Kryo kryo = kryoThreadLocal.get();
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final Output output = new Output(stream);
		
		kryo.writeObject(output, context);
		output.close();
		
		return new String(stream.toByteArray(), StandardCharsets.UTF_8);
	}*/
}
