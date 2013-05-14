package robots.gui.romo;

import org.dobots.R;
import org.dobots.robotalk.control.ZmqRemoteControlHelper;
import org.dobots.robotalk.control.ZmqRemoteListener;
import org.dobots.utilities.CameraPreview;
import org.dobots.utilities.log.ILogListener;
import org.dobots.utilities.log.LogTypes;

import robots.RobotInventory;
import robots.RobotType;
import robots.ctrl.ICameraControlListener;
import robots.ctrl.IRobotDevice;
import robots.ctrl.romo.Romo;
import robots.gui.RobotView;
import robots.gui.SensorGatherer;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;

public class RomoRobot extends RobotView implements ICameraControlListener, ILogListener {
	
	private static Activity CONTEXT;
	
	private CameraPreview m_oCamera;
	
	private ImageButton m_btnCameraToggle;

	private ZmqRemoteListener m_oZmqRemoteListener;
	private ZmqRemoteControlHelper m_oRemoteCtrl;
	
	private RomoSensorGatherer m_oSensorGatherer;
	
	private String m_strRobotID;
	
	private boolean m_bOwnsRobot = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        CONTEXT = this;
        
        Romo oRomo = (Romo) getRobot();
        oRomo.setDebug(true);
        oRomo.setLogListener(this);

    	m_oZmqRemoteListener = new ZmqRemoteListener();

		m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity, m_oZmqRemoteListener, "RomoGUI");
        m_oRemoteCtrl.setProperties();
		m_oRemoteCtrl.setCameraControlListener(this);

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

//		Button btnSend = (Button) findViewById(R.id.btnSend);
//		btnSend.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				m_oSensorGatherer.let(100);
//			}
//		});
	}
	
	@Override
	public void onDestroy() {
		m_oCamera.stopCamera();
		m_oRemoteCtrl.close();
		m_oZmqRemoteListener.close();
		
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
		m_oCamera.toggleCamera();
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
	public void onTrace(LogTypes i_eType, String i_strTag, String i_strMessage) {
		switch(i_eType) {
		case tt_Debug:
			Log.d(i_strTag, i_strMessage);
		}
	}

	@Override
	public void onTrace(LogTypes i_eType, String i_strTag, String i_strMessage,
			Throwable i_oObj) {
		switch(i_eType) {
		case tt_Debug:
			Log.d(i_strTag, i_strMessage);
		}
	}

}
