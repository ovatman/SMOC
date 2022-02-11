using System;
using System.Timers;
using ReplicatedSM.BankApp;
using ReplicatedSM.Enums;
using ReplicatedSM.Service;

namespace ReplicatedSM.Agent
{
    public interface ISMAgent
    {
        // Main event callbacks
        int TRIGGER(BankSM account, Message msg, Event optional, Boolean optionalParam);
        // Below are statistical methods that run async to write results
        void RunObserver(Object source, ElapsedEventArgs e);
        void WriteAccountChange(BankSM account);
    }
}