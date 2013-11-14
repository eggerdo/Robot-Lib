package robots.ctrl;

import robots.gui.BaseBluetooth;

public class AsciiProtocolHandler extends Thread {

	public interface IAsciiMessageHandler {
		public void onMessage(String message);
	}
	
	private IAsciiMessageHandler mHandler = null;
	
	private BaseBluetooth mConnection = null;
	
	private boolean mStopped = false;
	
	public AsciiProtocolHandler(BaseBluetooth connection, IAsciiMessageHandler handler) {
		mConnection = connection;
		mHandler = handler;
	}
	
	public void run() {
		while (mConnection.isConnected() && !mStopped) {
			String message = receiveMessage();
			if (message != null) {
				if (mHandler != null) {
					mHandler.onMessage(message);
				}
			}
		}
	}
	
	public String receiveMessage() {
		if (mConnection.isDataAvailable()) {
			String message = mConnection.readLine();
			return message;
		}
		return null;
	}
	
	public void close() {
		mStopped = true;
	}
	
};
