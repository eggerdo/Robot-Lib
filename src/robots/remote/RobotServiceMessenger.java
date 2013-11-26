package robots.remote;

import java.util.ArrayList;

import org.dobots.communication.msg.RoboCommands;
import org.dobots.communication.msg.RoboCommands.BaseCommand;
import org.dobots.communication.msg.RoboCommands.ControlCommand;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqUtils;
import org.dobots.utilities.ThreadMessenger;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

import robots.ctrl.IRobotDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class RobotServiceMessenger {
	
	private static final String TAG = "RemoteWrapperRobot";
	
	private IRobotDevice mRobot;

	// socket to forward commands coming in over the messenger to the zmq handler
	private ZMQ.Socket m_oZmqForwarder;
	
	public RobotServiceMessenger(IRobotDevice robot) {
		mRobot = robot;

		m_oZmqForwarder = ZmqHandler.getInstance().obtainCommandSendSocket();

		mRobot.setHandler(mUiHandler);
	}

	private ArrayList<Messenger> mServiceOutMessengers = new ArrayList<Messenger>();
	
	private ThreadMessenger mReceiver = new ThreadMessenger("serviceInMessenger") {

		@Override
		protected boolean handleIncomingMessage(Message msg) {

			switch(msg.what) {
			case RemoteRobotMessenger.RPC:
				String data = msg.getData().getString("data");

				Log.d(TAG, "recv rpc: " + data);
				
				BaseCommand cmd = RoboCommands.decodeCommand(data);
				if (cmd instanceof ControlCommand) {
					Object result = RoboCommands.handleControlCommand((ControlCommand)cmd, mRobot);
					if (result != null) {
						sendReply(msg.replyTo, (ControlCommand)cmd, result);
					}
				}
				break;
			case RemoteRobotMessenger.INIT:
				mServiceOutMessengers.add(msg.replyTo);
				
				Message reply = Message.obtain(null, RemoteRobotMessenger.INIT);
				Bundle bundle = new Bundle();
				bundle.putString("robot_id", mRobot.getID());
				reply.setData(bundle);
				try {
					msg.replyTo.send(reply);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			case RemoteRobotMessenger.CLOSE:
				mServiceOutMessengers.remove(msg.replyTo);
				break;
//			case RemoteRobot.REGISTER:
//				mServiceOutMessengers
//				break;
			default:
				return false;
			}
			return true;
		}
		
	};
	
	private void sendReply(Messenger replyTo, ControlCommand cmd, Object result) {
		
		JSONObject replyJson = new JSONObject();
		try {
			replyJson.put(cmd.mCommand, result);

			Message reply = Message.obtain(null, RemoteRobotMessenger.REPLY);
			
			Bundle bundle = new Bundle();
			bundle.putString("data", replyJson.toString());
			reply.setData(bundle);

			Log.d(TAG, "send reply: " + replyJson.toString());
			
			replyTo.send(reply);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// does it need it's own thread or we don't mind if it is executed
	// by the main thread??
	private Handler mUiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Message newMessage = Message.obtain();
			newMessage.copyFrom(msg);
			
			for (Messenger messenger : mServiceOutMessengers) {
				try {
					messenger.send(newMessage);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public void destroy() {
		mRobot.destroy();
		mReceiver.destroy();
	}

	public void handleCommand(BaseCommand cmd) {
		if (cmd instanceof ControlCommand) {
			// control commands are used as RPC, the command is the
			// name of the method to be called, and the parameters are
			// given to the method in the order that they are defined.
			// works only if the type and order of the parameters is correct
			// and only for primitive types, not for objects!!
			RoboCommands.handleControlCommand((ControlCommand)cmd, mRobot);
		} else {
			if (cmd != null) {
				// Assumption: messages that arrive on this port are for this robot
				// if that is not the case, the robot id has to be added to the header
				cmd.strRobotID = mRobot.getID();
				ZmqUtils.sendCommand(cmd, m_oZmqForwarder);
			}
		}
	}

	public IBinder getBinder() {
		return mReceiver.getMessenger().getBinder();
	}

	public IBinder onBind(Intent intent) {
		return getBinder();
	}
	
}
