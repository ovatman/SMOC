using System;
using System.Threading;
using ReplicatedSM.Agent;
using ReplicatedSM.BankApp;
using ReplicatedSM.Enums;
using ReplicatedSM.RabbitFiles;
using ReplicatedSM.Service;
using Stateless;

namespace ReplicatedSM
{
    public class RSMCheckPrioService : IRSMService
    {
        #region fields
        public readonly SMAgentPriority _agent;
        private readonly RSMServer _caller;
        private static volatile BankAccount _bankAccount;
        public readonly string _rpcName;

        #region Communication 
        private static int COUNTER;
        private static int PRIORITY_COUNTER;        
        private static readonly ManualResetEvent _prioritySignalEvent = new ManualResetEvent(false);
        private static readonly ManualResetEvent _successSignalEvent = new ManualResetEvent(false);
        private static readonly object ResponseLock = new object();
        private static string _response = "SAFE";
        private static bool _writeResponse = true;
        private readonly int _machineCount;
        #endregion

        private static ForeignDictionaryService _foreignDictService;
        private string _serverName;
        #endregion
        public RSMCheckPrioService(
            SMAgentPriority agent,
            string rpcName,
            BankAccount bankAccount,
            bool blocking,
            ForeignDictionaryService foreignDictService,
            string serverName)
        {
            _agent = agent;
            // Imagine each service as another machine
            _caller = new RSMServer(rpcName, Process, Response);
            _machineCount = _caller._otherMachineCount;
            _rpcName = rpcName;
            // Bank account for processing different IDs
            _bankAccount = bankAccount;
            _foreignDictService = foreignDictService;
            _serverName = serverName;
        }
        // Main Call
        public int Process(Message msg)
        {
            // Get the related account
            BankSM _account = _bankAccount.GetAccount(msg.AccountID);
            // Get the related state machine
            StateMachine<State, Event> _stateMachine = _account.StateMachine;
            // [EXECUTION] if it is a trivial or the wrapping up event just process it if you can
            if(msg.Ev != Event.CHECK && msg.Ev != Event.WRITE)
            {               
                return _agent.TRIGGER(_account, msg);
            }
            // if it is a CHECK event
            else if(msg.Ev == Event.CHECK)
            {                
                // Check the current priorities of other machines
                int result = CheckProcess(_account, _stateMachine, msg);
                // Process the event according to result
                if(result == 1)
                    return _agent.TRIGGER(_account, msg);
                // Checking step failed
                else
                {
                    // Update the statistics counter
                    SMAgentPriority._totalCheckFails++;
                    msg.Ev = Event.FAIL;
                    return _agent.TRIGGER(_account, msg );
                }
            }
            // If it is a WRITE event
            else if(msg.Ev == Event.WRITE)
            { 
                int writeGetResult = _agent.TRIGGER(_account, msg, Event.GET_WRITING);
                if(writeGetResult == 0)
                    return 0;
                // Consistency Check
                if(_account.Writing == false || _account.Read == false || _account.StateMachine.CanFire(msg.Ev) == false)
                {
                    System.Console.WriteLine("[EXTREME FATAL ERROR]: " + msg.Ev + _account.Writing + _account.Read + _account.StateMachine.State);
                    Console.ReadLine();
                }
                // Process the event with conflict number
                int result = WriteProcess(_account, _stateMachine, msg, _account);
                // 2 means write was successful
                if(result == 2)
                    return _agent.TRIGGER(_account, msg);
                // Else write is failed
                else
                {
                    System.Console.WriteLine("CRITICAL ERROR :: WRITE FAILED AFTER OBTAINING THE LOCK");
                    msg.Ev = Event.FAIL;
                    return _agent.TRIGGER(_account, msg);
                }
            }
            // Event is not in one of the specified classes - client should send a proper event - fail
            System.Console.WriteLine("CRITICAL ERROR :: INCOMING EVENT IS NOT VALID");
            return 0;
        }
        // Only manages the communication part
        public int CheckProcess(BankSM account, StateMachine<State, Event> _stateMachine, Message msg)
        {
            // Counter is to indicate how many machines will send response
            PRIORITY_COUNTER = 0;
            // Response is to keep the incoming check response from other machines
            _response = "SAFE";
            // Send message to other machines
            _caller.BroadCast(msg);
            // Wait for all of them to send a response
            if(_machineCount != 0)
            {            
                _prioritySignalEvent.WaitOne();
                _prioritySignalEvent.Reset();
            }
            // Proceeed according to response
            // Do not forget to reset the response
            if(_response == "FAIL")
                return 0;
            // else the response is SAFE
            else if(_response == "SAFE")
                return 1;
            // otherwise something wrong happened fail
            else
            {
                System.Console.WriteLine("[FATAL ERROR] WRONG RESPONSE TYPE");
                return 0;
            }
        }
        // Write process is NOT a 2 phase call now
        // Only manages the communication part
        public int WriteProcess(BankSM account, StateMachine<State, Event> _stateMachine, Message msg, BankSM _account)
        {
            _writeResponse = true;
            // Counters are for the consensus
            COUNTER = 0;
            // Send write message with its Value to other machines
            // Write messages are unstoppable under the assumption that availability is 100%
            _caller.BroadCast(msg);
            // Wait until some timeout or receiving N amount of OKs from other machines
            if(_machineCount != 0)
            {
                _successSignalEvent.WaitOne();
                _successSignalEvent.Reset();
            }
            // Check the response
            if(_writeResponse == false)
            {
                System.Console.WriteLine("[FATAL ERROR] WRITE SHOULD NOT FAIL!");
                Console.ReadLine();
                return 0;
            }
            else
                return 2;
        }
        // Response is used when another machine makes a call for CHECK and WRITE events
        public int Response(Message msg)
        {
            // Get the related account
            BankSM _account = _bankAccount.GetAccount(msg.AccountID);
            // Receiving WRITE means the machine will reset its own state machine and update the local
            // account Value.
            if(msg.Ev == Event.WRITE)
            {
                // Machine calls the function to keep itself up to date.
                msg.Ev = Event.COMMIT;
                _agent.TRIGGER(_account, msg);
                return 1;
            }
            else if(msg.Ev == Event.CHECK)
            {
                // One of the machines who will try to write in the future will definitely fail in the 
                // priority checking part!
                return _agent.TRIGGER(_account, msg, Event.CONDITION_CHECK);
            }
            else if(msg.Ev == Event.WRITE_OK)
            {
                lock(ResponseLock)
                {
                    Interlocked.Increment(ref COUNTER);
                    if(COUNTER == _machineCount)
                    {
                        _successSignalEvent.Set();
                    }                
                    return 3;
                }
            }
            else if(msg.Ev == Event.WRITE_FAIL)
            {
                lock(ResponseLock)
                {
                    _writeResponse = false;
                    Interlocked.Increment(ref COUNTER);
                    if(COUNTER == _machineCount)
                    {
                        _successSignalEvent.Set();
                    }                 
                    return 3;
                }
            }
            else if(msg.Ev == Event.CHECK_SAFE)
            {
                lock(ResponseLock)
                {
                    Interlocked.Increment(ref PRIORITY_COUNTER);
                    if(PRIORITY_COUNTER == _machineCount)
                    {
                        _prioritySignalEvent.Set();
                    }
                    return 3;
                }
            }
            else if(msg.Ev == Event.CHECK_FAIL)
            {
                lock(ResponseLock)
                {
                    _response = "FAIL";
                    Interlocked.Increment(ref PRIORITY_COUNTER);
                    if(PRIORITY_COUNTER == _machineCount)
                    {
                        _prioritySignalEvent.Set();
                    }                
                    return 3;
                }
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
