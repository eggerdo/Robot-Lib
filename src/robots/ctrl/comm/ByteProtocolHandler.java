package robots.ctrl.comm;

import java.util.Arrays;

import org.dobots.utilities.DoBotsThread;

import robots.gui.comm.IRobotConnection;

public class ByteProtocolHandler extends DoBotsThread implements IProtocolHandler {

	public interface IByteMessageHandler {
		public void onMessage(byte[] buffer);
	}
	
	private IByteMessageHandler mHandler = null;
	
	private IRobotConnection mConnection = null;
	
	public ByteProtocolHandler(IRobotConnection connection, IByteMessageHandler handler) {
		super("AsciiProtocolHandler");
		mConnection = connection;
		mHandler = handler;
	}
	
	public byte[] receiveMessage() {
		if (mConnection.isDataAvailable()) {
			byte[] buffer = new byte[256];
			int length = mConnection.read(buffer, 0, 256);
			
			return Arrays.copyOf(buffer, length);
		}
		return null;
	}
	
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void execute() {
		if (mConnection.isConnected()) {
			byte[] buffer = receiveMessage();
			if (buffer != null) {
				if (mHandler != null) {
					mHandler.onMessage(buffer);
				}
			}
		}
	}
	
};
