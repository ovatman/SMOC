package tr.edu.itu.cloudcorelab.twittersm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.zookeeper.ZookeeperStateMachineEnsemble;

import tr.edu.itu.cloudcorelab.twittersm.util.TweetActivity;
import tr.edu.itu.cloudcorelab.twittersm.util.User;

@Configuration
@EnableStateMachineFactory(name = "zk_sm_factory")
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events>
{
	@Override
	public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception
	{
		final Action<States, Events> initAction = context ->
		{
			Map<Object, Object> vars = context.getExtendedState().getVariables();
			
			vars.put("users", new HashMap<Integer, User>());
		};
		
		states
			.withStates()
				.initial(States.IDLE, initAction)
				.states(EnumSet.allOf(States.class));
	}
	
	@Override
	public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception
	{
		config.withDistributed()
			.ensemble(stateMachineEnsemble());
	}
	
	@Bean
	public StateMachineEnsemble<States, Events> stateMachineEnsemble() throws Exception
	{
		return new ZookeeperStateMachineEnsemble<States, Events>(curatorClient(), "/zkPath");
	}
	
	@Bean
	public CuratorFramework curatorClient()
	{
		String zkConnectionString = "zookeeper:2181";
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder()
			.defaultData(new byte[0])
			.retryPolicy(retryPolicy)
			.connectString(zkConnectionString)
			.build();
		client.start();
		
		return client;
	}
	
	@Override
	public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception
	{
		final Action<States, Events> tweetAction = context ->
		{
			MessageHeaders headers = context.getMessageHeaders();
			
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			
			@SuppressWarnings("unchecked")
			Map<Integer, User> users = (Map<Integer, User>) variables.get("users");
			
			User poster = users.computeIfAbsent(headers.get("subject", Integer.class), User::new);
			User associated = users.computeIfAbsent(headers.get("associated", Integer.class), User::new);
			long timestamp = headers.get("eventTimestamp", Long.class);
			
			TweetActivity tweet = new TweetActivity(poster, associated, timestamp, null);
			
			poster.timeline.add(tweet);
			poster.followers.forEach(f -> f.timeline.add(tweet));
			associated.timeline.add(tweet);
		};
		
		final Action<States, Events> followAction = context ->
		{
			MessageHeaders headers = context.getMessageHeaders();
			
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			
			@SuppressWarnings("unchecked")
			Map<Integer, User> users = (Map<Integer, User>) variables.get("users");
			
			User follower = users.computeIfAbsent(headers.get("subject", Integer.class), User::new);
			User followed = users.computeIfAbsent(headers.get("associated", Integer.class), User::new);
			
			// TODO unfollowing
			
			follower.following.add(followed);
			followed.followers.add(follower);
		};
		
		final Action<States, Events> transitionAction = context ->
		{
			MessageHeaders headers = context.getMessageHeaders();
			
		//	Map<Object, Object> variables = context.getExtendedState().getVariables();
			
			long sleep = ((Number) headers.get("timeSleep")).longValue();
			String processedEvent = headers.get("processedEvent").toString();
			UUID uuid = UUID.fromString(headers.get("machineId").toString());
			
			Application.LOGGER.info("Event {} is processed by SM: {}", processedEvent, uuid.toString());
			Application.LOGGER.info("UUID: " + context.getStateMachine().getUuid());
			
			try
			{
				TimeUnit.MILLISECONDS.sleep(sleep);
			}
			catch (InterruptedException e)
			{
				Application.LOGGER.info("Exception during sleep: " + e.toString());
			}
		};
		
		transitions
			.withExternal()
				.source(States.IDLE)
				.target(States.PROCESSING_TWEET)
				.event(Events.TWEET)
				.action(tweetAction)
				.and()
			.withExternal()
				.source(States.IDLE)
				.target(States.PROCESSING_TWEET)
				.event(Events.RETWEET)
				.action(tweetAction)
				.and()
			.withExternal()
				.source(States.PROCESSING_TWEET)
				.target(States.IDLE)
				.event(Events.PROCESSED_TWEET)
				.action(transitionAction)
				.and()
			.withExternal()
				.source(States.IDLE)
				.target(States.PROCESSING_FOLLOW)
				.event(Events.FOLLOW)
				.action(followAction)
				.and()
			.withExternal()
				.source(States.IDLE)
				.target(States.PROCESSING_FOLLOW)
				.event(Events.UNFOLLOW)
				.action(followAction)
				.and()
			.withExternal()
				.source(States.PROCESSING_FOLLOW)
				.target(States.IDLE)
				.event(Events.PROCESSED_FOLLOW)
				.action(transitionAction);
	}
}
