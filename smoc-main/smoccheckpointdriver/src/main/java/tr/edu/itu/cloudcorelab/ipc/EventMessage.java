package tr.edu.itu.cloudcorelab.checkpointing.ipc;

import java.io.Serializable;

public class EventMessage implements Serializable {

    public String event;

    public EventMessage(){}

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}
