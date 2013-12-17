package robots.robo40.ctrl;

import java.io.IOException;

import org.dobots.utilities.Utils;
import org.dobots.utilities.log.Loggable;

import robots.ctrl.comm.AsciiProtocolHandler;
import robots.ctrl.comm.AsciiProtocolHandler.IAsciiMessageHandler;
import robots.gui.comm.IRobotConnection;
import robots.nxt.MsgTypes;
import android.os.Handler;

public class Robo40Controller extends Loggable implements IAsciiMessageHandler {
	
	public static final String TAG = "Robo40Controller";
	
	private IRobotConnection m_oConnection;
	
	private Handler mUiHandler;

	private AsciiProtocolHandler mProtocolHandler;
	
	public void setHandler(Handler handler) {
		mUiHandler = handler;
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
	
	public boolean isConnected() {
		if (m_oConnection != null) {
			return m_oConnection.isConnected();
		} else
			return false;
	}
	
	public void connect() {
		// connection will be done on thread startup
		m_oConnection.open();
		mProtocolHandler.start();
	}
	
	private void send(byte[] message) {
		if (isConnected()) {
			m_oConnection.send(message);
		}
	}
	
	public void disconnect() {
		byte[] message = Robo40Types.getDisconnectPackage();
		send(message);
		destroyConnection();
	}
		
	public void control(boolean i_bEnable) {
		debug(TAG, "control(%b)", i_bEnable);
		byte[] message = Robo40Types.getControlCommandPackage(i_bEnable);
		send(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, "drive(%d, %d)", i_nLeftVelocity, i_nRightVelocity);
		byte[] message = Robo40Types.getDriveCommandPackage(i_nLeftVelocity, i_nRightVelocity);
		send(message);
	}
	
	public void driveStop() {
		debug(TAG, "driveStop()");
		byte[] message = Robo40Types.getDriveCommandPackage(0, 0);
		send(message);
	}

	//	
//	public void requestSensorData() {
//		byte[] message = Robo40Types.getDataRequestPackage();
//		send(message);
//	}
//	
//	public void startStreaming(int i_nInterval) {
//		byte[] message = Robo40Types.getStreamingONPackage(i_nInterval);
//		send(message);
//	}
//	
//	public void stopStreaming() {
//		byte[] message = Robo40Types.getStreamingOFFPackage();
//		send(message);
//	}

	public void setMotor(int id, int direction, int value) {
		debug(TAG, "setMotor(%d, %d, %d)", id, direction, value);
		byte[] message = Robo40Types.getMotorCommandPackage(id, direction, value);
		send(message);
	}

	@Override
	public void onMessage(String message) {
		Utils.sendMessage(mUiHandler, Robo40Types.SENSOR_DATA, MsgTypes.assembleRawDataMsg(message.getBytes()));
	}
	
}
