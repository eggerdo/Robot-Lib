package robots.rover.ac13.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;

import robots.RobotRemoteListener;
import robots.RobotType;
import robots.ctrl.RemoteControlHelper;
import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.gui.RoverBaseRobot;
import android.os.Bundle;

public class AC13RoverRobot extends RoverBaseRobot {

	private static final String TAG = "AC13Rover";

	public AC13RoverRobot(BaseActivity i_oOwner) {
		super(i_oOwner);

		RobotRemoteListener oListener = new RobotRemoteListener(getRobot());
//		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, oListener);
//        m_oRemoteCtrl.setProperties();

        m_oRemoteCtrl.setRemoteControlListener(oListener);
	}
	
	public AC13RoverRobot() {
		super();
	}
	
	private AC13Rover getRover() {
		return (AC13Rover) getRobot();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		m_oSensorGatherer = new AC13RoverSensorGatherer(this, getRover());
    }

	@Override
	protected void setProperties(RobotType i_eRobot) {
    	m_oActivity.setContentView(R.layout.ac13rover_main);
    	super.setProperties(i_eRobot);
	}

}
