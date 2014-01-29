package robots.parrot.gui;

import org.dobots.R;
import org.dobots.comm.Move;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.control.IDriveControlListener;
import robots.ctrl.control.HolonomicRemoteControlHelper;
import robots.ctrl.control.RemoteControlHelper;
import robots.ctrl.control.RobotDriveCommandListener;
import robots.ctrl.zmq.ZmqRemoteControlSender;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.gui.comm.IConnectListener;
import robots.parrot.ctrl.IParrot;
import robots.parrot.ctrl.ParrotTypes;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.ToggleButton;

import com.codeminders.ardrone.ARDrone.VideoChannel;

public abstract class ParrotUI extends WifiRobot implements IDriveControlListener {

	private static String TAG = "Parrot";

	private static final int DIALOG_CONNECTION_SETTINGS_ID = 1;
	
	private static final int CONNECTION_SETTINGS_ID = CONNECT_ID + 1;
	private static final int VIDEO_ID = CONNECTION_SETTINGS_ID + 1;
	private static final int VIDEO_SCALE_ID = VIDEO_ID + 1;
	
	private static final int SENSOR_GRP = GENERAL_GRP + 1;
	private static final int VIDEO_GRP = SENSOR_GRP + 1;

	private IParrot m_oParrot;

	private ParrotSensorGatherer m_oSensorGatherer;

	protected RemoteControlHelper m_oRemoteCtrl;
	
//	private Button m_btnCalibrate;
	
	private double m_dblSpeed;

	private Button m_btnLand;
	private Button m_btnTakeOff;
	private Button m_btnUp;
	private Button m_btnDown;
	private Button m_btnRotateLeft;
	private Button m_btnRotateRight;
	
	private Button m_btnCamera;
	private ToggleButton m_btnSensors;
	
//	private boolean m_bSensorsEnabled = false;

	private Button m_btnSetAltitude;

	private EditText m_edtAltitude;

//    private EditText edtKp, edtKd, edtKi;

//	private RobotDriveCommandListener m_oRobotRemoteListener;

	// command and media port are hardcoded in the javadrone-api library. adjust
	// library if it is necessary to have them configurable
	private int m_nCommandPort;
	private int m_nMediaPort;

	private AlertDialog m_dlgSettingsDialog;

//	private Button m_btnEmergency;

	public ParrotUI(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public ParrotUI() {
		super();
	}

	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		m_oRemoteCtrl = new HolonomicRemoteControlHelper(m_oActivity);
        m_oRemoteCtrl.setJoystickControlAvailable(false);

        checkConnectionSettings();
        
        updateButtons(false);
    }

    @Override
    public void onRobotCtrlReady() {

    	m_oParrot = (IParrot) getRobot();
    	m_oParrot.setHandler(m_oUiHandler);
    	m_oParrot.setConnection(m_strAddress);
    	
		m_dblSpeed = m_oParrot.getBaseSpeed();
    	
    	m_oSensorGatherer = createSensorGatherer(m_oActivity, m_oParrot);

//		m_oRobotRemoteListener = new RobotDriveCommandListener(m_oParrot);
		m_oZmqRemoteSender = new ZmqRemoteControlSender(m_oParrot.getID());
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);

		if (m_oParrot.isConnected()) {
			onConnect();
		} else {
	        connectToRobot();
		}
		
    }
    
    protected abstract ParrotSensorGatherer createSensorGatherer(BaseActivity i_oActivity, IParrot i_oParrot);

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(GENERAL_GRP, CONNECTION_SETTINGS_ID, CONNECTION_SETTINGS_ID, "Connection Settings");
		
		menu.add(SENSOR_GRP, VIDEO_ID, 2, "Video ON");

		menu.add(VIDEO_GRP, VIDEO_SCALE_ID, 3, "Scale Video ON");
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECTION_SETTINGS_ID:
			showDialog(DIALOG_CONNECTION_SETTINGS_ID);
			break;
		case VIDEO_ID:
			if (m_oParrot.isStreaming()) {
				m_oParrot.stopVideo();
			} else {
				m_oParrot.startVideo();
			}
			return true;
		case VIDEO_SCALE_ID:
			m_oSensorGatherer.setVideoScaled(!m_oSensorGatherer.isVideoScaled());
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	public void disconnect() {
		m_oParrot.disconnect();
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	menu.setGroupVisible(SENSOR_GRP, m_oParrot.isConnected());
    	menu.setGroupVisible(VIDEO_GRP, m_oParrot.isConnected() && m_oParrot.isARDrone1());

    	Utils.updateOnOffMenuItem(menu.findItem(VIDEO_ID), m_oParrot.isStreaming());
    	Utils.updateOnOffMenuItem(menu.findItem(VIDEO_SCALE_ID), m_oSensorGatherer.isVideoScaled());
    	
    	return true;
    }

	@Override
	public void connect() {
		m_oUiHandler.post(new Runnable() {
			@Override
			public void run() {
				m_oParrot.connect();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	};

	@Override
	protected void onConnect() {
		m_oSensorGatherer.onConnect();

		updateButtons(true);
	}
	
	@Override
	protected void onDisconnect() {
		m_oSensorGatherer.onDisconnect();
		
		updateButtons(false);
	}
	
	@Override
	public void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
	}

	@Override
	protected void setLayout(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.robot_parrot_main);

        m_edtAltitude = (EditText) findViewById(R.id.edtAltitude);
        
//        edtKp = (EditText) findViewById(R.id.edtKp);
//        edtKd = (EditText) findViewById(R.id.edtKd);
//        edtKi = (EditText) findViewById(R.id.edtKi);
        
        Button btnStopAltitudeCtrl = (Button) findViewById(R.id.btnStopAltitudeCtrl);
        btnStopAltitudeCtrl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.stopAltitudeControl();
			}
		});
        
        m_btnSetAltitude = (Button) findViewById(R.id.btnSetAltitude);
        m_btnSetAltitude.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				double altitude = (new Double(m_edtAltitude.getText().toString())).doubleValue();
				double altitude = Double.valueOf(m_edtAltitude.getText().toString());
//				m_oParrot.Kp = (new Double(edtKp.getText().toString())).doubleValue();
//				m_oParrot.Kd = (new Double(edtKd.getText().toString())).doubleValue();
//				m_oParrot.Ki = (new Double(edtKi.getText().toString())).doubleValue();
				m_oParrot.setAltitude(altitude);
			}
		});
   
        m_btnCamera = (Button) findViewById(R.id.btnCamera);
        m_btnCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.switchCamera();
		        updateCameraButton();
			}
		});
        updateCameraButton();
        
        m_btnSensors = (ToggleButton) findViewById(R.id.btnSensors);
        m_btnSensors.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				m_bSensorsEnabled = !m_bSensorsEnabled;
				m_oSensorGatherer.enableSensors(m_btnSensors.isChecked());
//				m_btnSensors.setText("Sensors: " + (m_bSensorsEnabled ? "ON" : "OFF"));
			}
		});

        m_btnLand = (Button) findViewById(R.id.btnLand);
        m_btnLand.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.land();
				Log.i(TAG, "land()");
			}
		});
        
        m_btnTakeOff = (Button) findViewById(R.id.btnTakeOff);
        m_btnTakeOff.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.takeOff();
				Log.i(TAG, "takeOff()");
			}
		});
        
        m_btnUp = (Button) findViewById(R.id.btnUp);
        m_btnUp.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					Log.i(TAG, "stop()");
					break;
				case MotionEvent.ACTION_DOWN:
					m_oParrot.increaseAltitude();
					Log.i(TAG, "lift()");
					break;
				}
				return true;
			}
		});
        
        m_btnDown = (Button) findViewById(R.id.btnDown);
        m_btnDown.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					Log.i(TAG, "stop()");
					break;
				case MotionEvent.ACTION_DOWN:
					m_oParrot.decreaseAltitude();
					Log.i(TAG, "lower()");
					break;
				}
				return true;
			}
		});
        
        m_btnRotateLeft = (Button) findViewById(R.id.btnRotateLeft);
        m_btnRotateLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					Log.i(TAG, "stop()");
					break;
				case MotionEvent.ACTION_DOWN:
					Log.i(TAG, "c cw()");
					m_oParrot.rotateCounterClockwise();
					break;
				}
				return true;
			}
		});

        m_btnRotateRight = (Button) findViewById(R.id.btnRotateRight);
        m_btnRotateRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					Log.i(TAG, "stop()");
					break;
				case MotionEvent.ACTION_DOWN:
					Log.i(TAG, "cw()");
					m_oParrot.rotateClockwise();
					break;
				}
				return true;
			}
		});
        
	}
	
	private void updateCameraButton() {
		if (m_oParrot != null && (m_oParrot.getVidoeChannel() == VideoChannel.VERTICAL_ONLY)) {
			m_btnCamera.setText("Camera: Bottom");
		} else {
			m_btnCamera.setText("Camera: Front");
		}
	}

	protected void resetLayout() {
        m_oRemoteCtrl.resetLayout();
        
        updateButtons(false);
	}

	public void updateButtons(boolean enabled) {
		m_oRemoteCtrl.setControlEnabled(enabled);
		
		m_btnCamera.setEnabled(enabled);
		m_btnSensors.setEnabled(enabled);
	}

	@Override
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMove(Move i_oMove) {

		// execute this move
		switch(i_oMove) {
		case NONE:
			m_oParrot.moveStop();
			Log.i(TAG, "stop()");
			break;
		case BACKWARD:
			m_oParrot.moveBackward();
			Log.i(TAG, "bwd()");
			break;
		case FORWARD:
			m_oParrot.moveForward();
			Log.i(TAG, "fwd()");
			break;
		case LEFT:
			m_oParrot.moveLeft();
			Log.i(TAG, "left()");
			break;
		case RIGHT:
			m_oParrot.moveRight();
			Log.i(TAG, "right()");
			break;
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		m_oRemoteCtrl.enableRobotControl(i_bEnable);
	}

	@Override
	public void toggleInvertDrive() {
		// not available
	}

    /**
     * This is called when a dialog is created for the first time.  The given
     * "id" is the same value that is passed to showDialog().
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DIALOG_CONNECTION_SETTINGS_ID:
    		return createConnectionSettingsDialog();
    	}
    	return null;
    }

    private Dialog createConnectionSettingsDialog() {
    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	View layout = inflater.inflate(R.layout.connection_settings, null);
    	builder.setTitle("Connection Settings");
    	builder.setView(layout);
    	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				adjustConnectionSettings();
			}
		});
    	m_dlgSettingsDialog = builder.create();
    	return m_dlgSettingsDialog;
    }
    
    /**
     * This is called each time a dialog is shown.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    	case DIALOG_CONNECTION_SETTINGS_ID:
    		prepareConnectionSettingsDialog(dialog);
    		break;
    	}
    }

    protected void prepareConnectionSettingsDialog(Dialog dialog) {
		EditText editText;
		
		editText = (EditText) dialog.findViewById(R.id.txtAddress);
		editText.setText(m_strAddress);
		
		editText = (EditText) dialog.findViewById(R.id.txtPort);
		editText.setText(Integer.toString(m_nCommandPort));
		editText.setEnabled(false);
		
		TableRow trMediaPort = (TableRow) dialog.findViewById(R.id.trMediaPort);
		trMediaPort.setVisibility(View.VISIBLE);

		editText = (EditText) dialog.findViewById(R.id.txtMediaPort);
		editText.setText(Integer.toString(m_nMediaPort));
		editText.setEnabled(false);
    }

	protected void checkConnectionSettings() {
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		m_strAddress = prefs.getString(ParrotTypes.PREFS_ADDRESS, ParrotTypes.PARROT_IP);
		m_nCommandPort = prefs.getInt(ParrotTypes.PREFS_COMMANDPORT, ParrotTypes.COMMAND_PORT);
		m_nMediaPort = prefs.getInt(ParrotTypes.PREFS_MEDIAPORT, ParrotTypes.MEDIA_PORT);
	}

	protected void adjustConnectionSettings() {

    	EditText editText;
    	
    	editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtAddress);
		m_strAddress = editText.getText().toString();
		
//		editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtPort);
//		m_nCommandPort = Integer.valueOf(editText.getText().toString());
//
//		editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtMediaPort);
//		m_nMediaPort = Integer.valueOf(editText.getText().toString());

//		m_oParrot.setConnection(m_strAddress, m_nCommandPort, m_nMediaPort);
		m_oParrot.setConnection(m_strAddress);
		
		connect();
		
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(ParrotTypes.PREFS_ADDRESS, m_strAddress);
		editor.putInt(ParrotTypes.PREFS_COMMANDPORT, m_nCommandPort);
		editor.putInt(ParrotTypes.PREFS_MEDIAPORT, m_nMediaPort);
		editor.commit();
		
	}

}
