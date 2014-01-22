package robots.parrot.ardrone1.ctrl;

import java.io.IOException;

import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.parrot.ctrl.Parrot;

public class ArDrone1 extends Parrot {

	private static final String TAG = ArDrone1.class.getName();
	
	@Override
	public RobotType getType() {
		// TODO Auto-generated method stub
		return RobotType.RBT_ARDRONE1;
	}

	@Override
	public void startVideo() {
		debug(TAG, "startVideo()");
		Utils.runAsyncTask(new Runnable() {

			@Override
			public void run() {
				try {
					m_oController.connectVideoArDrone1();
					setVideoListener(ArDrone1.this);
				} catch (IOException e) {
					e.printStackTrace();
					error(TAG, "... failed");
				}
			}
		});
	}

	@Override
	public void stopVideo() {
		removeVideoListener(this);
	}

}
