package robots.spytank.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.remote.RobotProxy;
import robots.spytank.ctrl.ISpyTank;

public class SpyTankProxy extends RobotProxy implements ISpyTank {

	public SpyTankProxy(RobotView activity, Class serviceClass) {
		super(activity, serviceClass);
	}

	@Override
	public RobotType getType() {
		if (mBound) {
			return RobotType.RBT_SPYTANK;
		}
		return null;
	}
	
	private ISpyTank getSpyTank() {
		return (ISpyTank)mRobot;
	}

	@Override
	public boolean isStreaming() {
		if (mBound) {
			return getSpyTank().isStreaming();
		}
		return false;
	}

	@Override
	public void startVideo() {
		if (mBound) {
			getSpyTank().startVideo();
		}
	}

	@Override
	public void stopVideo() {
		if (mBound) {
			getSpyTank().stopVideo();
		}
	}

	@Override
	public void setConnection(String address, int command_port, int media_port) {
		if (mBound) {
			getSpyTank().setConnection(address, command_port, media_port);
		}
	}

	@Override
	public void cameraUp() {
		if (mBound) {
			getSpyTank().cameraUp();
		}
	}

	@Override
	public void cameraStop() {
		if (mBound) {
			getSpyTank().cameraStop();
		}
	}

	@Override
	public void cameraDown() {
		if (mBound) {
			getSpyTank().cameraDown();
		}
	}

}
