using RabbitMQ.Client.Events;
using ReplicatedSM.BankApp;
using ReplicatedSM.Enums;
using ReplicatedSM.Service;
using Stateless;

namespace ReplicatedSM
{
    public interface IRSMService
    {
        int Process(Message message);
        int WriteProcess(BankSM account, StateMachine<State, Event> _stateMachine, Message msg, BankSM _account);
        int Response(Message msg);
    }
}