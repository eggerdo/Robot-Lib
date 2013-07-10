package org.dobots.communication.video;

import org.dobots.communication.msg.VideoMessage;
import org.dobots.communication.zmq.ZmqHandler;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import robots.IVideoListener;

public class ZmqVideoSender implements IVideoListener {

	private ZMQ.Socket m_oVideoSocket;
	
	private byte[] robotID;
	
	public ZmqVideoSender(String i_strRobotID) {
		robotID = i_strRobotID.getBytes();

		m_oVideoSocket = ZmqHandler.getInstance().obtainVideoSendSocket();
	}

	@Override
	public void frameReceived(byte[] rgb) {

		VideoMessage oMsg = new VideoMessage(robotID, rgb, 0);
		
		ZMsg zmsg = oMsg.toZmsg();
		zmsg.send(m_oVideoSocket);
	}

}
