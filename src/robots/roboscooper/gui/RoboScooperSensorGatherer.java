package robots.roboscooper.gui;

import org.dobots.utilities.BaseActivity;

import robots.brainlink.gui.BrainlinkSensorGatherer;
import robots.roboscooper.ctrl.RoboScooper;

public class RoboScooperSensorGatherer extends BrainlinkSensorGatherer {

	public RoboScooperSensorGatherer(BaseActivity i_oActivity, RoboScooper i_oRobot) {
		super(i_oActivity, i_oRobot.getBrainlink());
	}

}
