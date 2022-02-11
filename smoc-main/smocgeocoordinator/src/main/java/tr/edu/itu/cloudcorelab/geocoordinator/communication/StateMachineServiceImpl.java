package tr.edu.itu.cloudcorelab.geocoordinator.communication;

import tr.edu.itu.cloudcorelab.geomessaging.DefaultResponse;
import tr.edu.itu.cloudcorelab.geomessaging.EmptyRequest;
import tr.edu.itu.cloudcorelab.geomessaging.Event;
import tr.edu.itu.cloudcorelab.geomessaging.State;
import tr.edu.itu.cloudcorelab.geomessaging.StateMachineServiceGrpc;
import tr.edu.itu.cloudcorelab.geomessaging.ThroughputResult;
import tr.edu.itu.cloudcorelab.geocoordinator.analyze.Analyzer;

import org.springframework.statemachine.StateMachine;

import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;


public class StateMachineServiceImpl extends StateMachineServiceGrpc.StateMachineServiceImplBase {
    private final Logger logger = Logger.getLogger(StateMachineServiceImpl.class.getName());
    
    private StateMachine<String, String> machine;
    private Analyzer analyzer;

    public StateMachineServiceImpl(StateMachine<String, String> machine) {
        super();
        this.machine = machine;
        analyzer  = new Analyzer();
    }

    @Override
    public synchronized void processEvent(Event request, StreamObserver<DefaultResponse> responseObserver) {
        logger.info("Process Event request is arrived. Event " + request.toString());
        boolean result = this.machine.sendEvent(request.getName());
        responseObserver.onNext(DefaultResponse.getDefaultInstance().toBuilder().setAck(result).build());
        responseObserver.onCompleted();
        analyzer.increase();
        logger.info("Process Event request is completed");
    }

    @Override
    public synchronized void getState(EmptyRequest request, StreamObserver<State> responseObserver) {
        logger.info("Get State request is arrived.");
        responseObserver.onNext(State.getDefaultInstance().toBuilder().setName(machine.getState().toString()).build());
        responseObserver.onCompleted();
        logger.info("Get State request is completed");
    }

    @Override
    public void getThroughput(EmptyRequest request, StreamObserver<ThroughputResult> responseObserver) {
        
        responseObserver.onNext(analyzer.getThroughput());
        responseObserver.onCompleted();
    }

    @Override
    public void startAnalyze(EmptyRequest request, StreamObserver<DefaultResponse> responseObserver) {
        analyzer.start();
        responseObserver.onNext(DefaultResponse.getDefaultInstance().toBuilder().setAck(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void finishAnalyze(EmptyRequest request, StreamObserver<DefaultResponse> responseObserver) {
        analyzer.stop();
        responseObserver.onNext(DefaultResponse.getDefaultInstance().toBuilder().setAck(true).build());
        responseObserver.onCompleted();
    }
    
}
