package tr.edu.itu.cloudcorelab.geocoordinator.analyze;

import tr.edu.itu.cloudcorelab.geomessaging.ThroughputResult;

public class Analyzer {
    
    private long m_start = 0;

    private long m_stop = 0;

    private long m_req = 0;

    private boolean m_is_started = false;

    public void start(){
        reset();
        m_is_started = true;
        m_start = System.nanoTime();
    }

    public void stop(){
        m_is_started = false;
        m_stop = System.nanoTime();
    }

    public void increase(){
        if (m_is_started)
            m_req++;
    }

    public ThroughputResult getThroughput(){
        ThroughputResult result =  ThroughputResult.getDefaultInstance();
        
        result = ThroughputResult.getDefaultInstance().toBuilder().setNumberOfRequest(m_req).setElapsedTimeInMilli((m_stop - m_start) / 1000000).build();

        return result;
    }

    public void reset(){
        m_req = 0;
        m_start = 0;
        m_stop = 0;
    }
}
