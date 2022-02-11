using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using ReplicatedSM.Enums;
using ReplicatedSM.Service;

namespace ReplicatedSM
{
    public interface ForeignDictionaryServiceInterface
    {
        void AddToForeignDict(Message msg);
        void RemoveFromForeignDict(Message msg);
        bool ForeignHasHigherPriority(Message msg, string serverName);
    }

    public class ForeignDictionaryService : ForeignDictionaryServiceInterface
    {
        private string _holdingServer;
        private static readonly object _lock  = new object();
        public ForeignDictionaryService(string holdingServer)
        {
            _holdingServer = holdingServer;
        }
        public Dictionary<string, int> FOREIGN_PRIORITY_DICT = new Dictionary<string, int>();
        Dictionary<string, DateTime> FOREIGN_PRIORITY_TIMER = new Dictionary<string, DateTime>();
        public void AddToForeignDict(Message msg)
        {
            lock(_lock)
            {
                if(FOREIGN_PRIORITY_DICT.ContainsKey(msg.Sender) == false)
                {
                    FOREIGN_PRIORITY_DICT.Add(msg.Sender, msg.Priority);
                    FOREIGN_PRIORITY_TIMER.Add(msg.Sender, DateTime.Now);
                }
            }
        }
        public void RemoveFromForeignDict(Message msg)
        {
            lock(_lock)
            {    
                FOREIGN_PRIORITY_DICT.Remove(msg.Sender, out _);  
                FOREIGN_PRIORITY_TIMER.Remove(msg.Sender, out _);
            }
        }
        public bool ForeignHasHigherPriority(Message msg, string serverName)
        {
            lock(_lock)
            {
                if(FOREIGN_PRIORITY_DICT.Keys.Count > 10)
                    System.Console.WriteLine("ALERT ALERT, TOO MANY KEYS IN THE PRIORITY DICT!!");

                foreach(string server in FOREIGN_PRIORITY_DICT.Keys)
                {
                    if(serverName != server && msg.Priority == FOREIGN_PRIORITY_DICT[server])
                    {
                        System.Console.WriteLine("[EQUAAAAAAAAAAAAAAAAAALITY]");
                        Console.ReadLine();
                    }                        
                    
                    if(msg.Priority <= FOREIGN_PRIORITY_DICT[server] && serverName != server)
                    {
                        return true;
                    }
                }
                return false;
            }
        }
        public void PrintForeignDictionary()
        {
            lock(_lock)
            {    
                foreach (var item in FOREIGN_PRIORITY_DICT)
                {
                    System.Console.WriteLine(item.Key + " - " + item.Value + " - " + FOREIGN_PRIORITY_TIMER[item.Key]);
                }   
            }
        }
    }
}