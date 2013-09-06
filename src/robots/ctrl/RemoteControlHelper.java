/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief:	Helper class to handle remote control
* @file: ${file_name}
*
* @desc:	Provides Remote Control over the User Interface. Expects that the layout of the activity
* 			includes the remote_control.xml layout. It handles the button presses and forwards the
* 			controls to the remote control listener. also provides a joystick interface for
* 			advanced remote control
*
* Copyright (c) 2013 Dominik Egger <dominik@dobots.nl>
*
* @author:		Dominik Egger
* @date:		2.9.2013
* @project:		Robot-Lib
* @company:		Distributed Organisms B.V.
*/
package robots.ctrl;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.IMenuListener;
import org.dobots.utilities.LockableScrollView;
import org.dobots.utilities.Utils;
import org.dobots.utilities.joystick.IJoystickListener;
import org.dobots.utilities.joystick.Joystick;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class RemoteControlHelper implements IJoystickListener, IMenuListener {

	private static final String TAG = "RemoteControlHelper";
	
	private static final int MENU_JOYSTICK_CTRL = 110;
	private static final int GRP_REMOTE_CTRL = 110;

	final static int ROTATE_THRESHOLD = 40;
	final static int STRAIGHT_THRESHOLD = 2;
	final static int DIRECTION_THRESHOLD_1 = 10;
	final static int DIRECTION_THRESHOLD_2 = 30;
	
	public enum Move {
		NONE, STRAIGHT_FORWARD, FORWARD, STRAIGHT_BACKWARD, BACKWARD, ROTATE_LEFT, ROTATE_RIGHT, LEFT, RIGHT
	}
	
	// negative angle -> right radius
	// positive angle -> left radius
	// angle from -90 to +90
	// angle is used as a common control for rotation. the robot itself uses radius. but radius can
	//   differ from robot to robot
	
	protected IDriveControlListener m_oRemoteControlListener = null;
	
	private Move lastMove = Move.NONE;

	private long lastTime = SystemClock.uptimeMillis();
	private double updateFrequency = 5.0; // Hz
	private int threshold = 20;
	
	private BaseActivity m_oActivity;
	
	// flag if remote control
	private boolean m_bControl;
	
	// flag if remote control with joystick
	private boolean m_bJoystickControl = false;
	// flag if joystick can be used / chosen
	private boolean m_bJoystickControlAvailable = true;
	// flag if fwdleft, fwdright, etc. buttons are available
	private boolean m_bAdvancedControl = true;

	private ToggleButton m_btnControl;
	private ImageButton m_btnFwd;
	private ImageButton m_btnFwdLeft;
	private ImageButton m_btnFwdRight;
	private ImageButton m_btnBwd;
	private ImageButton m_btnBwdLeft;
	private ImageButton m_btnBwdRight;
	private ImageButton m_btnLeft;
	private ImageButton m_btnRight;
	private ImageButton m_btnStop;
	
	private LockableScrollView m_oScrollView;
	private LinearLayout m_oBasicControl;
	private LinearLayout m_oRemoteControl;
	
	private Joystick m_oJoystick;
	
	private int mAlpha = 99;

	private class RemoteControlTouchListener implements OnTouchListener {

		private Move mMove;
		private double mRadius = 0;
		
		public RemoteControlTouchListener(Move move) {
			mMove = move;
		}

		public RemoteControlTouchListener(Move move, double radius) {
			mMove = move;
			mRadius = radius;
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent e) {
			if (m_oRemoteControlListener != null) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					onMove(Move.NONE);
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					if (mRadius == 0) {
						onMove(mMove);
					} else {
						// -1 is used as a placeholder for the speed so that
						// the robot is using its default speed
						onMove(mMove, -1, mRadius);
					}
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;					
				case MotionEvent.ACTION_MOVE:
					break;
				}
			}
			return false;
		}
		
	}
	
//	protected RemoteControlHelper(IRemoteControlListener i_oListener) {
//
//		// by default, this class is handling the move commands triggered either by the remote control buttons
//		// or by the joystick. However it is possible to overwrite the listener so that move commands
//		// can be individually handled
//		m_oRemoteControlListener = i_oListener;
//	}
	
	// At least one of the parameters i_oRobot or i_oListener has to be assigned! the other can be null.
	// It is also possible to assign both
	public RemoteControlHelper(BaseActivity i_oActivity) {
//		this(i_oListener);
		
		m_oActivity = i_oActivity;
		setProperties();
	}
	
	public void destroy() {
		if (m_oActivity != null) {
			m_oActivity.addMenuListener(null);
		}
	}
	
	public void setDriveControlListener(IDriveControlListener i_oListener) {
		m_oRemoteControlListener = i_oListener;
	}
	
	public void removeDriveControlListener(IDriveControlListener i_oListener) {
		if (m_oRemoteControlListener == i_oListener) {
			m_oRemoteControlListener = null;
		}
	}
	
	public boolean hasControlButton() {
		return m_btnControl != null;
	}
	
	private void setProperties() {
		if (m_oActivity == null) 
			return;
		
		m_oActivity.addMenuListener(this);
		
		m_oScrollView = (LockableScrollView) m_oActivity.findViewById(R.id.scrollview);

		m_oRemoteControl = (LinearLayout) m_oActivity.findViewById(R.id.layRemoteControl);
		m_oBasicControl = (LinearLayout) m_oActivity.findViewById(R.id.layBasicControl);
		
		m_oJoystick = (Joystick) m_oActivity.findViewById(R.id.oJoystick);
		m_oJoystick.setUpdateListener(this);

		m_btnControl = (ToggleButton) m_oActivity.findViewById(R.id.btnRemoteControl);
		if (hasControlButton()) {
			m_btnControl.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					m_bControl = !m_bControl;
					enableControl(m_bControl);
					showRemoteControl(m_bControl);
				}
			});
		} else {
			m_bControl = true;
		}
	

		m_btnFwdLeft = (ImageButton) m_oActivity.findViewById(R.id.btnFwdLeft);
		if (m_btnFwdLeft != null) {
			m_btnFwdLeft.getBackground().setAlpha(mAlpha);
			m_btnFwdLeft.setOnTouchListener(new RemoteControlTouchListener(Move.FORWARD, 80));
		}
		
		m_btnFwd = (ImageButton) m_oActivity.findViewById(R.id.btnFwd);
		if (m_btnFwd != null) {
			m_btnFwd.getBackground().setAlpha(mAlpha);
			m_btnFwd.setOnTouchListener(new RemoteControlTouchListener(Move.STRAIGHT_FORWARD));
		}
		
		m_btnFwdRight = (ImageButton) m_oActivity.findViewById(R.id.btnFwdRight);
		if (m_btnFwdRight != null) {
			m_btnFwdRight.getBackground().setAlpha(mAlpha);
			m_btnFwdRight.setOnTouchListener(new RemoteControlTouchListener(Move.FORWARD, -80));
		}

		m_btnLeft = (ImageButton) m_oActivity.findViewById(R.id.btnLeft);
		if (m_btnLeft != null) {
			m_btnLeft.getBackground().setAlpha(mAlpha);
			m_btnLeft.setOnTouchListener(new RemoteControlTouchListener(Move.ROTATE_LEFT));
		}
		
		m_btnStop = (ImageButton) m_oActivity.findViewById(R.id.btnStop);
		if (m_btnStop != null) {
			m_btnStop.getBackground().setAlpha(mAlpha);
			m_btnStop.setOnTouchListener(new RemoteControlTouchListener(Move.NONE));
		}
		
		m_btnRight = (ImageButton) m_oActivity.findViewById(R.id.btnRight);
		if (m_btnRight != null) {
			m_btnRight.getBackground().setAlpha(mAlpha);
			m_btnRight.setOnTouchListener(new RemoteControlTouchListener(Move.ROTATE_RIGHT));
		}
		
		m_btnBwdLeft = (ImageButton) m_oActivity.findViewById(R.id.btnBwdLeft);
		if (m_btnBwdLeft != null) {
			m_btnBwdLeft.getBackground().setAlpha(mAlpha);
			m_btnBwdLeft.setOnTouchListener(new RemoteControlTouchListener(Move.BACKWARD, 80));
		}
		
		m_btnBwd = (ImageButton) m_oActivity.findViewById(R.id.btnBwd);
		if (m_btnBwd != null) {
			m_btnBwd.getBackground().setAlpha(mAlpha);
			m_btnBwd.setOnTouchListener(new RemoteControlTouchListener(Move.STRAIGHT_BACKWARD));
		}
		
		m_btnBwdRight = (ImageButton) m_oActivity.findViewById(R.id.btnBwdRight);
		if (m_btnBwdRight != null) {
			m_btnBwdRight.getBackground().setAlpha(mAlpha);
			m_btnBwdRight.setOnTouchListener(new RemoteControlTouchListener(Move.BACKWARD, -80));
		}
		
		resetLayout();
	}
	
	public void showRemoteControl(boolean visible) {
		Utils.showLayout(m_oRemoteControl, visible);
	}
	
	private void updateControlType() {
		if (m_bJoystickControl) {
			m_oBasicControl.setVisibility(View.INVISIBLE);
			m_oJoystick.setVisibility(View.VISIBLE);
		} else {
			m_oBasicControl.setVisibility(View.VISIBLE);
			m_oJoystick.setVisibility(View.INVISIBLE);
			updateBasicControl(m_bAdvancedControl);
		}
		m_oRemoteControl.requestLayout();
	}
	
	private void updateBasicControl(boolean i_bAdvanced) {
		int visibility;
		
		if (i_bAdvanced) {
			visibility = View.VISIBLE;
		} else {
			visibility = View.INVISIBLE;
		}
		
		m_btnFwdLeft.setVisibility(visibility);
		m_btnFwdRight.setVisibility(visibility);
		m_btnBwdLeft.setVisibility(visibility);
		m_btnBwdRight.setVisibility(visibility);
	}
	
	public void resetLayout() {
		if (hasControlButton()) {
			m_bControl = false;
			m_btnControl.setChecked(m_bControl);
			showRemoteControl(m_bControl);
			setControlEnabled(m_bControl);
		} else {
			m_bControl = true;
			showRemoteControl(m_bControl);
		}
	}
	
	public void onConnect() {
		setControlEnabled(true);
	}

	public void setControlEnabled(boolean enabled) {
		if (hasControlButton()) {
			m_btnControl.setEnabled(enabled);
		}
	}
	
	@Override
	public void onJoystickTouch(boolean start) {
		if (m_oScrollView != null) {
			if (start) {
				m_oScrollView.setScrollingEnabled(false);
			} else {
				m_oScrollView.setScrollingEnabled(true);
			}
		} else {
//			Log.e(TAG, "scroll view not lockable!");
		}
	}

	@Override
	public void onUpdate(double i_dblPercentage, double i_dblAngle) {
		
		if (i_dblPercentage == 0 && i_dblAngle == 0) {
			// if percentage and angle is 0 this means the joystick was released
			// so we stop the robot
			onMove(Move.NONE, 0, 0);
			lastMove = Move.NONE;
		} else {

			// only allow a rate of updateFrequency to send drive commands
			// otherwise it will overload the robot's command queue
			if ((SystemClock.uptimeMillis() - lastTime) < 1/updateFrequency * 1000)
				return;

			lastTime = SystemClock.uptimeMillis();
			double dblAbsAngle = Math.abs(i_dblAngle);
			
			// determine which move should be executed based on the
			// last move and the angle of the joystick
			Move thisMove = Move.NONE;
			switch(lastMove) {
			case NONE:
				// for a low percentage (close to the center of the joystick) the
				// angle is too sensitive, so we only start once the percentage
				// is over the threshold
				if (i_dblPercentage < threshold) {
					return;
				}
			case ROTATE_LEFT:
			case ROTATE_RIGHT:
				// if the last move was left (or right respectively) we use a window
				// of +- 30 degrees, otherwise we switch to moving forward or backward
				// depending on the angle
				if (dblAbsAngle < ROTATE_THRESHOLD) {
					thisMove = Move.ROTATE_RIGHT;
				} else if ((180 - dblAbsAngle) < ROTATE_THRESHOLD) {
					thisMove = Move.ROTATE_LEFT;
				} else if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				}
				break;
			case STRAIGHT_BACKWARD:
			case BACKWARD:
				// if the last move was backward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move backward
				// and cap the degree to 0 or 180 respectively
				// if the angle is within 30 degree we rotate on the spot
				// otherwise we change direction
				if (Utils.inInterval(i_dblAngle, -90, STRAIGHT_THRESHOLD)) {
					thisMove = Move.STRAIGHT_BACKWARD;
				} else if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle < DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 0;
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle > 180 - DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 180;
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle < DIRECTION_THRESHOLD_2) {
					thisMove = Move.ROTATE_RIGHT;
				} else if (i_dblAngle > 180 - DIRECTION_THRESHOLD_2) {
					thisMove = Move.ROTATE_LEFT;
				} else {
					thisMove = Move.FORWARD;
				}
				break;
			case STRAIGHT_FORWARD:
			case FORWARD:
				// if the last move was forward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move forward
				// and cap the degree to 0 or 180 respectively
				// if the angle is within 30 degree we rotate on the spot
				// otherwise we change direction
				if (Utils.inInterval(i_dblAngle, 90, STRAIGHT_THRESHOLD)) {
					thisMove = Move.STRAIGHT_FORWARD;
				} else if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle > -DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 0;
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < -(180 - DIRECTION_THRESHOLD_1)) {
					dblAbsAngle = 180;
					thisMove = Move.FORWARD;
				} else if (i_dblAngle > -DIRECTION_THRESHOLD_2) {
					thisMove = Move.ROTATE_RIGHT;
				} else if (i_dblAngle < -(180 - DIRECTION_THRESHOLD_2)) {
					thisMove = Move.ROTATE_LEFT;
				} else {
					thisMove = Move.BACKWARD;
				}
				break;
			}

			// modify the angle so that it is between -90 and +90
			// instead of 0 and 180
			// where -90 is left and +90 is right
			dblAbsAngle -= 90.0;
			
			onMove(thisMove, i_dblPercentage, dblAbsAngle);
			lastMove = thisMove;
		}
	}

	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {
		m_oRemoteControlListener.onMove(i_oMove, i_dblSpeed, i_dblAngle);
	}

	public void onMove(Move i_oMove) {
		m_oRemoteControlListener.onMove(i_oMove);
	}

	public void enableControl(boolean i_bEnable) {
		m_oRemoteControlListener.enableControl(i_bEnable);
	}
	
	public boolean isControlEnabled() {
		return m_bControl;
	}
	
	public void setAdvancedControl(boolean i_bAdvancedControl) {
		m_bAdvancedControl = i_bAdvancedControl;
		updateControlType();
	}

	public void toggleJoystickControl() {
		setJoystickControl(!m_bJoystickControl);
	}
	
	public void setJoystickControl(boolean i_bJoystickControl) {
		m_bJoystickControl = i_bJoystickControl;
		updateControlType();
	}

	public boolean isJoystickControl() {
		return m_bJoystickControl;
	}
	
	public void setJoystickControlAvailable(boolean i_bAvailable) {
		m_bJoystickControlAvailable = i_bAvailable;
	}

	@Override
	public boolean onCreateOptionsMenu(Activity activity, Menu menu) {
		
		if (m_bJoystickControlAvailable) {
			menu.add(GRP_REMOTE_CTRL, MENU_JOYSTICK_CTRL, Menu.NONE, activity.getString(R.string.joystick));
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Activity activity, MenuItem item) {
		
		switch(item.getItemId()) {
			case MENU_JOYSTICK_CTRL:
				if (m_bJoystickControlAvailable) {
					toggleJoystickControl();
					return true;
				}
				break;
		}
		
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Activity activity, Menu menu) {

		if (m_bJoystickControlAvailable) {
			menu.setGroupVisible(GRP_REMOTE_CTRL, m_bControl);
			Utils.updateOnOffMenuItem(menu.findItem(MENU_JOYSTICK_CTRL), m_bJoystickControl);
		}	
		
		return true;
	}
		
}
