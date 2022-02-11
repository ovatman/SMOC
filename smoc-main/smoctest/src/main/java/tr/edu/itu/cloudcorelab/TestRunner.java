package tr.edu.itu.cloudcorelab.smoctest;

import tr.edu.itu.cloudcorelab.smoctest.tests.MultiClients;

import java.io.IOException;

public class TestRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        MultiClients multiClients = new MultiClients("src\\main\\resources\\parameters.yaml");
        multiClients.runTest();
    }
}
