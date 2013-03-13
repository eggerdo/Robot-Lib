package org.dobots.robotalk.zmq;

import org.dobots.robotalk.R;
import org.dobots.utilities.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ZmqSettings {
	
	public static final String PREFS_ADDRESS 		= "address";
	public static final String PREFS_COMMANDPORT 	= "command_port";
	public static final String PREFS_VIDEOPORT 		= "video_port";
	public static final String PREFS_EVENTPORT 		= "event_port";
	public static final String PREFS_ROBOTNAME		= "robotname";
	
	public static final String DEFAULT_ADDRESS 		= null;
	public static final String DEFAULT_COMMANDPORT 	= null;
	public static final String DEFAULT_VIDEOPORT 	= null;
	public static final String DEFAULT_ROBOTNAME	= null;
	public static final String DEFAULT_EVENTPORT 	= null;
	
	public interface SettingsChangeListener {
		public void onChange();
	}

	private static final int DIALOG_SETTINGS_ID = 1;

	private Activity context;

	private Dialog m_dlgSettings;

	private String m_strRobotName;
	private String m_strAddress;
	private String m_strCommandPort;
	private String m_strVideoPort;
	private String m_strEventPort;

	private boolean m_bSettingsValid;

	private SettingsChangeListener m_oChangeListener;

	public ZmqSettings(Activity i_oActivity) {
		context = i_oActivity;
	}
	
	public void setSettingsChangeListener(SettingsChangeListener i_oListener) {
		m_oChangeListener = i_oListener;
	}
	
	public boolean isValid() {
		return m_bSettingsValid;
	}

	public void showDialog() {
		context.showDialog(DIALOG_SETTINGS_ID);
	}

    public Dialog onCreateDialog(int id) {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	View layout;
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	switch (id) {
    	case DIALOG_SETTINGS_ID:
        	layout = inflater.inflate(R.layout.connection_settings, null);
        	builder.setTitle("Connection Settings");
        	builder.setView(layout);
        	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface arg0, int arg1) {
    				adjustConnection();
    			}
    		});
        	m_dlgSettings = builder.create();
        	return m_dlgSettings;
    	}
    	return null;
    }

    public void onPrepareDialog(int id, Dialog dialog) {
    	if (id == DIALOG_SETTINGS_ID) {
    		// Pre-fill the text fields with the saved login settings.
    		EditText editText;
    		if (m_strRobotName != null) {
    			editText = (EditText) dialog.findViewById(R.id.txtNickName);
    			editText.setText(m_strRobotName);
    		}
    		if (m_strAddress != null) {
    			editText = (EditText) dialog.findViewById(R.id.txtAddress);
    			editText.setText(m_strAddress);
    		}
    		if (m_strCommandPort != null) {
    			editText = (EditText) dialog.findViewById(R.id.txtCommandPort);
    			editText.setText(m_strCommandPort);
    		}
    		if (m_strVideoPort != null) {
    			editText = (EditText) dialog.findViewById(R.id.txtVideoPort);
    			editText.setText(m_strVideoPort);
    		}
    		if (m_strEventPort != null) {
    			editText = (EditText) dialog.findViewById(R.id.txtEventPort);
    			editText.setText(m_strEventPort);
    		}
    	}
    }
    
    public boolean checkSettings() {
		// Read the settings from the preferences file.
		SharedPreferences prefs = context.getPreferences(Activity.MODE_PRIVATE);
		m_strAddress = prefs.getString(PREFS_ADDRESS, DEFAULT_ADDRESS);
		m_strCommandPort = prefs.getString(PREFS_COMMANDPORT, DEFAULT_COMMANDPORT);
		m_strVideoPort = prefs.getString(PREFS_VIDEOPORT, DEFAULT_VIDEOPORT);
		m_strEventPort = prefs.getString(PREFS_EVENTPORT, DEFAULT_EVENTPORT);
		m_strRobotName = prefs.getString(PREFS_ROBOTNAME, DEFAULT_ROBOTNAME);
		
		// settings only valid if all values assigned
		m_bSettingsValid = (((m_strAddress != "") && (m_strAddress != null)) &&
							((m_strCommandPort != "") && (m_strCommandPort != null)) &&
							((m_strVideoPort != "") && (m_strVideoPort != null)) &&
							((m_strEventPort != "") && (m_strEventPort != null)) &&
							((m_strRobotName != "") && (m_strRobotName != null)));
		return m_bSettingsValid;
    }

    public void adjustConnection() {
    	// Read the login settings from the text fields.
    	EditText editText = (EditText) m_dlgSettings.findViewById(R.id.txtAddress);
		String strAddress = editText.getText().toString();
    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtCommandPort);
		String strCommandPort = editText.getText().toString();
    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtVideoPort);
		String strVideoPort = editText.getText().toString();
    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtEventPort);
		String strEventPort = editText.getText().toString();
    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtNickName);
		String strNickName = editText.getText().toString();
		
		// Save the current login settings
		SharedPreferences prefs = context.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREFS_ADDRESS, strAddress);
		editor.putString(PREFS_COMMANDPORT, strCommandPort);
		editor.putString(PREFS_VIDEOPORT, strVideoPort);
		editor.putString(PREFS_EVENTPORT, strEventPort);
		editor.putString(PREFS_ROBOTNAME, strNickName);
		editor.commit();
		
		if (!checkSettings()) {
			Utils.showToast("Connection Settings not valid, please check again!", Toast.LENGTH_LONG);
		} else {
			m_oChangeListener.onChange();
		}
    }

	public String getAddress() {
		return m_strAddress;
	}

	public int getCommandPort() {
		return Integer.valueOf(m_strCommandPort);
	}

	public int getVideoPort() {
		return Integer.valueOf(m_strVideoPort);
	}
	
	public int getEventPort() {
		return Integer.valueOf(m_strEventPort);
	}
	
	public String getRobotName() {
		return m_strRobotName;
	}
	
}
