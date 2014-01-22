package robots.remote;

import robots.ctrl.IRobotDevice;
import robots.gui.RobotView;
import robots.remote.RobotServiceBinder.RobotBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public abstract class RemoteRobotBinder implements IRobotDevice {

	private static final String TAG = "RemoteRobotDirect";

	protected RobotView mRobotView;

	protected boolean mBound;

	protected IRobotDevice mRobot;

	public RemoteRobotBinder(RobotView activity, Class serviceClass) {
		mRobotView = activity;

		Intent intent = new Intent(activity, serviceClass);
		activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	public void destroy() {
		if (mBound) {
			mRobotView.unbindService(mConnection);
			mBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.i(TAG, "disconnected from service...");
			mBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i(TAG, "connected to service... " + service.toString());
			
			mBound = true;
			RobotBinder binder = (RobotBinder) service;
			mRobot = ((IRobotDevice) binder.getRobot());
			
			// inform the view that the robot is ready (connected to controller, not to
			// robot itself)
			mRobotView.onRobotCtrlReady();
		}
	};

	@Override
	public void setHandler(Handler handler) {
		if (mBound) {
			mRobot.setHandler(handler);
		}
	}

	@Override
	public String getAddress() {
		if (mBound) {
			return mRobot.getAddress();
		}
		return null;
	}

	@Override
	public String getID() {
		if (mBound) {
			return mRobot.getID();
		}
		return null;
	}

	@Override
	public void connect() {
		if (mBound) {
			mRobot.connect();
		}
	}

	@Override
	public void disconnect() {
		if (mBound) {
			mRobot.disconnect();
		}
	}

	@Override
	public boolean isConnected() {
		if (mBound) {
			return mRobot.isConnected();
		}
		return false;
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		if (mBound) {
			mRobot.enableControl(i_bEnable);
		}
	}

	@Override
	public boolean toggleInvertDrive() {
		if (mBound) {
			return mRobot.toggleInvertDrive();
		}
		return false;
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		if (mBound) {
			mRobot.moveForward(i_dblSpeed);
		}
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		if (mBound) {
			mRobot.moveForward(i_dblSpeed, i_nRadius);
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		if (mBound) {
			mRobot.moveBackward(i_dblSpeed);		
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		if (mBound) {
			mRobot.moveBackward(i_dblSpeed, i_nRadius);	
		}	
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		if (mBound) {
			mRobot.moveBackward(i_dblSpeed, i_dblAngle);	
		}	
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		if (mBound) {
			mRobot.moveForward(i_dblSpeed, i_dblAngle);	
		}	
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		if (mBound) {
			mRobot.rotateClockwise(i_dblSpeed);		
		}
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		if (mBound) {
			mRobot.rotateCounterClockwise(i_dblSpeed);
		}		
	}

	@Override
	public void moveStop() {
		if (mBound) {
			mRobot.moveStop();		
		}
	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		if (mBound) {
			mRobot.setBaseSpeed(i_dblSpeed);	
		}	
	}

	@Override
	public double getBaseSpeed() {
		if (mBound) {
			return mRobot.getBaseSpeed();
		}
		return -1;
	}

	@Override
	public void moveForward() {
		if (mBound) {
			mRobot.moveForward();	
		}	
	}

	@Override
	public void moveBackward() {
		if (mBound) {
			mRobot.moveBackward();		
		}
	}

	@Override
	public void rotateCounterClockwise() {
		if (mBound) {
			mRobot.rotateCounterClockwise();	
		}	
	}

	@Override
	public void rotateClockwise() {
		if (mBound) {
			mRobot.rotateClockwise();	
		}	
	}

}
