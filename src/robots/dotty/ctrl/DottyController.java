package robots.dotty.ctrl;

import java.io.IOException;

import org.dobots.utilities.Utils;
import org.dobots.utilities.log.Loggable;

import robots.ctrl.comm.ProtocolHandler;
import robots.ctrl.comm.ProtocolHandler.ICommHandler;
import robots.gui.comm.bluetooth.BluetoothConnection;
import robots.nxt.MsgTypes;
import android.os.Handler;

public class DottyController extends Loggable implements ICommHandler<byte[]> {
	
	private static final String TAG = "DottyController";
	
	private BluetoothConnection m_oConnection;

	private Handler mUiHandler;

	private class DottyProtocolHandler extends ProtocolHandler {

		public DottyProtocolHandler(BluetoothConnection connection, ICommHandler handler) {
			super(connection, handler);
		}

		@Override
		public void execute() {
			byte[] returnMessage = receiveMessage();
			if (returnMessage != null) {
				if (mMessageHandler != null) {
					mMessageHandler.onMessage(returnMessage);
				}
			}
		}
		
		protected byte[] receiveMessage() {
			int nHeader = mConnection.read();
			byte[] receiveMessage;
			int nBytesExpected;
			int nReceivedBytes;
			
			if (nHeader == 0xa5) {
				receiveMessage = new byte[DottyTypes.DATA_PKG_SIZE];

				nBytesExpected = receiveMessage.length;
				nReceivedBytes = 1;
				
			} else if (nHeader == 0xa6) {
				nBytesExpected = mConnection.read() + 2; // + 1 for header + 1 for length byte
				nReceivedBytes = 2;
				receiveMessage = new byte[nBytesExpected];
				
				receiveMessage[1] = (byte)(nBytesExpected - 2);
				
			} else {
				return null;
			}

			receiveMessage[0] = (byte)nHeader;
			while (nReceivedBytes != nBytesExpected) { 
				int nReadBytes = mConnection.read(receiveMessage, nReceivedBytes, nBytesExpected - nReceivedBytes);
				if (nReadBytes == -1) {
					return null;
				} else {
					nReceivedBytes += nReadBytes;
				}
			}
			return receiveMessage;
			
		}

		@Override
		public void shutDown() {
			// TODO Auto-generated method stub
			
		}
	}
	
	private DottyProtocolHandler mProtocolHandler;

	public void setHandler(Handler handler) {
		mUiHandler = handler;
	}
	
	public void setConnection(BluetoothConnection i_oConnection) {
		m_oConnection = i_oConnection;
		mProtocolHandler = new DottyProtocolHandler(m_oConnection, this);
	}
	
	public BluetoothConnection getConnection() {
		return m_oConnection;
	}
	
	public void destroyConnection() {
		if (mProtocolHandler != null) {
			mProtocolHandler.destroy();
			mProtocolHandler = null;
		}
		
		if (m_oConnection != null) {
			try {
				m_oConnection.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_oConnection = null;
	}
	
	public boolean isConnected() {
		if (m_oConnection != null) {
			return m_oConnection.isConnected();
		} else
			return false;
	}
	
	public void connect() {
		debug(TAG, "connect...");
		m_oConnection.open();
		mProtocolHandler.start();
	}
	
	public void disconnect() {
		debug(TAG, "disconnect...");
		byte[] message = DottyTypes.getDisconnectPackage();
		send(message);
		destroyConnection();
	}
	
	public void control(boolean i_bEnable) {
		debug(TAG, "control(%b)", i_bEnable);
		byte[] message = DottyTypes.getControlPackage(i_bEnable);
		send(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, "drive(%d,%d)", i_nLeftVelocity, i_nRightVelocity);
		byte[] message = DottyTypes.getDrivePackage(i_nLeftVelocity, i_nRightVelocity);
		send(message);
	}
	
	public void driveStop() {
		debug(TAG, "driveStop()");
		byte[] message = DottyTypes.getDriveStopPackage();
		send(message);
	}
	
	public void requestSensorData() {
		debug(TAG, "requestSensorData()");
		byte[] message = DottyTypes.getDataRequestPackage();
		send(message);
	}
	
	public void startStreaming(int i_nInterval) {
		debug(TAG, "startStreaming(%d)", i_nInterval);
		byte[] message = DottyTypes.getStreamingONPackage(i_nInterval);
		send(message);
	}
	
	public void stopStreaming() {
		debug(TAG, "stopStreaming()");
		byte[] message = DottyTypes.getStreamingOFFPackage();
		send(message);
	}
	
	private void send(byte[] message) {
		if (isConnected()) {
			m_oConnection.send(message);
		}
	}

	@Override
	public void onMessage(byte[] message) {
		switch (message[0]) {
    	case DottyTypes.HEADER:
    		switch (message[3]) {
            case DottyTypes.SENSOR_DATA:
            	sendStateAndData(DottyTypes.SENSOR_DATA, message);
            	break;
            }
    		break;
    	case DottyTypes.LOGGING:
    		sendStateAndData(DottyTypes.LOGGING, message);
        	break;
    	}
        
    }

    private void sendStateAndData(int i_nCmd, byte[] i_rgbyData) {
    	Utils.sendMessage(mUiHandler, i_nCmd, MsgTypes.assembleRawDataMsg(i_rgbyData));
    }
	
}
