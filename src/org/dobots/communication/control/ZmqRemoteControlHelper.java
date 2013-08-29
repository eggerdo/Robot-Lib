package org.dobots.communication.control;

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
import robots.ctrl.IRemoteControlListener;
import robots.ctrl.RemoteControlHelper;
import android.app.Activity;

public class ZmqRemoteControlHelper extends RemoteControlHelper {

	private static final String TAG = "ZmqRemoteControl";
	
	private ZContext m_oZContext;
	
	private ZMQ.Socket m_oCmdRecvSocket;
	
	private CommandReceiveThread m_oReceiver;

	private ICameraControlListener m_oCameraListener;
	
	public ZmqRemoteControlHelper(BaseActivity i_oActivity, IRemoteControlListener i_oListener) {
		super(i_oActivity, i_oListener);
		
		m_oZContext = ZmqHandler.getInstance().getContext();
		
//		setupCommandConnections(i_strName);
	}
	
	public ZmqRemoteControlHelper(IRemoteControlListener i_oListener, String i_strName) {
		super(i_oListener);
		
		m_oZContext = ZmqHandler.getInstance().getContext();
		
		setupCommandConnections(i_strName);
	}
	
	public void setupCommandConnections(String i_strName) {
		
		m_oCmdRecvSocket = ZmqHandler.getInstance().obtainCommandRecvSocket();
//		m_oCmdRecvSocket.subscribe(i_strName.getBytes());
		m_oCmdRecvSocket.subscribe("".getBytes());

		m_oReceiver = new CommandReceiveThread(m_oZContext.getContext(), m_oCmdRecvSocket, i_strName + "ZmqRC");
		m_oReceiver.start();
	}

	public void setCameraControlListener(ICameraControlListener i_oCameraListener) {
		m_oCameraListener = i_oCameraListener;
	}
	
	public void toggleCamera() {
		if (m_oCameraListener != null) {
			m_oCameraListener.toggleCamera();
		}
	}
	
	public void switchCameraOn() {
		if (m_oCameraListener != null) {
			m_oCameraListener.switchCameraOn();
		}
	}

	public void switchCameraOff() {
		if (m_oCameraListener != null) {
			m_oCameraListener.switchCameraOff();
		}
	}
	
	public void cameraUp() {
		if (m_oCameraListener != null) {
			m_oCameraListener.cameraUp();
		}
	}

	public void cameraDown() {
		if (m_oCameraListener != null) {
			m_oCameraListener.cameraDown();
		}
	}

	public void cameraStop() {
		if (m_oCameraListener != null) {
			m_oCameraListener.cameraStop();
		}
	}
	
	public void doMove(Move i_eMove, double i_dblSpeed, double i_dblRadius) {
		if (m_oRemoteControlListener != null) {
			m_oRemoteControlListener.onMove(i_eMove, i_dblSpeed, i_dblRadius);
		}
	}
	
	public void toggleInvertDrive() {
		if (m_oRemoteControlListener != null) {
			m_oRemoteControlListener.toggleInvertDrive();
		}
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
					doMove(oDriveCmd.eMove, oDriveCmd.dblSpeed, oDriveCmd.dblRadius);
					
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
						toggleInvertDrive();
						toggleCamera();
						break;
					case UP:
						cameraUp();
						break;
					case DOWN:
						cameraDown();
						break;
					case STOP:
						cameraStop();
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
