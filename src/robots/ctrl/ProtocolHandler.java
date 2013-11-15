package robots.ctrl;

import robots.gui.BluetoothConnection;

public abstract class ProtocolHandler extends Thread {

	public interface ICommHandler<T> {
		public void onMessage(T message);
	}
	
	protected ICommHandler mMessageHandler = null;
	
	protected BluetoothConnection mConnection = null;
	
	private boolean mStopped = false;
	
	public ProtocolHandler(BluetoothConnection connection, ICommHandler handler) {
		mConnection = connection;
		mMessageHandler = handler;
	}
	
	public void run() {
		while (!mStopped) {
			if (mConnection.isConnected()) {
				execute();
			}
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
