package robots.rover.base.gui;

import org.dobots.R;
import org.dobots.communication.control.ZmqRemoteControlHelper;
import org.dobots.communication.control.ZmqRemoteControlSender;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.RemoteControlHelper;
import robots.gui.IConnectListener;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.rover.ac13.gui.AC13RoverRobot;
import robots.rover.base.ctrl.RoverBase;
import robots.rover.base.ctrl.RoverBaseTypes.VideoResolution;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public abstract class RoverBaseRobot extends WifiRobot {

	protected static final int DIALOG_VIDEO_SETTINGS_ID = 1;
	protected static final int DIALOG_CONNECTION_SETTINGS_ID = DIALOG_VIDEO_SETTINGS_ID + 1;
	
	protected static final int CONNECTION_SETTINGS_ID = CONNECT_ID + 1;
	protected static final int ACCEL_ID = CONNECTION_SETTINGS_ID + 1;
	protected static final int VIDEO_ID = ACCEL_ID + 1;
	protected static final int VIDEO_SETTINGS_ID = VIDEO_ID + 1;

	protected static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	protected static final int SENSOR_GRP = REMOTE_CTRL_GRP + 1;
	protected static final int VIDEO_GRP = SENSOR_GRP + 1	;
	
	private RoverBase m_oRover;
	protected RoverBaseSensorGatherer m_oSensorGatherer;
	protected ZmqRemoteControlHelper m_oRemoteCtrl;
	private ZmqRemoteControlSender m_oZmqRemoteListener;

	protected boolean connected;

	protected double m_dblSpeed;

	protected AlertDialog m_dlgSettingsDialog;

	private ToggleButton m_btnInfrared;

	protected int m_nPort;

	public RoverBaseRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}

	public RoverBaseRobot() {
		super();
	}

	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	m_oRover = (RoverBase) getRobot();
        m_oRover.setHandler(m_oUiHandler);
        
		m_dblSpeed = m_oRover.getBaseSped();

    	m_oZmqRemoteListener = new ZmqRemoteControlSender(getRobot().getID());
		m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity);
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteListener);
        
        updateButtons(false);

    	checkConnectionSettings();
    	m_oRover.setConnection(m_strAddress, m_nPort);

        if (m_oRover.isConnected()) {
			updateButtons(true);
		} else {
	        connectToRobot();
		}
        
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(GENERAL_GRP, CONNECTION_SETTINGS_ID, CONNECTION_SETTINGS_ID, "Connection Settings");

		menu.add(REMOTE_CTRL_GRP, ACCEL_ID, ACCEL_ID, "Accelerometer");
		
		menu.add(SENSOR_GRP, VIDEO_ID, VIDEO_ID, "Video");

		menu.add(VIDEO_GRP, VIDEO_SETTINGS_ID, VIDEO_SETTINGS_ID, "Video Settings");

		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	menu.setGroupVisible(REMOTE_CTRL_GRP, m_oRover.isConnected() && m_oRemoteCtrl.isControlEnabled());
    	menu.setGroupVisible(SENSOR_GRP, m_oRover.isConnected());
    	menu.setGroupVisible(VIDEO_GRP, m_oRover.isConnected() && m_oRover.isStreaming());
    	
    	Utils.updateOnOffMenuItem(menu.findItem(ACCEL_ID), m_bAccelerometer);
    	Utils.updateOnOffMenuItem(menu.findItem(VIDEO_ID), m_oRover.isStreaming());
    	
		return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECTION_SETTINGS_ID:
			showDialog(DIALOG_CONNECTION_SETTINGS_ID);
			break;
		case VIDEO_SETTINGS_ID:
    		showDialog(DIALOG_VIDEO_SETTINGS_ID);
    		break;
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oRover.moveStop();
			}
			break;
		case VIDEO_ID:
			m_oSensorGatherer.setVideoEnabled(!m_oRover.isStreaming());
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

    @Override
    public void onStop() {
    	// first stop streaming ...
    	if (m_oRover.isStreaming()) {
    		m_oRover.stopVideo();
    	}
    	
    	// ... then disconnect
    	super.onStop();
    }

	@Override
	protected void onConnect() {
		connected = true;
		updateButtons(true);
		m_oSensorGatherer.onConnect();
	}

	@Override
	protected void onDisconnect() {
		connected = false;
		updateButtons(false);
		m_oSensorGatherer.onDisconnect();
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void connect() {
		m_oRover.connect();
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {

    	m_btnInfrared = (ToggleButton) m_oActivity.findViewById(R.id.btnInfrared);
    	m_btnInfrared.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRover.toggleInfrared();
			}
		});
    	
	}

	@Override
	protected void disconnect() {
		m_oRover.disconnect();
	}

	@Override
	protected void resetLayout() {
		m_oSensorGatherer.resetLayout();
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		m_oRemoteCtrl.setControlEnabled(i_bEnabled);
		m_btnInfrared.setEnabled(i_bEnabled);
	}

    /**
     * This is called when a dialog is created for the first time.  The given
     * "id" is the same value that is passed to showDialog().
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DIALOG_VIDEO_SETTINGS_ID:
        	return createVideoSettingsDialog();
    	case DIALOG_CONNECTION_SETTINGS_ID:
    		return createConnectionSettingsDialog();
    	}
    	return null;
    }
    
    private Dialog createVideoSettingsDialog() {
    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	View layout = inflater.inflate(R.layout.rover_videosettings, null);
    	builder.setTitle("Video Resolution");
    	builder.setView(layout);
    	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				adjustVideoResolution();
			}
		});
    	m_dlgSettingsDialog = builder.create();
    	return m_dlgSettingsDialog;
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
    	case DIALOG_VIDEO_SETTINGS_ID:
    		prepareVideoSettingsDialog(dialog);
    		break;
    	case DIALOG_CONNECTION_SETTINGS_ID:
    		prepareConnectionSettingsDialog(dialog);
    		break;
    	}
    }
    
    private void prepareVideoSettingsDialog(Dialog dialog) {
    	switch (m_oRover.getResolution()) {
		case res_320x240:
			((RadioButton) dialog.findViewById(R.id.rb320x240)).setChecked(true);
			break;
		case res_640x480:
			((RadioButton) dialog.findViewById(R.id.rb640x480)).setChecked(true);
			break;
		default:
			((RadioGroup) dialog.findViewById(R.id.rgVideoResolution)).clearCheck();
			break;
		}
    }

    protected void prepareConnectionSettingsDialog(Dialog dialog) {
		EditText editText;
		
		editText = (EditText) dialog.findViewById(R.id.txtAddress);
		editText.setText(m_strAddress);
		
		editText = (EditText) dialog.findViewById(R.id.txtPort);
		editText.setText(Integer.toString(m_nPort));
    }

	protected abstract void checkConnectionSettings();

	protected abstract void adjustConnectionSettings();
	
    private void adjustVideoResolution() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// in order for the resolution change to take effect we need to disconnect
				// and reconnect to the robot.
		    	disconnect();
		    	// wait for a moment to give the threads time to shut down
		    	Utils.waitSomeTime(500);
		    	// because the settings are changed over http requests we don't need to have
		    	// the tcp sockets connected in order to change the settings!
		    	int nId = ((RadioGroup) m_dlgSettingsDialog.findViewById(R.id.rgVideoResolution)).getCheckedRadioButtonId();
		    	if (nId == R.id.rb320x240) {
		    		m_oSensorGatherer.setResolution(VideoResolution.res_320x240);
		    	} else if (nId == R.id.rb640x480) {
		    		m_oSensorGatherer.setResolution(VideoResolution.res_640x480);
		    	}
		    	// connect again to receive the new video stream
		    	connect();
			}
		}).start();
    }

	public static void connectToAC13Rover(final BaseActivity m_oOwner, RoverBase i_oRover, final IConnectListener i_oConnectListener) {
		AC13RoverRobot m_oRobot = new AC13RoverRobot(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oRover.isConnected()) {
			i_oRover.disconnect();
		}

		i_oRover.setHandler(m_oRobot.getUIHandler());
		i_oRover.connect();
	}

}
