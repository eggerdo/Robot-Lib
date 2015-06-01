package robots.ctrl.control;


import org.dobots.comm.Move;
import org.dobots.utilities.log.Loggable;

import robots.ctrl.IRobotDevice;
import android.util.Log;

public class RobotDriveCommandListener extends Loggable implements IDriveControlListener {
	
	private static final String TAG = RobotDriveCommandListener.class.getSimpleName();

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
			debug(TAG, "stop()");
			m_oRobot.moveStop();
			break;
		case BACKWARD:
			if (i_dblAngle != 0) {
				debug(TAG, "bwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle);
				m_oRobot.moveBackward(i_dblSpeed, i_dblAngle);
				break;
			}
		case STRAIGHT_BACKWARD:
			debug(TAG, "bwd(s=%f)", i_dblSpeed);
			m_oRobot.moveBackward(i_dblSpeed);
			break;
		case FORWARD:
			if (i_dblAngle != 0) {
				debug(TAG, "fwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle);
				m_oRobot.moveForward(i_dblSpeed, i_dblAngle);
				break;
			}
		case STRAIGHT_FORWARD:
			debug(TAG, "fwd(s=%f)", i_dblSpeed);
			m_oRobot.moveForward(i_dblSpeed);
			break;
		case ROTATE_LEFT:
			debug(TAG, "c cw(s=%f)", i_dblSpeed);
			m_oRobot.rotateCounterClockwise(i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			debug(TAG, "cw(s=%f)", i_dblSpeed);
			m_oRobot.rotateClockwise(i_dblSpeed);
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
			debug(TAG, "stop()");
			m_oRobot.moveStop();
			break;
		case STRAIGHT_BACKWARD:
		case BACKWARD:
			debug(TAG, "bwd()");
			m_oRobot.moveBackward();
			break;
		case STRAIGHT_FORWARD:
		case FORWARD:
			debug(TAG, "fwd()");
			m_oRobot.moveForward();
			break;
		case ROTATE_LEFT:
			debug(TAG, "c cw()");
			m_oRobot.rotateCounterClockwise();
			break;
		case ROTATE_RIGHT:
			debug(TAG, "cw()");
			m_oRobot.rotateClockwise();
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