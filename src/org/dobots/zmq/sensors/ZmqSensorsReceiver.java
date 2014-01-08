package org.dobots.zmq.sensors;

import org.dobots.comm.msg.ISensorDataListener;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.ZmqReceiveThread;
import org.dobots.zmq.comm.RobotMessage;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class ZmqSensorsReceiver extends ZmqReceiveThread {
	
	ZMQ.Socket m_oSensorsRecvSocket;
	
	ISensorDataListener m_oSensorDataListener;
	
	public ZmqSensorsReceiver(ZMQ.Socket i_oInSocket, String i_strThreadName) {
		super(ZmqHandler.getInstance().getContext().getContext(), i_oInSocket, i_strThreadName);
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
			
			if (m_oSensorDataListener != null) {
				m_oSensorDataListener.onSensorData(strJson);
			}
		}
	}
		

}
