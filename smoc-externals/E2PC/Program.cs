using System;
using System.Collections.Generic;
using ReplicatedSM.Agent;
using ReplicatedSM.Enums;
using ReplicatedSM.RabbitFiles;
using ReplicatedSM.Service;
using ReplicatedSM.SMFiles;
using ReplicatedSM.BankApp;
using System.IO;

namespace ReplicatedSM
{
    class Program
    {
        private static int CLIENT_MESSAGE_COUNT = 200;
        private static int repeatCtr = 0;
        private static bool _blocking = true;
        static void Main(string[] args)
        {       

            if(args[0] == "sender")
            {
                Console.WriteLine("Running the sender with args: " + args[0] + "...");
                CLIENT_MESSAGE_COUNT = Int32.Parse(args[2]);
                ClientTest(new RSMClient(args[1], args[3]), 0, args[3], args[4], Int32.Parse(args[5]));
            }
            else if(args[0] == "service")
            {                
                Console.WriteLine("Running the service with args: " + args[0] + "...");
                SetupService(args[1], args[2], args[3]);
            }        
            else if(args[0] == "CONSISTENT")
            {
                Console.WriteLine("Checking consistency values...");
                // Checks if the values in both files are consistent with each other 
                CheckLocalConsistency("2PC");
                CheckLocalConsistency("CP");
            } 
            else
            {
                System.Console.WriteLine("Testing probability generator");
                PriorityGenerator pg = new PriorityGenerator(0, 100, 0);
                int[] arr = {0 , 0};
                for (int i = 0; i < 1000; i++)
                {
                    if(pg.NextUniform() < 30)
                        arr[0]++;
                    else
                        arr[1]++;
                }
                System.Console.WriteLine("Exponential results 0: " + arr[0] + " 1: " + arr[1]);
            }
        }
        public static void CheckLocalConsistency(string file_path)
        {
            List<int> server1_values = new List<int>();
            List<int> server2_values = new List<int>();
            using (StreamReader sr = new StreamReader("Output\\" + file_path + "\\server1_account_log.txt"))
            {
                // Read the stream to a string, and write the string to the console.
                while(!sr.EndOfStream)
                {
                    string line = sr.ReadLine();
                    server1_values.Add(Convert.ToInt32(line.Split(" ")[2]));
                }
            }
            using (StreamReader sr = new StreamReader("Output\\" + file_path + "\\server2_account_log.txt"))
            {
                // Read the stream to a string, and write the string to the console.
                while(!sr.EndOfStream)
                {
                    string line = sr.ReadLine();
                    server2_values.Add(Convert.ToInt32(line.Split(" ")[2]));
                }
            }
            // First compare the length -- they should be equal in size otherwise something went wrong
            if(server1_values.Count != server2_values.Count)
            {
                Console.WriteLine("[" + file_path + "] Account logs are not equal in size, something must have went wrong, or you stopped the test early!");
                Console.WriteLine("[INFO] Sizes: " + server1_values.Count + " ::: " + server2_values.Count);
                return;
            }
            // Compare the values AND their order!!!
            for(int i=0;i<server1_values.Count;i++)
            {
                if(server1_values[i] != server2_values[i])
                {
                    Console.WriteLine("[" + file_path + "] An inequal Value is detected at line {0}, this means the algorithm is not working right!", i);
                    return;
                }
            }
            // If everthing went smoothly up to this point model is working correctly
            Console.WriteLine("[" + file_path + "] Values are equal in both files,  model is correct!");
        }
        public static void SetupService(string serverName, string serverType, string rsmType)
        {
            /*
                Account Initialization
            */
            BankAccount bank = new BankAccount(serverType, rsmType);
            for(int i=0;i<1;i++)
            {
                bank.AddAccount(i, 100);
            }            
            /*
                SETTING UP THE MACHINES
             */ 
            // First Machine   
            // A machine should trigger events using an agent
            ForeignDictionaryService _foreignDictService = new ForeignDictionaryService(serverName);
            if(serverType == "2PC")
            {
                RSM2PCPrioService service = new RSM2PCPrioService(
                    new SMAgentPriority(serverName, serverType, _foreignDictService),
                    serverName,
                    bank,
                    _blocking,
                    _foreignDictService,
                    serverName
                );
            }
            if(serverType == "CP")
            {
                RSMCheckPrioService service = new RSMCheckPrioService(
                    new SMAgentPriority(serverName, serverType, _foreignDictService),
                    serverName,
                    bank,
                    _blocking,
                    _foreignDictService,
                    serverName
                );
            }
        }
        public static void ClientTest(RSMClient sender, int AccountID, string serverType, string rsmType, int writeRatio)
        {
            /*
                Randomly generated data
            */
            if(serverType != "2PC" && serverType != "CP" && rsmType != "B" && rsmType != "N")
                return;
            RandomSMInputGenerator generator;
            if(serverType == "2PC")
                if(rsmType == "N")
                    generator = new RandomSMInputGenerator(
                        (new Rsm2PCBuilder("builder", false)).BuildStateMachine("123"), writeRatio
                    );    
                else
                    generator = new RandomSMInputGenerator(
                        (new Rsm2PCBranchBuilder("builder", false)).BuildStateMachine("123"), writeRatio
                    );    
            else
                if(rsmType == "N")
                    generator = new RandomSMInputGenerator(
                        (new RsmCPBuilder("builder", false)).BuildStateMachine("123"), writeRatio
                    );              
                else        
                    generator = new RandomSMInputGenerator(
                        (new RsmCPBranchBuilder("builder", false)).BuildStateMachine("123"), writeRatio
                    );              
            List<List<Event>> eventTrace = generator.GenerateNumberOfTrace(CLIENT_MESSAGE_COUNT);
            Console.WriteLine("Total amount of traces: " + eventTrace.Count);        
            Random rand = new Random();
            int localCtr = 0;
            while(true)
            {
                foreach(List<Event> list in eventTrace)
                {
                    sender.SendStream(list, AccountID);
                    // throttlerCtr++;
                    // We rest the clients at every X streams so that RabbitMQ wont get too clogged up with Nacks
                    // if(throttlerCtr == _throttlerLimit)
                    // {
                    //     throttlerCtr = 0;
                    //     Thread.Sleep(1000);
                    // }
                }
                // Sleep 1 second before sending the next request stream
                // This is to prevent overhead in not-so-powerful machines
                // Thread.Sleep(1000);
                if(localCtr == repeatCtr)
                    break;
                localCtr++;
            }
            Console.WriteLine("All operations are done: " + sender._totalOpDone);
            // Environment.Exit(0);
        }
    }
}
