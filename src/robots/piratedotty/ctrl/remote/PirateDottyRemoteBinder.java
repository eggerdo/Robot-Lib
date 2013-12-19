package robots.piratedotty.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.gui.comm.IRobotConnection;
import robots.piratedotty.ctrl.IPirateDotty;
import robots.remote.RemoteRobotBinder;

public class PirateDottyRemoteBinder extends RemoteRobotBinder implements IPirateDotty {

	public PirateDottyRemoteBinder(RobotView activity, Class serviceClass) {
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

}
