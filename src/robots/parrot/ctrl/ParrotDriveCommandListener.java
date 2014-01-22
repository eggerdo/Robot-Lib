package robots.parrot.ctrl;

import org.dobots.comm.Move;

import robots.ctrl.control.HolonomicRobotDriveCommandListener;

public class ParrotDriveCommandListener extends
		HolonomicRobotDriveCommandListener {

	private static final String TAG = ParrotDriveCommandListener.class.getName();
	
	IParrot mParrot;
	
	public ParrotDriveCommandListener(IParrot i_oRobot) {
		super(i_oRobot);
		mParrot = i_oRobot;
	}

	// called by RemoteControlHelper when the joystick is used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {

		i_dblSpeed = checkSpeed(i_dblSpeed);
		
		// execute this move
		switch(i_oMove) {
		case UP:
			mParrot.increaseAltitude(i_dblSpeed);
			debug(TAG, "up(s=%3f)", i_dblSpeed);
			break;
		case DOWN:
			mParrot.decreaseAltitude(i_dblSpeed);
			debug(TAG, "down()", i_dblSpeed);
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
		case UP:
			mParrot.increaseAltitude();
			info(TAG, "up()");
			break;
		case DOWN:
			mParrot.decreaseAltitude();
			info(TAG, "down()");
		default:
			super.onMove(i_oMove);
		}
	}

}
