package org.dobots.robotalk.zmq;

import org.dobots.robotalk.zmq.ZmqMessageHandler.ZmqMessageListener;
import org.dobots.robotalk.zmq.ZmqSettings.SettingsChangeListener;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

public class ZmqCoordinator {
	
	private static final String TAG = "Coordinator";

	private static final int SETTINGS_ID 		= 0;
	
	private static final int FAILURE_DIALOG_ID 	= 2;

	private ZmqHandler m_oZmqHandler;
	ZmqSettings m_oSettings;
	
	private ZmqMessageHandler m_oCmdHandler;
	private ZmqMessageHandler m_oVideoHandler;

	private boolean m_bRemote;
	
	private ZmqActivity m_oActivity;

    public void setup(ZmqActivity i_oActivity) {
    	m_oActivity = i_oActivity;
        
        m_oZmqHandler = new ZmqHandler(i_oActivity);
        m_oSettings = m_oZmqHandler.getSettings();

        m_oSettings.setSettingsChangeListener(new SettingsChangeListener() {
			
			@Override
			public void onChange() {
				closeConnections();
	        	setupConnections(m_oSettings.isRemote());
	        	m_oActivity.ready();
			}

			@Override
			public void onCancel() {
				m_oActivity.failed();
			}
		});

        m_oVideoHandler = new ZmqMessageHandler();

        m_oCmdHandler = new ZmqMessageHandler();
        
		if (!m_oSettings.isValid()) {
			m_oZmqHandler.getSettings().showDialog();
		} else {
			setupConnections(m_oSettings.isRemote());
			m_oActivity.ready();
		}
		
    }
    
	private void closeConnections() {
		m_oVideoHandler.closeConnections();
		m_oCmdHandler.closeConnections();
	}

	private void setupConnections(boolean i_bRemote) {
		setupVideoConnection(i_bRemote);
		setupCommandConnection(i_bRemote);
	}

	private void setupVideoConnection(boolean i_bRemote) {
		ZMQ.Socket oVideoSender = m_oZmqHandler.createSocket(ZMQ.PUB);

		// set the output queue size down, we don't really want to have old video frames displayed
		// we only want the most recent ones
		oVideoSender.setHWM(20);

		if (i_bRemote) {
			oVideoSender.connect(m_oSettings.getVideoSendAddress());
		} else {
			try {
				oVideoSender.bind(m_oSettings.getVideoSendAddress());
			} catch (Exception e) {
				Utils.showToast("Video Port is already taken", Toast.LENGTH_LONG);
				return;
			}
		}

		m_oVideoHandler.setupConnections(null, oVideoSender);

		// link external with internal command handler. incoming commands in external are sent to
		// internal, incoming commands on internal are sent to external
		m_oVideoHandler.setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oZmqHandler.getVideoHandler().sendZmsg(i_oMsg);
			}
		}); 
        m_oZmqHandler.getVideoHandler().setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oVideoHandler.sendZmsg(i_oMsg);
			}
		});
        
	}

	private void setupCommandConnection(boolean i_bRemote) {
		ZMQ.Socket oCommandReceiver = m_oZmqHandler.createSocket(ZMQ.SUB);

		if (i_bRemote) {
			oCommandReceiver.connect(m_oSettings.getCommandReceiveAddress());
		} else {
			try {
				oCommandReceiver.bind(m_oSettings.getCommandReceiveAddress());
			} catch (Exception e) {
				Utils.showToast("Video Port is already taken", Toast.LENGTH_LONG);
				return;
			}
		}

		oCommandReceiver.subscribe("".getBytes());
		
		m_oCmdHandler.setupConnections(oCommandReceiver, null);
		
		// link external with internal command handler. incoming commands in external are sent to
		// internal, incoming commands on internal are sent to external
        m_oCmdHandler.setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oZmqHandler.getCommandHandler().sendZmsg(i_oMsg);
			}
		});     
        m_oZmqHandler.getCommandHandler().setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oCmdHandler.sendZmsg(i_oMsg);
			}
		});

	}
	
    public Dialog onCreateDialog(int id) {
    	switch(id) {
    	case ZmqSettings.DIALOG_SETTINGS_ID:
    		return m_oZmqHandler.getSettings().onCreateDialog(id);
    	case FAILURE_DIALOG_ID:
    		return createFailureDialog();
    	}
    	return null;
    }
    
	public void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    	case ZmqSettings.DIALOG_SETTINGS_ID:
    		m_oZmqHandler.getSettings().onPrepareDialog(id, dialog);
    	}
	}
	
	private Dialog createFailureDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
    	builder.setTitle("ZMQ Setup failed")
	    	.setMessage("Check your settings or try again")
	        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	               setupConnections(m_oSettings.isRemote());
	            }
	        })
	        .setNeutralButton("Settings", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					m_oActivity.showDialog(SETTINGS_ID);
				}
			})
	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	m_oActivity.failed();
	            }
	        });

    	return builder.create();
	}

}
