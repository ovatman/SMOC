package tr.edu.itu.cloudcorelab.smoc;

import tr.edu.itu.cloudcorelab.smoc.grpclient.Client;

public class ClientRunner {

    public static void main(String[] args) throws Exception {
        Client machine = new Client(
                "src\\main\\resources\\statemachine.yaml",
                "src\\main\\resources\\_eventInputs.txt",
                "src\\main\\resources\\_eventOutputs.txt");
        machine.run();
    }

}
