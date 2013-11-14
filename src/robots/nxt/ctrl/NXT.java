package robots.nxt.ctrl;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Timer;
import java.util.TimerTask;

import org.dobots.communication.control.ZmqRemoteControlHelper;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.gui.MessageTypes;
import robots.gui.RobotDriveCommandListener;
import robots.nxt.MsgTypes;
import robots.nxt.MsgTypes.MotorDataRequestMsg;
import robots.nxt.MsgTypes.MotorSpeedMsg;
import robots.nxt.MsgTypes.RawDataMsg;
import robots.nxt.MsgTypes.ResetMotorPositionMsg;
import robots.nxt.MsgTypes.SensorDataRequestMsg;
import robots.nxt.MsgTypes.SensorTypeMsg;
import robots.nxt.ctrl.NXTTypes.DistanceData;
import robots.nxt.ctrl.NXTTypes.ENXTMotorID;
import robots.nxt.ctrl.NXTTypes.ENXTMotorSensorType;
import robots.nxt.ctrl.NXTTypes.ENXTSensorID;
import robots.nxt.ctrl.NXTTypes.ENXTSensorType;
import robots.nxt.ctrl.NXTTypes.MotorData;
import robots.nxt.ctrl.NXTTypes.SensorData;
import robots.nxt.gui.NXTBluetooth;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class NXT extends DifferentialRobot {

	private static String TAG = "NXT";

	private NXTBluetooth m_oConnection;

	private double m_dblBaseSpeed = 50.0;
	
	int motorLeft;
	int motorRight;

	private boolean connected = false;

	private NXTReceiver m_oReceiver;
	private NXTSender m_oSender;
	
	private Handler m_oSensorHandler = new Handler();

	private int m_nWaitID;
	private Object receiveEvent = this;
	private boolean m_bMessageReceived = false;

	private Timer m_oKeepAliveTimer;
	
	private int m_nInvertFactor = -1;	// normal = 1, inverted = -1
	
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
						connected = true;
						getFirmwareVersion();
						break;

					case MessageTypes.STATE_CONNECTERROR_PAIRING:
						m_oConnection = null;
						break;

					case MessageTypes.STATE_RECEIVEERROR:
					case MessageTypes.STATE_SENDERROR:
						connected = false;
						break;

					case NXTMessageTypes.DESTROY:
						m_oConnection = null;
					case NXTMessageTypes.FIRMWARE_VERSION:

						if (m_oConnection != null) {
							byte[] firmwareMessage = ((RawDataMsg)msg.obj).rgbyRawData;
						}

						break;
	
					case NXTMessageTypes.GET_INPUT_VALUES:
						
						byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
						SensorData oSensorData = NXTTypes.assembleSensorData(sensorMessage); 
						msg.obj = oSensorData;
						
						ENXTSensorID sensor = ENXTSensorID.fromValue(oSensorData.nInputPort);
						m_oSensorRequestActive.put(sensor, false);
						
						break;

					case NXTMessageTypes.MOTOR_STATE:
						
						byte[] motorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
						MotorData oMotorData = NXTTypes.assembleMotorData(motorMessage);
						msg.obj = oMotorData;
						
						ENXTMotorID motor = ENXTMotorID.fromValue(oMotorData.nOutputPort);
						m_oMotorRequestActive.put(motor, false);
						
						break;
				
					case NXTMessageTypes.GET_DISTANCE:

						DistanceData oDistanceData = (DistanceData) msg.obj;
						ENXTSensorID sens = ENXTSensorID.fromValue(oDistanceData.nInputPort);
						m_oSensorRequestActive.put(sens, false);
						
						break;
						
					}

					
					// forwards new message with same data to the ui handler
					Utils.sendMessage(m_oUiHandler, messageID, msg.obj);
				}
				
			};
			Looper.loop();
		}
		
	}
	
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

					if (connected) {
			            switch (msg.what) {
			                case NXTMessageTypes.GET_FIRMWARE_VERSION:
			                	m_oConnection.requestFirmwareVersion();
			                    break;
			                case NXTMessageTypes.SET_INPUT_MODE:
			                	SensorTypeMsg cmdSensorTypeMsg = (SensorTypeMsg)msg.obj;
			                	m_oConnection.setInputMode(cmdSensorTypeMsg.nPort, cmdSensorTypeMsg.byType, cmdSensorTypeMsg.byMode);
			                	break;
			                case NXTMessageTypes.GET_INPUT_VALUES:
			                	SensorDataRequestMsg cmdSensorDataRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	m_oConnection.requestInputValues(cmdSensorDataRequestMsg.nPort);
			                	break;
			                case NXTMessageTypes.SET_OUTPUT_STATE:
			                	MotorSpeedMsg cmdMotorSpeedMsg = (MotorSpeedMsg)msg.obj;
			                	m_oConnection.setMotorSpeed(cmdMotorSpeedMsg.nPort, cmdMotorSpeedMsg.nSpeed);
			                	break;
			                case NXTMessageTypes.MOTOR_STATE:
			                	MotorDataRequestMsg cmdMotorDataRequestMsg = (MotorDataRequestMsg)msg.obj;
			                	m_oConnection.requestMotorState(cmdMotorDataRequestMsg.nPort);
			                	break;
			                case NXTMessageTypes.RESET_MOTOR_POSITION:
			                	ResetMotorPositionMsg cmdResetMotorPosition = (ResetMotorPositionMsg)msg.obj;
			                	m_oConnection.resetMotorPosition(cmdResetMotorPosition.nPort, cmdResetMotorPosition.bRelative);
			                	break;
			                case NXTMessageTypes.GET_BATTERY_LEVEL:
			                	m_oConnection.requestBatteryLevel();
			                	break;
			                case NXTMessageTypes.GET_DISTANCE:
			                	SensorDataRequestMsg cmdDistanceRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	getDistanceSensorData(cmdDistanceRequestMsg.nPort);
			                	break;
			                case NXTMessageTypes.DISCONNECT:
			                	shutDown();
			                    break;
			                case NXTMessageTypes.KEEP_ALIVE:
			                	m_oConnection.keepAlive();
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
			if (connected) {
				keepAlive();
			}
		}
		
	};

	public NXT() {
		super(NXTTypes.AXLE_WIDTH, NXTTypes.MIN_VELOCITY, NXTTypes.MAX_VELOCITY, NXTTypes.MIN_RADIUS, NXTTypes.MAX_RADIUS);
		
		m_oReceiver = new NXTReceiver();
		m_oReceiver.start();
		
		m_oSender = new NXTSender();
		m_oSender.start();

		m_oMotorRequests = new EnumMap<ENXTMotorID, MotorRequest>(ENXTMotorID.class);
		m_oSensorRequests = new EnumMap<ENXTSensorID, SensorRequest>(ENXTSensorID.class);

		m_oSensorRequestActive = new EnumMap<ENXTSensorID, Boolean>(ENXTSensorID.class);
		m_oMotorRequestActive = new EnumMap<NXTTypes.ENXTMotorID, Boolean>(ENXTMotorID.class);
		
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
		
	}
	
	public void destroy() {
		m_oKeepAliveTimer.cancel();
		disconnect();
	}

	public RobotType getType() {
		return RobotType.RBT_NXT;
	}
	
	public String getAddress() {
		if (m_oConnection != null) {
			return m_oConnection.getAddress();
		} else {
			return "";
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void connect() {
		connected = false;       
		if (m_oConnection != null) {
			m_oConnection.open();
		}
	}
	
	@Override
	public void disconnect() {
		sendCmdMessage(NXTMessageTypes.DISCONNECT);
		while (isConnected()) {
			Utils.waitSomeTime(10);
		}
	}
	
	public void setConnection(NXTBluetooth i_oConnection) {
		m_oConnection = i_oConnection;
		m_oConnection.setReceiveHandler(m_oReceiver.getHandler());
	}
	
	public NXTBluetooth getConnection() {
		return m_oConnection;
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
		sendCmdMessage(NXTMessageTypes.GET_FIRMWARE_VERSION);
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
		startSensorDataStreaming(sensorID, sensorType, 200);
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
			sendCmdMessage(NXTMessageTypes.GET_DISTANCE, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		} else {
			sendCmdMessage(NXTMessageTypes.GET_INPUT_VALUES, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		}
	}
	
	public void setSensorType(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		sendCmdMessage(NXTMessageTypes.SET_INPUT_MODE, MsgTypes.assembleSensorTypeMsg(i_eSensorID, i_eSensorType));
		
		if (m_oSensorRequests.get(i_eSensorID) != null) {
			m_oSensorRequests.get(i_eSensorID).mSensorType = i_eSensorType;
			m_oSensorRequestActive.put(i_eSensorID, false);
		}
	}
	
	public void keepAlive() {
		sendCmdMessage(NXTMessageTypes.KEEP_ALIVE);
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
		startMotorDataStreaming(motorID, 200);
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
		sendCmdMessage(NXTMessageTypes.MOTOR_STATE, MsgTypes.assembleMotorDataRequestMsg(i_eMotorID));
	}
	
	public void resetMotorPosition(ENXTMotorID i_eMotorID, boolean i_bRelative) {
		sendCmdMessage(NXTMessageTypes.RESET_MOTOR_POSITION, MsgTypes.assembleResetMotorPositionMsg(i_eMotorID, i_bRelative));
	}

	private void setMotorSpeed(ENXTMotorID i_eMotor, int i_nVelocity) {
		sendCmdMessage(NXTMessageTypes.SET_OUTPUT_STATE, MsgTypes.assembleMotorSpeedMsg(i_eMotor, i_nVelocity * m_nInvertFactor));
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
	@Override
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
			m_oConnection.LSWrite(port, data, 1);
			
			Utils.waitSomeTime(100);
			
			for (int i = 0; i < 3; i++) {
				m_oConnection.LSGetStatus(port);
				waitAnswer(NXTMessageTypes.LS_GET_STATUS, 200);
				
				byte[] reply = m_oConnection.getReturnMessage();
				if (reply == null || reply[2] != LCPMessage.SUCCESS) {
					Utils.waitSomeTime(500);
				} else {
					break;
				}
			};
			
			m_oConnection.LSRead(port);
			waitAnswer(NXTMessageTypes.LS_READ, 200);
			
			sendResultMessage(NXTMessageTypes.GET_DISTANCE, NXTTypes.assembleDistanceData(port, m_oConnection.getReturnMessage()));
			
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

		if (m_oConnection != null) {
	        // send stop messages to motors
	    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_A, 0);
	    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_B, 0);
	    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_C, 0);
	    	
	        Utils.waitSomeTime(500);
	        try {
	        	// destroy connection
	        	m_oConnection.close();
	        }
	        catch (IOException e) { 
	        	e.printStackTrace();
	        }
		}
		connected = false;
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
	public double getBaseSped() {
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
