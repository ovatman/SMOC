package tr.edu.itu.cloudcorelab.geoclient.shell;

public interface ICommand {
    boolean execute();
    String name();
    void set_param(String param);
}
