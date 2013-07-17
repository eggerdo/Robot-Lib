package robots.ispytank.gui;

import org.dobots.R;
import org.dobots.communication.control.ZmqRemoteControlHelper;
import org.dobots.communication.control.ZmqRemoteListener;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import robots.RobotType;
import robots.ctrl.RemoteControlHelper;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.ispytank.ctrl.SpyTank;
import robots.rover.rover2.ctrl.Rover2;
import robots.rover.rover2.gui.Rover2SensorGatherer;

public class SpyTankRobot extends WifiRobot {
	
	private SpyTankSensorGatherer m_oSensorGatherer;
	private ZmqRemoteListener m_oZmqRemoteListener;
	private RemoteControlHelper m_oRemoteCtrl;
	
	private SpyTank m_oSpyTank;

	public SpyTankRobot() {
		
	}
	
	public SpyTankRobot(BaseActivity owner) {
		super(owner);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	m_oSpyTank = (SpyTank) getRobot();
    	m_oSpyTank.setHandler(m_oUiHandler);

    	m_oZmqRemoteListener = new ZmqRemoteListener();
    	m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity, m_oZmqRemoteListener, m_oSpyTank.getID());
        m_oRemoteCtrl.setProperties();
    	
        m_oSensorGatherer = new SpyTankSensorGatherer(this, m_oSpyTank);

    	updateButtons(false);
    	
    	if (m_oSpyTank.isConnected()) {
    		updateButtons(true);
    	} else {
    		connectToRobot();
    	}
    }

	@Override
	protected void connect() {
		m_oSpyTank.connect();
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
		m_oSensorGatherer.onConnect();
	}

	@Override
	protected void onDisconnect() {
		updateButtons(false);
		m_oSensorGatherer.onDisconnect();
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
		m_oActivity.setContentView(R.layout.spytank_main);

    	Button btnCameraUp = (Button) findViewById(R.id.btnCameraUp);
    	btnCameraUp.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oSpyTank.cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oSpyTank.cameraUp();
					break;
				}
				return true;
			}
		});

    	Button btnCameraDown = (Button) findViewById(R.id.btnCameraDown);
    	btnCameraDown.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oSpyTank.cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oSpyTank.cameraDown();
					break;
				}
				return true;
			}
		});
    	    	
	}

	@Override
	protected void disconnect() {
		m_oSpyTank.disconnect();
	}

	@Override
	protected void resetLayout() {
		m_oSensorGatherer.resetLayout();
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		m_oRemoteCtrl.updateButtons(i_bEnabled);
		
		Utils.setEnabledRecursive((ViewGroup)m_oActivity.findViewById(R.id.layCameraControl), i_bEnabled);
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		// TODO Auto-generated method stub
		return m_oSensorGatherer;
	}

}
