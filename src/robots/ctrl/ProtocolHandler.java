package robots.ctrl;

import robots.gui.BaseBluetooth;

public abstract class ProtocolHandler extends Thread {

	public interface IMessageHandler<T> {
		public void onMessage(T message);
	}
	
	protected IMessageHandler mHandler = null;
	
	protected BaseBluetooth mConnection = null;
	
	private boolean mStopped = false;
	
	public ProtocolHandler(BaseBluetooth connection, IMessageHandler handler) {
		mConnection = connection;
		mHandler = handler;
	}
	
	public void run() {
		while (mConnection.isConnected() && !mStopped) {
			execute();
		}
	}
	
	public abstract void execute();
	
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
	
	public void close() {
		mStopped = true;
	}
	
};
