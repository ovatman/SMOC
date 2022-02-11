package tr.edu.itu.cloudcorelab.geocoordinator.communication;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.springframework.statemachine.StateMachine;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RPCServer {
    private static final Logger logger = Logger.getLogger(RPCServer.class.getName());

    private final int port;
    private final Server server;

    public RPCServer(int port, StateMachine<String, String> machine) {
        this.port = port;
        server = ServerBuilder.forPort(port).addService(new StateMachineServiceImpl(machine)).build();
    }

    /** Start serving requests. */
    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown
                // hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    RPCServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon
     * threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
