package org.dobots.communication.zmq;

import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;

import android.os.Looper;

public abstract class ZmqReceiveThread extends Thread {

	protected ZMQ.Socket m_oInSocket;
	
	private ZMQ.Poller m_oPoller;
	
	private boolean bPause = false; 
	
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
				if (!bPause) {
					if (m_oPoller.poll() > 0) {
						execute();
					}
				} else {
					Utils.waitSomeTime(10);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		m_oPoller.unregister(m_oInSocket);
	}
	
	protected abstract void execute();
	
	public void close() {
		interrupt();
	}
	
	public void pauseThread() {
		bPause = true;
	}
	
	public void resumeThread() {
		bPause = false;
	}
	
}