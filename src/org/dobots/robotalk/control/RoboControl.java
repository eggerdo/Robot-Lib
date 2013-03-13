package org.dobots.robotalk.control;

import org.dobots.robotalk.control.RemoteControlHelper.Move;
import org.dobots.robotalk.msg.RoboCommands;
import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommand;
import org.dobots.robotalk.msg.RoboCommands.CameraCommandType;
import org.dobots.robotalk.msg.RoboCommands.DriveCommand;

public class RoboControl implements IRemoteControlListener, ICommandReceiveListener {
	
	private static final String TAG = "RoboControl";
	
	// if this is sent as speed the controller will chose
	// a predefined base speed for the correspondig move
	private static final int BASE_SPEED = -1;
	
	private CommandHandler m_oCmdHandler;
	
	private IRemoteControlListener m_oRemoteListener;
	private ICameraControlListener m_oCameraListener;
	
	public RoboControl(CommandHandler i_oHandler) {
		m_oCmdHandler = i_oHandler;
		m_oCmdHandler.setReceiveListener(this);
	}

	public void setRemoteControlListener(IRemoteControlListener i_oRemoteListener) {
		m_oRemoteListener = i_oRemoteListener;
	}
	
	public void setCameraControlListener(ICameraControlListener i_oCameraListener) {
		m_oCameraListener = i_oCameraListener;
	}
	
	private void sendCommand(BaseCommand i_oCmd) {
		m_oCmdHandler.sendCommand(i_oCmd);
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
		DriveCommand oCmd = RoboCommands.createDriveCommand(i_eMove, i_dblSpeed, i_dblAngle);
		sendCommand(oCmd);
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// enabling the control will be handled by the robot
		// controller
	}

	public void toggleCamera() {
		CameraCommand oCmd = RoboCommands.createCameraCommand(CameraCommandType.cameraToggle);
		sendCommand(oCmd);
	}
	
	public void switchCameraOn() {
		CameraCommand oCmd = RoboCommands.createCameraCommand(CameraCommandType.cameraOn);
		sendCommand(oCmd);
	}

	public void switchCameraOff() {
		CameraCommand oCmd = RoboCommands.createCameraCommand(CameraCommandType.cameraOff);
		sendCommand(oCmd);
	}
	
	@Override
	public void onCommandReceived(BaseCommand i_oCmd) {
		if (i_oCmd instanceof DriveCommand) {
			DriveCommand oCmd = (DriveCommand)i_oCmd;
			if (m_oRemoteListener != null) {
				m_oRemoteListener.onMove(oCmd.eMove, oCmd.dblSpeed, oCmd.dblAngle);
			}
		} else if (i_oCmd instanceof CameraCommand) {
			CameraCommand oCmd = (CameraCommand)i_oCmd;
			if (m_oCameraListener != null) {
				switch(oCmd.eType) {
				case cameraOff:
					m_oCameraListener.switchCameraOff();
					break;
				case cameraOn:
					m_oCameraListener.switchCameraOn();
					break;
				case cameraToggle:
					m_oCameraListener.toggleCamera();
					break;
				}
			}
		}
	}

}
