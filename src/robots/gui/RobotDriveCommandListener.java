package robots.gui;


import org.dobots.lib.comm.Move;

import robots.ctrl.IDriveControlListener;
import robots.ctrl.IRobotDevice;
import android.util.Log;

public class RobotDriveCommandListener implements IDriveControlListener {
	
	private static final String TAG = "RobotRemote";

	private IRobotDevice m_oRobot;
	
	public RobotDriveCommandListener(IRobotDevice i_oRobot) {
		m_oRobot = i_oRobot;
	}
	
	// called by RemoteControlHelper when the joystick is used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {

		if (i_dblSpeed == -1) {
			i_dblSpeed = m_oRobot.getBaseSpeed();
		}
		
		// execute this move
		switch(i_oMove) {
		case NONE:
			m_oRobot.moveStop();
			Log.i(TAG, "stop()");
			break;
		case BACKWARD:
			m_oRobot.moveBackward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("bwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle));
			break;
		case STRAIGHT_BACKWARD:
			m_oRobot.moveBackward(i_dblSpeed);
			Log.i(TAG, String.format("bwd(s=%f)", i_dblSpeed));
			break;
		case FORWARD:
			m_oRobot.moveForward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("fwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle));
			break;
		case STRAIGHT_FORWARD:
			m_oRobot.moveForward(i_dblSpeed);
			Log.i(TAG, String.format("fwd(s=%f)", i_dblSpeed));
			break;
		case ROTATE_LEFT:
			m_oRobot.rotateCounterClockwise(i_dblSpeed);
			Log.i(TAG, String.format("c cw(s=%f)", i_dblSpeed));
			break;
		case ROTATE_RIGHT:
			m_oRobot.rotateClockwise(i_dblSpeed);
			Log.i(TAG, String.format("cw(s=%f)", i_dblSpeed));
			break;
		}
	}

	// called by RemoteControlHelper when the buttons are used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove) {
		
		// execute this move
		switch(i_oMove) {
		case NONE:
			m_oRobot.moveStop();
			Log.i(TAG, "stop()");
			break;
		case STRAIGHT_BACKWARD:
		case BACKWARD:
			m_oRobot.moveBackward();
			Log.i(TAG, "bwd()");
			break;
		case STRAIGHT_FORWARD:
		case FORWARD:
			m_oRobot.moveForward();
			Log.i(TAG, "fwd()");
			break;
		case ROTATE_LEFT:
			m_oRobot.rotateCounterClockwise();
			Log.i(TAG, "c cw()");
			break;
		case ROTATE_RIGHT:
			m_oRobot.rotateClockwise();
			Log.i(TAG, "cw()");
			break;
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		m_oRobot.enableControl(i_bEnable);
	}
	
	public void toggleInvertDrive() {
		m_oRobot.toggleInvertDrive();
	}
	
}