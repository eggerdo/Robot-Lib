package org.dobots.zmq.video;


public class FpsCounter {

	// debug frame counters
    private int m_nFpsCounter = 0;
    private long m_lLastTime = System.currentTimeMillis();
    
    private IFpsListener m_oListener;
    
    public FpsCounter() {
    	
    }
    
    public FpsCounter(IFpsListener listener) {
    	setListener(listener);
    }
    
    public void setListener(IFpsListener listener) {
    	m_oListener = listener;
    }

    public void tick() {

        ++m_nFpsCounter;
        long now = System.currentTimeMillis();
        long dt = now - m_lLastTime;
        if (dt >= 1000)
        {
        	if (m_oListener != null) {
        		m_oListener.onFPS((double)m_nFpsCounter / dt * 1000);
        	}
            m_lLastTime = now;
            m_nFpsCounter = 0;
        }

    }
    
    public void reset() {
    	m_nFpsCounter = 0;
    	m_lLastTime = System.currentTimeMillis();
    }

}
