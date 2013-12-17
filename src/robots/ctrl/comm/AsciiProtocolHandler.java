package robots.ctrl.comm;

import robots.gui.comm.IRobotConnection;

public class AsciiProtocolHandler extends DoBotsThread {

	public interface IAsciiMessageHandler {
		public void onMessage(String message);
	}
	
	private IAsciiMessageHandler mHandler = null;
	
	private IRobotConnection mConnection = null;
	
	public AsciiProtocolHandler(IRobotConnection connection, IAsciiMessageHandler handler) {
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
		if (mConnection.isConnected) {
			String message = receiveMessage();
			if (message != null) {
				if (mHandler != null) {
					mHandler.onMessage(message);
				}
			}
		}
	}
	
};
