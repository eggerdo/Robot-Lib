package robots.spytank.gui;

import org.dobots.utilities.BaseActivity;

import robots.gui.VideoSensorGatherer;
import robots.spytank.ctrl.ISpyTank;

public class SpyTankSensorGatherer extends VideoSensorGatherer {

	public SpyTankSensorGatherer(BaseActivity i_oActivity, ISpyTank spyTank) {
		super(i_oActivity, spyTank, "SpyTankSensorGatherer");
	}

}
