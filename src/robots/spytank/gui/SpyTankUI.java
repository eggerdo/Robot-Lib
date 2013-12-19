package robots.spytank.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.ZmqRemoteControlSender;

import robots.RobotType;
import robots.ctrl.control.RemoteControlHelper;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.spytank.ctrl.ISpyTank;
import robots.spytank.ctrl.SpyTankTypes;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;

public class SpyTankUI extends WifiRobot {
	
	protected static final int DIALOG_CONNECTION_SETTINGS_ID = 1;
	
	protected static final int CONNECTION_SETTINGS_ID = CONNECT_ID + 1;
	
	private SpyTankSensorGatherer m_oSensorGatherer;
	
	private ISpyTank m_oSpyTank;
	
	private int m_nCommandPort;
	private int m_nMediaPort;

	private AlertDialog m_dlgSettingsDialog;

	public SpyTankUI() {
		
	}
	
	public SpyTankUI(BaseActivity owner) {
		super(owner);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity);

    	updateButtons(false);
    	
    	checkConnectionSettings();
    }

    @Override
    public void onRobotCtrlReady() {

    	m_oSpyTank = (ISpyTank) getRobot();
    	m_oSpyTank.setHandler(m_oUiHandler);

    	m_oZmqRemoteSender = new ZmqRemoteControlSender(m_oSpyTank.getID());
    	m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);

        m_oSensorGatherer = new SpyTankSensorGatherer(this, m_oSpyTank);

    	m_oSpyTank.setConnection(m_strAddress, m_nCommandPort, m_nMediaPort);
    	
    	if (m_oSpyTank.isConnected()) {
    		updateButtons(true);
    	} else {
    		connectToRobot();
    	}
    }
    
	@Override
	protected void connect() {
		m_oSpyTank.connect();
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
		m_oSensorGatherer.onConnect();
	}

	@Override
	protected void onDisconnect() {
		updateButtons(false);
		m_oSensorGatherer.onDisconnect();
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void setLayout(RobotType i_eRobot) {
		m_oActivity.setContentView(R.layout.robot_spytank_main);

    	Button btnCameraUp = (Button) findViewById(R.id.btnCameraUp);
    	btnCameraUp.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oSpyTank.cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oSpyTank.cameraUp();
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
					m_oSpyTank.cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oSpyTank.cameraDown();
					break;
				}
				return true;
			}
		});
    	    	
	}

	@Override
	protected void disconnect() {
		m_oSpyTank.disconnect();
	}

	@Override
	protected void resetLayout() {
		m_oSensorGatherer.resetLayout();
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		m_oRemoteCtrl.setControlEnabled(i_bEnabled);
		
		Utils.setEnabledRecursive((ViewGroup)m_oActivity.findViewById(R.id.layCameraControl), i_bEnabled);
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

	protected void checkConnectionSettings() {
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		m_strAddress = prefs.getString(SpyTankTypes.PREFS_SPYTANK_ADDRESS, SpyTankTypes.ADDRESS);
		m_nCommandPort = prefs.getInt(SpyTankTypes.PREFS_SPYTANK_COMMANDPORT, SpyTankTypes.COMMAND_PORT);
		m_nMediaPort = prefs.getInt(SpyTankTypes.PREFS_SPYTANK_MEDIAPORT, SpyTankTypes.MEDIA_PORT);
	}

	protected void adjustConnectionSettings() {

    	EditText editText;
    	
    	editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtAddress);
		m_strAddress = editText.getText().toString();
		
		editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtPort);
		m_nCommandPort = Integer.valueOf(editText.getText().toString());

		editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtMediaPort);
		m_nMediaPort = Integer.valueOf(editText.getText().toString());

		m_oSpyTank.setConnection(m_strAddress, m_nCommandPort, m_nMediaPort);
		connect();
		
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SpyTankTypes.PREFS_SPYTANK_ADDRESS, m_strAddress);
		editor.putInt(SpyTankTypes.PREFS_SPYTANK_COMMANDPORT, m_nCommandPort);
		editor.putInt(SpyTankTypes.PREFS_SPYTANK_MEDIAPORT, m_nMediaPort);
		editor.commit();
		
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(GENERAL_GRP, CONNECTION_SETTINGS_ID, CONNECTION_SETTINGS_ID, "Connection Settings");

		return true;
	}
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECTION_SETTINGS_ID:
			showDialog(DIALOG_CONNECTION_SETTINGS_ID);
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
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
		
		TableRow trMediaPort = (TableRow) dialog.findViewById(R.id.trMediaPort);
		trMediaPort.setVisibility(View.VISIBLE);

		editText = (EditText) dialog.findViewById(R.id.txtMediaPort);
		editText.setText(Integer.toString(m_nMediaPort));
    }
}
