package org.dobots.communication.zmq;

import org.dobots.R;
import org.dobots.utilities.IDialogListener;
import org.dobots.utilities.IMenuListener;
import org.dobots.utilities.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.Toast;

public class ZmqSettings implements IMenuListener, IDialogListener {

	private static final int MENU_ZMQ_SETTINGS	= 100;
	private static final int GRP_ZMQ = 100;
	
	public static final String PREFS_REMOTE			= "remote";
	public static final String PREFS_ADDRESS 		= "address";
	public static final String PREFS_COMMANDPORT 	= "command_port";
	public static final String PREFS_VIDEOPORT 		= "video_port";
	public static final String PREFS_EVENTPORT 		= "event_port";
//	public static final String PREFS_ROBOTNAME		= "robotname";
	
	public static final Boolean DEFAULT_REMOTE		= true;
	public static final String DEFAULT_ADDRESS 		= null;
	public static final String DEFAULT_COMMANDPORT 	= null;
	public static final String DEFAULT_VIDEOPORT 	= null;
//	public static final String DEFAULT_ROBOTNAME	= null;
	public static final String DEFAULT_EVENTPORT 	= null;
	
	public interface SettingsChangeListener {
		public void onChange();
		public void onCancel();
	}

	public static final int DIALOG_SETTINGS_ID = 1;

	private Activity m_oActivity;

	private Dialog m_dlgSettings;

//	private String m_strRobotName;
	private String m_strAddress;
	private String m_strCommandPort;
	private String m_strVideoPort;
	private String m_strEventPort;

	private boolean m_bRemote;

	private boolean m_bSettingsValid;

	private SettingsChangeListener m_oChangeListener;

	private String m_strCommandSendAddress;
	private String m_strCommandReceiveAddress;
	private String m_strVideoSendAddress;
	private String m_strVideoRecvAddress;

	public ZmqSettings(Activity i_oActivity) {
		m_oActivity = i_oActivity;
	}
	
	public void setSettingsChangeListener(SettingsChangeListener i_oListener) {
		m_oChangeListener = i_oListener;
	}
	
	public boolean isValid() {
		return m_bSettingsValid;
	}

	public void showDialog(Activity activity) {
		activity.showDialog(DIALOG_SETTINGS_ID);
	}
	
	public boolean isRemote() {
		return m_bRemote;
	}

	@Override
    public Dialog onCreateDialog(Activity activity, int id) {
    	LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	View layout;
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	switch (id) {
    	case DIALOG_SETTINGS_ID:
        	layout = inflater.inflate(R.layout.zmq_connection_settings, null);
        	builder.setTitle("Connection Settings");
        	builder.setView(layout);
        	builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface paramDialogInterface) {
					if (m_oChangeListener != null) {
						m_oChangeListener.onCancel();
					}
				}
			});
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

    private void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);

        if (view instanceof ViewGroup ) {
            ViewGroup group = (ViewGroup)view;

            for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }

    @Override
    public void onPrepareDialog(Activity activity, int id, final Dialog dialog) {
    	if (id == DIALOG_SETTINGS_ID) {
    		final TableLayout table = (TableLayout) dialog.findViewById(R.id.tblRemoteSettings);
    		
    		RadioButton rbLocal = (RadioButton) dialog.findViewById(R.id.rbLocal);
    		rbLocal.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View paramView) {
					m_bRemote = false;
//					enableDisableView(table, false);
				}
			});
    		
    		RadioButton rbRemote = (RadioButton) dialog.findViewById(R.id.rbRemote);
    		rbRemote.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View paramView) {
					m_bRemote = true;
//					enableDisableView(table, true);
				}
			});
    		
    		rbRemote.setChecked(m_bRemote);
    		
    		// Pre-fill the text fields with the saved login settings.
    		EditText editText;
//    		if (m_strRobotName != null) {
//    			editText = (EditText) dialog.findViewById(R.id.txtNickName);
//    			editText.setText(m_strRobotName);
//    		}
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
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		m_bRemote = prefs.getBoolean(PREFS_REMOTE, DEFAULT_REMOTE);
		
		m_strAddress = prefs.getString(PREFS_ADDRESS, DEFAULT_ADDRESS);
		m_strCommandPort = prefs.getString(PREFS_COMMANDPORT, DEFAULT_COMMANDPORT);
		m_strVideoPort = prefs.getString(PREFS_VIDEOPORT, DEFAULT_VIDEOPORT);
		m_strEventPort = prefs.getString(PREFS_EVENTPORT, DEFAULT_EVENTPORT);
//		m_strRobotName = prefs.getString(PREFS_ROBOTNAME, DEFAULT_ROBOTNAME);
		
		// settings only valid if all values assigned
		m_bSettingsValid = ((!m_bRemote || ((m_strAddress != "") && (m_strAddress != null))) &&
							((m_strCommandPort != "") && (m_strCommandPort != null)) &&
							((m_strVideoPort != "") && (m_strVideoPort != null)) &&
//							((m_strRobotName != "") && (m_strRobotName != null)) &&
							((m_strEventPort != "") && (m_strEventPort != null)));
		return m_bSettingsValid;
    }

    public void adjustConnection() {
    	// Read the login settings from the text fields.
    	RadioButton rbRemote = (RadioButton) m_dlgSettings.findViewById(R.id.rbRemote);
    	Boolean bRemote = rbRemote.isChecked();
    	
    	EditText editText = (EditText) m_dlgSettings.findViewById(R.id.txtAddress);
		String strAddress = editText.getText().toString();
    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtCommandPort);
		String strCommandPort = editText.getText().toString();
    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtVideoPort);
		String strVideoPort = editText.getText().toString();
    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtEventPort);
		String strEventPort = editText.getText().toString();
//    	editText = (EditText) m_dlgSettings.findViewById(R.id.txtNickName);
//		String strNickName = editText.getText().toString();
		
		// Save the current login settings
		SharedPreferences prefs = m_oActivity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(PREFS_REMOTE, bRemote);
		editor.putString(PREFS_ADDRESS, strAddress);
		editor.putString(PREFS_COMMANDPORT, strCommandPort);
		editor.putString(PREFS_VIDEOPORT, strVideoPort);
		editor.putString(PREFS_EVENTPORT, strEventPort);
//		editor.putString(PREFS_ROBOTNAME, strNickName);
		editor.commit();
		
		if (!checkSettings()) {
			Utils.showToast("Connection Settings not valid, please check again!", Toast.LENGTH_LONG);
		} else if (m_oChangeListener != null) {
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
	
//	public String getRobotName() {
//		return m_strRobotName;
//	}
	

	public String getVideoReceiveAddress() {
		
//		if (m_strVideoRecvAddress == null) {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nVideoRecvPort;
			if (isRemote()) {
				nVideoRecvPort = getVideoPort() + 1;
			} else {
				nVideoRecvPort = getVideoPort();
			}

			m_strVideoRecvAddress = assembleAddress(isRemote(), nVideoRecvPort);
//		}
		return m_strVideoRecvAddress;
	}

	public String getVideoSendAddress() {
		
//		if (m_strVideoSendAddress == null) {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nVideoSendPort;
			if (isRemote()) {
				nVideoSendPort = getVideoPort();
			} else {
				nVideoSendPort = getVideoPort() + 1;
			}

			m_strVideoSendAddress = assembleAddress(isRemote(), nVideoSendPort);
//		}
		return m_strVideoSendAddress;
	}
	
	public String getCommandReceiveAddress() {
		
//		if (m_strCommandReceiveAddress == null) {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nCommandRecvPort;
			if (isRemote()) {
				nCommandRecvPort = getCommandPort() + 1;
			} else {
				nCommandRecvPort = getCommandPort();
			}

			m_strCommandReceiveAddress = assembleAddress(isRemote(), nCommandRecvPort);
//		}
		return m_strCommandReceiveAddress;
	}

	public String getCommandSendAddress() {
		
//		if (m_strCommandSendAddress == null) {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nCommandSendPort;
			if (isRemote()) {
				nCommandSendPort = getCommandPort();
			} else {
				nCommandSendPort = getCommandPort() + 1;
			}
			
			m_strCommandSendAddress = assembleAddress(isRemote(), nCommandSendPort);
//		}
		return m_strCommandSendAddress;
	}
	
	public String assembleAddress(boolean i_bRemote, int i_nPort) {
//		if (i_bRemote) {
			return String.format("tcp://%s:%d", getAddress(), i_nPort);
//		} else {
//			return String.format("tcp://127.0.0.1:%d", i_nPort);
//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Activity activity, Menu menu) {
		menu.add(GRP_ZMQ, MENU_ZMQ_SETTINGS, MENU_ZMQ_SETTINGS, activity.getString(R.string.zmq_settings));
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(Activity activity, MenuItem item) {
		switch(item.getItemId()) {
		case MENU_ZMQ_SETTINGS:
			showDialog(activity);
			return true;
		}
		
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Activity activity, Menu menu) {
		// nothing to do
		return true;
	}

}
