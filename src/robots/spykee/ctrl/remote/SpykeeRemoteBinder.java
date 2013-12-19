package robots.spykee.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.remote.RemoteRobotBinder;
import robots.spykee.ctrl.ISpykee;
import robots.spykee.ctrl.SpykeeController.DockState;
import robots.spykee.ctrl.SpykeeTypes.SpykeeSound;

public class SpykeeRemoteBinder extends RemoteRobotBinder implements ISpykee {

	public SpykeeRemoteBinder(RobotView activity, Class serviceClass) {
		super(activity, serviceClass);
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_SPYKEE;
	}
	
	private ISpykee getSpykee() {
		return (ISpykee)mRobot;
	}

	@Override
	public boolean isStreaming() {
		if (mBound) {
			return getSpykee().isStreaming();
		}
		return false;
	}

	@Override
	public void startVideo() {
		if (mBound) {
			getSpykee().startVideo();
		}
	}

	@Override
	public void stopVideo() {
		if (mBound) {
			getSpykee().stopVideo();
		}
	}

	@Override
	public void dock() {
		if (mBound) {
			getSpykee().dock();
		}
	}

	@Override
	public void undock() {
		if (mBound) {
			getSpykee().undock();
		}
	}

	@Override
	public void cancelDock() {
		if (mBound) {
			getSpykee().cancelDock();
		}
	}

	@Override
	public void playSound(SpykeeSound i_eSound) {
		if (mBound) {
			getSpykee().playSound(i_eSound);
		}
	}

	@Override
	public DockState getDockState() {
		if (mBound) {
			return getSpykee().getDockState();
		}
		return DockState.UNKNOWN;
	}

	@Override
	public void setLed(int i_nLed, boolean i_bOn) {
		if (mBound) {
			getSpykee().setLed(i_nLed, i_bOn);
		}
	}

	@Override
	public int getBatteryLevel() {
		if (mBound) {
			return getSpykee().getBatteryLevel();
		}
		return -1;
	}

	@Override
	public boolean isCharging() {
		if (mBound) {
			return getSpykee().isCharging();
		}
		return false;
	}

	@Override
	public void setConnection(String address, String port, String login,
			String password) {
		if (mBound) {
			getSpykee().setConnection(address, port, login, password);
		}
	}

	@Override
	public boolean isInverted() {
		if (mBound) {
			return getSpykee().isInverted();
		}
		return false;
	}

	@Override
	public void setInverted(boolean inverted) {
		if (mBound) {
			getSpykee().setInverted(inverted);
		}
	}

}
