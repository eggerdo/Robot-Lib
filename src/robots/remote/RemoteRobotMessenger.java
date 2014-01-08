package robots.remote;

import java.util.concurrent.TimeoutException;

import org.dobots.comm.msg.RoboCommands;
import org.dobots.comm.msg.RoboCommands.ControlCommand;
import org.dobots.utilities.ThreadMessenger;
import org.dobots.utilities.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.gui.MessageTypes;
import robots.gui.RobotView;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class RemoteRobotMessenger implements IRobotDevice {
	
	private static final String TAG = "RemoteRobot";
	
	public static final int RPC = 1000;
	public static final int REPLY = RPC + 1;
	public static final int INIT = REPLY + 1;
	public static final int CLOSE = INIT + 1;

	private RobotType mType;

	private boolean mBound;
	
	private Messenger mOutMessenger = null;
	
	private Handler m_oUiHandler = null;
	
	private String mRobotId = null;
	
	private RobotView mRobotView = null;
	
	private Class mRobotServiceClass;
	
	public RemoteRobotMessenger(RobotView activity, RobotType type, Class serviceClass) {
		mRobotView = activity;
		mRobotServiceClass = serviceClass;
		
		mType = type;
		
		Intent intent = new Intent(activity, serviceClass);
		activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private boolean isServiceRunning() {
	    ActivityManager manager = (ActivityManager) mRobotView.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (mRobotServiceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	@Override
	public RobotType getType() {
		return mType;
	}
	
	protected void onTimeout() {
		Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_RECEIVEERROR, null);
	}

	@Override
	public String getAddress() {
		try {
			return (String)sendRPCandWaitForReply("getAddress");
		} catch (TimeoutException e) {
			onTimeout();
			return "";
		}
	}

	@Override
	public String getID() {
		if (mRobotId == null) {
			try {
				mRobotId = (String)sendRPCandWaitForReply("getID");
			} catch (TimeoutException e) {
				onTimeout();
			}
		}
		return mRobotId;
	}

	@Override
	public void destroy() {
		// we don't automatically send a disconnect to the robot, 
		// because the service could be running still.
		
		closeRemoteConnection();
		
		if (mBound) {
			mRobotView.unbindService(mConnection);
            mBound = false;
        }
		
		mReceiver.destroy();
	}
	
	@Override
	public void connect() {
		sendRPC("connect");
	}

	@Override
	public void disconnect() {
		sendRPC("disconnect");
	}

	@Override
	public boolean isConnected() {
		try {
			return (Boolean)sendRPCandWaitForReply("isConnected");
		} catch (TimeoutException e) {
			onTimeout();
			return false;
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		sendRPC("connect", i_bEnable);
	}

	@Override
	public boolean toggleInvertDrive() {
		try {
			return (Boolean)sendRPCandWaitForReply("toggleInvertDrive");
		} catch (TimeoutException e) {
			onTimeout();
			return false;
		}
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		sendRPC("moveForward", i_dblSpeed);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		sendRPC("moveForward", i_dblSpeed, i_nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		sendRPC("moveBackward", i_dblSpeed);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		sendRPC("moveBackward", i_dblSpeed, i_nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		sendRPC("moveBackward", i_dblSpeed, i_dblAngle);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		sendRPC("moveForward", i_dblSpeed, i_dblAngle);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		sendRPC("rotateClockwise", i_dblSpeed);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		sendRPC("rotateCounterClockwise", i_dblSpeed);
	}

	@Override
	public void moveStop() {
		sendRPC("moveStop");
	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		sendRPC("setBaseSpeed", i_dblSpeed);
	}

	@Override
	public double getBaseSpeed() {
		try {
			return toDouble(sendRPCandWaitForReply("getBaseSpeed"));
		} catch (TimeoutException e) {
			onTimeout();
			return -1;
		}
	}
	
	private double toDouble(Object value) {
		// this is a bit of a hack, because JSON makes Integers out
		// of doubles if they are x.0 and we use function JSONObject.put(Object)
		// so in case we get a integer although we expect a double we
		// convert the integer to double
		if (value.getClass().equals(Integer.class)) {
			return ((Integer)value).doubleValue();
		} else {
			return (Double)value;
		}
	}

	@Override
	public void moveForward() {
		sendRPC("moveForward");
	}

	@Override
	public void moveBackward() {
		sendRPC("moveBackward");
	}

	@Override
	public void rotateCounterClockwise() {
		sendRPC("rotateCounterClockwise");
	}

	@Override
	public void rotateClockwise() {
		sendRPC("rotateClockwise");
	}

	@Override
	public void moveLeft() {
		sendRPC("moveLeft");
	}

	@Override
	public void moveRight() {
		sendRPC("moveRight");
	}
	
	protected synchronized Object sendRPCandWaitForReply(String command) throws TimeoutException {
		synchronized (mWaitForReply) {
			mReplyJson = null;
			sendRPC(command);
			try {
				mWaitForReply.wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		Object reply = null;
		if (mReplyJson == null) {
			throw new TimeoutException();
		}
		
		try {
			reply = mReplyJson.get(command);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reply;
	}

	private void sendRPC(String command) {
		sendRPC(command, (Object[])null);
	}
	
	protected void sendRPC(String command, Object... parameters) {
		if (!mBound) return;
		
		ControlCommand cmd = RoboCommands.createControlCommand(getID(), command, parameters);
		
		Message msg = Message.obtain(null, RemoteRobotMessenger.RPC);
		Bundle bundle = new Bundle();
		bundle.putString("data", cmd.toJSONString());
		msg.setData(bundle);
		msg.replyTo = getInMessenger();
		
		Log.d(TAG, "send rpc: " + cmd.toJSONString());
		send(msg);
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
			
			mOutMessenger = new Messenger(service);
			initRemoteConnection();
		}
	};
	
	private Object mWaitForReply = new Object();
	private String mReply;
	private JSONObject mReplyJson;

	private ThreadMessenger mReceiver = new ThreadMessenger("remoteWrapperUi") {
		
		@Override
		protected boolean handleIncomingMessage(Message msg) {
			switch(msg.what) {
			case INIT:
				mRobotId = msg.getData().getString("robot_id");
				Utils.runAsyncTask(new Runnable() {
					
					@Override
					public void run() {
						mRobotView.onRobotCtrlReady();
					}
				});
				break;
			case CLOSE:
				mRobotId = null;
				break;
			case REPLY:
				synchronized (mWaitForReply) {
					mReply = msg.getData().getString("data");
					
					try {
						mReplyJson = new JSONObject(mReply);
					} catch (JSONException e) {
						mReplyJson = null;
					}
					
					mWaitForReply.notify();
				}
				break;
			default:
				// forward messages to the UI
				Message newMessage = Message.obtain();
				newMessage.copyFrom(msg);
				m_oUiHandler.sendMessage(newMessage);
			}
			return true;
		}
	};
	
	private Messenger getInMessenger() {
		return mReceiver.getMessenger();
	}
	
	private void initRemoteConnection() {
		Message msg = Message.obtain(null, INIT);
		msg.replyTo = getInMessenger();
		send(msg);
	}
	
	private void closeRemoteConnection() {
		Message msg = Message.obtain(null, CLOSE);
		msg.replyTo = getInMessenger();
		send(msg);
	}
	
	private void send(Message msg) {
		send(mOutMessenger, msg);
	}
	
	private void send(Messenger target, Message msg) {
		try {
			target.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setHandler(Handler handler) {
		m_oUiHandler = handler;
	}
}
