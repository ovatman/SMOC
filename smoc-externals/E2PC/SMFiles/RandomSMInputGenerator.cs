using System;
using System.Collections.Generic;
using System.Linq;
using ReplicatedSM.Enums;
using Stateless;

namespace ReplicatedSM.SMFiles
{
    public class RandomSMInputGenerator
    {
        private StateMachine<State, Event> _stateMachine;
        private CoinFlipGenerator _cfg;
        private int writeCtr = 0;
        private int readCtr = 0;
        public RandomSMInputGenerator(StateMachine<State, Event> stateMachine, int writeRatio)
        {
            _stateMachine = stateMachine;
            _cfg = new CoinFlipGenerator(0, 1000, writeRatio);
        }
        public List<List<Event>> GenerateNumberOfTrace(int number)
        {
            List<List<Event>> traceList = new List<List<Event>>();
            for(int i=0;i<number;i++)
            {
                // traceList.Add(GenerateTrace());
                traceList.Add(GenerateBiasedBranchingTrace());
            }
            Console.WriteLine("Generated write-read traces count: " + writeCtr + " - " + readCtr);
            return traceList;
        }
        // This method chooses branch cases with complete uniform distribution
        public List<Event> GenerateTrace()
        {
            List<Event> newTrace = new List<Event>();
            /*
                Generate a random event path from the state machine
             */
            Event current = Event.TRIVIAL1;
            List<Event> exception = new List<Event>();
            exception.Add(Event.FAIL);
            //
            var random = new Random();
            while(current != Event.FINISH)
            {
                // Get triggers from current state
                IEnumerable<Event> a = _stateMachine.GetPermittedTriggers();
                a = a.Except(exception);
                // Add it to the trace
                // New Case -- when a branching happens, as in there are actually multiple choices to go from one state
                // we can introduce biased random generation
                current = a.ToList()[random.Next(a.Count())];
                newTrace.Add(current);
                // Don't forget to trigger it
                _stateMachine.Fire(current);
            }
            // Return the new trace
            return newTrace;
        }
        // This method makes choices depending on the biased weight for flipping a coin
        public List<Event> GenerateBiasedBranchingTrace()
        {
            List<Event> newTrace = new List<Event>();
            /*
                Generate a random event path from the state machine
             */
            Event current = Event.TRIVIAL1;
            List<Event> exception = new List<Event>();
            exception.Add(Event.FAIL);
            //
            var random = new Random();
            while(current != Event.FINISH)
            {
                // Get triggers from current state
                IEnumerable<Event> a = _stateMachine.GetPermittedTriggers();
                a = a.Except(exception);
                var currentFlip = 0;
                // Non-branching case
                if(a.Count() == 1)
                    current = a.ToList()[0];
                // Branching case
                else
                {
                    currentFlip = _cfg.NextFlip();
                    // Biased Coin Flip
                    current = currentFlip == 0 ? a.ToList()[0] : a.ToList()[1];                    
                    if(current == Event.CHECK || current == Event.TRIVIAL_W)
                        writeCtr++;
                    else
                        readCtr++;
                }
                // Add the event to the trace
                newTrace.Add(current);
                // Don't forget to trigger it
                _stateMachine.Fire(current);
            }
            // Return the new trace
            return newTrace;
        }        
    }
}