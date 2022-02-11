package tr.edu.itu.cloudcorelab.geoclient.shell;

import tr.edu.itu.cloudcorelab.geoclient.StateMachineClient;

public class GetState implements ICommand {
    private String name;
    private StateMachineClient client;

    public GetState(String name, StateMachineClient client) {
        super();
        this.name = name;
        this.client = client;
    }

    @Override
    public boolean execute() {
        String state_info = this.client.get_state();
        return true;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void set_param(String param) {

    }

}
