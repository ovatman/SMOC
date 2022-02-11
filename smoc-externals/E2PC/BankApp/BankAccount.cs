using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using ReplicatedSM.Enums;
using ReplicatedSM.SMFiles;
using Stateless;

namespace ReplicatedSM.BankApp
{
    public class BankAccount
    {
        private static Dictionary<int, BankSM> _db;
        private static Rsm2PCBuilder _2pcBuilder = new Rsm2PCBuilder("builder", true);
        private static Rsm2PCBranchBuilder _2pcBranchBuilder = new Rsm2PCBranchBuilder("builder", true);
        private static RsmCPBuilder _CPBuilder = new RsmCPBuilder("builder", true);
        private static RsmCPBranchBuilder _CPBranchBuilder = new RsmCPBranchBuilder("builder", true);
        private static string _serverType;
        private static string _rsmType;
        public BankAccount(string serverType, string rsmType)
        {
            _db = new Dictionary<int, BankSM>();
            _serverType = serverType;
            _rsmType = rsmType;
        }
        public void AddAccount(int Id, int Value)
        {
            if(_serverType == "2PC")
            {
                if(_rsmType == "N")
                {
                    _db.Add(Id, new BankSM{
                        StateMachine= _2pcBuilder.BuildStateMachine(Id.ToString()),
                        Account= new Account{Id=Id, Value=Value, InUse=false, Changed=DateTime.Now}
                    });
                }
                else if(_rsmType == "B")
                {
                    _db.Add(Id, new BankSM{
                        StateMachine= _2pcBranchBuilder.BuildStateMachine(Id.ToString()),
                        Account= new Account{Id=Id, Value=Value, InUse=false, Changed=DateTime.Now}
                    });
                }
            }
            else
            {
                if(_rsmType == "N")
                {
                    _db.Add(Id, new BankSM{
                        StateMachine= _CPBuilder.BuildStateMachine(Id.ToString()),
                        Account= new Account{Id=Id, Value=Value, InUse=false, Changed=DateTime.Now}
                    }); 
                }
                else
                {
                    _db.Add(Id, new BankSM{
                        StateMachine= _CPBranchBuilder.BuildStateMachine(Id.ToString()),
                        Account= new Account{Id=Id, Value=Value, InUse=false, Changed=DateTime.Now}
                    });  
                }
            }    
        }
        public BankSM GetAccount(int Id)
        {
            return _db[Id];
        }
    }
    public class BankSM
    {
        public int Id { get; set; }
        public StateMachine<State, Event> StateMachine { get; set; }
        public Account Account { get; set; }
        public int CurrentValue { get; set; } = -1;
        // public readonly object Semaphore = new object(); 
        public bool Writing { get; set; } = false;
        public bool Read { get; set; } = false;
        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Reset()
        {
            CurrentValue = -1;
            Writing = false;
            Read = false;
        }
        
        [MethodImpl(MethodImplOptions.Synchronized)]
        public void SoftReset()
        {
            CurrentValue = -1;
            Read = false;
        }
        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Deposit(int Value)
        {
            Account.Value += Value;
        }        
        public string GetAccountInfo()
        {
            return this.Account.Value + " - " + StateMachine.State + " - " + CurrentValue + " - " + Read + " - " + Writing;
        }
    }
    public class Account
    {
        public int Id { get; set; }
        public int Value { get; set; }
        public bool InUse { get; set; }
        public DateTime Changed { get; set; }
    }
}