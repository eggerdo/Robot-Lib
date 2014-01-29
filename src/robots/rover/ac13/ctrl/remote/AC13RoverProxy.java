package robots.rover.ac13.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.parrot.ctrl.remote.ParrotProxy;
import robots.rover.ac13.ctrl.IAC13Rover;
import robots.rover.base.ctrl.RoverBaseTypes.VideoResolution;

public class AC13RoverProxy extends ParrotProxy implements IAC13Rover {

	private static final String TAG = "RemoteRobot";

	public AC13RoverProxy(RobotView activity, Class serviceClass) {
		super(activity, serviceClass);
	}
	
	private IAC13Rover getRover() {
		return (IAC13Rover)mRobot;
	}

	@Override
	public void setConnection(String address, int port) {
		if (mBound) {
			getRover().setConnection(address, port);
		}
	}

	@Override
	public void toggleInfrared() {
		if (mBound) {
			getRover().toggleInfrared();
		}
	}

	@Override
	public void enableInfrared() {
		if (mBound) {
			getRover().enableInfrared();
		}
	}

	@Override
	public void disableInfrared() {
		if (mBound) {
			getRover().disableInfrared();
		}
	}

	@Override
	public void setResolution(VideoResolution i_eResolution) {
		if (mBound) {
			getRover().setResolution(i_eResolution);
		}
	}

	@Override
	public VideoResolution getResolution() {
		if (mBound) {
			return getRover().getResolution();
		}
		return VideoResolution.res_unknown;
	}

	@Override
	public RobotType getType() {
		if (mBound) {
			return RobotType.RBT_ROVER2;
		}
		return null;
	}

	@Override
	public boolean isStreaming() {
		if (mBound) {
			return getRover().isStreaming();
		}
		return false;
	}

	@Override
	public void startVideo() {
		if (mBound) {
			getRover().startVideo();	
		}	
	}

	@Override
	public void stopVideo() {
		if (mBound) {
			getRover().stopVideo();		
		}
	}

}
