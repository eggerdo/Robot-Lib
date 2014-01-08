package robots.remote;

import org.dobots.comm.msg.RoboCommands;
import org.dobots.comm.msg.RoboCommands.BaseCommand;
import org.dobots.comm.msg.RoboCommands.ControlCommand;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.ZmqUtils;
import org.zeromq.ZMQ.Socket;

import robots.ctrl.IRobotDevice;
import android.os.Binder;
import android.os.IBinder;

public class RobotServiceBinder {
	
	private final IBinder mBinder = new RobotBinder();
	
	public class RobotBinder extends Binder {
		
		public IRobotDevice getRobot() {
			return mRobot;
		}
	}
	
	IRobotDevice mRobot;
	private Socket m_oZmqForwarder;
	
	public RobotServiceBinder(IRobotDevice robot) {
		mRobot = robot;
		m_oZmqForwarder = ZmqHandler.getInstance().obtainCommandSendSocket();
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
				ZmqUtils.send(mRobot.getID(), cmd.toJSONString(), m_oZmqForwarder);
			}
		}
	}

	public IBinder getBinder() {
		return mBinder;
	}

	public void destroy() {
		mRobot.destroy();
	}

}
