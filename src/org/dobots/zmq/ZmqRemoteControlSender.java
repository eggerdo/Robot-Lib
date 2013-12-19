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
package org.dobots.zmq;

import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;
import org.zeromq.ZMQ;

import robots.ctrl.control.RemoteControlSender;

public class ZmqRemoteControlSender extends RemoteControlSender {

	private ZMQ.Socket m_oCmdSendSocket;
	
	public ZmqRemoteControlSender(String i_strRobot) {
		m_oCmdSendSocket = ZmqHandler.getInstance().obtainCommandSendSocket();
		m_strRobot = i_strRobot;
	}
	
	protected void sendCommand(BaseCommand i_oCmd) {
		if (m_oCmdSendSocket != null) {
			ZmqUtils.send(i_oCmd.getRobotID(), i_oCmd.toJSONString(), m_oCmdSendSocket);
		}
	}

	public void close() {
		m_oCmdSendSocket.close();
	}

}
