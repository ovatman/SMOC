package tr.edu.itu.cloudcorelab.smoctest.threads;

import tr.edu.itu.cloudcorelab.smoc.TwoPhaseCommitServices;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ServerThread extends Thread {

    @Override
    public void run() {
        Server server = ServerBuilder
                .forPort(9090)
                .addService(new TwoPhaseCommitServices())
                .build();

        try {
            server.start();
            server.awaitTermination();

        }

        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
