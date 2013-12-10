package org.dobots.communication.zmq;

import org.dobots.communication.msg.RobotMessage;
import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;
import org.dobots.lib.comm.msg.SensorMessageArray;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class ZmqUtils {
	
	public static void sendCommand(BaseCommand i_oCmd, ZMQ.Socket i_oSocket) {
		String strJSON = i_oCmd.toJSONString();
		send(i_oCmd.getRobotID(), strJSON.getBytes(), i_oSocket);
//		RobotMessage oMsg = new RobotMessage(i_oCmd.getRobotID(), strJSON.getBytes());
//		ZMsg oZMsg = oMsg.toZmsg();
//		oZMsg.send(i_oSocket);
	}
	
//	public static void sendVideoBase64(Base64VideoMessage i_oVideo, ZMQ.Socket i_oSocket) {
//		String strJSON = i_oVideo.toJSONString();
//		RobotMessage oMsg = new RobotMessage(i_oVideo.getRobotID(), strJSON.getBytes());
//		ZMsg oZMsg = oMsg.toZmsg();
//		oZMsg.send(i_oSocket);
//	}
	
	public static void sendSensorData(SensorMessageArray data, ZMQ.Socket socket) {
		String strJSON = data.toJSONString();
		send(data.getRobotID(), strJSON.getBytes(), socket);
	}
	
	private static void send(String robotID, byte[] data, ZMQ.Socket socket) {
		RobotMessage oMsg = new RobotMessage(robotID, data);
		ZMsg oZMsg = oMsg.toZmsg();
		oZMsg.send(socket);
	}
	
}
