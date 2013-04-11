package org.dobots.robotalk.zmq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class ZmqMessageHandler {
	
	public interface ZmqMessageListener {
		public void onMessage(ZMsg i_oMsg);
	}
	
	private ZContext m_oZContext;
	
	// the channel on which messages are sent out
	private ZMQ.Socket m_oOutSocket = null;
	// the channel on which messages are coming in
	private ZMQ.Socket m_oInSocket = null;
	
	private ZmqMessageReceiveThread m_oRecvThread = null;
	
	private boolean m_bOutConnected;
	private boolean m_bInConnected;
	
	private ZmqMessageListener m_oMessageListener;
	
	public ZmqMessageHandler() {
		m_oZContext = ZmqHandler.getInstance().getContext();
	}

	/**
	 * If only incoming video should be handled, supply null as the OutSockete parameter (or vice versa)
	 * @param i_oInSocket
	 * @param i_oOutSocket
	 */
	public void setupConnections(ZMQ.Socket i_oInSocket, ZMQ.Socket i_oOutSocket) {
		
		if (i_oInSocket != null) {
			m_oInSocket = i_oInSocket;

			m_bInConnected = true;
			
			m_oRecvThread = new ZmqMessageReceiveThread(m_oZContext.getContext(), m_oInSocket, "IncomingMsgReceiver");
			m_oRecvThread.start();
		}
		
		if (i_oOutSocket != null) {
			m_oOutSocket = i_oOutSocket;

			m_bOutConnected = true;
		}
	}

	public void setIncomingMessageListener(ZmqMessageListener i_oListener) {
		m_oMessageListener = i_oListener;
	}

	private class ZmqMessageReceiveThread extends ZmqReceiveThread {
		
		public ZmqMessageReceiveThread(Context i_oContext, Socket i_oInSocket,
				String i_strThreadName) {
			super(i_oContext, i_oInSocket, i_strThreadName);
		}
		
		@Override
		protected void execute() {
			ZMsg msg = ZMsg.recvMsg(m_oInSocket);
			if (msg != null) {
				if (m_oMessageListener != null) {
					m_oMessageListener.onMessage(msg);
				}
			}
		}
		
	}

	public void closeConnections() {

		m_bOutConnected = false;
		m_bInConnected = false;
		
		if (m_oOutSocket != null) {
			m_oOutSocket.close();
			m_oOutSocket = null;
		}
		
		if (m_oInSocket != null) {
			m_oInSocket.close();
			m_oInSocket = null;
		}

	}

	public void sendZmsg(ZMsg i_oMsg) {
		if (m_bOutConnected) {
			i_oMsg.send(m_oOutSocket);
		}
	}

}
