package org.dobots.zmq.comm;

import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

public class RobotMessage {
	
	// A ZMQ chat message consists of two ZFrames:
	//
	// -----------------
	// | target | data |
	// -----------------
	//
	// where robotName	is the name of the robot
	// 		 data	 	is ...
	
	// the nickname of the sender
	public String robotName = "";
	// the message
	public byte[] data;
	
	private RobotMessage() {
		// nothing to do
	}
	
	public RobotMessage(String i_strRobotName, byte[] i_rgbyData) {
		data = i_rgbyData;
		robotName = i_strRobotName;
	}
	
	public static RobotMessage fromZMsg(ZMsg i_oMsg) {
		RobotMessage oMsg = new RobotMessage();
		ZFrame oRobot = i_oMsg.pop();
		ZFrame oData = i_oMsg.pop();
		
		oMsg.data = oData.getData();
		oMsg.robotName = oRobot.toString();
		
		return oMsg;
	}
	
	// create a zmsg from the chat message
	public ZMsg toZmsg() {
		ZMsg msg = new ZMsg();
		
		ZFrame channel = new ZFrame(robotName);
		ZFrame msgData = new ZFrame(data);
		
		msg.push(msgData);
		msg.push(channel);
		
		return msg;
	}

}

