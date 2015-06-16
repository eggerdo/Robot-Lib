package robots.zumo.gui;

import java.io.IOException;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.control.ICameraControlListener;
import robots.ctrl.zmq.ZmqRemoteControlHelper;
import robots.ctrl.zmq.ZmqRemoteControlSender;
import robots.gui.BluetoothRobot;
import robots.gui.RobotInventory;
import robots.gui.SensorGatherer;
import robots.gui.comm.IConnectListener;
import robots.gui.comm.IRobotConnection;
import robots.gui.comm.bluetooth.BluetoothConnection;
import robots.zumo.ctrl.IZumo;
import robots.zumo.ctrl.ZumoEncoder;
import robots.zumo.ctrl.ZumoTypes;
import android.bluetooth.BluetoothDevice;
import android.graphics.LightingColorFilter;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class ZumoUI extends BluetoothRobot implements ICameraControlListener {

	private static String TAG = "Zumo";
	
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int ACCEL_ID = CONNECT_ID + 1;
	private static final int REMOTE_CONTROL_ID = ACCEL_ID + 1;
	private static final int CAMERA_ID = REMOTE_CONTROL_ID + 1;
	private static final int CAMERA_DISP_ID = CAMERA_ID + 1;
//	private static final int RESET_STATS = CAMERA_DISP_ID + 1;

	private static final int CONTROL_GRP = GENERAL_GRP + 1;
//	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	private static final int CAMERA_CTRL_GRP = CONTROL_GRP + 1;
	
	private IZumo m_oZumo;

	private ZumoSensorGatherer m_oSensorGatherer;

	private ImageButton m_btnCameraToggle;

	private ZmqRemoteControlHelper m_oCameraCtrl;
	
	private boolean m_bCameraOn = false;

//	private Button m_btnShoot;

//	private Button m_btnVolley;
	
	private Handler mUIHandler = new Handler();

	private Button m_btnStartUp;

	private Button m_btnSolve;

	private Button m_btnRepeat;

	private Button m_btnCalibrateCompass;

	private Button m_btnResetHeading;

	private EditText m_edtAngle;

	private Button m_btnTurnDegrees;

//	private ToggleButton m_btnDock;
//	private boolean m_bDocking;
	
//	private SoundPool mSoundPool;

//	private int mGunShotID;
//	private int mVolleyID;

//	private LinearLayout m_layGuns;

//	private LinearLayout m_layControls;
	
	public ZumoUI(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public ZumoUI() {
		super();
	}
	
	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	// remote control helper, handles ui buttons and sends commands over zmq
		m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity);
		
        updateButtons(false);
    }

    @Override
    public void onRobotCtrlReady() {

    	m_oZumo = (IZumo) getRobot();
    	m_oZumo.setHandler(m_oUiHandler);

    	m_oZmqRemoteSender = new ZmqRemoteControlSender(m_oZumo.getID());
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);
		m_oRemoteCtrl.setCameraControlListener(m_oZmqRemoteSender);

        m_oSensorGatherer = new ZumoSensorGatherer(this, m_oZumo);
        
        if (m_bCameraOn) {
        	startVideo();
        	m_oSensorGatherer.startVideoPlayback();
        } else {
        	m_oSensorGatherer.stopVideoPlayback();
        }
        
        setRemoteControl(true);

        if (m_oZumo.isConnected()) {
			onConnect();
		} else {
			connectToRobot();
		}
    }
    
	@Override
	public void onDestroy() {
		m_oRemoteCtrl.destroy();
		
		if (m_oZmqRemoteSender != null) {
			m_oZmqRemoteSender.close();
		}
		
		if (m_bOwnsRobot) {
			RobotInventory.getInstance().removeRobot(m_strRobotID);
		}
		
		super.onDestroy();
	}

    @Override
	protected void setLayout(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.robot_zumo_main);

		m_btnCameraToggle = (ImageButton) findViewById(R.id.btnCameraToggle);
		if (Camera.getNumberOfCameras() <= 1) {
			m_btnCameraToggle.setVisibility(View.GONE);
		} else {
			m_btnCameraToggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleCamera();
					m_oZumo.toggleInvertDrive();
				}
			});
		}
		
		m_btnStartUp = (Button) findViewById(R.id.btnStartUp);
		m_btnStartUp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oZumo.initMazeSolver();
				
				m_btnSolve.setEnabled(true);
				m_btnRepeat.setEnabled(true);
			}
		});
		
		m_btnSolve = (Button) findViewById(R.id.btnSolveMaze);
		m_btnSolve.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!m_oZumo.isMazeSolving()) {
					m_oZumo.startMazeSolving();
					m_btnSolve.setText("Stop");
				} else {
					m_oZumo.stopMazeSolving();
					m_btnSolve.setText("Solve");
				}
			}
		});

		m_btnRepeat = (Button) findViewById(R.id.btnRepeat);
		m_btnRepeat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oZumo.repeatMaze();
			}
		});
		
		m_btnCalibrateCompass = (Button) findViewById(R.id.btnCalibrateCompass);
		m_btnCalibrateCompass.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oZumo.calibrateCompass();
			}
		});

		m_btnResetHeading = (Button) findViewById(R.id.btnResetHeading);
		m_btnResetHeading.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oZumo.resetHeading();
			}
		});

		m_edtAngle = (EditText) findViewById(R.id.edtAngle);
		
		m_btnTurnDegrees = (Button) findViewById(R.id.btnTurnDegrees);
		m_btnTurnDegrees.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int angle = Integer.valueOf(m_edtAngle.getText().toString());
				m_oZumo.turnDegrees(angle);
			}
		});
		
		
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

//		menu.add(REMOTE_CTRL_GRP, ACCEL_ID, ACCEL_ID, "Accelerometer");
		menu.add(CONTROL_GRP, REMOTE_CONTROL_ID, REMOTE_CONTROL_ID, "Remote Control");
		menu.add(CONTROL_GRP, CAMERA_ID, CAMERA_ID, "Camera");
		menu.add(CAMERA_CTRL_GRP, CAMERA_DISP_ID, CAMERA_DISP_ID, "Display Camera");
//		menu.add(CONTROL_GRP, RESET_STATS, RESET_STATS, "Reset Stats");
		
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	menu.setGroupVisible(CONTROL_GRP, m_oZumo.isConnected());
    	menu.setGroupVisible(CAMERA_CTRL_GRP, m_oZumo.isConnected() && m_bCameraOn );

//    	Utils.updateOnOffMenuItem(menu.findItem(ACCEL_ID), m_bAccelerometer);
    	Utils.updateOnOffMenuItem(menu.findItem(REMOTE_CONTROL_ID), m_oRemoteCtrl.isControlEnabled());
    	Utils.updateOnOffMenuItem(menu.findItem(CAMERA_ID), m_bCameraOn);
//    	Utils.updateOnOffMenuItem(menu.findItem(CAMERA_DISP_ID), !m_oSensorGatherer.isPlaybackStopped());

    	return true;
    }
    
    private void setRemoteControl(boolean enabled) {
    	m_oRemoteCtrl.setRemoteControl(enabled);
    	
//		Utils.showLayout(m_layControls, enabled);
//		Utils.showLayout(m_layGuns, enabled);
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oZumo.moveStop();
			}
			break;
		case REMOTE_CONTROL_ID:
			setRemoteControl(!m_oRemoteCtrl.isControlEnabled());
			break;
		case CAMERA_ID:
			if (m_bCameraOn) {
				stopVideo();
				m_oSensorGatherer.stopVideoPlayback();
			} else {
				startVideo();
				m_oSensorGatherer.startVideoPlayback();
			}
			m_bCameraOn = !m_bCameraOn;
			break;
		case CAMERA_DISP_ID:
			if (m_oSensorGatherer.isPlaybackStopped()) {
				m_oSensorGatherer.startVideoPlayback();
			} else {
				m_oSensorGatherer.stopVideoPlayback();
			}
			break;
//		case RESET_STATS:
//			m_oSensorGatherer.resetStats();
		}

		return super.onMenuItemSelected(featureId, item);
	}

	protected void resetLayout() {
        m_oRemoteCtrl.resetLayout();
        setRemoteControl(true);
        
        updateButtons(false);

		m_oSensorGatherer.resetLayout();
		
		m_btnSolve.setText("Solve");
		m_btnSolve.setEnabled(false);
		m_btnRepeat.setEnabled(false);
	}
	
	public void updateButtons(boolean enabled) {
		m_oRemoteCtrl.setControlEnabled(enabled);
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
		setRemoteControl(true);
	}
	
	@Override
	protected void onDisconnect() {
		updateButtons(false);
		setRemoteControl(false);
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void disconnect() {
		m_oZumo.disconnect();
	}
	
	@Override
	public void setConnection(BluetoothDevice i_oDevice) {
		m_strAddress = i_oDevice.getAddress();
		showConnectingDialog();
		
		if (m_oZumo.getConnection() != null) {
			try {
				m_oZumo.getConnection().close();
			}
			catch (IOException e) { }
		}
		IRobotConnection connection = new BluetoothConnection(i_oDevice, ZumoTypes.ZUMO_UUID);
		connection.setReceiveHandler(m_oUiHandler);
		m_oZumo.setConnection(connection);
	}
	
	@Override
	public void connect() {
		m_oZumo.connect();
	}

	public static void connectToZumo(final BaseActivity m_oOwner, IZumo i_oZumo, BluetoothDevice i_oDevice, final IConnectListener i_oConnectListener) {
		ZumoUI m_oRobot = new ZumoUI(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oZumo.isConnected()) {
			i_oZumo.disconnect();
		}

		i_oZumo.setHandler(m_oRobot.getUIHandler());
		IRobotConnection connection = new BluetoothConnection(i_oDevice, ZumoTypes.ZUMO_UUID);
		connection.setReceiveHandler(m_oRobot.getUIHandler());
		i_oZumo.setConnection(connection);
		i_oZumo.connect();
	}

	@Override
	public void toggleCamera() {
		// toggle camera only works if it is executed by the UI thread
		// so we check if the calling thread is the main thread, otherwise
		// we call the function again inside the main thread.
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m_oRemoteCtrl.toggleCamera();
			}
		});
	}

	@Override
	public void startVideo() {
		m_oRemoteCtrl.startVideo();
	}

	@Override
	public void stopVideo() {
		m_oRemoteCtrl.stopVideo();
	}
	
//	public void startVideoPlayback() {
//		m_oSensorGatherer.startVideoPlayback();
//	}
//	
//	public void stopVideoPlayback() {
//		m_oSensorGatherer.stopVideoPlayback();
//	}

	@Override
	public void cameraUp() {
		// NOT APPLICABLE FOR THIS ROBOT
	}

	@Override
	public void cameraDown() {
		// NOT APPLICABLE FOR THIS ROBOT
	}

	@Override
	public void cameraStop() {
		// NOT APPLICABLE FOR THIS ROBOT
	}

}
