package org.dobots.robotalk.control;

import org.dobots.robotalk.msg.RoboCommands;
import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommand;
import org.dobots.robotalk.msg.RoboCommands.DriveCommand;
import org.dobots.robotalk.msg.RobotMessage;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqReceiveThread;
import org.dobots.utilities.Utils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import robots.IRobotDevice;
import android.app.Activity;

public class ZmqRemoteControl extends RemoteControlHelper {

	private static final String TAG = "ZmqRemoteControl";
	
	private ZMQ.Socket m_oCmdReceiver;
	
	private CommandReceiveThread m_oReceiver;

	private ICameraControlListener m_oCameraListener;
	
	public ZmqRemoteControl(Activity i_oActivity, IRobotDevice i_oRobot,
			IRemoteControlListener i_oListener) {
		super(i_oActivity, i_oRobot, i_oListener);
		
		ZContext context = ZmqHandler.getInstance().getContext();
		
		String strCommandAddr = CommandHandler.getInstance().getIntCommandAddr();
		
		m_oCmdReceiver = context.createSocket(ZMQ.SUB);
//		m_oCmdReceiver.bind(String.format("ipc://%s/remotectrl", ZmqHandler.getInstance().getIPCpath()));
		m_oCmdReceiver.subscribe("".getBytes());
		m_oCmdReceiver.connect(strCommandAddr);
		
		m_oReceiver = new CommandReceiveThread(context.getContext(), m_oCmdReceiver, "ZmqRC:remote");
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
			ZMsg oZMsg = ZMsg.recvMsg(m_oCmdReceiver);
			if (oZMsg != null) {
				// create a chat message out of the zmq message
				RobotMessage oCmdMsg = RobotMessage.fromZMsg(oZMsg);
				
				String strJson = Utils.byteArrayToString(oCmdMsg.data);
				BaseCommand oCmd = RoboCommands.decodeCommand(strJson);
				
				if (oCmd instanceof DriveCommand) {
					
					DriveCommand oDriveCmd = (DriveCommand)oCmd;
					m_oRemoteControlListener.onMove(oDriveCmd.eMove, oDriveCmd.dblSpeed, oDriveCmd.dblAngle);
					
				} else if (oCmd instanceof CameraCommand) {
					CameraCommand oCameraCmd = (CameraCommand)oCmd;
					switch(oCameraCmd.eType) {
					case cameraOff:
						switchCameraOff();
						break;
					case cameraOn:
						switchCameraOn();
						break;
					case cameraToggle:
						toggleCamera();
						break;
					}
				}
			}
		}
		
	}


}
