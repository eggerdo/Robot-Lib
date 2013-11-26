package robots.rover.rover2.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.remote.RemoteRobotBinder;
import robots.rover.base.ctrl.RoverBaseTypes.VideoResolution;
import robots.rover.rover2.ctrl.IRover2;
import robots.rover.rover2.ctrl.Rover2;

public class Rover2RemoteBinder extends RemoteRobotBinder implements IRover2 {

	private static final String TAG = "RemoteRobot";

	public Rover2RemoteBinder(RobotView activity, Class serviceClass) {
		super(activity, serviceClass);
	}
	
	private IRover2 getRover() {
		return (IRover2)mRobot;
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

	@Override
	public void toggleLight() {
		if (mBound) {
			getRover().toggleLight();	
		}	
	}

	@Override
	public void enableLight() {
		if (mBound) {
			getRover().enableLight();	
		}	
	}

	@Override
	public void disableLight() {
		if (mBound) {
			getRover().disableLight();	
		}	
	}

	@Override
	public void cameraUp() {
		if (mBound) {
			getRover().cameraUp();		
		}
	}

	@Override
	public void cameraStop() {
		if (mBound) {
			getRover().cameraStop();	
		}	
	}

	@Override
	public void cameraDown() {
		if (mBound) {
			getRover().cameraDown();	
		}	
	}

	@Override
	public double getBatteryPower() {
		if (mBound) {
			return getRover().getBatteryPower();
		}
		return -1;
	}

}
