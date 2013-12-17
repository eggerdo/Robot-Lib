package robots.replicator.gui;

import org.dobots.utilities.BaseActivity;

import robots.gui.VideoSensorGatherer;
import robots.replicator.ctrl.Replicator;

/**
 * The ReplicatorSensorGatherer used to derive from the same thing as the SpyTank, but it's smarter to use the
 * AC13 thing as inspiration.
 * 
 * @author Anne C. van Rossum
 * @author Dominik Egger
 * @date Aug. 22, 2013
 * @license LGPLv3
 * @copyright Distributed Organisms B.V., Rotterdam, The Netherlands
 */
public class ReplicatorSensorGatherer extends VideoSensorGatherer {

	public static final String TAG = "ReplicatorSensorGatherer";
	
	public ReplicatorSensorGatherer(BaseActivity i_oActivity, Replicator i_oReplicator) {
		super(i_oActivity, i_oReplicator, "SpyTankSensorGatherer");
	}

}
