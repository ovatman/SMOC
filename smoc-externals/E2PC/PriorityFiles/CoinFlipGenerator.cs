using System;
using MathNet.Numerics.Distributions;

// https://numerics.mathdotnet.com/Probability.html

namespace ReplicatedSM
{
    public class CoinFlipGenerator
    {
        // LOW and HIGH indicates the extreme limits of the generator
        private int LOW;
        private int HIGH;
        private int BIAS;
        private PriorityGenerator _pg;
        public CoinFlipGenerator(int low, int high, int bias)
        {
            Console.WriteLine("Generating a flipper with bias: " + bias);
            LOW = low;
            HIGH = high;
            BIAS = bias;

            _pg = new PriorityGenerator(low, high, 0);
        }
        // <summary> Uniform uses the pure dotnet random generator </summary>
        public int NextFlip()
        {
            int nextNormal = _pg.NextUniform();
            return nextNormal < BIAS ? 0 : 1;
        }    
    }
}