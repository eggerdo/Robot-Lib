package robots.ctrl.control;


import org.dobots.comm.Move;
import org.dobots.utilities.log.Loggable;

import robots.ctrl.IRobotDevice;
import android.util.Log;

public class RobotDriveCommandListener extends Loggable implements IDriveControlListener {
	
	private static final String TAG = "RobotRemote";

	private IRobotDevice m_oRobot;
	
	public RobotDriveCommandListener(IRobotDevice i_oRobot) {
		m_oRobot = i_oRobot;
	}
	
	protected double checkSpeed(double i_dblSpeed) {
		if (i_dblSpeed == -1) {
			return m_oRobot.getBaseSpeed();
		} else {
			return i_dblSpeed;
		}
	}
	
	// called by RemoteControlHelper when the joystick is used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {

		i_dblSpeed = checkSpeed(i_dblSpeed);
		
		// execute this move
		switch(i_oMove) {
		case NONE:
			m_oRobot.moveStop();
			debug(TAG, "stop()");
			break;
		case BACKWARD:
			m_oRobot.moveBackward(i_dblSpeed, i_dblAngle);
			debug(TAG, "bwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle);
			break;
		case STRAIGHT_BACKWARD:
			m_oRobot.moveBackward(i_dblSpeed);
			debug(TAG, "bwd(s=%f)", i_dblSpeed);
			break;
		case FORWARD:
			m_oRobot.moveForward(i_dblSpeed, i_dblAngle);
			debug(TAG, "fwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle);
			break;
		case STRAIGHT_FORWARD:
			m_oRobot.moveForward(i_dblSpeed);
			debug(TAG, "fwd(s=%f)", i_dblSpeed);
			break;
		case ROTATE_LEFT:
			m_oRobot.rotateCounterClockwise(i_dblSpeed);
			debug(TAG, "c cw(s=%f)", i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			m_oRobot.rotateClockwise(i_dblSpeed);
			debug(TAG, "cw(s=%f)", i_dblSpeed);
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
			debug(TAG, "stop()");
			break;
		case STRAIGHT_BACKWARD:
		case BACKWARD:
			m_oRobot.moveBackward();
			debug(TAG, "bwd()");
			break;
		case STRAIGHT_FORWARD:
		case FORWARD:
			m_oRobot.moveForward();
			debug(TAG, "fwd()");
			break;
		case ROTATE_LEFT:
			m_oRobot.rotateCounterClockwise();
			debug(TAG, "c cw()");
			break;
		case ROTATE_RIGHT:
			m_oRobot.rotateClockwise();
			debug(TAG, "cw()");
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