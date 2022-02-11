using System;
using ReplicatedSM.Enums;
using Stateless;
/*
    This class is used solely for building the required state machine
    All functionailities should be used by the SMAgent
*/
namespace ReplicatedSM.SMFiles
{
    public class Rsm2PCBuilder: IRSMBuilder
    {
        private string _name;
        private bool _logging;
        public Rsm2PCBuilder(string name, bool logging)
        {
            _name = name;
            _logging = logging;
        }
        /*
            Sequential SM design for basic atm bank process.
         */
        public StateMachine<State, Event> BuildStateMachine(string smName)
        {
            StateMachine<State, Event> sm = new StateMachine<State, Event>(State.STATE0);

            sm.Configure(State.STATE0).Permit(Event.TRIVIAL1, State.STATE1);
            sm.Configure(State.STATE1).Permit(Event.TRIVIAL2, State.STATE2);
            sm.Configure(State.STATE2).Permit(Event.TRIVIAL3, State.STATE3);
            sm.Configure(State.STATE3).Permit(Event.READ, State.STATE4);
            sm.Configure(State.STATE4).Permit(Event.TRIVIAL4, State.STATE5);
            sm.Configure(State.STATE5).Permit(Event.TRIVIAL5, State.STATE6);
            sm.Configure(State.STATE6).Permit(Event.TRIVIAL6, State.STATE7);
            sm.Configure(State.STATE7).Permit(Event.TRIVIAL7, State.STATE8);
            sm.Configure(State.STATE8).Permit(Event.TRIVIAL8, State.STATE9);
            sm.Configure(State.STATE9).Permit(Event.TRIVIAL9, State.STATE10);
            sm.Configure(State.STATE10).Permit(Event.TRIVIAL10, State.STATE11);
            sm.Configure(State.STATE11).Permit(Event.TRIVIAL11, State.STATE12);
            sm.Configure(State.STATE12).Permit(Event.TRIVIAL12, State.STATE13);
            sm.Configure(State.STATE13).Permit(Event.TRIVIAL13, State.STATE14);
            sm.Configure(State.STATE14).Permit(Event.TRIVIAL14, State.STATE15);
            sm.Configure(State.STATE15).Permit(Event.WRITE, State.STATE16);
            sm.Configure(State.STATE16).Permit(Event.TRIVIAL15, State.STATE17);
            // Additional SUCCESS event for wrapping up a cycle
            sm.Configure(State.STATE17).Permit(Event.FINISH, State.STATE0);

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
            sm.OnTransitioned((transition) => {
                // if(_logging)
                //     Console.WriteLine(smName + " state changed from: " + transition.Source + " to: " + transition.Destination + " with event: " + transition.Trigger);
            });
            // 
            sm.OnTransitioned((transition) => {
                if(_logging)
                {
                    if(transition.Destination == State.STATE8)
                    {
                        // Console.WriteLine("Reached a final state. Still might continue.");
                    }
                    else if(transition.Destination == State.STATE0 && transition.Trigger == Event.FINISH)
                    {
                        // Console.WriteLine("Wrapping up a request cycle.");
                    }
                }
            });            

            return sm;
        }
    }
}