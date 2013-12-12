package robots.ctrl.comm;

import org.dobots.utilities.DoBotsThread;

import robots.gui.comm.IRobotConnection;

public abstract class ProtocolHandler extends DoBotsThread {

	public interface ICommHandler<T> {
		public void onMessage(T message);
	}
	
	protected ICommHandler mMessageHandler = null;
	
	protected IRobotConnection mConnection = null;
	
//	private boolean mStopped = false;
	
	public ProtocolHandler(IRobotConnection connection, ICommHandler handler) {
		super("ProtocolHandler");
		mConnection = connection;
		mMessageHandler = handler;
	}
	
//	public void run() {
//		while (!mStopped) {
//			if (mConnection.isConnected()) {
//				execute();
//			}
//		}
//	}
	
//	public abstract void execute();
	
//	public void run() {
//		while (mConnection.isConnected() && !mStopped) {
//			String message = receiveMessage();
//			if (message != null) {
//				if (mHandler != null) {
//					mHandler.onMessage(message);
//				}
//			}
//		}
//	}
	
//	public String receiveMessage() {
//		if (mConnection.isDataAvailable()) {
//			String message = mConnection.readLine();
//			return message;
//		}
//		return null;
//	}
	
//	public void close() {
//		mStopped = true;
//	}
	
};
