package tr.edu.itu.cloudcorelab.geoclient.shell;

import tr.edu.itu.cloudcorelab.geoclient.StateMachineClient;

public class StartAnalyze implements ICommand {

    private String param;
    private String name;
    private StateMachineClient client;

    public StartAnalyze(String name, StateMachineClient client) {
        super();
        this.name = name;
        this.client = client;
    }

    @Override
    public boolean execute() {
        return this.client.startAnalyze();
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
