package org.dobots.zmq;

import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class ZmqForwarderThread extends ZmqReceiveThread {
	
	protected Socket m_oOutSocket;

	public ZmqForwarderThread(Context i_oContext, Socket i_oInSocket, Socket i_oOutSocket, String i_strThreadName) {
		super(i_oContext, i_oInSocket, i_strThreadName);
		
		m_oOutSocket = i_oOutSocket;
	}

	@Override
	protected void execute() {
		ZMsg msg = ZMsg.recvMsg(m_oInSocket);
		if (msg != null) {
			msg.send(m_oOutSocket);
		}
	}
	
}