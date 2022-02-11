package tr.edu.itu.cloudcorelab.geoclient.shell;

import tr.edu.itu.cloudcorelab.geoclient.StateMachineClient;

public class StopAnalyze implements ICommand {

    private String param;
    private String name;
    private StateMachineClient client;

    public StopAnalyze(String name, StateMachineClient client) {
        super();
        this.name = name;
        this.client = client;
    }

    @Override
    public boolean execute() {
        return this.client.finishAnalyze();
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
