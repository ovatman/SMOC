package tr.edu.itu.cloudcorelab.geoclient;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import tr.edu.itu.cloudcorelab.geoclient.shell.Analyzer;
import tr.edu.itu.cloudcorelab.geoclient.shell.CommanLineShell;
import tr.edu.itu.cloudcorelab.geoclient.shell.GetResults;
import tr.edu.itu.cloudcorelab.geoclient.shell.GetState;
import tr.edu.itu.cloudcorelab.geoclient.shell.SendEvent;
import tr.edu.itu.cloudcorelab.geoclient.shell.StartAnalyze;
import tr.edu.itu.cloudcorelab.geoclient.shell.StopAnalyze;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {

        ArgumentParser parser = ArgumentParsers.newFor("Client for StateMachine").build().defaultHelp(true)
                .description("Send requests for state machines");
        parser.addArgument("-a", "--address").setDefault("localhost").help("IP address of server");
        parser.addArgument("-p", "--port").type(Integer.class).setDefault(8980).help("Port number of server");
        parser.addArgument("--cont").action(Arguments.storeTrue()).help("Send requests to server in a closed loop");

        Namespace parsed_args = parser.parseArgsOrFail(args);
        System.console().printf("\n\n" + parsed_args.toString() + "\n\n");

        Channel server_channel = ManagedChannelBuilder
                .forAddress(parsed_args.getString("address"), parsed_args.getInt("port").intValue()).usePlaintext()
                .build();

        StateMachineClient client = new StateMachineClient(server_channel);

        SendEvent event_cmd = new SendEvent("event", client);
        GetState state_cmd = new GetState("state", client);
        StartAnalyze start_cmd = new StartAnalyze("start", client);
        StopAnalyze stop_cmd = new StopAnalyze("stop", client);
        GetResults result_cmd = new GetResults("result", client);

        CommanLineShell shell = new CommanLineShell();
        shell.add_command(event_cmd);
        shell.add_command(state_cmd);
        shell.add_command(start_cmd);
        shell.add_command(stop_cmd);
        shell.add_command(result_cmd);


        List<String> list = Arrays.asList("MAKE_RESERVATION", "PAY_MONEY", "TAKE_ROOM", "MAKE_RESERVATION", "CANCEL");
        
        ListIterator<String> listIterator = list.listIterator();
        
        Analyzer analyzer =  new Analyzer(30);
        

        if (parsed_args.getBoolean("cont")) {
            while(true){
                long start = System.nanoTime();
                if(listIterator.hasNext()){
                    shell.handle("event", listIterator.next());
                }
                else{
                    listIterator = list.listIterator();
                    shell.handle("event", listIterator.next());
                }
                long stop = System.nanoTime();
                double elapsed_ms = (stop-start)/ 1000000 ;

                analyzer.pushValue(elapsed_ms);
                System.console().printf("Response avg ms : " + analyzer.getValue() + "\n");

            }
        } else {
            shell.run();
        }

        System.console().printf("Client is closed\n");
    }
}
