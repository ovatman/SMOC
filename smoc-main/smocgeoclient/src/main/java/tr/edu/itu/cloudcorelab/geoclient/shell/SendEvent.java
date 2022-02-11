package tr.edu.itu.cloudcorelab.geoclient.shell;

import tr.edu.itu.cloudcorelab.geoclient.StateMachineClient;

public class SendEvent implements ICommand {

    private String param;
    private String name;
    private StateMachineClient client;

    public SendEvent(String name, StateMachineClient client) {
        super();
        this.name = name;
        this.client = client;
    }

    @Override
    public boolean execute() {
        this.client.process_event(param);
        return true;
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
