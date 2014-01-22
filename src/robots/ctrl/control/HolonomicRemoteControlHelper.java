package robots.ctrl.control;

import org.dobots.comm.Move;
import org.dobots.utilities.BaseActivity;

public class HolonomicRemoteControlHelper extends RemoteControlHelper {

	public HolonomicRemoteControlHelper(BaseActivity i_oActivity) {
		super(i_oActivity);
	}
	
	@Override
	protected void setProperties() {
		// overwrite the keypad radius from 80 to 45 to have a diagonal movement
		mKeypadRadius = 45;
		
		super.setProperties();
	}
	
	// overwrite the touch listener for omniwheel remote control. no rotation, instead
	// we have a movement left and right.
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {
		switch (i_oMove) {
		case ROTATE_LEFT:
			i_oMove = Move.LEFT;
			break;
		case ROTATE_RIGHT:
			i_oMove = Move.RIGHT;
		}
		m_oRemoteControlListener.onMove(i_oMove, i_dblSpeed, i_dblAngle);
	}

	public void onMove(Move i_oMove) {
		switch (i_oMove) {
		case ROTATE_LEFT:
			i_oMove = Move.LEFT;
			break;
		case ROTATE_RIGHT:
			i_oMove = Move.RIGHT;
		}
		m_oRemoteControlListener.onMove(i_oMove);
	}

}
