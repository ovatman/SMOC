/*
    Defines the general message structure
 */
using ReplicatedSM.Enums;

namespace ReplicatedSM.Service
{
    public class Message
    {
        public Event Ev { get; set; }
        public int Value { get; set; }
        public int AccountID { get; set; }
        public int Priority { get; set; }
        public string Guid { get; set; }
        public string Sender { get; set; }
        public bool IsServer { get; set; }
        public Message(Event Ev, int Value, int AccountID, int priority, string guid, string sender, bool isServer)
        {
            this.Ev = Ev;
            this.Value = Value;
            this.AccountID = AccountID;
            this.Priority = priority;
            this.Guid = guid;
            this.Sender = sender;
            this.IsServer = isServer;
        }
        
        public void PrintMessage()
        {
            System.Console.WriteLine(this.Ev + " " + this.Value + " " + this.AccountID + " " + this.Priority + " " + this.Sender + " " + this.IsServer);
        }
    }
}