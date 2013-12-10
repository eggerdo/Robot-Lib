package org.dobots.communication.sensors;

import org.dobots.communication.msg.RobotMessage;
import org.dobots.communication.zmq.ZmqReceiveThread;
import org.dobots.lib.comm.msg.SensorMessageArray;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class ZmqSensorsReceiver extends ZmqReceiveThread {
	
	public interface ISensorDataListener {
		public void onSensorData(SensorMessageArray data);
	}
	
	ZMQ.Socket m_oSensorsRecvSocket;
	
	ISensorDataListener m_oSensorDataListener;
	
	public ZmqSensorsReceiver(ZMQ.Context i_oContext, ZMQ.Socket i_oInSocket, String i_strThreadName) {
		super(i_oContext, i_oInSocket, i_strThreadName);
	}
	
	public void setSensorDataListener(ISensorDataListener listener) {
		m_oSensorDataListener = listener;
	}
	
	@Override
	protected void execute() {
		ZMsg oZMsg = ZMsg.recvMsg(m_oInSocket);
		if (oZMsg != null) {
			// create a chat message out of the zmq message
			RobotMessage oSensorMsg = RobotMessage.fromZMsg(oZMsg);
			
			String strJson = Utils.byteArrayToString(oSensorMsg.data);
			final SensorMessageArray sensorData = SensorMessageArray.decodeJSON(oSensorMsg.robotName, strJson);

			if (m_oSensorDataListener != null) {
				m_oSensorDataListener.onSensorData(sensorData);
			}
		}
	}
		

}
