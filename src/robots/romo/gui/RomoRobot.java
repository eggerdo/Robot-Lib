package robots.romo.gui;

import org.dobots.R;
import org.dobots.communication.control.ZmqRemoteControlHelper;
import org.dobots.communication.control.ZmqRemoteControlSender;
import org.dobots.utilities.CameraPreview;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.control.ICameraControlListener;
import robots.gui.RobotInventory;
import robots.gui.RobotView;
import robots.gui.SensorGatherer;
import robots.romo.ctrl.Romo;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;

public class RomoRobot extends RobotView implements ICameraControlListener {
	
	private static Activity CONTEXT;
	
	private CameraPreview m_oCamera;
	
	private ImageButton m_btnCameraToggle;

	private ZmqRemoteControlSender m_oZmqRemoteSender;
	private ZmqRemoteControlHelper m_oRemoteCtrl;
	private ZmqRemoteControlHelper m_oCameraCtrl;
	
	private RomoSensorGatherer m_oSensorGatherer;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        CONTEXT = this;
        
        Romo oRomo = (Romo) getRobot();
        oRomo.setDebug(true);

    	m_oZmqRemoteSender = new ZmqRemoteControlSender(oRomo.getID());

    	// remote control helper, handles ui buttons and sends commands over zmq
		m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity);
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);
		
		// receives and handles incoming camera control commands
		m_oCameraCtrl = new ZmqRemoteControlHelper();
		m_oCameraCtrl.setCameraControlListener(this);
		m_oCameraCtrl.startReceiver("RomoUI");

        m_oSensorGatherer = new RomoSensorGatherer(this, m_strRobotID);
		m_oCamera.setFrameListener(m_oSensorGatherer);
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {

		setContentView(R.layout.romo_main);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_oCamera = (CameraPreview) findViewById(R.id.svCamera);
		m_oCamera.setScale(false);
		m_oCamera.setPreviewSize(640, 480);
	
		m_btnCameraToggle = (ImageButton) findViewById(R.id.btnCameraToggle);
		if (Camera.getNumberOfCameras() <= 1) {
			m_btnCameraToggle.setVisibility(View.GONE);
		} else {
			m_btnCameraToggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleCamera();
				}
			});
		}
	}
	
	@Override
	public void onDestroy() {
		m_oCamera.stopCamera();
		m_oRemoteCtrl.destroy();
		m_oZmqRemoteSender.close();
		
		if (m_bOwnsRobot) {
			RobotInventory.getInstance().removeRobot(m_strRobotID);
		}
		
		super.onDestroy();
	}

	public static Activity getContext() {
		return CONTEXT;
	}

	@Override
	protected void onConnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onConnectError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connectToRobot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void resetLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		// TODO Auto-generated method stub
		return m_oSensorGatherer;
	}

	@Override
	public void toggleCamera() {
		// toggle camera only works if it is executed by the UI thread
		// so we check if the calling thread is the main thread, otherwise
		// we call the function again inside the main thread.
		if (Looper.myLooper() != Looper.getMainLooper()) {
			Utils.runAsyncUiTask(new Runnable() {
				@Override
				public void run() {
					toggleCamera();
				}
			});
		} else {
			m_oCamera.toggleCamera();
		}
	}

	@Override
	public void switchCameraOn() {
		m_oCamera.startCamera();
	}

	@Override
	public void switchCameraOff() {
		m_oCamera.stopCamera();
	}

	@Override
	public void cameraUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cameraDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cameraStop() {
		// TODO Auto-generated method stub
		
	}

}
