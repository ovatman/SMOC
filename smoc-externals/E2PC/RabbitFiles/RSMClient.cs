using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Threading;
using EasyNetQ;
using Newtonsoft.Json;
using ReplicatedSM.Enums;
using ReplicatedSM.Service;

namespace ReplicatedSM.RabbitFiles
{
    public class RSMClient
    {
        private string _targetServer;
        private string _queueName;
        private string _selfId;
        // Statistics
        public int _totalOpDone = 0;
        public int _totalFailure = 0;
        // Stats for coin flips
        public int _totalReadOnlyDone = 0;
        public int _totalReadOnlyFailed = 0;
        public int _totalReadOnlyWasted = 0;
        public int _totalWriteOnlyDone = 0;
        public int _totalWriteOnlyFailed = 0;
        public int _totalWriteOnlyWasted = 0;
        public int _totalMessagesSent = 0;
        public int _totalFinishFailure = 0;
        
        public int _executedEventCount = 0;
        private static string SETTING_FILE = "WANSettings.json";
        // Variables to check while waiting for a response
        private Event _response;
        private ManualResetEvent oSignalEvent = new ManualResetEvent(false);
        // EasyMQ connection provider
        private IBus bus;
        // Probability and Priority generators
        private static readonly PriorityGenerator _generator = new PriorityGenerator();
        public Random _rand = new Random();
        public string _serverType;
        public RSMClient(string targetServer, string serverType)
        {
            // Set global variables
            _serverType = serverType;
            _targetServer = targetServer;
            // Read the settings file
            // Find the connection string from settings file
            string file = File.ReadAllText(SETTING_FILE);
            List<Setting> settings = JsonConvert.DeserializeObject<List<Setting>>(file);
            foreach(Setting s in settings)
            {
                // Find the IP of the target server to send messages to
                if(s.ID == targetServer)
                {
                    // Create the connection            
                    bus = RabbitHutch.CreateBus("host=" + s.IP);
                    _selfId = s.ClientPort;
                    _queueName = s.ID;
                    break;
                }
            }        
            // Create the listener for incoming messages
            bus.Subscribe<Message>(_selfId, Receive, x => x.WithTopic(_selfId));
            // Create the file for statistics
            _path = Path.Combine("Output", "Client", targetServer + "_" + serverType + "_message_rates_log.txt");
            File.Create(_path).Close();
        }
        int slept = 0;
        public void SendStream(List<Event> eventStream, int AccountID)
        {
            // It is troublesome to make it less then 0 because one of the
            // machines might check exactly 0 Value.
            int priority = _generator.NextUniform();
            int Value = _rand.Next(30) + 1;
            int retry = 0;
            // if the client is doing a READ only process it doesnt need a priority
            // so that it wont fail the writing machines!
            if(eventStream.Contains(Event.TRIVIAL_R))
                priority = -1;
            // Console.WriteLine("[CLIENT] " + _totalOpDone + " A stream started with priority: " + priority);
            // Generate new GUID, GUID needs to change when an operation fails because it also
            // helps removing items from failed machines
            string guid = Guid.NewGuid().ToString();
            // Reset the timer
            watch.Restart();
            Event lastFailedEvent = Event.GO_ON;
            // Main loop
            while(true)
            {
                Event stat = Event.GO_ON;
                int sentEventCount = 0;
                foreach(Event e in eventStream)
                {
                    stat = e;
                    // Sleep trivial events for 2ms
                    if(e == Event.TRIVIAL1 || 
                        e == Event.TRIVIAL2 || 
                        e == Event.TRIVIAL3 || 
                        e == Event.TRIVIAL4 || 
                        e == Event.TRIVIAL5 || 
                        e == Event.TRIVIAL6 || 
                        e == Event.TRIVIAL7 || 
                        e == Event.TRIVIAL8 || 
                        e == Event.TRIVIAL9 || 
                        e == Event.TRIVIAL10 || 
                        e == Event.TRIVIAL11 || 
                        e == Event.TRIVIAL12 || 
                        e == Event.TRIVIAL13 || 
                        e == Event.TRIVIAL14 || 
                        e == Event.TRIVIAL15 || 
                        e == Event.TRIVIAL_R || 
                        e == Event.TRIVIAL_W
                    )
                    {
                        slept++;
                        Thread.Sleep(2);
                    }
                    // Send each event and wait for the response
                    // Each message needs: Event + AccountID + Value + Priority
                    Message msg = new Message(e, Value, AccountID, priority, guid, _selfId, false);
                    Send(msg);
                    _totalMessagesSent++;
                    sentEventCount++;
                    // Wait until the response comes
                    oSignalEvent.WaitOne();
                    oSignalEvent.Reset();
                    // Process the result
                    if(_response == Event.DONE)
                    {
                        // Stream is done so return
                        _totalOpDone++;
                        if(eventStream.Contains(Event.TRIVIAL_R))
                            _totalReadOnlyDone++;
                        if(eventStream.Contains(Event.TRIVIAL_W) || eventStream.Contains(Event.CHECK))
                            _totalWriteOnlyDone++;
                        // Write the execution time
                        WriteCycleExecutionTime(priority, Value);
                        if(_totalOpDone % 250 == 0)
                            System.Console.WriteLine("Total op done: " + _totalOpDone);
                        return;
                    }
                    else if(_response == Event.FAIL)
                    {
                        if(msg.Ev == Event.FINISH)
                        {
                            _totalFinishFailure++;
                        }
                        if(eventStream.Contains(Event.TRIVIAL_R))
                        {
                            _totalReadOnlyWasted += sentEventCount;
                            _totalReadOnlyFailed++;
                        }
                        if(eventStream.Contains(Event.TRIVIAL_W) || eventStream.Contains(Event.CHECK))
                        {
                            _totalWriteOnlyWasted += sentEventCount;
                            _totalWriteOnlyFailed++;      
                        }
                        _totalFailure++;                                              
                        guid = Guid.NewGuid().ToString();
                        // Break this loop so that event stream can be re-send from start
                        lastFailedEvent = e;
                        break;
                    }
                    // GO_ON response just continue sending the next event
                }
                retry++;
                if(retry % 100 == 0)
                    Console.WriteLine("[WARNING] retry Value is a lot: " + retry + "Failure count: " + _totalFailure + " Event: " + stat + " Last Failed Event: " + lastFailedEvent);
            }            
        }
        public void Send(Message msg)
        {
            bus.Publish(msg, _targetServer);
        }
        public void Receive(Message msg)
        {
            _response = msg.Ev;
            oSignalEvent.Set();
        }        
        // Saving statistics - time it takes to execute a message with its priority
        string _path;
        Stopwatch watch = System.Diagnostics.Stopwatch.StartNew();
        public void WriteCycleExecutionTime(int priority, int value)
        {
            // Stop the watch
            watch.Stop();
            // Write the time to file
            using (System.IO.StreamWriter file = 
                new System.IO.StreamWriter(_path, true))
            {
                string line = "" + DateTime.Now + "," + priority + "," + value + "," + _totalOpDone + "," + _totalReadOnlyDone + "," + _totalReadOnlyFailed + "," 
                                 + _totalWriteOnlyDone + "," + _totalWriteOnlyFailed + ","
                                 + watch.ElapsedMilliseconds + "," + _totalMessagesSent + ","
                                 + _totalFinishFailure + "," + _totalWriteOnlyWasted + "," + _totalReadOnlyWasted + "," + slept;
                file.WriteLine(line);
            }      
            // Restart the watch
            watch.Restart();
        }        
    }
}