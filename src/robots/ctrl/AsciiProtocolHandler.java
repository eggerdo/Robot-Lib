package robots.ctrl;

import org.dobots.utilities.DoBotsThread;

import robots.gui.BluetoothConnection;

public class AsciiProtocolHandler extends DoBotsThread {

	public interface IAsciiMessageHandler {
		public void onMessage(String message);
	}
	
	private IAsciiMessageHandler mHandler = null;
	
	private BluetoothConnection mConnection = null;
	
	public AsciiProtocolHandler(BluetoothConnection connection, IAsciiMessageHandler handler) {
		super("AsciiProtocolHandler");
		mConnection = connection;
		mHandler = handler;
	}
	
	public String receiveMessage() {
		if (mConnection.isDataAvailable()) {
			String message = mConnection.readLine();
			return message;
		}
		return null;
	}
	
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void execute() {
		String message = receiveMessage();
		if (message != null) {
			if (mHandler != null) {
				mHandler.onMessage(message);
			}
		}
	}
	
};
