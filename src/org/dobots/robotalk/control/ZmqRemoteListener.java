package org.dobots.robotalk.control;

import org.dobots.robotalk.msg.RoboCommands;
import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommandType;
import org.dobots.robotalk.msg.RoboCommands.DriveCommand;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqUtils;
import org.zeromq.ZMQ;

import robots.ctrl.ICameraControlListener;
import robots.ctrl.IRemoteControlListener;
import robots.ctrl.RemoteControlHelper.Move;

public class ZmqRemoteListener implements IRemoteControlListener, ICameraControlListener {

	private static final int BASE_SPEED = -1;
	
	private ZMQ.Socket m_oCmdSendSocket;
	
	public ZmqRemoteListener() {
		m_oCmdSendSocket = ZmqHandler.getInstance().obtainCommandSendSocket();
	}

	@Override
	// callback function when the joystick is used
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {
		sendMove(i_oMove, i_dblSpeed, i_dblAngle);
	}
	
	@Override
	// callback function when the arrow keys are used
	public void onMove(Move i_oMove) {
		// execute this move
		sendMove(i_oMove, BASE_SPEED, 0.0);
	}

	private void sendMove(Move i_eMove, double i_dblSpeed, double i_dblAngle) {
		DriveCommand oCmd = RoboCommands.createDriveCommand("", i_eMove, i_dblSpeed, i_dblAngle);
		sendCommand(oCmd);
	}

	public void toggleCamera() {
		CameraCommand oCmd = RoboCommands.createCameraCommand("", CameraCommandType.TOGGLE);
		sendCommand(oCmd);
	}
	
	public void switchCameraOn() {
		CameraCommand oCmd = RoboCommands.createCameraCommand("", CameraCommandType.ON);
		sendCommand(oCmd);
	}

	public void switchCameraOff() {
		CameraCommand oCmd = RoboCommands.createCameraCommand("", CameraCommandType.OFF);
		sendCommand(oCmd);
	}

	private void sendCommand(BaseCommand i_oCmd) {
		if (m_oCmdSendSocket != null) {
			ZmqUtils.sendCommand(i_oCmd, m_oCmdSendSocket);
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// TODO Auto-generated method stub
	}
	
	public void toggleInvertDrive() {
		
	}

	public void close() {
		m_oCmdSendSocket.close();
	}
}
