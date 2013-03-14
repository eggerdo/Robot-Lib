package org.dobots.robotalk.zmq;

import org.zeromq.ZMQ;

public abstract class ZmqReceiveThread extends Thread {

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
		try {
			while(!Thread.currentThread().isInterrupted()) {
				if (m_oPoller.poll() > 0) {
					execute();
				}
			}
			m_oPoller.unregister(m_oInSocket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected abstract void execute();
	
}