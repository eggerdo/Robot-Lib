package org.dobots.communication.sensors;

import org.dobots.comm.msg.SensorMessageArray;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqUtils;
import org.zeromq.ZMQ;

public class ZmqSensorsSender {

	private ZMQ.Socket m_oSensorsSendSocket;
	
	public ZmqSensorsSender() {
		m_oSensorsSendSocket = ZmqHandler.getInstance().obtainSensorsSendSocket();
	}
	
	public void sendSensors(SensorMessageArray data) {
		if (m_oSensorsSendSocket != null) {
			ZmqUtils.sendSensorData(data, m_oSensorsSendSocket);
		}
	}

}
