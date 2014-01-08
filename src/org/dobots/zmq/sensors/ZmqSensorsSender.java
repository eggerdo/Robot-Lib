package org.dobots.zmq.sensors;

import org.dobots.comm.msg.SensorMessageArray;
import org.dobots.comm.msg.SensorMessageObj;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.ZmqUtils;
import org.zeromq.ZMQ;

public class ZmqSensorsSender {

	private ZMQ.Socket m_oSensorsSendSocket;
	
	public ZmqSensorsSender() {
		m_oSensorsSendSocket = ZmqHandler.getInstance().obtainSensorsSendSocket();
	}
	
	public void sendSensors(SensorMessageArray data) {
		if (m_oSensorsSendSocket != null) {
			ZmqUtils.send(data.getRobotID(), data.toJSONString(), m_oSensorsSendSocket);
		}
	}

	public void sendSensors(SensorMessageObj data) {
		if (m_oSensorsSendSocket != null) {
			ZmqUtils.send(data.getRobotID(), data.toJSONString(), m_oSensorsSendSocket);
		}
	}

}
