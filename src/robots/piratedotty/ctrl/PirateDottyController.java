package robots.piratedotty.ctrl;

import java.io.IOException;

import org.dobots.comm.msg.ISensorDataListener;
import org.dobots.utilities.log.Loggable;
import org.json.JSONException;
import org.json.JSONObject;

import robots.arduino.ctrl.ArduinoTypes;
import robots.ctrl.comm.AsciiProtocolHandler;
import robots.ctrl.comm.AsciiProtocolHandler.IAsciiMessageHandler;
import robots.gui.comm.IRobotConnection;

public class PirateDottyController extends Loggable implements IAsciiMessageHandler {
	
	private static final String TAG = "PirateDottyController";
	
	private IRobotConnection m_oConnection;

	private AsciiProtocolHandler mProtocolHandler;
	
	private boolean mSimulated = false;

	private ISensorDataListener mListener;
	
	public void setSensorListener(ISensorDataListener listener) {
		mListener = listener;
	}

	public void setConnection(IRobotConnection i_oConnection) {
		m_oConnection = i_oConnection;
		mProtocolHandler = new AsciiProtocolHandler(i_oConnection, this);
	}
	
	public IRobotConnection getConnection() {
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
	
	private void send(byte[] message) {
		if (m_oConnection != null) {
			m_oConnection.send(message);
		}
	}
	
	public boolean isConnected() {
		if (mSimulated) return true;
		
		if (m_oConnection != null) {
			return m_oConnection.isConnected();
		} else {
			return false;
		}
	}
	
	public void connect() {
		if (mSimulated) return;
		
		m_oConnection.open();
		mProtocolHandler.start();
	}
	
	public void disconnect() {
		if (mSimulated) return;
		
		byte[] message = PirateDottyTypes.getDisconnectPackage();
		send(message);
		destroyConnection();
	}

	public void control(boolean i_bEnable) {
		byte[] message = PirateDottyTypes.getControlCommandPackage(i_bEnable);
		send(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, String.format("drive(%d, %d)", i_nLeftVelocity, i_nRightVelocity));
		byte[] message = PirateDottyTypes.getDriveCommandPackage(i_nLeftVelocity, i_nRightVelocity);
		send(message);
	}
	
	public void driveStop() {
		debug(TAG, "driveStop()");
		byte[] message = PirateDottyTypes.getDriveCommandPackage(0, 0);
		send(message);
	}

	@Override
	public void onMessage(String message) {
		try {
			JSONObject json = new JSONObject(message);
			switch(PirateDottyTypes.getType(json)) {
			case PirateDottyTypes.HIT_DETECTED:
				if (mListener != null) {
					mListener.onSensorData(message);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void shootGuns() {
		debug(TAG, "shootGuns");
		byte[] message = PirateDottyTypes.getSimpleCommandPackage(PirateDottyTypes.SHOOT_GUNS);
		send(message);
	}

	public void fireVolley() {
		debug(TAG, "fireVolley");
		byte[] message = PirateDottyTypes.getSimpleCommandPackage(PirateDottyTypes.FIRE_VOLLEY);
		send(message);
	}

	public void dock(boolean isDocking) {
		debug(TAG, "dock(%b)", isDocking);
		byte[] message = PirateDottyTypes.getDockingCommandPackage(isDocking);
		send(message);
	}

//	public void requestSensorData() {
//		byte[] message = PirateDottyTypes.getSensorRequestPackage();
//		sendMessage(message);
//	}
	
//	public void startStreaming(int i_nInterval) {
//		byte[] message = PirateDottyTypes.getStreamingONPackage(i_nInterval);
//		sendMessage(message);
//	}
	
//	public void stopStreaming() {
//		byte[] message = PirateDottyTypes.getStreamingOFFPackage();
//		sendMessage(message);
//	}
	
	
	
}
