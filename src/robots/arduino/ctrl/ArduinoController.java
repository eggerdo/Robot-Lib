package robots.arduino.ctrl;

import java.io.IOException;

import org.dobots.comm.msg.ISensorDataListener;
import org.dobots.utilities.log.Loggable;
import org.json.JSONException;
import org.json.JSONObject;

import robots.ctrl.comm.AsciiProtocolHandler;
import robots.ctrl.comm.AsciiProtocolHandler.IAsciiMessageHandler;
import robots.gui.comm.IRobotConnection;
import android.os.Handler;
import android.util.Log;

public class ArduinoController extends Loggable implements IAsciiMessageHandler {

	private static final String TAG = "ArduinoController";

	private IRobotConnection m_oConnection;
	private AsciiProtocolHandler mProtocolHandler;

	private Handler mHandler;
	
	private ISensorDataListener mListener;
	
	public void setSensorListener(ISensorDataListener listener) {
		mListener = listener;
	}

	public void setConnection(IRobotConnection i_oConnection) {
		m_oConnection = i_oConnection;
		mProtocolHandler = new AsciiProtocolHandler(i_oConnection, this);
		Log.d(TAG, String.format("setConnection(%s)", m_oConnection.getAddress()));
	}

	public IRobotConnection getConnection() {
		return m_oConnection;
	}

	public void destroyConnection() {
		Log.d(TAG, "destroyConnection");
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
		} else {
			return false;
		}
	}

	public void connect() {
		if (m_oConnection != null) {
			Log.d(TAG, "connect...");
			m_oConnection.open();
			mProtocolHandler.start();
		}
	}

	public void disconnect() {
		if (m_oConnection != null) {
			Log.d(TAG, "disconnect...");
			byte[] message = ArduinoTypes.getDisconnectPackage();
			m_oConnection.send(message);
		}
		destroyConnection();
	}

	public void control(boolean i_bEnable) {
		if (m_oConnection != null) {
			byte[] message = ArduinoTypes.getControlCommandPackage(i_bEnable);
			m_oConnection.send(message);
		}
	}

	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		if (m_oConnection != null) {
			debug(TAG, String.format("drive(%d, %d)", i_nLeftVelocity, i_nRightVelocity));
			byte[] message = ArduinoTypes.getDriveCommandPackage(i_nLeftVelocity, i_nRightVelocity);
			m_oConnection.send(message);
		}
	}

	public void driveStop() {
		if (m_oConnection != null) {
			debug(TAG, "driveStop()");
			byte[] message = ArduinoTypes.getDriveCommandPackage(0, 0);
			m_oConnection.send(message);
		}
	}

	public void requestSensorData() {
		if (m_oConnection != null) {
			debug(TAG, "requestSensorData");
			byte[] message = ArduinoTypes.getSensorRequestPackage();
			m_oConnection.send(message);
		}
	}

	@Override
	public void onMessage(String message) {
		try {
			JSONObject json = new JSONObject(message);
			switch(ArduinoTypes.getType(json)) {
			case ArduinoTypes.SENSOR_DATA:
				if (mListener != null) {
					mListener.onSensorData(json.get("data").toString());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

}
