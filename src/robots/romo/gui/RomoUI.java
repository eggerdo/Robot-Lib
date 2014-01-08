package robots.romo.gui;

import org.dobots.R;
import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.ZmqRemoteControlSender;

import robots.RobotType;
import robots.gui.RobotInventory;
import robots.gui.RobotView;
import robots.gui.SensorGatherer;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;

public class RomoUI extends RobotView {
	
	private static Activity CONTEXT;
	
	private ImageButton m_btnCameraToggle;

	private RomoSensorGatherer m_oSensorGatherer;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        CONTEXT = this;
        
//        Romo oRomo = (Romo) getRobot();
//        oRomo.setDebug(true);

//    	m_oZmqRemoteSender = new ZmqRemoteControlSender(oRomo.getID());

    	// remote control helper, handles ui buttons and sends commands over zmq
		m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity);
//		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);
		
		// receives and handles incoming camera control commands

//        m_oSensorGatherer = new RomoSensorGatherer(this, m_strRobotID);
	}

    @Override
    public void onRobotCtrlReady() {
    	m_oZmqRemoteSender = new ZmqRemoteControlSender(getRobot().getID());
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);
		m_oRemoteCtrl.setCameraControlListener(m_oZmqRemoteSender);

		m_oSensorGatherer = new RomoSensorGatherer(this, getRobot().getID());
		m_oSensorGatherer.startVideo();
    }
    
	@Override
	protected void setLayout(RobotType i_eRobot) {

		setContentView(R.layout.robot_romo_main);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_btnCameraToggle = (ImageButton) findViewById(R.id.btnCameraToggle);
		if (Camera.getNumberOfCameras() <= 1) {
			m_btnCameraToggle.setVisibility(View.GONE);
		} else {
			m_btnCameraToggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					m_oRemoteCtrl.toggleCamera();
				}
			});
		}
	}
	
	@Override
	public void onDestroy() {
		m_oRemoteCtrl.destroy();
		m_oZmqRemoteSender.close();
		
		m_oSensorGatherer.stopVideo();
		
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

}
