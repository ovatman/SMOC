using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using System.Timers;
using ReplicatedSM.BankApp;
using ReplicatedSM.Enums;
using ReplicatedSM.Service;

namespace ReplicatedSM.Agent
{
    public class SMAgentPriority : ISMAgent
    {
        #region Fields
        public static string _name { get; set; }
        /*
        Logs are held in a list while the agent is active on a state machine
        But they need to be reset/stored after a full run to prevent one full request from interrupting another.
        */
        // Exceptions is one of the most crucial part of the program
        public static readonly List<Event> _exceptions = new List<Event>();
        
        #region Statistics
        public static int _successfullRuns { get; set; } = 0;
        public static int _failRuns { get; set; } = 0;
        public static int _totalExecutions { get; set; } = 0;
        public static int _totalCheckFails { get; set; } = 0;
        #endregion

        #region Logger
        private static int _logTimer = 1000;
        private static readonly System.Timers.Timer aTimer = new System.Timers.Timer(_logTimer);
        #endregion

        #region Important Paths
        private static string _path;
        private static string _accountChangePath;
        private static string _outputDirectory { get; set; }
        #endregion

        #region Lock and Consistency Stuff
        private static ForeignDictionaryService _foreignDictServer;
        private static int lockHolder = 0;
        private static readonly object EventLock = new object();
        #endregion

        #endregion
        public SMAgentPriority(string name, string outputDirectory, ForeignDictionaryService foreignDictServer)
        {
            _outputDirectory = outputDirectory;
            _name = name;
            // Reset event
            _exceptions.Add(Event.FAIL);
            // Rest the statistic output file
            _path = Path.Combine("Output", outputDirectory, _name + "_results_log.txt");
            _accountChangePath = Path.Combine("Output", outputDirectory, _name + "_account_log.txt");
            File.Create(_path).Close();
            File.Create(_accountChangePath).Close();
            // Creating a timer with two second interval
            aTimer.Elapsed += RunObserver;
            aTimer.AutoReset = true;
            aTimer.Enabled = true;
            _foreignDictServer = foreignDictServer;
        }
        public int TRIGGER(BankSM account, Message msg, Event optional = Event.NONE, Boolean optionalParam = false)
        {
            // System.Console.WriteLine("[CALLED] " + msg.Sender + " " + msg.Ev + " " + optional);
            // If another machine has ongoing write path operation with higher priority than my current incoming one wait till it is over if i am a READ operation
            // Only self can send READ event
            if(msg.Ev == Event.READ && _foreignDictServer.ForeignHasHigherPriority(msg, _name))
            {
                SpinWait.SpinUntil(() => _foreignDictServer.ForeignHasHigherPriority(msg, _name) == false);
            }
            // Two events should not try to trigger the same state machine
            // at the same time becuase they also change account variables.
            lock(EventLock)
            {           
                lockHolder++;
                if(lockHolder > 1 || lockHolder < 0)
                {
                    System.Console.WriteLine("[LOCK IS BROKEEEEEEEEN]");
                    Console.ReadLine();
                }
                // [OPTIONALS]
                switch (optional)
                {
                    case Event.CONDITION_CHECK:
                        return CONDITION_CHECK(account, msg);
                    case Event.GET_WRITING:
                        return GET_WRITING(account, msg);
                    case Event.FIRST_PHASE_2PC:
                        return FIRST_PHASE_2PC(account, msg, optionalParam);
                    case Event.NONE:
                        break;
                    default:
                        break;
                }                   

                #region VALIDATIONS
                if(optional != Event.NONE)
                {
                    System.Console.WriteLine("[FATAL ERROR] No non-optionals should pass the initial switch!");
                    Console.ReadLine();
                }                
                if(account.Writing)
                {
                    WRITE_STATE_VALIDATION(account, msg);
                }
                Event oldev = msg.Ev;       
                if(msg.Ev == Event.COMMIT)
                {
                    COMMIT_VALIDATION(account, msg);
                }
                else if(msg.Ev == Event.WRITE)
                {
                    WRITE_VALIDATION(account, msg);
                }
                else
                {
                    // [VALIDATION] What is the point of resetting if it is already at the initial state
                    if(!account.StateMachine.CanFire(msg.Ev) && account.StateMachine.State == State.STATE0)
                    {
                        lockHolder--;
                        return 0;
                    }   
                    // [VALIDATION] if the incoming event is fail and machine has already failed no need for hard reset
                    else if(msg.Ev == Event.FAIL && account.StateMachine.State == State.STATE0)
                    {
                        lockHolder--;
                        return 0;   
                    }
                    // [VALIDATION] if the account value has changed during another operation we need to fail and
                    // start from beginning
                    else if(account.Read && account.CurrentValue != account.Account.Value)
                        msg.Ev = Event.FAIL;
                    // [VALIDATION] if state machine cannot fire an event let client retry from beginnig
                    // because either another machine reseted it or another user is processing it already
                    else if(!account.StateMachine.CanFire(msg.Ev) && account.StateMachine.State != State.STATE0)
                        msg.Ev = Event.FAIL;
                }
                #endregion
                // [EXECUTION]
                switch (msg.Ev)
                {
                    case Event.TRIVIAL1:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL2:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL3:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL4:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL5:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL6:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL7:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL8:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL_R:
                        return TRIVIAL(account, msg);
                    case Event.TRIVIAL_W:
                        return TRIVIAL(account, msg);
                    case Event.READ:
                        return READ(account, msg);
                    case Event.WRITE:
                        return WRITE(account, msg);
                    case Event.FINISH:
                        return FINISH(account, msg);
                    case Event.FAIL:
                        return FAIL(account, msg);
                    case Event.CHECK:
                        return CHECK(account, msg);
                    case Event.COMMIT:
                        return COMMIT(account, msg);
                    default:
                        Console.WriteLine("WRONG TYPE OF EVENT!!!");
                        return 0;
                }
            }
        }
        // 2PC Final Control
        private int FIRST_PHASE_2PC(BankSM account, Message msg, Boolean _checkResponse)
        {
            // Check the write response from other machines AND other machines priorirty
            if(_checkResponse == false || MACHINE_WRITE_RESET_CHECK(account, msg))
            {
                return this.FAIL(account, msg);
            }
            account.Writing = true;
            lockHolder--;
            return 1;
        }
        // CP Final Control
        private int GET_WRITING(BankSM account, Message msg)
        {
            if(MACHINE_WRITE_RESET_CHECK(account, msg))
            {
                return this.FAIL(account, msg);
            }
            account.Writing = true;
            lockHolder--;
            return 1;
        }
        // This method is shared by both 2PC and E2PC algorithms
        private int CONDITION_CHECK(BankSM account, Message msg)
        {
            if(msg.Priority == -1)
            {
                System.Console.WriteLine("[FATAL PRIORITY INPUT]");
                msg.PrintMessage();
                Console.ReadLine();
            }
            // If the other machine is already trying to write let it write AND we can reset this state machine because its priority is lower
            _foreignDictServer.AddToForeignDict(msg);
            if(account.Writing || _foreignDictServer.ForeignHasHigherPriority(msg, msg.Sender))
            {
                lockHolder--;
                return 4;
            }
            // Since the priority of the asking machine is higher this machine can wait until its operation is over or it can fail
            this.FAIL(account, msg);
            // Send SAFE response
            return 5;
        }

        private int TRIVIAL(BankSM account, Message msg)
        {
            account.StateMachine.Fire(msg.Ev);   
            _totalExecutions++; 
            lockHolder--;
            return 2;
        }

        private int READ(BankSM account, Message msg)
        {
            account.CurrentValue = account.Account.Value;
            // Always add the priority to the dictionary with servers name
            msg.Sender = _name;
            // if it is a read operation its priority does not matter
            if(msg.Priority != -1) 
            {
                _foreignDictServer.AddToForeignDict(msg);
            }
            // Set read flag
            account.Read = true;
            // Finally fire the event if nothing wrong happened
            account.StateMachine.Fire(msg.Ev);
            _totalExecutions++;  
            lockHolder--;
            return 2;
        }

        private int WRITE(BankSM account, Message msg)
        {
            // Consistency Check
            if(account.Writing == false || account.Read == false)
            {
                System.Console.WriteLine("[BIG FATAL WRITE ERROR] : ");
                _foreignDictServer.PrintForeignDictionary();
                Console.ReadLine();
            }
            // If at any point WRITE fails it means there is a huge problem!
            // Commit the changes
            account.Deposit(msg.Value);
            WriteAccountChange(account);
            // Reset the values
            account.Reset();            
            // Remove own priority if it was not -1
            msg.Sender = _name;
            if(msg.Priority != -1)
            {
                _foreignDictServer.RemoveFromForeignDict(msg);
            }
            // Finally fire the event if nothing wrong happened
            account.StateMachine.Fire(msg.Ev);      
            _totalExecutions++; 
            Interlocked.Increment(ref _writeCount);
            lockHolder--;
            return 2;
        }

        private int FINISH(BankSM account, Message msg)
        {
            _successfullRuns++;
            _totalExecutions++;  
            // Since after adding the branching situation we dont have write events in some cases, we need to reset read here as well
            account.Reset();               
            // Finally fire the event if nothing wrong happened
            account.StateMachine.Fire(msg.Ev);
            lockHolder--;
            return 1;
        }

        private int FAIL(BankSM account, Message msg)
        {        
            // [CONSISTENTCY]
            if(account.Writing)
            {
                System.Console.WriteLine("[ALARM] If writing do not fail.");
                msg.PrintMessage();
                System.Console.WriteLine(account.GetAccountInfo());
                Console.ReadLine();
            }
            // [VALIDATION] No need to hard reset if not reading
            if(!account.Read)  
            {
                lockHolder--;
                return 0;
            }
            // Store old command for further usage
            Event old = msg.Ev;
            // Update the event of the message to be failure
            msg.Ev = Event.FAIL;
            // Reset everything completely
            account.Reset();
            // Finally fire the event
            account.StateMachine.Fire(msg.Ev);
            // Increment stats
            _totalExecutions++;  
            _failRuns++;
            lockHolder--;
            return 0;
        }

        private int CHECK(BankSM account, Message msg)
        {
            account.StateMachine.Fire(msg.Ev);
            _totalExecutions++; 
            lockHolder--;
            return 2;
        }

        private int COMMIT(BankSM account, Message msg)
        {
            // Update the local account Value
            account.Deposit(msg.Value);
            WriteAccountChange(account);            
            // If the local machine is still in reading status reset it hard
            _foreignDictServer.RemoveFromForeignDict(msg);
            Interlocked.Increment(ref _commitCount);
            // Since the account value has changed machine should fail itself
            lockHolder--;
            return 0;
        }
        public void RunObserver(object source, ElapsedEventArgs e)
        {
            using (System.IO.StreamWriter file = 
                new System.IO.StreamWriter(_path, true))
            {
                string line = "stat: " + e.SignalTime + " " + _successfullRuns + " " + _failRuns + " " + _totalExecutions + " " + _totalCheckFails + " " + _writeCount + " " + _commitCount;
                file.WriteLine(line);
            }
        }
        private int _writeCount = 0;
        private int _commitCount = 0;
        public void WriteAccountChange(BankSM account)
        {
            using (System.IO.StreamWriter file = 
                new System.IO.StreamWriter(_accountChangePath, true))
            {
                string line = "" + DateTime.Now + " " + account.Account.Value;
                file.WriteLine(line);
            }      
        }
        #region VALIDATION_METHODS
        // [VALIDATIONS]
        public bool COMMIT_VALIDATION(BankSM account, Message msg)
        {
            // commit should not fail and no other machine can have the writing lock at the same time
            if(account.Writing == true)
            {
                PrintErrorMessage(account, msg, "COMMIT_VALIDATION");
                return false;
            }
            return true;
        }
        public bool MACHINE_WRITE_RESET_CHECK(BankSM account, Message msg)
        {
            // if my local value has changed or i cannot fire the event anymore
            if(account.CurrentValue != account.Account.Value 
                || !account.StateMachine.CanFire(msg.Ev) 
                || account.StateMachine.State == State.STATE0
                || account.Read == false)
            {
                return true;
            }
            return false;
        }
        public bool WRITE_VALIDATION(BankSM account, Message msg)
        {
            // self write should not fail no matter what
            if(account.Writing == false || !account.Read || !account.StateMachine.CanFire(msg.Ev) 
                || account.StateMachine.State == State.STATE0 || account.CurrentValue != account.Account.Value)
            {
                System.Console.WriteLine(
                    "read: " + !account.Read + "\n" +
                    "canfire: " + !account.StateMachine.CanFire(msg.Ev) + "\n" +
                    "state : " +  (account.StateMachine.State == State.STATE0)  + "\n" +
                    "value: " + (account.CurrentValue != account.Account.Value)
                );
                PrintErrorMessage(account, msg, "WRITE_VALIDATION");
                return false;
            }
            return true;
        }
        public bool WRITE_STATE_VALIDATION(BankSM account, Message msg)
        {
            // A cycle should not fail after doing a writing operation!      
            if(!account.Read 
                    || !account.StateMachine.CanFire(msg.Ev) 
                    || account.StateMachine.State == State.STATE0
                    || account.CurrentValue != account.Account.Value
                    || msg.Ev == Event.FAIL)
            {
                PrintErrorMessage(account, msg, "WRITE_STATE_VALIDATION");
                return false;
            }            
            return true;
        }
        public void PrintErrorMessage(BankSM account, Message msg, string errorMessage)
        {
            System.Console.WriteLine("[" + errorMessage + "]");
            msg.PrintMessage();
            System.Console.WriteLine(account.GetAccountInfo());
            _foreignDictServer.PrintForeignDictionary();
            Console.ReadLine();
        }
        #endregion
    }
}