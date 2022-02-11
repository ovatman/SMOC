package tr.edu.itu.cloudcorelab.smoctest.tests;

import tr.edu.itu.cloudcorelab.smoctest.threads.ClientThread;
import tr.edu.itu.cloudcorelab.smoctest.threads.ServerThread;


public class SmokeTest {

    /**
     * Runner of the SmokeTest
     */
    public void runTest() throws InterruptedException {
        ClientThread client = new ClientThread(
                "C:\\Users\\Batuhan\\Documents\\GitHub\\DistributedReadWrite\\smgrpctest\\src\\main\\resources\\sm1.yaml",
                "C:\\Users\\Batuhan\\Documents\\GitHub\\DistributedReadWrite\\smgrpctest\\src\\main\\resources\\_smokeInput.txt",
                "C:\\Users\\Batuhan\\Documents\\GitHub\\DistributedReadWrite\\smgrpctest\\src\\main\\resources\\_smokeOutput.txt"
        );

        ServerThread server = new ServerThread();

        server.start();
        Thread.sleep(100);
        client.start();
    }
}
