package robots.gui.rover.ac13;

import org.dobots.utilities.BaseActivity;

import robots.ctrl.rover.ac13.AC13Rover;
import robots.gui.rover.RoverBaseSensorGatherer;

public class AC13RoverSensorGatherer extends RoverBaseSensorGatherer {

	public AC13RoverSensorGatherer(BaseActivity i_oActivity, AC13Rover i_oRover) {
		super(i_oActivity, i_oRover, "AC13RoverSensorGatherer");
	}

}
