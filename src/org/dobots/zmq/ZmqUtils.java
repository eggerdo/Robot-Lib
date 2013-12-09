package org.dobots.zmq;

import org.dobots.zmq.comm.RobotMessage;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;


public class ZmqUtils {
	
	public static void send(String robotID, String data, ZMQ.Socket socket) {
		RobotMessage oMsg = new RobotMessage(robotID, data.getBytes());
		ZMsg oZMsg = oMsg.toZmsg();
		oZMsg.send(socket);
	}
	
}
