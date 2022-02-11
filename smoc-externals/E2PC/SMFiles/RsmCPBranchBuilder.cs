using System;
using ReplicatedSM.Enums;
using Stateless;
/*
    This class is used solely for building the required state machine
    All functionailities should be used by the SMAgent
*/
namespace ReplicatedSM.SMFiles
{
    public class RsmCPBranchBuilder: IRSMBuilder
    {
        private string _name;
        private bool _logging;
        public RsmCPBranchBuilder(string name, bool logging)
        {
            _name = name;
            _logging = logging;
        }
        public StateMachine<State, Event> BuildStateMachine(string smName)
        {
            StateMachine<State, Event> sm = new StateMachine<State, Event>(State.STATE0);

            sm.Configure(State.STATE0).Permit(Event.TRIVIAL1, State.STATE1);
            sm.Configure(State.STATE1).Permit(Event.TRIVIAL2, State.STATE2);
            sm.Configure(State.STATE2).Permit(Event.READ, State.STATE3);
            sm.Configure(State.STATE3).Permit(Event.TRIVIAL3, State.STATE4);

            // WRITE PATH
            sm.Configure(State.STATE4).Permit(Event.CHECK, State.STATE5);
            sm.Configure(State.STATE5).Permit(Event.TRIVIAL4, State.STATE6);
            sm.Configure(State.STATE6).Permit(Event.TRIVIAL5, State.STATE7);
            sm.Configure(State.STATE7).Permit(Event.WRITE, State.STATE8);
            sm.Configure(State.STATE8).Permit(Event.FINISH, State.STATE0);

            // TRIVIAL ONLY PATH
            sm.Configure(State.STATE4).Permit(Event.TRIVIAL_R, State.STATE10);
            sm.Configure(State.STATE10).Permit(Event.TRIVIAL4, State.STATE11);
            sm.Configure(State.STATE11).Permit(Event.TRIVIAL5, State.STATE12);
            sm.Configure(State.STATE12).Permit(Event.TRIVIAL6, State.STATE13);
            sm.Configure(State.STATE13).Permit(Event.FINISH, State.STATE0);
            
            // Additional FAIL events for resetting states
            // Do not forget, you need to have additional checkpoints somewhere else
            // for this to work, otherwise it is pointless
            sm.Configure(State.STATE0).PermitReentry(Event.FAIL);
            sm.Configure(State.STATE1).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE2).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE3).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE4).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE5).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE6).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE7).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE8).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE9).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE10).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE11).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE12).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE13).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE14).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE15).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE16).Permit(Event.FAIL, State.STATE0);
            sm.Configure(State.STATE17).Permit(Event.FAIL, State.STATE0);

            // Observers for logging
            // Execute at every transition
            // sm.OnTransitioned((transition) => {
            //     if(_logging)
            //         Console.WriteLine(smName + " state changed from: " + transition.Source + " to: " + transition.Destination + " with event: " + transition.Trigger);
            // });
            

            return sm;
        }
    }
}