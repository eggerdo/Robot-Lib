package robots.romo.gui;

import org.dobots.R;
import org.dobots.utilities.log.ILogListener;
import org.dobots.utilities.log.LogTypes;
import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.ZmqRemoteControlSender;

import robots.RobotType;
import robots.gui.RobotInventory;
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

public class RomoUI extends RobotView implements ILogListener {
	
	private static Activity CONTEXT;
	
	private ImageButton m_btnCameraToggle;

	private ZmqRemoteControlSender m_oZmqRemoteSender;
	private ZmqRemoteControlHelper m_oRemoteCtrl;
	
	private RomoSensorGatherer m_oSensorGatherer;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        CONTEXT = this;
        
//        Romo oRomo = (Romo) getRobot();
//        oRomo.setDebug(true);
//        oRomo.setLogListener(this);

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

		m_oSensorGatherer = new RomoSensorGatherer(this, getRobot().getID());
		m_oSensorGatherer.startVideo();
    }
    
	@Override
	protected void setProperties(RobotType i_eRobot) {

		setContentView(R.layout.romo_main);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_btnCameraToggle = (ImageButton) findViewById(R.id.btnCameraToggle);
		if (Camera.getNumberOfCameras() <= 1) {
			m_btnCameraToggle.setVisibility(View.GONE);
		} else {
			m_btnCameraToggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					m_oZmqRemoteSender.toggleCamera();
				}
			});
		}
	}
	
	@Override
	public void onDestroy() {
		m_oRemoteCtrl.close();
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
