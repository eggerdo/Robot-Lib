package robots.gui.rover.ac13;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotRemoteListener;
import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.ctrl.RemoteControlHelper;
import robots.ctrl.rover.RoverBase;
import robots.ctrl.rover.RoverBaseTypes.VideoResolution;
import robots.ctrl.rover.ac13.AC13Rover;
import robots.gui.IConnectListener;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.gui.rover.RoverBaseRobot;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class AC13RoverRobot extends RoverBaseRobot {

	private static final String TAG = "AC13Rover";

	public AC13RoverRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
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
