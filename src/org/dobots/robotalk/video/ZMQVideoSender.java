package org.dobots.robotalk.video;

import org.dobots.robotalk.msg.RawVideoMessage;
import org.dobots.robotalk.msg.RobotVideoMessage;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import robots.IVideoListener;

public class ZMQVideoSender implements IVideoListener {
	
	private ZMQ.Socket m_oVideoSocket;
	
	private String m_strRobotID;
	
	public ZMQVideoSender(String i_strRobotID) {
		m_strRobotID = i_strRobotID;

		m_oVideoSocket = ZmqHandler.getInstance().obtainVideoSendSocket();
	}

	@Override
	public void frameReceived(byte[] rgb) {

		RawVideoMessage vmsg = new RawVideoMessage(m_strRobotID, rgb, 0);
		RobotVideoMessage oMsg = new RobotVideoMessage(vmsg.getRobotID(), vmsg.getHeader(), vmsg.getVideoData());
		
		ZMsg zmsg = oMsg.toZmsg();
		zmsg.send(m_oVideoSocket);
	}

}
