package org.dobots.robotalk.control;

import org.dobots.robotalk.msg.RoboCommands;
import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommand;
import org.dobots.robotalk.msg.RoboCommands.DriveCommand;
import org.dobots.robotalk.msg.RobotMessage;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqReceiveThread;
import org.dobots.robotalk.zmq.ZmqTypes;
import org.dobots.utilities.Utils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import robots.IRobotDevice;
import android.app.Activity;

public class ZmqRemoteControlHelper extends RemoteControlHelper {

	private static final String TAG = "ZmqRemoteControl";
	
	private ZContext m_oZContext;
	
	private ZMQ.Socket m_oCmdRecvSocket;
	
	private CommandReceiveThread m_oReceiver;

	private ICameraControlListener m_oCameraListener;
	
	public ZmqRemoteControlHelper(Activity i_oActivity, IRobotDevice i_oRobot,
			IRemoteControlListener i_oListener) {
		super(i_oActivity, i_oRobot, i_oListener);
		
		m_oZContext = ZmqHandler.getInstance().getContext();
		
		setupCommandConnections();
	}
	
	public void setupCommandConnections() {
		
		m_oCmdRecvSocket = ZmqHandler.getInstance().obtainCommandRecvSocket();
//		m_oCmdRecvSocket.subscribe(m_oRobot.getID().getBytes());
		m_oCmdRecvSocket.subscribe("".getBytes());

		m_oReceiver = new CommandReceiveThread(m_oZContext.getContext(), m_oCmdRecvSocket, "ZmqRC:remote");
		m_oReceiver.start();
	}

	public void setCameraControlListener(ICameraControlListener i_oCameraListener) {
		m_oCameraListener = i_oCameraListener;
	}
	
	public void toggleCamera() {
		m_oCameraListener.toggleCamera();
	}
	
	public void switchCameraOn() {
		m_oCameraListener.switchCameraOn();
	}

	public void switchCameraOff() {
		m_oCameraListener.switchCameraOff();
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
					
					DriveCommand oDriveCmd = (DriveCommand)oCmd;
					m_oRemoteControlListener.onMove(oDriveCmd.eMove, oDriveCmd.dblSpeed, oDriveCmd.dblRadius);
					
				} else if (oCmd instanceof CameraCommand) {
					CameraCommand oCameraCmd = (CameraCommand)oCmd;
					switch(oCameraCmd.eType) {
					case OFF:
						switchCameraOff();
						break;
					case ON:
						switchCameraOn();
						break;
					case TOGGLE:
						toggleCamera();
						break;
					}
				}
			}
		}
		
	}


}
