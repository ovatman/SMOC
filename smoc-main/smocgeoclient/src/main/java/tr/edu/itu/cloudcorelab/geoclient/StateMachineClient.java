package tr.edu.itu.cloudcorelab.geoclient;

import java.util.logging.Level;
import java.util.logging.Logger;

import tr.edu.itu.cloudcorelab.geomessaging.StateMachineServiceGrpc;
import tr.edu.itu.cloudcorelab.geomessaging.ThroughputResult;
import tr.edu.itu.cloudcorelab.geomessaging.State;
import tr.edu.itu.cloudcorelab.geomessaging.DefaultResponse;
import tr.edu.itu.cloudcorelab.geomessaging.EmptyRequest;
import tr.edu.itu.cloudcorelab.geomessaging.Event;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

public class StateMachineClient {

    private static final Logger logger = Logger.getLogger(StateMachineClient.class.getName());

    private final StateMachineServiceGrpc.StateMachineServiceBlockingStub blockingStub;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public StateMachineClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's
        // responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to
        // reuse Channels.
        blockingStub = StateMachineServiceGrpc.newBlockingStub(channel);
    }

    public void process_event(String name) {
        logger.info("Will try to process event " + name + " ...");
        Event request = Event.newBuilder().setName(name).build();
        DefaultResponse response;
        try {
            response = blockingStub.processEvent(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Event is processesed: " + response.getAck());
    }

    public String get_state() {
        logger.info("Will try to get state");
        EmptyRequest request = EmptyRequest.newBuilder().build();
        State response;
        try {
            response = blockingStub.getState(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return "";
        }
        logger.info("State is : " + response.getName());
        return response.getName();
    }

    public boolean startAnalyze() {
        logger.info("Will try to get state");
        EmptyRequest request = EmptyRequest.newBuilder().build();
        DefaultResponse response;
        try {
            response = blockingStub.startAnalyze(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return false;
        }
        logger.info("Starting the analyze is   : " + response.getAck());
        return response.getAck();
    }

    public boolean finishAnalyze() {
        logger.info("Will try to get state");
        EmptyRequest request = EmptyRequest.newBuilder().build();
        DefaultResponse response;
        try {
            response = blockingStub.finishAnalyze(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return false;
        }
        logger.info("Finishing the analyze is   : " + response.getAck());
        return response.getAck();
    }

    public ThroughputResult getResults() {
        logger.info("Will try to get state");
        EmptyRequest request = EmptyRequest.newBuilder().build();
        ThroughputResult response;
        try {
            response = blockingStub.getThroughput(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }
        logger.info(response.toString());
        return response;
    }
}
