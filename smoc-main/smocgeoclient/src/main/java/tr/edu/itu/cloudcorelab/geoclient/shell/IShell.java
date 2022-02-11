package tr.edu.itu.cloudcorelab.geoclient.shell;

public interface IShell {
    boolean handle(String command);

    boolean handle(String command, String params);

    void add_command(ICommand command);

    void remove_all();

    boolean run();

    boolean stop();
}
