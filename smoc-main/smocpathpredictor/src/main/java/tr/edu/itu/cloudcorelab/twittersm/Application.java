package tr.edu.itu.cloudcorelab.twittersm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import tr.edu.itu.cloudcorelab.twittersm.util.TweetActivity;
import tr.edu.itu.cloudcorelab.pathpredictor.service.StateMachineWorker;
import tr.edu.itu.cloudcorelab.pathpredictor.util.SmocUtil;

@SpringBootApplication
//@ImportResource({ "classpath*:channel-config.xml" })
//@PropertySource(value = { "classpath:application.properties" })
@ComponentScan(basePackages = { "tr.edu.itu.akyuzj.twittersm", "tr.edu.itu.bbf.cloudcore.distributed" })
//@EnableMongoRepositories(basePackageClasses = CheckpointRepository.class)
public class Application implements CommandLineRunner
{
	public static final Logger LOGGER = LogManager.getLogger();
	
	@Autowired
	private StateMachineWorker worker;
	
	@Autowired
	private Environment environment;
	
//	@Autowired
//	private Reporter reporter;
	
	private int totalEvents = 0;
	
	@Override
	public void run(String... args)
	{
		final int sleep = Integer.parseInt(environment.getProperty("tsm.sleep_time"));
		
		final int eventLimit = Integer.parseInt(environment.getProperty("tsm.event_quantity"));
		
	//	reporter.logMemoryFootprint();
		
		worker.getClass();
		
		final long delta = SmocUtil.measure(() ->
		{
			try
			{
				Files.lines(Paths.get("higgs-social_network.edgelist")).allMatch(line ->
				{
					if (totalEvents >= eventLimit)
					{
						return false;
					}
					
					String[] ids = line.split(" ");
					
					worker.sendFollowEvent(Events.FOLLOW, Integer.parseInt(ids[0]), Integer.parseInt(ids[1]), -1, totalEvents++, sleep);
					
					worker.sendProcessEvent(Events.PROCESSED_FOLLOW, -1, totalEvents++, sleep);
					
					LOGGER.info(totalEvents);
					
					return true;
				});
			}
			catch (IOException e)
			{
				LOGGER.error("Failed to open edgelist file: {}", e);
			}
			
			try
			{
				Files.lines(Paths.get("higgs-activity_time.txt")).allMatch(line ->
				{
					if (totalEvents >= eventLimit)
					{
						return false;
					}
					
					String[] ids = line.split(" ");
					
					TweetActivity.Type type = TweetActivity.Type.valueOf(ids[3]);
					
					worker.sendFollowEvent(type == TweetActivity.Type.RT ? Events.RETWEET : Events.TWEET, Integer.parseInt(ids[0]), Integer.parseInt(ids[1]), Integer.parseInt(ids[2]), totalEvents++, sleep);
					
					worker.sendProcessEvent(Events.PROCESSED_TWEET, -1, totalEvents++, sleep);
					
					LOGGER.info(totalEvents);
					
					return true;
				});
			}
			catch (IOException e)
			{
				LOGGER.error("Failed to open activity time file: {}", e);
			}
		});
		
		LOGGER.info("Finished in {} seconds", (float) delta / 1000);
		
		/* Store delta memory usage */
	//	reporter.logMemoryFootprint();
	}
	
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}
}
