package tr.edu.itu.cloudcorelab.geoclient.shell;

import tr.edu.itu.cloudcorelab.geomessaging.ThroughputResult;
import tr.edu.itu.cloudcorelab.geoclient.StateMachineClient;

public class GetResults implements ICommand {

    private String param;
    private String name;
    private StateMachineClient client;

    public GetResults(String name, StateMachineClient client) {
        super();
        this.name = name;
        this.client = client;
    }

    @Override
    public boolean execute() {
        ThroughputResult result = this.client.getResults();

        if (result != null) {
            System.out.println(
                "NumberOfRequest: " + result.getNumberOfRequest() + " Elapsed: " + result.getElapsedTimeInMilli()
                        + " Througput: " + (result.getNumberOfRequest() / (result.getElapsedTimeInMilli() / 1000)));
            return true;
        }

        System.err.println("No result is arrived");        

        return false;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void set_param(String param) {
        this.param = param;
    }

}
