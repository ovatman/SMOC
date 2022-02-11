using System;
using MathNet.Numerics.Distributions;

// https://numerics.mathdotnet.com/Probability.html

namespace ReplicatedSM
{
    public class PriorityGenerator
    {
        // LOW and HIGH indicates the extreme limits of the generator
        private int LOW;
        private int HIGH;
        private int _offset;
        private Random _uniform;
        private Normal _normal;
        private Bernoulli _bernoulli;
        private Exponential _exponential;
        public PriorityGenerator(int low = 0, int high = 100000, int offset = 1)
        {
            LOW = low;
            HIGH = high;
            _offset = offset;
            // Normal requires mean and standard deviation
            _uniform = new Random();
            _normal = new Normal((LOW + HIGH)/2, 10);
            _bernoulli = new Bernoulli(1);
            _exponential = new Exponential(1);
        }
        // <summary> Uniform uses the pure dotnet random generator </summary>
        public int NextUniform()
        {
            return _uniform.Next(LOW, HIGH) + _offset;
        }
        public int NextNormal()
        {
            return Convert.ToInt32(_normal.Sample()) + _offset;
        }
        public int NextBernoulli()
        {
            return Convert.ToInt32(_bernoulli.Sample()) + _offset;
        }
        public int NextExponential()
        {
            return Convert.ToInt32(_exponential.Sample()) + _offset;
        }        
    }
}