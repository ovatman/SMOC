using ReplicatedSM.Enums;
using Stateless;

namespace ReplicatedSM
{
    public interface IRSMBuilder
    {
        StateMachine<State, Event> BuildStateMachine(string name);
    }
}