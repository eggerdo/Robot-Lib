package robots.replicator.gui;

import org.dobots.R;
import org.dobots.communication.control.RemoteControlReceiver;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.IDriveControlListener;
import robots.ctrl.RemoteControlHelper;
import robots.gui.RobotDriveCommandListener;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.replicator.ctrl.Replicator;
import robots.replicator.ctrl.ReplicatorTypes;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;

public class ReplicatorUI extends WifiRobot {

	private static final String TAG = "ReplicatorUI";

	protected static final int DIALOG_CONNECTION_SETTINGS_ID = 1;
	
	protected static final int CONNECTION_SETTINGS_ID = CONNECT_ID + 1;
	protected static final int VIDEO_ID = CONNECTION_SETTINGS_ID + 1;
	
	protected static final int SENSOR_GRP = GENERAL_GRP + 1;
	
	private Replicator m_oReplicator;

	private ReplicatorSensorGatherer m_oSensorGatherer;

	private RemoteControlHelper m_oDriveCtrl;

	private Dialog m_dlgSettingsDialog;

	private int m_nCommandPort;
	private int m_nVideoPort;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.w(TAG, "Creating Replicator UI");

		m_oReplicator = (Replicator) getRobot();
		m_oReplicator.setHandler(m_oUiHandler);

		m_oSensorGatherer = new ReplicatorSensorGatherer(this, m_oReplicator);
		
		m_oDriveCtrl = new RemoteControlHelper(this);

		updateButtons(false);

    	checkConnectionSettings();
    	m_oReplicator.setConnection(m_strAddress, m_nCommandPort, m_nVideoPort);

		if (m_oReplicator.isConnected()) {
			updateButtons(true);
		} else {
			connectToRobot();
		}
	}
	
	
	@Override
	protected void connect() {
		m_oReplicator.connect();
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
		m_oDriveCtrl.onConnect();
		m_oSensorGatherer.onConnect();
	}

	@Override
	protected void onDisconnect() {
		updateButtons(false);
		m_oSensorGatherer.onDisconnect();
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
		m_oActivity.setContentView(R.layout.replicator_main);
	}

	@Override
	protected void disconnect() {
		m_oReplicator.disconnect();
	}

	@Override
	protected void resetLayout() {
		m_oSensorGatherer.resetLayout();
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(GENERAL_GRP, CONNECTION_SETTINGS_ID, CONNECTION_SETTINGS_ID, "Connection Settings");

		menu.add(SENSOR_GRP, VIDEO_ID, VIDEO_ID, "Video");

		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	menu.setGroupVisible(SENSOR_GRP, m_oReplicator.isConnected());
    	
    	Utils.updateOnOffMenuItem(menu.findItem(VIDEO_ID), m_oReplicator.isStreaming());
    	
		return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECTION_SETTINGS_ID:
			showDialog(DIALOG_CONNECTION_SETTINGS_ID);
			break;
		case VIDEO_ID:
			m_oSensorGatherer.setVideoEnabled(!m_oReplicator.isStreaming());
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
    	return super.onCreateDialog(id);
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
    		return;
    	}
    	
    	super.onPrepareDialog(id, dialog);
    }

    private void prepareConnectionSettingsDialog(Dialog dialog) {
		EditText editText;
		
		editText = (EditText) dialog.findViewById(R.id.txtAddress);
		editText.setText(m_strAddress);
		
		editText = (EditText) dialog.findViewById(R.id.txtPort);
		editText.setText(Integer.toString(m_nCommandPort));
		
		TableRow trMediaPort = (TableRow) dialog.findViewById(R.id.trMediaPort);
		trMediaPort.setVisibility(View.VISIBLE);

		editText = (EditText) dialog.findViewById(R.id.txtMediaPort);
		editText.setText(Integer.toString(m_nVideoPort));
    }
    
	private void checkConnectionSettings() {
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		m_strAddress = prefs.getString(ReplicatorTypes.PREFS_REPLICATOR_ADDRESS, ReplicatorTypes.ADDRESS);
		m_nCommandPort = prefs.getInt(ReplicatorTypes.PREFS_REPLICATOR_COMMANDPORT, ReplicatorTypes.COMMAND_PORT);
		m_nVideoPort = prefs.getInt(ReplicatorTypes.PREFS_REPLICATOR_VIDEOPORT, ReplicatorTypes.VIDEO_PORT);
	}

	private void adjustConnectionSettings() {

    	EditText editText;
    	
    	editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtAddress);
		m_strAddress = editText.getText().toString();
		
		editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtPort);
		m_nCommandPort = Integer.valueOf(editText.getText().toString());

		editText = (EditText) m_dlgSettingsDialog.findViewById(R.id.txtMediaPort);
		m_nVideoPort = Integer.valueOf(editText.getText().toString());

		m_oReplicator.setConnection(m_strAddress, m_nCommandPort, m_nVideoPort);
		connect();
		
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(ReplicatorTypes.PREFS_REPLICATOR_ADDRESS, m_strAddress);
		editor.putInt(ReplicatorTypes.PREFS_REPLICATOR_COMMANDPORT, m_nCommandPort);
		editor.putInt(ReplicatorTypes.PREFS_REPLICATOR_VIDEOPORT, m_nVideoPort);
		editor.commit();
		
	}


	@Override
	public void setCameraCtrlReceiver(
			RemoteControlReceiver m_oCameraCtrlReceiver) {
		// TODO Auto-generated method stub
	}


	@Override
	public void setDriveControlListener(IDriveControlListener i_oListener) {
		m_oDriveCtrl.setDriveControlListener(i_oListener);
	}

}
