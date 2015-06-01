package robots.zumo.ctrl;

import java.io.IOException;

import org.dobots.comm.msg.ISensorDataListener;
import org.dobots.utilities.log.Loggable;

import robots.ctrl.comm.ByteProtocolHandler;
import robots.ctrl.comm.ByteProtocolHandler.IByteMessageHandler;
import robots.ctrl.comm.IProtocolHandler;
import robots.gui.comm.IRobotConnection;

public class ZumoController extends Loggable implements IByteMessageHandler {
	
	private static final String TAG = "ZumoController";
	
	private IRobotConnection m_oConnection;

	private IProtocolHandler mProtocolHandler;
	
	private boolean mSimulated = false;

//	private ISensorDataListener mListener;
	
	ZumoEncoder mEncoder;
	
	public ZumoController() {
		mEncoder = new ZumoEncoder();
	}
	
//	public void setSensorListener(ISensorDataListener listener) {
//		mListener = listener;
//	}

	public void setConnection(IRobotConnection i_oConnection) {
		m_oConnection = i_oConnection;
		mProtocolHandler = new ByteProtocolHandler(i_oConnection, this);
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
		
		byte[] message = mEncoder.getDisconnectPackage();
		send(message);
		destroyConnection();
	}

	public void control(boolean i_bEnable) {
		byte[] message = mEncoder.getControlCommandPackage(i_bEnable);
		send(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, String.format("drive(%d, %d)", i_nLeftVelocity, i_nRightVelocity));
		byte[] message = mEncoder.getDriveCommandPackage(i_nLeftVelocity, i_nRightVelocity);
		send(message);
	}
	
	public void driveStop() {
		debug(TAG, "driveStop()");
		byte[] message = mEncoder.getDriveCommandPackage(0, 0);
		send(message);
	}

//	@Override
//	public void onMessage(String message) {
//		try {
//			JSONObject json = new JSONObject(message);
//			switch(mEncoder.getType(json)) {
//			case ZumoEncoder.HIT_DETECTED:
//				if (mListener != null) {
//					mListener.onSensorData(message);
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
	
	@Override
	public void onMessage(byte[] buffer) {
		switch(ZumoEncoder.getType(buffer)) {
		default:
			// nada
		}
	}

	public void initMazeSolver() {
		debug(TAG, "initMazeSolver");
		byte[] message = mEncoder.getSimplePackage(ZumoTypes.INIT_MAZE);
		send(message);
	}

	public void startMazeSolving() {
		debug(TAG, "startMazeSolving");
		byte[] message = mEncoder.getSimplePackage(ZumoTypes.START_MAZE);
		send(message);
	}

	public void stopMazeSolving() {
		debug(TAG, "stopMazeSolving");
		byte[] message = mEncoder.getSimplePackage(ZumoTypes.STOP_MAZE);
		send(message);
	}

	public void repeatMaze() {
		debug(TAG, "repeatMaze");
		byte[] message = mEncoder.getSimplePackage(ZumoTypes.REPEAT_MAZE);
		send(message);
	}

	public void calibrateCompass() {
		debug(TAG, "calibrateCompass");
		byte[] message = mEncoder.getSimplePackage(ZumoTypes.CALIBRATE_COMPSS);
		send(message);
	}

	public void resetHeading() {
		debug(TAG, "resetHeading");
		byte[] message = mEncoder.getSimplePackage(ZumoTypes.RESET_HEADING);
		send(message);
	}

	public void turnDegrees(int angle) {
		debug(TAG, "ResetHeading");
		byte[] message = mEncoder.getTurnDegreespackage(angle);
		send(message);
	}
	
	

//	public void shootGuns() {
//		debug(TAG, "shootGuns");
//		byte[] message = mEncoder.getSimpleCommandPackage(ZumoEncoder.SHOOT_GUNS);
//		send(message);
//	}

//	public void fireVolley() {
//		debug(TAG, "fireVolley");
//		byte[] message = mEncoder.getSimpleCommandPackage(ZumoEncoder.FIRE_VOLLEY);
//		send(message);
//	}

//	public void dock(boolean isDocking) {
//		debug(TAG, "dock(%b)", isDocking);
//		byte[] message = mEncoder.getDockingCommandPackage(isDocking);
//		send(message);
//	}

//	public void requestSensorData() {
//		byte[] message = ZumoTypes.getSensorRequestPackage();
//		sendMessage(message);
//	}
	
//	public void startStreaming(int i_nInterval) {
//		byte[] message = ZumoTypes.getStreamingONPackage(i_nInterval);
//		sendMessage(message);
//	}
	
//	public void stopStreaming() {
//		byte[] message = ZumoTypes.getStreamingOFFPackage();
//		sendMessage(message);
//	}
	
	
	
}
