package robots.piratedotty.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.gui.comm.IRobotConnection;
import robots.piratedotty.ctrl.IPirateDotty;
import robots.remote.RobotProxy;

public class PirateDottyProxy extends RobotProxy implements IPirateDotty {

	public PirateDottyProxy(RobotView activity, Class serviceClass) {
		super(activity, serviceClass);
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_PIRATEDOTTY;
	}
	
	private IPirateDotty getRobot() {
		return (IPirateDotty)mRobot;
	}

	@Override
	public boolean isStreaming() {
		if (mBound) {
			return getRobot().isStreaming();
		}
		return false;
	}

	@Override
	public void startVideo() {
		if (mBound) {
			getRobot().startVideo();
		}
	}

	@Override
	public void stopVideo() {
		if (mBound) {
			getRobot().stopVideo();
		}
	}

	@Override
	public IRobotConnection getConnection() {
		if (mBound) {
			return getRobot().getConnection();
		}
		return null;
	}

	@Override
	public void setConnection(IRobotConnection connection) {
		if (mBound) {
			getRobot().setConnection(connection);
		}
	}

	@Override
	public void shootGuns() {
		if (mBound) {
			getRobot().shootGuns();
		}
	}

	@Override
	public void fireVolley() {
		if (mBound) {
			getRobot().fireVolley();
		}
	}

	@Override
	public void dock(boolean isDocking) {
		if (mBound) {
			getRobot().dock(isDocking);
		}
	}

}
