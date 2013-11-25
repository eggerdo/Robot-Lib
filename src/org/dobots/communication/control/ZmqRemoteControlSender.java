/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief:	Helper class to handle remote control over Zmq
* @file: ${file_name}
*
* @desc:	Creates Zmq messsages out of remote commands and sends them out.
*
*
* Copyright (c) 2013 Dominik Egger <dominik@dobots.nl>
*
* @author:		Dominik Egger
* @date:		2.9.2013
* @project:		Robot-Lib
* @company:		Distributed Organisms B.V.
*/
package org.dobots.communication.control;

import org.dobots.communication.msg.RoboCommands.BaseCommand;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqUtils;
import org.zeromq.ZMQ;

public class ZmqRemoteControlSender extends RemoteControlSender {

	private ZMQ.Socket m_oCmdSendSocket;
	
	public ZmqRemoteControlSender(String i_strRobot) {
		m_oCmdSendSocket = ZmqHandler.getInstance().obtainCommandSendSocket();
		m_strRobot = i_strRobot;
	}
	
	protected void sendCommand(BaseCommand i_oCmd) {
		if (m_oCmdSendSocket != null) {
			ZmqUtils.sendCommand(i_oCmd, m_oCmdSendSocket);
		}
	}

	public void close() {
		m_oCmdSendSocket.close();
	}

}
