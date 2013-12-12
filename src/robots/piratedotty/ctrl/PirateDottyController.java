package robots.piratedotty.ctrl;

import java.io.IOException;

import org.dobots.utilities.log.Loggable;

import robots.ctrl.comm.AsciiProtocolHandler;
import robots.ctrl.comm.AsciiProtocolHandler.IAsciiMessageHandler;
import robots.gui.comm.IRobotConnection;

public class PirateDottyController extends Loggable implements IAsciiMessageHandler {
	
	private static final String TAG = "PirateDottyController";
	
	private IRobotConnection m_oConnection;

	private AsciiProtocolHandler mProtocolHandler;
	
	public void setConnection(IRobotConnection i_oConnection) {
		m_oConnection = i_oConnection;
		mProtocolHandler = new AsciiProtocolHandler(i_oConnection, this);
	}
	
	public IRobotConnection getConnection() {
		return m_oConnection;
	}
	
	public void destroyConnection() {
		if (mProtocolHandler != null) {
			mProtocolHandler.close();
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
		} else {
			return false;
		}
	}
	
	public void connect() {
		m_oConnection.open();
		mProtocolHandler.start();
	}
	
	public void disconnect() {
		byte[] message = PirateDottyTypes.getDisconnectPackage();
		m_oConnection.send(message);
		destroyConnection();
	}

	public void control(boolean i_bEnable) {
		byte[] message = PirateDottyTypes.getControlCommandPackage(i_bEnable);
		m_oConnection.send(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, String.format("drive(%d, %d)", i_nLeftVelocity, i_nRightVelocity));
		byte[] message = PirateDottyTypes.getDriveCommandPackage(i_nLeftVelocity, i_nRightVelocity);
		m_oConnection.send(message);
	}
	
	public void driveStop() {
		debug(TAG, "driveStop()");
		byte[] message = PirateDottyTypes.getDriveCommandPackage(0, 0);
		m_oConnection.send(message);
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
	}

//	public void requestSensorData() {
//		byte[] message = PirateDottyTypes.getSensorRequestPackage();
//		m_oConnection.sendMessage(message);
//	}
	
//	public void startStreaming(int i_nInterval) {
//		byte[] message = PirateDottyTypes.getStreamingONPackage(i_nInterval);
//		m_oConnection.sendMessage(message);
//	}
	
//	public void stopStreaming() {
//		byte[] message = PirateDottyTypes.getStreamingOFFPackage();
//		m_oConnection.sendMessage(message);
//	}
	
}
