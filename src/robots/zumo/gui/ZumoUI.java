package robots.zumo.gui;

import java.io.IOException;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;

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
import robots.zumo.ctrl.ZumoTypes;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.hardware.Camera;
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
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class ZumoUI extends BluetoothRobot implements ICameraControlListener {

	private static String TAG = "Zumo";
	
	enum Programs {
		progMazeSolver,
		progLineFollower,
		progSumo,
		progPlanner
	}
	
	enum Tabs {
		tabControl,
		tabCamera,
		tabPrograms
	}
	
	private static final int CONNECT_ID = Menu.FIRST;
//	private static final int ACCEL_ID = CONNECT_ID + 1;
//	private static final int REMOTE_CONTROL_ID = ACCEL_ID + 1;
//	private static final int CAMERA_ID = ACCEL_ID + 1;
//	private static final int CAMERA_DISP_ID = CAMERA_ID + 1;
//	private static final int RESET_STATS = CAMERA_DISP_ID + 1;

//	private static final int CONTROL_GRP = GENERAL_GRP + 1;
//	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
//	private static final int CAMERA_CTRL_GRP = CONTROL_GRP + 1;
	
	private IZumo m_oZumo;

	private ZumoSensorGatherer m_oSensorGatherer;

	private ImageButton m_btnCameraToggle;

	private ZmqRemoteControlHelper m_oCameraCtrl;
	
	private boolean m_bCameraOn = false;

//	private Button m_btnShoot;

//	private Button m_btnVolley;
	
	private Handler mUIHandler = new Handler();

	private Button m_btnCalibrateCompass;
	private Button m_btnResetHeading;
	private EditText m_edtAngle;
	private Button m_btnTurnDegrees;
	
	private ToggleButton m_btnTabControl;
	private ToggleButton m_btnTabCamera;
	private ToggleButton m_btnTabPrograms;

	private RelativeLayout m_layTabControl;
	private RelativeLayout m_layTabCamera;
	private LinearLayout m_layTabPrograms;

	private Programs _currentProgram = Programs.progMazeSolver;
	private Tabs _currentTab = Tabs.tabControl;
	
	private LinearLayout m_layProgMazeSolver;
	private LinearLayout m_layProgLineFollower;
	private LinearLayout m_layProgSumo;
	private ToggleButton m_btnProgMazeSolver;
	private ToggleButton m_btnProgLineFollower;
	private ToggleButton m_btnProgPlanner;
	private ToggleButton m_btnProgSumo;

	private Button m_btnStartUpMazeSolver;
	private Button m_btnSolveMaze;
	private Button m_btnRepeatMaze;
	private Button m_btnStartUpLineFollower;
	private Button m_btnStartStopLineFollower;
	private Button m_btnStartStopSumo;

	private boolean m_bUseWhiteLines;
	
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
		m_bOwnsRobot = true;
		
		loadSettings();
		
//        updateButtons(false);
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
        m_oRemoteCtrl.setJoystickControl(true);

        if (m_oZumo.isConnected()) {
			onConnect();
		} else {
			if (m_strAddress == "") {
				connectToRobot();
			} else {
				setConnection(m_strAddress);
				connect();
			}
		}
    }
    
	@Override
	public void onDestroy() {
		saveSettings();
		
		m_oRemoteCtrl.destroy();
		
		if (m_oZmqRemoteSender != null) {
			m_oZmqRemoteSender.close();
		}
		
		if (m_bOwnsRobot) {
			RobotInventory.getInstance().removeRobot(m_strRobotID);
		}
		
		super.onDestroy();
	}
	
	private void hideAllTabs() {
        m_layTabControl.setVisibility(View.GONE);
        setRemoteControl(false);
		setCameraOn(false);
        m_layTabCamera.setVisibility(View.GONE);
        m_layTabPrograms.setVisibility(View.GONE);
		m_btnTabControl.setChecked(false);
		m_btnTabCamera.setChecked(false);
		m_btnTabPrograms.setChecked(false);
	}
	
	private void showTab(Tabs tab) {
		hideAllTabs();
		
		switch(tab) {
		case tabControl:
			m_layTabControl.setVisibility(View.VISIBLE);
			m_btnTabControl.setChecked(true);
			setRemoteControl(true);
			break;
		case tabCamera:
			m_layTabCamera.setVisibility(View.VISIBLE);
			m_btnTabCamera.setChecked(true);
			setCameraOn(true);
			break;
		case tabPrograms:
			m_layTabPrograms.setVisibility(View.VISIBLE);
			m_btnTabPrograms.setChecked(true);
			break;
		}
		_currentTab = tab;
	}

	private void hideAllPrograms() {
        m_layProgMazeSolver.setVisibility(View.GONE);
        m_layProgLineFollower.setVisibility(View.GONE);
        m_layProgSumo.setVisibility(View.GONE);
//        m_layProgPlanner.setVisibility(View.GONE);
        m_btnProgMazeSolver.setChecked(false);
        m_btnProgLineFollower.setChecked(false);
        m_btnProgSumo.setChecked(false);
        m_btnProgPlanner.setChecked(false);
	}

	private void showProgram(Programs program) {
		hideAllPrograms();
		
		switch (program) {
		case progMazeSolver:
			m_layProgMazeSolver.setVisibility(View.VISIBLE);
	        m_btnProgMazeSolver.setChecked(true);
			break;
		case progLineFollower:
			m_layProgLineFollower.setVisibility(View.VISIBLE);
	        m_btnProgLineFollower.setChecked(true);
			break;
		case progSumo:
			m_layProgSumo.setVisibility(View.VISIBLE);
	        m_btnProgSumo.setChecked(true);
			break;
		case progPlanner:
//			m_layProgPlanner.setVisibility(View.VISIBLE);
	        m_btnProgPlanner.setChecked(true);
			break;
		}
		_currentProgram = program;
	}
	
    @Override
	protected void setLayout(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.robot_zumo_main);
        
        //////////////////////
        // TABS
        //////////////////////
        
        m_layTabControl = (RelativeLayout) findViewById(R.id.layControl);
        m_layTabCamera = (RelativeLayout) findViewById(R.id.layCamera);
        m_layTabPrograms = (LinearLayout) findViewById(R.id.layPrograms);

        m_btnTabControl = (ToggleButton) findViewById(R.id.tabControl);
        m_btnTabControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTab(Tabs.tabControl);
			}
		});
        m_btnTabControl.setChecked(true);
        
        m_btnTabCamera = (ToggleButton) findViewById(R.id.tabCamera);
        m_btnTabCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTab(Tabs.tabCamera);
			}
		});
        
        m_btnTabPrograms = (ToggleButton) findViewById(R.id.tabPrograms);
        m_btnTabPrograms.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTab(Tabs.tabPrograms);
			}
		});

        //////////////////////
        // PROGRAMS
        //////////////////////
        
        m_layProgMazeSolver = (LinearLayout) findViewById(R.id.layMazeSolver);
        m_layProgLineFollower = (LinearLayout) findViewById(R.id.layLineFollower);
        m_layProgSumo = (LinearLayout) findViewById(R.id.laySumo);
//        m_layProgPlanner = (LinearLayout) findViewById(R.id.layPlanner);
        
        m_btnProgMazeSolver = (ToggleButton) findViewById(R.id.tabMazeSolver);
        m_btnProgMazeSolver.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showProgram(Programs.progMazeSolver);
			}
		});
        m_btnProgMazeSolver.setChecked(true);
        
        m_btnProgLineFollower = (ToggleButton) findViewById(R.id.tabLineFollower);
        m_btnProgLineFollower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showProgram(Programs.progLineFollower);
			}
		});
        
        m_btnProgSumo = (ToggleButton) findViewById(R.id.tabSumo);
        m_btnProgSumo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showProgram(Programs.progSumo);
			}
		});
        
        m_btnProgPlanner = (ToggleButton) findViewById(R.id.tabPlanner);
        m_btnProgPlanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showProgram(Programs.progPlanner);
			}
		});

        //////////////////////
        // OTHERS
        //////////////////////
        
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
		
		m_btnStartUpMazeSolver = (Button) findViewById(R.id.btnStartUpMazeSolver);
		m_btnStartUpMazeSolver.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oZumo.initMazeSolver();
				
				m_btnStartUpMazeSolver.setVisibility(View.GONE);
				m_btnSolveMaze.setVisibility(View.VISIBLE);
				m_btnSolveMaze.setEnabled(true);
				m_btnRepeatMaze.setEnabled(true);
			}
		});
		
		m_btnSolveMaze = (Button) findViewById(R.id.btnSolveMaze);
		m_btnSolveMaze.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!m_oZumo.isMazeSolving()) {
					m_oZumo.startMazeSolving();
					m_btnSolveMaze.setText("Stop");
				} else {
					m_oZumo.stopMazeSolving();
					m_btnSolveMaze.setText("Solve");
				}
			}
		});

		m_btnRepeatMaze = (Button) findViewById(R.id.btnRepeatMaze);
		m_btnRepeatMaze.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oZumo.repeatMaze();
			}
		});
		
		m_btnStartUpLineFollower = (Button) findViewById(R.id.btnStartUpLineFollower);
		m_btnStartUpLineFollower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oZumo.initLineFollower();
				
				m_btnStartUpLineFollower.setVisibility(View.GONE);
				m_btnStartStopLineFollower.setVisibility(View.VISIBLE);
				m_btnStartStopLineFollower.setEnabled(true);
			}
		});
		
		m_btnStartStopLineFollower = (Button) findViewById(R.id.btnStartStopLineFollower); 
		m_btnStartStopLineFollower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!m_oZumo.isLineFollowing()) {
					m_oZumo.startLineFollowing();
					m_btnStartStopLineFollower.setText("Stop");
				} else {
					m_oZumo.stopLineFollowing();
					m_btnStartStopLineFollower.setText("Start");
				}
			}
		});
		
		m_btnStartStopSumo = (Button) findViewById(R.id.btnStartStopSumo); 
		m_btnStartStopSumo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!m_oZumo.isSumoRunning()) {
					m_oZumo.startSumo();
					m_btnStartStopSumo.setText("Stop");
				} else {
					m_oZumo.stopSumo();
					m_btnStartStopSumo.setText("Start");
				}
			}
		});
		
		
		
//		m_btnCalibrateCompass = (Button) findViewById(R.id.btnCalibrateCompass);
//		m_btnCalibrateCompass.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				m_oZumo.calibrateCompass();
//			}
//		});
//
//		m_btnResetHeading = (Button) findViewById(R.id.btnResetHeading);
//		m_btnResetHeading.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				m_oZumo.resetHeading();
//			}
//		});
//
//		m_edtAngle = (EditText) findViewById(R.id.edtAngle);
//		
//		m_btnTurnDegrees = (Button) findViewById(R.id.btnTurnDegrees);
//		m_btnTurnDegrees.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				int angle = Integer.valueOf(m_edtAngle.getText().toString());
//				m_oZumo.turnDegrees(angle);
//			}
//		});
		
		
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

//		menu.add(REMOTE_CTRL_GRP, ACCEL_ID, ACCEL_ID, "Accelerometer");
//		menu.add(CONTROL_GRP, REMOTE_CONTROL_ID, REMOTE_CONTROL_ID, "Remote Control");
//		menu.add(CONTROL_GRP, CAMERA_ID, CAMERA_ID, "Camera");
//		menu.add(CAMERA_CTRL_GRP, CAMERA_DISP_ID, CAMERA_DISP_ID, "Display Camera");
//		menu.add(CONTROL_GRP, RESET_STATS, RESET_STATS, "Reset Stats");
		
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
//    	menu.setGroupVisible(CONTROL_GRP, m_oZumo.isConnected());
//    	menu.setGroupVisible(CAMERA_CTRL_GRP, m_oZumo.isConnected() && m_bCameraOn );

//    	Utils.updateOnOffMenuItem(menu.findItem(ACCEL_ID), m_bAccelerometer);
//    	Utils.updateOnOffMenuItem(menu.findItem(REMOTE_CONTROL_ID), m_oRemoteCtrl.isControlEnabled());
//    	Utils.updateOnOffMenuItem(menu.findItem(CAMERA_ID), m_bCameraOn);
//    	Utils.updateOnOffMenuItem(menu.findItem(CAMERA_DISP_ID), !m_oSensorGatherer.isPlaybackStopped());

    	return true;
    }
    
    private void setRemoteControl(boolean enabled) {
    	if (m_oRemoteCtrl.isControlEnabled() != enabled) {
    		m_oRemoteCtrl.setRemoteControl(enabled);
    	}
    	
//		Utils.showLayout(m_layControls, enabled);
//		Utils.showLayout(m_layGuns, enabled);
    }
    
    private void setCameraOn(boolean on) {
    	if (m_bCameraOn != on) {
			if (!on) {
				stopVideo();
				m_oSensorGatherer.stopVideoPlayback();
			} else {
				startVideo();
				m_oSensorGatherer.startVideoPlayback();
			}
			m_bCameraOn = on;
    	}
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
//		case ACCEL_ID:
//			m_bAccelerometer = !m_bAccelerometer;
//
//			if (m_bAccelerometer) {
//				m_bSetAccelerometerBase = true;
//			} else {
//				m_oZumo.moveStop();
//			}
//			break;
//		case REMOTE_CONTROL_ID:
//			setRemoteControl(!m_oRemoteCtrl.isControlEnabled());
//			break;
//		case CAMERA_ID:
//			startCamera();
//			break;
//		case CAMERA_DISP_ID:
//			if (m_oSensorGatherer.isPlaybackStopped()) {
//				m_oSensorGatherer.startVideoPlayback();
//			} else {
//				m_oSensorGatherer.stopVideoPlayback();
//			}
//			break;
//		case RESET_STATS:
//			m_oSensorGatherer.resetStats();
		}

		return super.onMenuItemSelected(featureId, item);
	}

	protected void resetLayout() {
        m_oRemoteCtrl.resetLayout();
		if (_currentTab == Tabs.tabControl) {
			setRemoteControl(true);
		}
//        setRemoteControl(true);
        
        updateButtons(false);

		m_oSensorGatherer.resetLayout();
		
		m_btnStartUpMazeSolver.setVisibility(View.VISIBLE);
		m_btnSolveMaze.setText("Solve");
		m_btnSolveMaze.setVisibility(View.GONE);

		m_btnStartUpLineFollower.setVisibility(View.VISIBLE);
		m_btnStartStopLineFollower.setText("Start");
		m_btnStartStopLineFollower.setVisibility(View.GONE);
		
		m_btnStartStopSumo.setText("Start");

	}
	
	public void updateButtons(boolean enabled) {
//		m_oRemoteCtrl.setControlEnabled(enabled);
		
		m_btnStartUpMazeSolver.setEnabled(enabled);
		m_btnSolveMaze.setEnabled(false);
		m_btnRepeatMaze.setEnabled(false);
		m_btnStartUpLineFollower.setEnabled(enabled);
		m_btnStartStopLineFollower.setEnabled(false);
		m_btnStartStopSumo.setEnabled(enabled);
	}

	@Override
	protected void onConnect() {
		resetLayout();
		updateButtons(true);
//		setRemoteControl(true);
	}
	
	@Override
	protected void onDisconnect() {
		resetLayout();
//		updateButtons(false);
//		setRemoteControl(false);
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

	private static final String PREFS_INVERTDRIVING = "invert_driving";
	private static final boolean DEF_INVERTDRIVING = false;
	private static final String PREFS_ADDRESS = "address";
	private static final String DEF_ADDRESS = "";
	private static final String PREFS_USEWHITELINES = "invert_driving";
	private static final boolean DEF_USEWHITELINES = false;
	
    private void saveSettings() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
//		editor.putBoolean(String.format("%s_%s", "zumo", PREFS_INVERTDRIVING), m_oZumo.isInverted());
		editor.putString(String.format("%s_%s", "zumo", PREFS_ADDRESS), m_strAddress);
		editor.putBoolean(String.format("%s_%s", "zumo", PREFS_USEWHITELINES), m_bUseWhiteLines);
		
		m_oRemoteCtrl.saveSettings(editor, "zumo");
		
		editor.commit();
    }
    
    private void loadSettings() {

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		
//		boolean invertDriving = prefs.getBoolean(String.format("%s_%s", "zumo", PREFS_INVERTDRIVING), DEF_INVERTDRIVING);
		m_strAddress = prefs.getString(String.format("%s_%s", "zumo", PREFS_ADDRESS), DEF_ADDRESS);
		m_bUseWhiteLines = prefs.getBoolean(String.format("%s_%s", "zumo", PREFS_USEWHITELINES), DEF_USEWHITELINES);
		
//		m_oZumo.setInverted(invertDriving);
		
		m_oRemoteCtrl.loadSettings(prefs, "zumo");
    }

}
