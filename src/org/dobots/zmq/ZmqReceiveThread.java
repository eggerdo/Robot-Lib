package org.dobots.zmq;

import org.dobots.utilities.DoBotsThread;
import org.zeromq.ZMQ;

import android.os.Looper;

public abstract class ZmqReceiveThread extends DoBotsThread {

	protected ZMQ.Socket m_oInSocket;
	
	private ZMQ.Poller m_oPoller;
	
	public ZmqReceiveThread(ZMQ.Context i_oContext, ZMQ.Socket i_oInSocket, String i_strThreadName) {
		super(i_strThreadName);
		
		m_oInSocket = i_oInSocket;
		m_oPoller = i_oContext.poller(1);
		m_oPoller.register(m_oInSocket, ZMQ.Poller.POLLIN);
	}

	@Override
	public void run() {
		Looper.prepare();
		while(!Thread.currentThread().isInterrupted()) {
			try {
				if (!m_bPaused) {
					if (m_oPoller.poll(1000) > 0) {
						execute();
					}
				} else {
					sleep(10);
				}
			} catch (InterruptedException e) {
				if (!m_bStopped) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		m_oPoller.unregister(m_oInSocket);
	}
	
	public void close() {
		stopThread();
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}
}