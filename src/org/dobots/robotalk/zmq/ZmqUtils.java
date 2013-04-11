package org.dobots.robotalk.zmq;

import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RobotMessage;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class ZmqUtils {
	
	public static void sendCommand(BaseCommand i_oCmd, ZMQ.Socket i_oSocket) {
		String strJSON = i_oCmd.toJSONString();
		RobotMessage oMsg = new RobotMessage(i_oCmd.getRobotID(), strJSON.getBytes());
		ZMsg oZMsg = oMsg.toZmsg();
		oZMsg.send(i_oSocket);
	}
	
//	public static void sendVideoBase64(Base64VideoMessage i_oVideo, ZMQ.Socket i_oSocket) {
//		String strJSON = i_oVideo.toJSONString();
//		RobotMessage oMsg = new RobotMessage(i_oVideo.getRobotID(), strJSON.getBytes());
//		ZMsg oZMsg = oMsg.toZmsg();
//		oZMsg.send(i_oSocket);
//	}

}
