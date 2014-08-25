package robots.piratedotty.gui;

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
import robots.piratedotty.ctrl.IPirateDotty;
import robots.piratedotty.ctrl.PirateDottyTypes;
import android.bluetooth.BluetoothDevice;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
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
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class PirateDottyUI extends BluetoothRobot implements ICameraControlListener {

	private static String TAG = "PirateDotty";
	
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int ACCEL_ID = CONNECT_ID + 1;
	private static final int REMOTE_CONTROL_ID = ACCEL_ID + 1;
	private static final int CAMERA_ID = REMOTE_CONTROL_ID + 1;
	private static final int CAMERA_DISP_ID = CAMERA_ID + 1;

	private static final int CONTROL_GRP = GENERAL_GRP + 1;
//	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	private static final int CAMERA_CTRL_GRP = CONTROL_GRP + 1;
	
	private IPirateDotty m_oPirateDotty;

	private PirateDottySensorGatherer m_oSensorGatherer;

	private ImageButton m_btnCameraToggle;

	private ZmqRemoteControlHelper m_oCameraCtrl;
	
	private boolean m_bCameraOn = true;

	private Button m_btnShoot;

	private Button m_btnVolley;
	
	private Handler mUIHandler = new Handler();

	private ToggleButton m_btnDock;
	private boolean m_bDocking;
	
	private SoundPool mSoundPool;

	private int mGunShotID;
	
	public PirateDottyUI(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public PirateDottyUI() {
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
        
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mGunShotID = mSoundPool.load(this, R.raw.cannon_blast, 1);
    }

    @Override
    public void onRobotCtrlReady() {

    	m_oPirateDotty = (IPirateDotty) getRobot();
    	m_oPirateDotty.setHandler(m_oUiHandler);

    	m_oZmqRemoteSender = new ZmqRemoteControlSender(m_oPirateDotty.getID());
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);
		m_oRemoteCtrl.setCameraControlListener(m_oZmqRemoteSender);

        m_oSensorGatherer = new PirateDottySensorGatherer(this, m_oPirateDotty);
        m_oSensorGatherer.stopVideoPlayback();
//        stopVideo();
        
        m_oRemoteCtrl.setRemoteControl(true);

//        if (m_oPirateDotty.isConnected()) {
			onConnect();
//		} else {
//			connectToRobot();
//		}
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
        m_oActivity.setContentView(R.layout.robot_piratedotty_main);

		m_btnCameraToggle = (ImageButton) findViewById(R.id.btnCameraToggle);
		if (Camera.getNumberOfCameras() <= 1) {
			m_btnCameraToggle.setVisibility(View.GONE);
		} else {
			m_btnCameraToggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleCamera();
					m_oPirateDotty.toggleInvertDrive();
				}
			});
		}
		
		m_btnShoot = (Button) findViewById(R.id.btnShoot);
		m_btnShoot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				m_oPirateDotty.shootGuns();
				m_btnShoot.setClickable(false);
				m_btnVolley.setClickable(false);
				m_btnShoot.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
				mUIHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						m_btnShoot.setClickable(true);
						m_btnVolley.setClickable(true);
						m_btnShoot.getBackground().clearColorFilter();
					}
				}, 2000);
				mSoundPool.play(mGunShotID, 1, 1, 1, 0, 1f);
			}
		});

		m_btnVolley = (Button) findViewById(R.id.btnVolley);
		m_btnVolley.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				m_oPirateDotty.fireVolley();
				m_btnShoot.setClickable(false);
				m_btnVolley.setClickable(false);
				m_btnVolley.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
				mUIHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						m_btnShoot.setClickable(true);
						m_btnVolley.setClickable(true);
						m_btnVolley.getBackground().clearColorFilter();
					}
				}, 6000);
			}
		});
		
		m_btnDock = (ToggleButton) m_oActivity.findViewById(R.id.btnDock);
		m_btnDock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bDocking = !m_bDocking;
				m_oPirateDotty.dock(m_bDocking);
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
		
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	menu.setGroupVisible(CONTROL_GRP, m_oPirateDotty.isConnected());
    	menu.setGroupVisible(CAMERA_CTRL_GRP, m_oPirateDotty.isConnected() && m_bCameraOn );

//    	Utils.updateOnOffMenuItem(menu.findItem(ACCEL_ID), m_bAccelerometer);
    	Utils.updateOnOffMenuItem(menu.findItem(REMOTE_CONTROL_ID), m_oRemoteCtrl.isControlEnabled());
    	Utils.updateOnOffMenuItem(menu.findItem(CAMERA_ID), m_bCameraOn);
//    	Utils.updateOnOffMenuItem(menu.findItem(CAMERA_DISP_ID), !m_oSensorGatherer.isPlaybackStopped());

    	return true;
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oPirateDotty.moveStop();
			}
			break;
		case REMOTE_CONTROL_ID:
			m_oRemoteCtrl.setRemoteControl(!m_oRemoteCtrl.isControlEnabled());
			break;
		case CAMERA_ID:
			if (m_bCameraOn) {
				stopVideo();
			} else {
				startVideo();
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
		}

		return super.onMenuItemSelected(featureId, item);
	}

	protected void resetLayout() {
        m_oRemoteCtrl.resetLayout();
        
        updateButtons(false);

		m_oSensorGatherer.resetLayout();
	}
	
	public void updateButtons(boolean enabled) {
		m_oRemoteCtrl.setControlEnabled(enabled);
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
	}
	
	@Override
	protected void onDisconnect() {
		updateButtons(false);
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void disconnect() {
		m_oPirateDotty.disconnect();
	}
	
	@Override
	public void setConnection(BluetoothDevice i_oDevice) {
		m_strAddress = i_oDevice.getAddress();
		showConnectingDialog();
		
		if (m_oPirateDotty.getConnection() != null) {
			try {
				m_oPirateDotty.getConnection().close();
			}
			catch (IOException e) { }
		}
		IRobotConnection connection = new BluetoothConnection(i_oDevice, PirateDottyTypes.PIRATEDOTTY_UUID);
		connection.setReceiveHandler(m_oUiHandler);
		m_oPirateDotty.setConnection(connection);
	}
	
	@Override
	public void connect() {
		m_oPirateDotty.connect();
	}

	public static void connectToPirateDotty(final BaseActivity m_oOwner, IPirateDotty i_oPirateDotty, BluetoothDevice i_oDevice, final IConnectListener i_oConnectListener) {
		PirateDottyUI m_oRobot = new PirateDottyUI(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oPirateDotty.isConnected()) {
			i_oPirateDotty.disconnect();
		}

		i_oPirateDotty.setHandler(m_oRobot.getUIHandler());
		IRobotConnection connection = new BluetoothConnection(i_oDevice, PirateDottyTypes.PIRATEDOTTY_UUID);
		connection.setReceiveHandler(m_oRobot.getUIHandler());
		i_oPirateDotty.setConnection(connection);
		i_oPirateDotty.connect();
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
