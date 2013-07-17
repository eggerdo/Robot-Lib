package robots.rover.ac13.gui;

import org.dobots.R; 
import org.dobots.utilities.BaseActivity;

import robots.RobotRemoteListener;
import robots.RobotType;
import robots.ctrl.RemoteControlHelper;
import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.ac13.ctrl.AC13RoverTypes;
import robots.rover.gui.RoverBaseRobot;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

public class AC13RoverRobot extends RoverBaseRobot {

	private static final String TAG = "AC13Rover";

	public AC13RoverRobot(BaseActivity i_oOwner) {
		super(i_oOwner);

		RobotRemoteListener oListener = new RobotRemoteListener(getRobot());
//		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, oListener);
//        m_oRemoteCtrl.setProperties();

        m_oRemoteCtrl.setRemoteControlListener(oListener);
	}
	
	public AC13RoverRobot() {
		super();
	}
	
	private AC13Rover getRover() {
		return (AC13Rover) getRobot();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		m_oSensorGatherer = new AC13RoverSensorGatherer(this, getRover());
    }

	@Override
	protected void setProperties(RobotType i_eRobot) {
    	m_oActivity.setContentView(R.layout.ac13rover_main);
    	super.setProperties(i_eRobot);
	}

	@Override
    protected void prepareConnectionSettingsDialog(Dialog dialog) {
		EditText editText;
		
		editText = (EditText) dialog.findViewById(R.id.txtAddress);
		editText.setText(m_strAddress);
		
		editText = (EditText) dialog.findViewById(R.id.txtPort);
		editText.setText(Integer.valueOf(m_nPort));
    }

	@Override
	protected void checkConnectionSettings() {
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		m_strAddress = prefs.getString(AC13RoverTypes.PREFS_AC13_ADDRESS, AC13RoverTypes.ADDRESS);
		m_nPort = prefs.getInt(AC13RoverTypes.PREFS_AC13_PORT, AC13RoverTypes.PORT);
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
		editor.putString(AC13RoverTypes.PREFS_AC13_ADDRESS, m_strAddress);
		editor.putInt(AC13RoverTypes.PREFS_AC13_PORT, m_nPort);
		editor.commit();
		
	}
}
