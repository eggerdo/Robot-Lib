package robots.parrot.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.parrot.ctrl.IParrot;
import robots.remote.RobotProxy;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavDataListener;

public class ParrotProxy extends RobotProxy implements IParrot {

	private static final String TAG = "RemoteRobot";

	public ParrotProxy(RobotView activity, Class serviceClass) {
		super(activity, serviceClass);
	}
	
	protected IParrot getParrot() {
		return (IParrot)mRobot;
	}

	@Override
	public void setConnection(String address) {
		if (mBound) {
			getParrot().setConnection(address);
		}
	}

	@Override
	public RobotType getType() {
		if (mBound) {
			return RobotType.RBT_ARDRONE2;
		}
		return null;
	}

	@Override
	public boolean isStreaming() {
		if (mBound) {
			return getParrot().isStreaming();
		}
		return false;
	}

	@Override
	public void startVideo() {
		if (mBound) {
			getParrot().startVideo();	
		}	
	}

	@Override
	public void stopVideo() {
		if (mBound) {
			getParrot().stopVideo();		
		}
	}

	@Override
	public void setNavDataListener(NavDataListener i_oListener) {
		if (mBound) {
			getParrot().setNavDataListener(i_oListener);
		}
	}

	@Override
	public void removeNavDataListener(NavDataListener i_oListener) {
		if (mBound) {
			getParrot().removeNavDataListener(i_oListener);
		}
	}

//	@Override
//	public void setVideoListener(DroneVideoListener i_oListener) {
//		if (mBound) {
//			getParrot().setVideoListener(i_oListener);
//		}
//	}
//
//	@Override
//	public void removeVideoListener(DroneVideoListener i_oListener) {
//		if (mBound) {
//			getParrot().removeVideoListener(i_oListener);
//		}
//	}

	@Override
	public boolean isARDrone1() {
		if (mBound) {
			return getParrot().isARDrone1();
		}
		return false;
	}

	@Override
	public void land() {
		if (mBound) {
			getParrot().land();
		}
	}

	@Override
	public void takeOff() {
		if (mBound) {
			getParrot().takeOff();
		}
	}

	@Override
	public void stopAltitudeControl() {
		if (mBound) {
			getParrot().stopAltitudeControl();
		}
	}

	@Override
	public void setAltitude(double altitude) {
		if (mBound) {
			getParrot().setAltitude(altitude);
		}
	}

	@Override
	public void increaseAltitude() {
		if (mBound) {
			getParrot().increaseAltitude();
		}
	}

	@Override
	public void decreaseAltitude() {
		if (mBound) {
			getParrot().decreaseAltitude();
		}
	}

	@Override
	public void increaseAltitude(double i_dblSpeed) {
		if (mBound) {
			getParrot().increaseAltitude(i_dblSpeed);
		}
	}

	@Override
	public void decreaseAltitude(double i_dblSpeed) {
		if (mBound) {
			getParrot().decreaseAltitude(i_dblSpeed);
		}
	}

	@Override
	public void switchCamera() {
		if (mBound) {
			getParrot().switchCamera();
		}
	}

	@Override
	public VideoChannel getVidoeChannel() {
		if (mBound) {
			getParrot().getVidoeChannel();
		}
		return ARDrone.VideoChannel.HORIZONTAL_ONLY;
	}

	@Override
	public void moveLeft(double i_dblSpeed) {
		if (mBound) {
			getParrot().moveLeft(i_dblSpeed);
		}
	}

	@Override
	public void moveRight(double i_dblSpeed) {
		if (mBound) {
			getParrot().moveRight(i_dblSpeed);
		}
	}

	@Override
	public void moveLeft() {
		if (mBound) {
			getParrot().moveLeft();
		}
	}

	@Override
	public void moveRight() {
		if (mBound) {
			getParrot().moveRight();
		}
	}

	@Override
	public void setVideoListener(DroneVideoListener i_oListener) {
		if (mBound) {
			getParrot().setVideoListener(i_oListener);
		}
	}

	@Override
	public void removeVideoListener(DroneVideoListener i_oListener) {
		if (mBound) {
			getParrot().removeVideoListener(i_oListener);
		}
	}

}
