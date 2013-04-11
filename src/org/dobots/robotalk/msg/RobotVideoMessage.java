package org.dobots.robotalk.msg;

import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

public class RobotVideoMessage {

	// A ZMQ video message consists of three ZFrames:
	//
	// -----------------------------
	// | nick | hdeader | rgb data |
	// -----------------------------
	//
	// where nick		is the nick name of the sender which is used for
	//					channel subscription / filtering
	//		 rgb data	is the video frame as a compressed JPEG rgb array. 

	// the nickname of the sender
	public String robotName = "";
	// the nickname of the sender
	public String header = "";
	// the message
	public byte[] data;
	
	private RobotVideoMessage() {
		// nothing to do
	}
	
	public RobotVideoMessage(String i_strRobotName, String i_strHeader, byte[] i_rgbyData) {
		data = i_rgbyData;
		header = i_strHeader;
		robotName = i_strRobotName;
	}
	
	public static RobotVideoMessage fromZMsg(ZMsg i_oMsg) {
		RobotVideoMessage oMsg = new RobotVideoMessage();
		ZFrame oRobot = i_oMsg.pop();
		ZFrame oHeader = i_oMsg.pop();
		ZFrame oData = i_oMsg.pop();
		
		oMsg.data = oData.getData();
		oMsg.header = oHeader.toString();
		oMsg.robotName = oRobot.toString();
		
		return oMsg;
	}
	
	// create a zmsg from the chat message
	public ZMsg toZmsg() {
		ZMsg msg = new ZMsg();
		
		ZFrame channel = new ZFrame(robotName);
		ZFrame msgHeader = new ZFrame(header);
		ZFrame msgData = new ZFrame(data);
		
		msg.push(msgData);
		msg.push(msgHeader);
		msg.push(channel);
		
		return msg;
	}

}

