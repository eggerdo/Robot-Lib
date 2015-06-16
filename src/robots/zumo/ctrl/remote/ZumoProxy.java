package robots.zumo.ctrl.remote;

import robots.RobotType;
import robots.gui.RobotView;
import robots.gui.comm.IRobotConnection;
import robots.remote.RobotProxy;
import robots.zumo.ctrl.IZumo;

public class ZumoProxy extends RobotProxy implements IZumo {

	public ZumoProxy(RobotView activity, Class serviceClass) {
		super(activity, serviceClass);
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_PIRATEDOTTY;
	}
	
	private IZumo getRobot() {
		return (IZumo)mRobot;
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

	public boolean isMazeSolving() {
		if (mBound) {
			return getRobot().isMazeSolving();
		}
		return false;
	}

	public void initMazeSolver() {
		if (mBound) {
			getRobot().initMazeSolver();
		}
	}

	public void startMazeSolving() {
		if (mBound) {
			getRobot().startMazeSolving();
		}
	}

	public void stopMazeSolving() {
		if (mBound) {
			getRobot().stopMazeSolving();
		}
	}

	public void repeatMaze() {
		if (mBound) {
			getRobot().repeatMaze();
		}
	}

	public void calibrateCompass() {
		if (mBound) {
			getRobot().calibrateCompass();
		}
	}

	public void resetHeading() {
		if (mBound) {
			getRobot().resetHeading();
		}
	}

	public void turnDegrees(int angle) {
		if (mBound) {
			getRobot().turnDegrees(angle);
		}
	}
	
//	@Override
//	public void shootGuns() {
//		if (mBound) {
//			getRobot().shootGuns();
//		}
//	}

//	@Override
//	public void fireVolley() {
//		if (mBound) {
//			getRobot().fireVolley();
//		}
//	}

//	@Override
//	public void dock(boolean isDocking) {
//		if (mBound) {
//			getRobot().dock(isDocking);
//		}
//	}

}
