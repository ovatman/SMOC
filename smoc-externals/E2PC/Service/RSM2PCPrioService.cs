using System;
using System.Threading;
using ReplicatedSM.Agent;
using ReplicatedSM.BankApp;
using ReplicatedSM.Enums;
using ReplicatedSM.RabbitFiles;
using Stateless;
/*
A service basically has a state machine
It takes in events and processes them using the state machine
Also communicates with other services if needed using rabbitmq

This service provides eventual consistency with limiting the communication
only at the specific transactions of the state machine.

An example can be the write operations for a bank transaction.
*/
namespace ReplicatedSM.Service
{
    public class RSM2PCPrioService : IRSMService
    {
        #region Fields
        public readonly SMAgentPriority _agent;
        private readonly RSMServer _caller;
        private static volatile BankAccount _bankAccount;
        public readonly string _rpcName;
        // Self conditions
        private volatile bool _checkResponse = false;
        private volatile int _checkResponseCtr = 0;
        private volatile int _commitResponseCtr = 0;
        // Foreign logs
        private ForeignDictionaryService _foreignDictionaryService;
        private ManualResetEvent _counterSignalEvent = new ManualResetEvent(false);
        private ManualResetEvent _successSignalEvent = new ManualResetEvent(false);
        // When a machine asks another machine about its priority, it should also ask 
        // Global counter for write confirmation
        private readonly int _machineCount;
        private bool _blocking;
        private string _serverName;
        #endregion
        public RSM2PCPrioService(
            SMAgentPriority agent,
            string rpcName,
            BankAccount bankAccount,
            bool blocking,
            ForeignDictionaryService _foreignDictService,
            string serverName)
        {
            _agent = agent;
            // Imagine each service as another machine
            _caller = new RSMServer(rpcName, Process, Response);
            _machineCount = _caller._otherMachineCount;
            _rpcName = rpcName;
            // Bank account for processing different IDs
            _bankAccount = bankAccount;
            _blocking = blocking;
            _foreignDictionaryService = _foreignDictService;
            _serverName = serverName;
        }
        // Main Call
        public int Process(Message msg)
        {
            // Get the related account
            BankSM _account = _bankAccount.GetAccount(msg.AccountID);
            // Get the related state machine
            StateMachine<State, Event> _stateMachine = _account.StateMachine;
            // if it is a trivial or read or the wrapping up event just process it if you can
            if(msg.Ev != Event.WRITE)
                return _agent.TRIGGER(_account, msg);
            // If it is a WRITE event
            else if(msg.Ev == Event.WRITE)
            {
                int result = WriteProcess(_account, _stateMachine, msg, _account);
                if(result == 1)
                    return _agent.TRIGGER(_account, msg);
                else
                {
                    msg.Ev = Event.FAIL;
                    return _agent.TRIGGER(_account, msg);
                }
            }
            // Event is not in one of the specified classes - client should send a proper event - fail
            else
            {
                System.Console.WriteLine("[CRITICAL ERROR] NO VALID INCOMING EVENTS.");
                Console.ReadLine();
                return 0;
            }
        }
        // Write process is a 2 phase commit (2PC) call
        // Only manages the communication part
        public int WriteProcess(BankSM account, StateMachine<State, Event> _stateMachine, Message msg, BankSM _account)
        {
            _checkResponse = true;
            _checkResponseCtr = 0;
            _commitResponseCtr = 0;
            _counterSignalEvent.Reset();
            _successSignalEvent.Reset();
            // Send WRITE command to other machines
            _caller.BroadCast(msg);
            // Wait until some timeout or receiving N amount of OKs from other machines
            if(_caller._otherMachineCount != 0)
            {
                _counterSignalEvent.WaitOne();
                _counterSignalEvent.Reset();
            }
            // There is a race between setting writing values and other machines checking it!
            int firstPhaseControl = _agent.TRIGGER(account, msg, Event.FIRST_PHASE_2PC, _checkResponse);
            // If first phase failed send failure to all machines, otherwise you got the writing set so continue as usual
            if(firstPhaseControl == 0)
            {
                // Tell the machines waiting for commit to not do anything
                msg.Ev = Event.NO_WRITE;
                _caller.BroadCast(msg); 
                // If broadcasting no write we should also wait for the last response before re-sending the message again
                if(_caller._otherMachineCount != 0)
                {       
                    _successSignalEvent.WaitOne();
                    _successSignalEvent.Reset();
                }
                return 0;                
            }
            // Send COMMIT to other machines
            msg.Ev = Event.COMMIT;
            _caller.BroadCast(msg);
            // Machine needs to wait SUCCESS from other machines before Commiting changes
            // This step SHOULD NOT FAIL NO MATTER WHAT!!!!
            if(_caller._otherMachineCount != 0)
            {            
                _successSignalEvent.WaitOne();
                _successSignalEvent.Reset();
            }
            // Return success -- machine should not be able to fail committing process
            msg.Ev = Event.WRITE;
            return 1;
        }
        // Response is used when another machine makes a call for CHECK and WRITE events
        // Only manages the communication part
        public int Response(Message msg)
        { 
            // Get the related account
            BankSM _account = _bankAccount.GetAccount(msg.AccountID);
            // string sender = ea.BasicProperties.AppId;
            string sender = msg.Guid;
            // Receiving WRITE means the machine will reset its own state machine and update the local account Value.
            if(msg.Ev == Event.WRITE)
            {
                int firstPhaseOutsideControl = _agent.TRIGGER(_account, msg, Event.CONDITION_CHECK);
                return firstPhaseOutsideControl == 4 ? 0 : 1;  
            }
            // Recieving COMMIT means the machine can push the changes.
            // COMMIT means the machine which sent it definitely knows all other machines can push it!
            else if(msg.Ev == Event.COMMIT)
            {          
                _agent.TRIGGER(_account, msg);
                // Send the last signal
                return 2;
            }
            // Fail this machine since it cannot fire the writing operation anymore
            else if(msg.Ev == Event.NO_WRITE)
            {
                return 2;
            }
            // Confirmations
            else if(msg.Ev == Event.WRITE_OK)
            {
                Interlocked.Increment(ref _checkResponseCtr);
                if(_checkResponseCtr == _machineCount)
                    _counterSignalEvent.Set();
                return 3;
            }
            else if(msg.Ev == Event.WRITE_FAIL)
            {
                _checkResponse = false;
                Interlocked.Increment(ref _checkResponseCtr);
                if(_checkResponseCtr == _machineCount)
                    _counterSignalEvent.Set();                
                return 3;
            }
            else if(msg.Ev == Event.WRITE_SUCCESS)
            {
                Interlocked.Increment(ref _commitResponseCtr);
                if(_commitResponseCtr == _machineCount)
                    _successSignalEvent.Set();
                return 3;
            }
            // Wrong type of event
            else
            {
                // Just send a fail response to caller
                return 0;
            }
        }
    }
}