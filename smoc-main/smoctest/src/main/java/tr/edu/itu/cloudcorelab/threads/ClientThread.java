package tr.edu.itu.cloudcorelab.smoctest.threads;

import tr.edu.itu.cloudcorelab.smoc.grpclient.Client;

public class ClientThread extends Thread {
    private String configPath;
    private String inputPath;
    private String outputPath;

    public ClientThread(String configPath, String inputPath, String outputPath) {
        this.configPath = configPath;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    @Override
    public void run() {
        try {
            Client client = new Client(this.configPath, this.inputPath, this.outputPath);
            client.run();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
