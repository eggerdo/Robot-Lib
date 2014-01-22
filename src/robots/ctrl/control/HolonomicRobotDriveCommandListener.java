package robots.ctrl.control;

import org.dobots.comm.Move;

import robots.ctrl.IHolonomicRobotDevice;

public class HolonomicRobotDriveCommandListener extends
		RobotDriveCommandListener {
	
	private static final String TAG = "HolonomicRobotDriveCommandListener";

	IHolonomicRobotDevice mHolonomicRobot;
	
	public HolonomicRobotDriveCommandListener(IHolonomicRobotDevice i_oRobot) {
		super(i_oRobot);
		mHolonomicRobot = i_oRobot;
	}

	// called by RemoteControlHelper when the joystick is used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {

		i_dblSpeed = checkSpeed(i_dblSpeed);
		
		// execute this move
		switch(i_oMove) {
		case LEFT:
			mHolonomicRobot.moveLeft(i_dblSpeed);
			debug(TAG, "left(s=%f)", i_dblSpeed);
			break;
		case RIGHT:
			mHolonomicRobot.moveRight(i_dblSpeed);
			debug(TAG, "right(s=%f)", i_dblSpeed);
		default:
			super.onMove(i_oMove, i_dblSpeed, i_dblAngle);
		}
	}

	// called by RemoteControlHelper when the buttons are used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove) {

		// execute this move
		switch(i_oMove) {
		case LEFT:
			mHolonomicRobot.moveLeft();
			debug(TAG, "left()");
			break;
		case RIGHT:
			mHolonomicRobot.moveRight();
			debug(TAG, "right()");
		default:
			super.onMove(i_oMove);
		}
	}
}
