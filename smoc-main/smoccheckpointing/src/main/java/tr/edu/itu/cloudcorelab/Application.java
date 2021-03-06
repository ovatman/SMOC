package tr.edu.itu.cloudcorelab.checkpointing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import tr.edu.itu.cloudcorelab.checkpointing.entity.Events;
import tr.edu.itu.cloudcorelab.checkpointing.entity.States;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private StateMachine<States, Events> stateMachine;

    @Override
    public void run(String... args) throws Exception {
        /* Reads timesleep argument */
        String[] argument = args[0].split("=");
        int timeSleep = Integer.parseInt(argument[1]);

        stateMachine.start();
        Message<Events> messagePay = MessageBuilder
                .withPayload(Events.PAY)
                .setHeader("timeSleep", timeSleep)
                .build();
        stateMachine.sendEvent(messagePay);

        Message<Events> messageReceive = MessageBuilder
                .withPayload(Events.RECEIVE)
                .setHeader("timeSleep", timeSleep)
                .build();
        stateMachine.sendEvent(messageReceive);

        Message<Events> messageStartFromScratch = MessageBuilder
                .withPayload(Events.STARTFROMSCRATCH)
                .setHeader("timeSleep", timeSleep)
                .build();
        stateMachine.sendEvent(messageStartFromScratch);

        stateMachine.stop();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
