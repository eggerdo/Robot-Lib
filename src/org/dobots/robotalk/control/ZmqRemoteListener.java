package org.dobots.robotalk.control;

import org.dobots.robotalk.control.RemoteControlHelper.Move;
import org.dobots.robotalk.msg.RoboCommands;
import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommandType;
import org.dobots.robotalk.msg.RoboCommands.DriveCommand;

public class ZmqRemoteListener implements IRemoteControlListener, ICameraControlListener {

	private static final int BASE_SPEED = -1;
	
	private CommandHandler m_oCmdHandler;

	public ZmqRemoteListener(CommandHandler i_oCommandHandler) {
		m_oCmdHandler = i_oCommandHandler;
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
		CameraCommand oCmd = RoboCommands.createCameraCommand("", CameraCommandType.cameraToggle);
		sendCommand(oCmd);
	}
	
	public void switchCameraOn() {
		CameraCommand oCmd = RoboCommands.createCameraCommand("", CameraCommandType.cameraOn);
		sendCommand(oCmd);
	}

	public void switchCameraOff() {
		CameraCommand oCmd = RoboCommands.createCameraCommand("", CameraCommandType.cameraOff);
		sendCommand(oCmd);
	}

	private void sendCommand(BaseCommand i_oCmd) {
		if (m_oCmdHandler != null) {
			m_oCmdHandler.sendCommand(i_oCmd);
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// TODO Auto-generated method stub
	}
}
