using System;
using System.Collections.Generic;

namespace ReplicatedSM
{
    public class PriorityGeneratorTest
    {
        private PriorityGenerator _generator = new PriorityGenerator();
        public void TestNormal()
        {
            // Create the mapping
            SortedDictionary<int, int> distribution = new SortedDictionary<int, int>();
            // Generate 100000 numbers and check the distribution
            for(int i=0; i<100000; i++)
            {
                int next = _generator.NextNormal();
                if(distribution.ContainsKey(next))
                {
                    distribution[next]++;
                }   
                else
                {
                    distribution.Add(next, 1);
                }
            }
        }
        public void TestUniform()
        {
            // Create the mapping
            SortedDictionary<int, int> distribution = new SortedDictionary<int, int>();
            // Generate 100000 numbers and check the distribution
            for(int i=0; i<100000; i++)
            {
                int next = _generator.NextUniform();
                if(distribution.ContainsKey(next))
                {
                    distribution[next]++;
                }   
                else
                {
                    distribution.Add(next, 1);
                }
            }
        }       
        public void TestBernoulli()
        {
            // Create the mapping
            SortedDictionary<int, int> distribution = new SortedDictionary<int, int>();
            // Generate 100000 numbers and check the distribution
            for(int i=0; i<100000; i++)
            {
                int next = _generator.NextBernoulli();
                if(distribution.ContainsKey(next))
                {
                    distribution[next]++;
                }   
                else
                {
                    distribution.Add(next, 1);
                }
            }
        }            
        public void TestExponential()
        {
            // Create the mapping
            SortedDictionary<int, int> distribution = new SortedDictionary<int, int>();
            // Generate 100000 numbers and check the distribution
            for(int i=0; i<100000; i++)
            {
                int next = _generator.NextExponential();
                if(distribution.ContainsKey(next))
                {
                    distribution[next]++;
                }   
                else
                {
                    distribution.Add(next, 1);
                }
            }
        }
    }
}