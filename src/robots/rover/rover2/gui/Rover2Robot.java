package robots.rover.rover2.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.rover.base.gui.RoverBaseRobot;
import robots.rover.rover2.ctrl.Rover2;
import robots.rover.rover2.ctrl.Rover2Types;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class Rover2Robot extends RoverBaseRobot {

	private static final int DIALOG_SETTINGS_ID = 1;

	private static final int ACCEL_ID = CONNECT_ID + 1;
	private static final int VIDEO_ID = ACCEL_ID + 1;
	private static final int VIDEO_SETTINGS_ID = VIDEO_ID + 1;

	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	private static final int SENSOR_GRP = REMOTE_CTRL_GRP + 1;
	private static final int VIDEO_GRP = SENSOR_GRP + 1	;
	
	private static Rover2Robot instance;

	public Rover2Robot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public Rover2Robot() {
		super();
	}
	
	private Rover2 getRover() {
		return (Rover2) getRobot();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		instance = this;
		
        m_oSensorGatherer = new Rover2SensorGatherer(this, getRover());
    }

	@Override
	protected void setProperties(RobotType i_eRobot) {
    	m_oActivity.setContentView(R.layout.rover2_main);
    	super.setProperties(i_eRobot);

    	ToggleButton btnLight = (ToggleButton) findViewById(R.id.btnLight);
    	btnLight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getRover().toggleLight();
			}
		});
    	
    	Button btnCameraUp = (Button) findViewById(R.id.btnCameraUp);
    	btnCameraUp.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					getRover().cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					getRover().cameraUp();
					break;
				}
				return true;
			}
		});

    	Button btnCameraDown = (Button) findViewById(R.id.btnCameraDown);
    	btnCameraDown.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					getRover().cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					getRover().cameraDown();
					break;
				}
				return true;
			}
		});
    	    	
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		super.updateButtons(i_bEnabled);
		
		Utils.setEnabledRecursive((ViewGroup)m_oActivity.findViewById(R.id.layControl), i_bEnabled);
		Utils.setEnabledRecursive((ViewGroup)m_oActivity.findViewById(R.id.layCameraControl), i_bEnabled);
	}

	public static Rover2Robot getInstance() {
		// TODO Auto-generated method stub
		return instance;
	}

	@Override
	protected void checkConnectionSettings() {
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		m_strAddress = prefs.getString(Rover2Types.PREFS_ROVER2_ADDRESS, Rover2Types.ADDRESS);
		m_nPort = prefs.getInt(Rover2Types.PREFS_ROVER2_PORT, Rover2Types.PORT);
	}

	@Override
	protected void adjustConnectionSettings() {

    	EditText editText;
    	
    	editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtAddress);
		m_strAddress = editText.getText().toString();
		
		editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtPort);
		m_nPort = Integer.valueOf(editText.getText().toString());

		getRover().setConnection(m_strAddress, m_nPort);
		connect();
		
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Rover2Types.PREFS_ROVER2_ADDRESS, m_strAddress);
		editor.putInt(Rover2Types.PREFS_ROVER2_PORT, m_nPort);
		editor.commit();
		
	}
}
