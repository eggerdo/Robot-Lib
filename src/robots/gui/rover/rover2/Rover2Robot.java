package robots.gui.rover.rover2;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotInventory;
import robots.RobotRemoteListener;
import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.ctrl.RemoteControlHelper;
import robots.ctrl.rover.RoverBaseTypes.VideoResolution;
import robots.ctrl.rover.rover2.Rover2;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.gui.rover.RoverBaseRobot;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class Rover2Robot extends RoverBaseRobot {

	private static final int DIALOG_SETTINGS_ID = 1;

	private static final int ACCEL_ID = CONNECT_ID + 1;
	private static final int ADVANCED_CONTROL_ID = ACCEL_ID + 1;
	private static final int VIDEO_ID = ADVANCED_CONTROL_ID + 1;
	private static final int VIDEO_SETTINGS_ID = VIDEO_ID + 1;

	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	private static final int SENSOR_GRP = REMOTE_CTRL_GRP + 1;
	private static final int VIDEO_GRP = SENSOR_GRP + 1	;
	
	private AlertDialog m_dlgSettingsDialog;

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
	protected void onConnectError() {
		// TODO Auto-generated method stub

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
    
}
