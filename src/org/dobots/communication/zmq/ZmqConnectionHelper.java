package org.dobots.communication.zmq;

import org.dobots.communication.zmq.ZmqMessageHandler.ZmqMessageListener;
import org.dobots.communication.zmq.ZmqSettings.SettingsChangeListener;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

public class ZmqConnectionHelper {
	
	public enum UseCase { ROBOT, USER, FULL }
	
	private static final String TAG = "ZmqRobotHelper";

	private static final int SETTINGS_ID 		= 0;
	
	private static final int FAILURE_DIALOG_ID 	= 2;

	private ZmqHandler m_oZmqHandler;
	ZmqSettings m_oSettings;
	
	private ZmqMessageHandler m_oCmdHandler;
	private ZmqMessageHandler m_oVideoHandler;

	private ZmqActivity m_oActivity;

	private UseCase m_eType;
	
	public ZmqConnectionHelper(UseCase i_eType) {
		m_eType = i_eType;
	}

    public void setup(ZmqHandler i_oZmqHandler, ZmqActivity i_oActivity) {
    	m_oActivity = i_oActivity;
        
        m_oZmqHandler = i_oZmqHandler;
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
				if (!m_oSettings.checkSettings()) {
					m_oActivity.failed();
				}
			}
		});

        m_oVideoHandler = new ZmqMessageHandler();

        m_oCmdHandler = new ZmqMessageHandler();
        
		if (!m_oSettings.isValid()) {
			m_oZmqHandler.getSettings().showDialog(m_oActivity);
		} else {
			setupConnections(m_oSettings.isRemote());
			m_oActivity.ready();
		}
		
    }
    
    private boolean isUser() {
    	return (m_eType == UseCase.USER) || (m_eType == UseCase.FULL);
    }
    
    private boolean isRobot() {
    	return (m_eType == UseCase.ROBOT) || (m_eType == UseCase.FULL);
    }
    
    public void close() {
    	closeConnections();
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
		ZMQ.Socket oVideoReceiver = null;
		ZMQ.Socket oVideoSender = null;
		
		if (isUser()) {
			oVideoReceiver = m_oZmqHandler.createSocket(ZMQ.SUB);
	
			// set the output queue size down, we don't really want to have old video frames displayed
			// we only want the most recent ones
			oVideoReceiver.setHWM(20);
	
			if (i_bRemote) {
				oVideoReceiver.connect(m_oSettings.getVideoReceiveAddress());
			} else {
				try {
					oVideoReceiver.bind(m_oSettings.getVideoReceiveAddress());
				} catch (Exception e) {
					Utils.showToast("Video Port is already taken", Toast.LENGTH_LONG);
					return;
				}
			}

			oVideoReceiver.subscribe("".getBytes());
		}
		
		if (isRobot()) {
			oVideoSender = m_oZmqHandler.createSocket(ZMQ.PUB);
//			oVideoSender = m_oZmqHandler.createSocket(ZMQ.PUSH);

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
		}

		m_oVideoHandler.setupConnections(oVideoReceiver, oVideoSender);

		// link external with internal command handler. incoming commands in external are sent to
		// internal, incoming commands on internal are sent to external
		m_oVideoHandler.addIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oZmqHandler.getVideoHandler().sendZmsg(i_oMsg);
			}
		}); 
        m_oZmqHandler.getVideoHandler().addIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oVideoHandler.sendZmsg(i_oMsg);
			}
		});
        
	}

	private void setupCommandConnection(boolean i_bRemote) {
		ZMQ.Socket oCommandSender = null;
		ZMQ.Socket oCommandReceiver = null;
		
		if (isUser()) {
			oCommandSender = m_oZmqHandler.createSocket(ZMQ.PUSH);
	
			if (i_bRemote) {
				oCommandSender.connect(m_oSettings.getCommandSendAddress());
			} else {
				try {
					oCommandSender.bind(m_oSettings.getCommandSendAddress());
				} catch (Exception e) {
					Utils.showToast("Command Port is already taken", Toast.LENGTH_LONG);
					return;
				}
			}
		}
		
		if (isRobot()) {
			oCommandReceiver = m_oZmqHandler.createSocket(ZMQ.SUB);

			if (i_bRemote) {
				oCommandReceiver.connect(m_oSettings.getCommandReceiveAddress());
			} else {
				try {
					oCommandReceiver.bind(m_oSettings.getCommandReceiveAddress());
				} catch (Exception e) {
					Utils.showToast("Command Port is already taken", Toast.LENGTH_LONG);
					return;
				}
			}

			oCommandReceiver.subscribe("".getBytes());
		}

		m_oCmdHandler.setupConnections(oCommandReceiver, oCommandSender);
		
		// link external with internal command handler. incoming commands in external are sent to
		// internal, incoming commands on internal are sent to external
        m_oCmdHandler.addIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oZmqHandler.getCommandHandler().sendZmsg(i_oMsg);
			}
		});     
        m_oZmqHandler.getCommandHandler().addIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oCmdHandler.sendZmsg(i_oMsg);
			}
		});

	}
	
    public Dialog onCreateDialog(int id) {
    	switch(id) {
    	case FAILURE_DIALOG_ID:
    		return createFailureDialog();
    	}
    	return null;
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
