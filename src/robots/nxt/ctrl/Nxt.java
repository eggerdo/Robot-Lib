package robots.nxt.ctrl;

import java.util.EnumMap;
import java.util.Timer;
import java.util.TimerTask;

import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.sensors.ZmqSensorsSender;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.gui.BluetoothConnection;
import robots.gui.MessageTypes;
import robots.gui.RobotDriveCommandListener;
import robots.nxt.MsgTypes;
import robots.nxt.MsgTypes.MotorDataRequestMsg;
import robots.nxt.MsgTypes.MotorSpeedMsg;
import robots.nxt.MsgTypes.RawDataMsg;
import robots.nxt.MsgTypes.ResetMotorPositionMsg;
import robots.nxt.MsgTypes.SensorDataRequestMsg;
import robots.nxt.MsgTypes.SensorTypeMsg;
import robots.nxt.ctrl.NxtTypes.DistanceData;
import robots.nxt.ctrl.NxtTypes.ENXTMotorID;
import robots.nxt.ctrl.NxtTypes.ENXTSensorID;
import robots.nxt.ctrl.NxtTypes.ENXTSensorType;
import robots.nxt.ctrl.NxtTypes.MotorData;
import robots.nxt.ctrl.NxtTypes.SensorData;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class Nxt extends DifferentialRobot {

	private static String TAG = "NXT";

	private NxtController m_oController;

	private double m_dblBaseSpeed = 50.0;
	
	int motorLeft;
	int motorRight;

	private NXTReceiver m_oReceiver;
	private NXTSender m_oSender;
	
	private Handler m_oSensorHandler = new Handler();

	private int m_nWaitID;
	private Object receiveEvent = this;
	private boolean m_bMessageReceived = false;

	private Timer m_oKeepAliveTimer;
	
	private int m_nInvertFactor = -1;	// normal = 1, inverted = -1

	private ZmqSensorsSender m_oSensorsSender;
	
	private RobotDriveCommandListener m_oRemoteListener;
	private ZmqRemoteControlHelper m_oRemoteHelper;

	private EnumMap<ENXTSensorID, SensorRequest> m_oSensorRequests;
	private EnumMap<ENXTMotorID, MotorRequest> m_oMotorRequests;

	private EnumMap<ENXTSensorID, Boolean> m_oSensorRequestActive; // TODO should be solved with timeouts
	private EnumMap<ENXTMotorID, Boolean> m_oMotorRequestActive; // TODO should be solved with timeouts
	
	private class NXTReceiver extends Thread {
		
		private Handler m_oHandler;
		
		public Handler getHandler() {
			return m_oHandler;
		}

		@Override
		public void run() {
		
			Looper.prepare();
			m_oHandler = new Handler() {
				
				@Override
				public void handleMessage(Message msg) {

					int messageID = msg.what;
					
					if (messageID == m_nWaitID) {
						m_bMessageReceived = true;
						synchronized(receiveEvent) {
							receiveEvent.notify();
						}
					}
					
					switch (messageID) {
					case MessageTypes.STATE_CONNECTED:
						getFirmwareVersion();
						break;

					case MessageTypes.STATE_CONNECTERROR_PAIRING:
						m_oController = null;
						break;

					case MessageTypes.STATE_RECEIVEERROR:
					case MessageTypes.STATE_SENDERROR:
						break;

					case NxtMessageTypes.DESTROY:
						m_oController = null;
					case NxtMessageTypes.FIRMWARE_VERSION:

						if (m_oController != null) {
							byte[] firmwareMessage = ((RawDataMsg)msg.obj).rgbyRawData;
						}

						break;
	
					case NxtMessageTypes.GET_INPUT_VALUES:
						
						byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
						SensorData sensorResult = NxtTypes.assembleSensorData(getID(), sensorMessage);
						m_oSensorsSender.sendSensors(sensorResult.sensorData);
						
//						SensorData oSensorData = NXTTypes.assembleSensorData(sensorMessage); 
//						msg.obj = oSensorData;
						
						ENXTSensorID sensor = ENXTSensorID.fromValue(sensorResult.nInputPort);
						m_oSensorRequestActive.put(sensor, false);
						
						return;

					case NxtMessageTypes.MOTOR_STATE:
						
						byte[] motorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
						MotorData motorResult = NxtTypes.assembleMotorData(getID(), motorMessage);
						m_oSensorsSender.sendSensors(motorResult.sensorData);
						
						ENXTMotorID motor = ENXTMotorID.fromValue(motorResult.nOutputPort);
						m_oMotorRequestActive.put(motor, false);
						
						return;
				
					}
					
					// forwards new message with same data to the ui handler
					Utils.sendMessage(m_oUiHandler, messageID, msg.obj);
				}
				
			};
			Looper.loop();
		}
		
	}
	
	// TODO: this class could be skipped, and functions directly forwarded to the controller
	private class NXTSender extends Thread {

		private Handler m_oHandler;

		public Handler getHandler() {
			return m_oHandler;
		}

		public Looper getLooper() {
			return Looper.myLooper();
		}

		@Override
		public void run() {

			Looper.prepare();
			m_oHandler = new Handler() {
				
				@Override
				public void handleMessage(Message msg) {

					if (m_oController.isConnected()) {
			            switch (msg.what) {
			                case NxtMessageTypes.GET_FIRMWARE_VERSION:
			                	m_oController.requestFirmwareVersion();
			                    break;
			                case NxtMessageTypes.SET_INPUT_MODE:
			                	SensorTypeMsg cmdSensorTypeMsg = (SensorTypeMsg)msg.obj;
			                	m_oController.setInputMode(cmdSensorTypeMsg.nPort, cmdSensorTypeMsg.byType, cmdSensorTypeMsg.byMode);
			                	break;
			                case NxtMessageTypes.GET_INPUT_VALUES:
			                	SensorDataRequestMsg cmdSensorDataRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	m_oController.requestInputValues(cmdSensorDataRequestMsg.nPort);
			                	break;
			                case NxtMessageTypes.SET_OUTPUT_STATE:
			                	MotorSpeedMsg cmdMotorSpeedMsg = (MotorSpeedMsg)msg.obj;
			                	m_oController.setMotorSpeed(cmdMotorSpeedMsg.nPort, cmdMotorSpeedMsg.nSpeed);
			                	break;
			                case NxtMessageTypes.MOTOR_STATE:
			                	MotorDataRequestMsg cmdMotorDataRequestMsg = (MotorDataRequestMsg)msg.obj;
			                	m_oController.requestMotorState(cmdMotorDataRequestMsg.nPort);
			                	break;
			                case NxtMessageTypes.RESET_MOTOR_POSITION:
			                	ResetMotorPositionMsg cmdResetMotorPosition = (ResetMotorPositionMsg)msg.obj;
			                	m_oController.resetMotorPosition(cmdResetMotorPosition.nPort, cmdResetMotorPosition.bRelative);
			                	break;
			                case NxtMessageTypes.GET_BATTERY_LEVEL:
			                	m_oController.requestBatteryLevel();
			                	break;
			                case NxtMessageTypes.GET_DISTANCE:
			                	SensorDataRequestMsg cmdDistanceRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	getDistanceSensorData(cmdDistanceRequestMsg.nPort);
			                	break;
			                case NxtMessageTypes.DISCONNECT:
			                	shutDown();
			                    break;
			                case NxtMessageTypes.KEEP_ALIVE:
			                	m_oController.keepAlive();
			                	break;
			            }
		            }
				}
			};
			Looper.loop();
			
		}
	}
	
	private final TimerTask m_oKeepAlive = new TimerTask() {
		
		@Override
		public void run() {
			if (m_oController.isConnected()) {
				keepAlive();
			}
		}
		
	};

	public Nxt() {
		super(NxtTypes.AXLE_WIDTH, NxtTypes.MIN_VELOCITY, NxtTypes.MAX_VELOCITY, NxtTypes.MIN_RADIUS, NxtTypes.MAX_RADIUS);
		
		m_oReceiver = new NXTReceiver();
		m_oReceiver.start();
		
		m_oSender = new NXTSender();
		m_oSender.start();
		
		m_oController = new NxtController();
		m_oController.setHandler(m_oReceiver.getHandler());

		m_oMotorRequests = new EnumMap<ENXTMotorID, MotorRequest>(ENXTMotorID.class);
		m_oSensorRequests = new EnumMap<ENXTSensorID, SensorRequest>(ENXTSensorID.class);

		m_oSensorRequestActive = new EnumMap<ENXTSensorID, Boolean>(ENXTSensorID.class);
		m_oMotorRequestActive = new EnumMap<NxtTypes.ENXTMotorID, Boolean>(ENXTMotorID.class);
		
		for (ENXTSensorID sensor : ENXTSensorID.values()) {
			m_oSensorRequestActive.put(sensor, false);
		}
		for (ENXTMotorID motor : ENXTMotorID.values()) {
			m_oMotorRequestActive.put(motor, false);
		}
		
		m_oKeepAliveTimer = new Timer("KeepAliveTimer");
		m_oKeepAliveTimer.schedule(m_oKeepAlive, 30000, 30000);
		
		m_oRemoteListener = new RobotDriveCommandListener(this);
		m_oRemoteHelper = new ZmqRemoteControlHelper();
		m_oRemoteHelper.setDriveControlListener(m_oRemoteListener);
		m_oRemoteHelper.startReceiver(getID());
		
		m_oSensorsSender = new ZmqSensorsSender();
		
	}
	
	public void destroy() {
		m_oKeepAliveTimer.cancel();
		disconnect();
	}

	public RobotType getType() {
		return RobotType.RBT_NXT;
	}
	
	@Override
	public String getAddress() {
		if (m_oController.getConnection() != null) {
			return m_oController.getConnection().getAddress();
		} else {
			return "";
		}
	}
	
	@Override
	public boolean isConnected() {
		return m_oController.isConnected();
	}

	@Override
	public void connect() {
		if (m_oController != null) {
			m_oController.connect();
		}
	}
	
	@Override
	public void disconnect() {
		sendCmdMessage(NxtMessageTypes.DISCONNECT);
		while (isConnected()) {
			Utils.waitSomeTime(10);
		}
	}
	
	public void setConnection(BluetoothConnection i_oConnection) {
		m_oController.setConnection(i_oConnection);
	}

	public BluetoothConnection getConnection() {
		return m_oController.getConnection();
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// nothing to do, control always enabled
	}
	
	private void sendCmdMessage(int i_nCmd) {
		sendCmdMessage(i_nCmd, null);
	}
	
	private void sendCmdMessage(int i_nCmd, Object i_oData) {
		if (isConnected()) {
			Utils.sendMessage(m_oSender.getHandler(), i_nCmd, i_oData);
		}
	}
	
	private void sendResultMessage(int i_nCmd, Object i_oData) {
		Utils.sendMessage(m_oReceiver.getHandler(), i_nCmd, i_oData);
	}

	public void getFirmwareVersion() {
		sendCmdMessage(NxtMessageTypes.GET_FIRMWARE_VERSION);
	}
	
	private class SensorRequest implements Runnable {

		ENXTSensorID mSensorID; 
		ENXTSensorType mSensorType;
		int mInterval;
		
		public SensorRequest(ENXTSensorID sensorID, ENXTSensorType sensorType, int interval) {
			mSensorID = sensorID;
			mSensorType = sensorType;
			mInterval = interval;
		}
		
		@Override
		public void run() {
			if (!isConnected()) {
				return;
			}
			
			if (mSensorType != ENXTSensorType.sensType_None) {
				if (!m_oSensorRequestActive.get(mSensorID)) {
					requestSensorData(mSensorID, mSensorType);
				}
				m_oSensorRequestActive.put(mSensorID, true);
			}
			
			if (mInterval > 0) {
				m_oSensorHandler.postDelayed(this, mInterval);
			}
		}
		
	}

	public void startSensorDataStreaming(ENXTSensorID sensorID, ENXTSensorType sensorType) {
		startSensorDataStreaming(sensorID, sensorType, 300);
	}
	
	public void startSensorDataStreaming(ENXTSensorID sensorID, ENXTSensorType sensorType, int interval) {
		SensorRequest req = new SensorRequest(sensorID, sensorType, interval);
		m_oSensorRequests.put(sensorID, req);
		m_oSensorHandler.post(req);
	}
	
	public void stopSensorDataStreaming(ENXTSensorID sensorID) {
		SensorRequest req = m_oSensorRequests.get(sensorID);
		if (req != null) {
			m_oSensorHandler.removeCallbacks(req);
		}
	}
	
	public void requestSensorData(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		if (i_eSensorType == ENXTSensorType.sensType_Distance) {
			sendCmdMessage(NxtMessageTypes.GET_DISTANCE, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		} else {
			sendCmdMessage(NxtMessageTypes.GET_INPUT_VALUES, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		}
	}
	
	public void setSensorType(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		sendCmdMessage(NxtMessageTypes.SET_INPUT_MODE, MsgTypes.assembleSensorTypeMsg(i_eSensorID, i_eSensorType));
		
		if (m_oSensorRequests.get(i_eSensorID) != null) {
			m_oSensorRequests.get(i_eSensorID).mSensorType = i_eSensorType;
			m_oSensorRequestActive.put(i_eSensorID, false);
		}
	}
	
	public void keepAlive() {
		sendCmdMessage(NxtMessageTypes.KEEP_ALIVE);
	}

	private class MotorRequest implements Runnable {

		ENXTMotorID mMotorID; 
		int mInterval;
		
		public MotorRequest(ENXTMotorID motorID, int interval) {
			mMotorID = motorID;
			mInterval = interval;
		}
		
		@Override
		public void run() {
			if (!isConnected()) {
				return;
			}
			
			if (!m_oMotorRequestActive.get(mMotorID)) {
				requestMotorData(mMotorID);
			}
			m_oMotorRequestActive.put(mMotorID, true);
			
			if (mInterval > 0) {
				m_oSensorHandler.postDelayed(this, mInterval);
			}
		}
		
	}

	public void startMotorDataStreaming(ENXTMotorID motorID) {
		startMotorDataStreaming(motorID, 300);
	}
	
	public void startMotorDataStreaming(ENXTMotorID motorID, int interval) {
		MotorRequest req = new MotorRequest(motorID, interval);
		m_oMotorRequests.put(motorID, req);
		m_oSensorHandler.post(req);
	}
	
	public void stopMotorDataStreaming(ENXTMotorID motorID) {
		MotorRequest req = m_oMotorRequests.get(motorID);
		if (req != null) {
			m_oSensorHandler.removeCallbacks(req);
		}
	}
	
	public void requestMotorData(ENXTMotorID i_eMotorID) {
		sendCmdMessage(NxtMessageTypes.MOTOR_STATE, MsgTypes.assembleMotorDataRequestMsg(i_eMotorID));
	}
	
	public void resetMotorPosition(ENXTMotorID i_eMotorID, boolean i_bRelative) {
		sendCmdMessage(NxtMessageTypes.RESET_MOTOR_POSITION, MsgTypes.assembleResetMotorPositionMsg(i_eMotorID, i_bRelative));
	}

	private void setMotorSpeed(ENXTMotorID i_eMotor, int i_nVelocity) {
		sendCmdMessage(NxtMessageTypes.SET_OUTPUT_STATE, MsgTypes.assembleMotorSpeedMsg(i_eMotor, i_nVelocity * m_nInvertFactor));
	}
	
	private void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, "left=" + i_nLeftVelocity + ", right=" + i_nRightVelocity);
		
		setMotorSpeed(ENXTMotorID.motor_1, i_nLeftVelocity);
		setMotorSpeed(ENXTMotorID.motor_2, i_nRightVelocity);
	}
	
	@Override
	public void moveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		drive(nVelocity, nVelocity);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		debug(TAG, String.format("speed=%3f, radius=%d", i_dblSpeed, i_nRadius));

		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		drive(oVelocity.left, oVelocity.right);
	}
	
	public void moveForward() {
		moveForward(m_dblBaseSpeed);
	}

	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveForward(i_dblSpeed, nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		drive(-nVelocity, -nVelocity);
	}
	
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		debug(TAG, String.format("speed=%3f, radius=%d", i_dblSpeed, i_nRadius));
		
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		drive(-oVelocity.left, -oVelocity.right);
	}

	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveBackward(i_dblSpeed, nRadius);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		drive(nVelocity, -nVelocity);
	}
	
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		drive(-nVelocity, nVelocity);
	}
	
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	Handler executor = new Handler();
	public void executeCircle(final double i_dblTime, final double i_dblSpeed) {
		executor.post(new Runnable() {
			
			@Override
			public void run() {
				rotateClockwise(i_dblSpeed);
			}
		});
		executor.post(new Runnable() {
			
			@Override
			public void run() {
				Utils.waitSomeTime((int)i_dblTime);
			}
		});
		executor.post(new Runnable() {
			
			@Override
			public void run() {
				moveStop();
			}
		});
	}
	
	int m_lCircleTime = 4000;

	@Override
	public void moveStop() {
		drive(0, 0);
	}
	
    public synchronized void getDistanceSensorData(int port) {

		try {
			byte[] data = new byte[] { 0x02, 0x42 };
			m_oController.LSWrite(port, data, 1);
			
			Utils.waitSomeTime(100);
			
			for (int i = 0; i < 3; i++) {
				m_oController.LSGetStatus(port);
				waitAnswer(NxtMessageTypes.LS_GET_STATUS, 200);
				
				byte[] reply = m_oController.getReturnMessage();
				if (reply == null || reply[2] != LCPMessage.SUCCESS) {
					Utils.waitSomeTime(500);
				} else {
					break;
				}
			};
			
			m_oController.LSRead(port);
			waitAnswer(NxtMessageTypes.LS_READ, 200);
			
			byte[] distanceMessage = m_oController.getReturnMessage();
			DistanceData distanceResult = NxtTypes.assembleDistanceData(getID(), port, distanceMessage);
			m_oSensorsSender.sendSensors(distanceResult.sensorData);

			ENXTSensorID sens = ENXTSensorID.fromValue(port);
			m_oSensorRequestActive.put(sens, false);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private boolean waitAnswer(int id, long timeout) throws InterruptedException {
		m_nWaitID = id;
		m_bMessageReceived = false;
		receiveEvent.wait(timeout);
		m_nWaitID = -1;
		return m_bMessageReceived;
	}

	public void shutDown() {
		// turn off input ports
		for (ENXTSensorID eSensor : ENXTSensorID.values()) {
			setSensorType(eSensor, ENXTSensorType.sensType_None);
		}

		if (m_oController != null) {
	        // send stop messages to motors
	    	m_oController.setMotorSpeed(NxtTypes.MOTOR_A, 0);
	    	m_oController.setMotorSpeed(NxtTypes.MOTOR_B, 0);
	    	m_oController.setMotorSpeed(NxtTypes.MOTOR_C, 0);
	    	
	        Utils.waitSomeTime(500);
        	// destroy connection
        	m_oController.disconnect();
		}
	}

	public void setInverted() {
		m_nInvertFactor *= -1;
	}
	
	public boolean isInverted() {
		return m_nInvertFactor == -1;
	}

	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSpeed() {
		// TODO Auto-generated method stub
		return m_dblBaseSpeed;
	}

	@Override
	public void moveLeft() {
		// not available
	}

	@Override
	public void moveRight() {
		// not available
	}

	@Override
	public boolean toggleInvertDrive() {
		return false;
	}

}
