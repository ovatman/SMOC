using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using EasyNetQ;
using Newtonsoft.Json;
using ReplicatedSM.Enums;
using ReplicatedSM.Service;

namespace ReplicatedSM.RabbitFiles
{
    public class RSMServer
    {
        // General variables
        private string _serverName;
        private string _queueName;
        private string _customerName;
        public int _otherMachineCount;
        private List<Setting> _connectionSettings;
        // Statistics
        private static string SETTING_FILE = "WANSettings.json";
        // EasyMQ connection provider
        private IBus bus;
        // Func delagates
        Func<Message, int> Process;
        Func<Message, int> Response;
        
        public RSMServer(
            string serverName, 
            Func<Message, int> ProcessClient,
            Func<Message, int> ResponseServer
        )
        {
            // General setup
            _serverName = serverName;
            Process = ProcessClient;
            Response = ResponseServer;
            // Read the WAN IP settings file and set up the connections...
            string file = File.ReadAllText(SETTING_FILE);
            _connectionSettings = JsonConvert.DeserializeObject<List<Setting>>(file);
            // Connect to rabbitmq server
            foreach(Setting s in _connectionSettings)
            {
                Console.WriteLine(s.IP + " " + s.ID + " " + s.ClientPort);
                if(s.ID == _serverName)
                {
                    bus = RabbitHutch.CreateBus("host=" + s.IP);
                    _queueName = s.IP;
                    _customerName = s.ClientPort;
                }
                else
                    _otherMachineCount++;
            }
            // Start the receiving process by creating a new exchange to queue
            Console.WriteLine("Creating a listener on queue: " + _queueName + " with exchange: " + _serverName);
            bus.Subscribe<Message>(_serverName, ProcessMessage, x =>  x.WithTopic(_serverName));
            Console.WriteLine("Total other machine count is: " + _otherMachineCount);
        }   
        public void ProcessMessage(Message msg)
        {
            Task.Run(() => {
                if(msg.IsServer == false)
                {
                    string clientName = msg.Sender;
                    try{
                        int processResult = Process(msg);
                        if(processResult == 1)
                            msg.Ev = Event.DONE;
                        else if(processResult == 2)
                            msg.Ev = Event.GO_ON;
                        else // -- 0
                            msg.Ev = Event.FAIL;
                    }
                    catch(Exception e)
                    {
                        Console.WriteLine("WHAT IS GOING ON?: " + e);
                        msg.Ev = Event.FAIL;
                        msg.PrintMessage();
                        Console.ReadLine();
                        int processResult = Process(msg);
                    }
                    finally
                    {
                        Send(msg, clientName);
                    }
                }
                else
                {
                    try
                    {
                        string clientName = msg.Sender;
                        
                        int processResult = Response(msg);
                        if(processResult == 0)
                            msg.Ev = Event.WRITE_FAIL;
                        else if(processResult == 1)
                            msg.Ev = Event.WRITE_OK;
                        else if(processResult == 2)
                            msg.Ev = Event.WRITE_SUCCESS;
                        else if(processResult == 4)
                            msg.Ev = Event.CHECK_FAIL;
                        else if(processResult == 5)
                            msg.Ev = Event.CHECK_SAFE;
                        else //  -- 3 : Do nothing -- this means only confirmation occured.
                        {
                            return;
                        }                   
                        Send(msg, clientName);      
                    }
                    catch(Exception e)
                    {
                        Console.WriteLine("nu: :( " + e);
                        Console.ReadLine();
                    }
                }
            });
        }
        // Send is used for single target message    
        public void Send(Message msg, string target)
        {
            msg.Sender = _serverName;
            msg.IsServer = true;
            bus.Publish(msg, target);
        }
        // Whereas broadcast is used for sending message to all other machine exchanges
        public void BroadCast(Message msg)
        {
            foreach(Setting conn in _connectionSettings)
                if(conn.ID != _serverName)
                    Send(msg, conn.ID);
        }       
    }
}