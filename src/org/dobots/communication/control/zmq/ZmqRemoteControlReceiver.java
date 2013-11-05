package org.dobots.communication.control.zmq;

import org.dobots.communication.control.RemoteControlReceiver;
import org.dobots.communication.msg.RoboCommands;
import org.dobots.communication.msg.RoboCommands.BaseCommand;
import org.dobots.communication.msg.RoboCommands.CameraCommand;
import org.dobots.communication.msg.RoboCommands.DriveCommand;
import org.dobots.communication.msg.RobotMessage;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqReceiveThread;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import robots.ctrl.ICameraControlListener;
import robots.ctrl.IDriveControlListener;
import robots.ctrl.RemoteControlHelper;
import android.app.Activity;
import android.util.Log;

public class ZmqRemoteControlReceiver extends RemoteControlReceiver {

	private static final String TAG = "ZmqRemoteControl";
	
	private ZContext m_oZContext;
	
	private ZMQ.Socket m_oCmdRecvSocket;
	
	private CommandReceiveThread m_oReceiver;

	public ZmqRemoteControlReceiver(String i_strName) {
		m_oZContext = ZmqHandler.getInstance().getContext();

		m_oCmdRecvSocket = ZmqHandler.getInstance().obtainCommandRecvSocket();
//		m_oCmdRecvSocket.subscribe(i_strName.getBytes());
		m_oCmdRecvSocket.subscribe("".getBytes());

		m_oReceiver = new CommandReceiveThread(m_oZContext.getContext(), m_oCmdRecvSocket, i_strName + "ZmqRC");
	}

	/**
	 * starts the zmq message receive thread. incoming messages will be parsed and forwarded
	 * to the respective listener (camera vs control).
	 * NOTE: do not start the receiver together with a zmq remote control listener. otherwise the
	 * receiver will forward the messages to the listener which sends them back as a zmq message to the 
	 * handler which sends them out again to the receiver, resulting in an endless loop. to avoid that the
	 * receiver only forwards messages if the listener is not an instance of ZmqRemoteControlSender.
	 * @param i_strName name used to identify the thread
	 */
	public void start() {
		m_oReceiver.start();
	}
	
	// in contrast to a normal RemoteControlHelper, the ZmqRemoteControl can receive remote commands
	// over Zmq messages. 
	class CommandReceiveThread extends ZmqReceiveThread {

		public CommandReceiveThread(Context i_oContext, Socket i_oInSocket,	String i_strThreadName) {
			super(i_oContext, i_oInSocket, i_strThreadName);
		}

		@Override
		protected void execute() {
			ZMsg oZMsg = ZMsg.recvMsg(m_oCmdRecvSocket);
			if (oZMsg != null) {

				// create a chat message out of the zmq message
				RobotMessage oCmdMsg = RobotMessage.fromZMsg(oZMsg);
				
				String strJson = Utils.byteArrayToString(oCmdMsg.data);
				BaseCommand oCmd = RoboCommands.decodeCommand(strJson);

				if (oCmd instanceof DriveCommand) {
					
					if (m_oRemoteControlListener instanceof ZmqRemoteControlSender) {
						Log.e(TAG, "receiver started with ZmqRemoteSender as listener. Abort!");
						return;
					} else {
				
						DriveCommand oDriveCmd = (DriveCommand)oCmd;
						m_oRemoteControlListener.onMove(oDriveCmd.eMove, oDriveCmd.dblSpeed, oDriveCmd.dblRadius);
					}
					
				} else if (oCmd instanceof CameraCommand) {
					
					if (m_oCameraListener instanceof ZmqRemoteControlSender) {
						Log.e(TAG, "receiver started with ZmqRemoteSender as listener. Abort!");
						return;
					} else {
				
						CameraCommand oCameraCmd = (CameraCommand)oCmd;
						switch(oCameraCmd.eType) {
						case OFF:
							m_oCameraListener.switchCameraOff();
							break;
						case ON:
							m_oCameraListener.switchCameraOn();
							break;
						case TOGGLE:
							if (m_oRemoteControlListener != null) {
								m_oRemoteControlListener.toggleInvertDrive();
							}
							m_oCameraListener.toggleCamera();
							break;
						case UP:
							m_oCameraListener.cameraUp();
							break;
						case DOWN:
							m_oCameraListener.cameraDown();
							break;
						case STOP:
							m_oCameraListener.cameraStop();
						}
					}
				}
			}
		}
		
	}

	public void close() {
		if (m_oCmdRecvSocket != null) {
			m_oCmdRecvSocket.close();
			m_oCmdRecvSocket = null;
		}
		if (m_oReceiver != null) {
			m_oReceiver.close();
			m_oReceiver = null;
		}
	}


}
