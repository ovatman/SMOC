package tr.edu.itu.cloudcorelab.smoctest.tests;

import tr.edu.itu.cloudcorelab.smoctest.threads.ClientThread;
import tr.edu.itu.cloudcorelab.smoctest.threads.ServerThread;
import tr.edu.itu.cloudcorelab.smoctest.yamlprocessor.Parameters;
import tr.edu.itu.cloudcorelab.smoctest.yamlprocessor.TestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MultiClients {
    Parameters params;
    ArrayList<ClientThread> clientThreads;

    /**
     * Build MultiClient test with given parameters
     * @param paramsPath file path includes test parameters
     */
    public MultiClients(String paramsPath) throws IOException {
        this.params = this.readYamlInput(paramsPath);
        this.clientThreads = new ArrayList<>();
    }

    /**
     * Reads yaml input
     * @param paramsPath file path including test params
     * @return Parameters object
     */
    private Parameters readYamlInput(String paramsPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        return mapper.readValue(new File(paramsPath), Parameters.class);
    }

    /**
     * Build list of ClientThreads in order to run in test
     */
    private void buildClientThreads() {
        for (TestClient testClient : this.params.getClients()) {
            clientThreads.add(new ClientThread(
                    testClient.getConfigPath(),
                    testClient.getInputPath(),
                    testClient.getOutputPath()
                )
            );
        }
    }

    /**
     * Runner of the MultiClient test
     */
    public void runTest() throws InterruptedException {
        ServerThread server = new ServerThread();
        buildClientThreads();

        server.start();
        Thread.sleep(250);
        for (ClientThread clientThread : this.clientThreads) {
            clientThread.start();
        }
    }
}
