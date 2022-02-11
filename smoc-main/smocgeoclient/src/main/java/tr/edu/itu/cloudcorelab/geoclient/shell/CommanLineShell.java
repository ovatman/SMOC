package tr.edu.itu.cloudcorelab.geoclient.shell;

import java.util.HashMap;

public class CommanLineShell implements IShell {

    private HashMap<String, ICommand> map;
    private boolean exit = false;

    public CommanLineShell() {
        super();
        map = new HashMap<String, ICommand>();
    }

    @Override
    public boolean handle(String command) {
        if (map.containsKey(command))
            return map.get(command).execute();
        System.console().printf("Command not found\n");
        return false;
    }

    @Override
    public boolean handle(String command, String params) {
        if (map.containsKey(command)) {
            ICommand temp = map.get(command);
            temp.set_param(params);
            return temp.execute();
        }
        System.console().printf("Command not found\n");
        return false;

    }

    @Override
    public void add_command(ICommand command) {
        map.putIfAbsent(command.name(), command);
    }

    @Override
    public void remove_all() {
        map.clear();
    }

    @Override
    public boolean run() {

        exit = false;
        while (!exit) {
            String line = "";

            System.console().printf("Enter a new command: ");
            line = System.console().readLine();
            String[] args = line.split(" ");

            if (args[0].equalsIgnoreCase("exit") || args[0].equalsIgnoreCase("quit"))
                break;

            if (args.length == 1)
                handle(args[0].trim());

            if (args.length > 1)
                handle(args[0].trim(), args[1].trim());
        }

        return true;
    }

    @Override
    public boolean stop() {
        exit = true;
        return true;
    }

}
