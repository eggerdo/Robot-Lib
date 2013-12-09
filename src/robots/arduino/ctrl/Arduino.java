package robots.arduino.ctrl;

import org.dobots.lib.comm.msg.ISensorDataListener;
import org.dobots.lib.comm.msg.SensorMessageObj;
import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.sensors.ZmqSensorsSender;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.gui.BluetoothConnection;
import robots.gui.RobotDriveCommandListener;
import android.os.Handler;

public class Arduino extends DifferentialRobot implements IArduino, ISensorDataListener {
	
	private ArduinoController m_oController;
	private RobotDriveCommandListener m_oRemoteListener;
	private ZmqRemoteControlHelper m_oRemoteHelper;

	private ZmqSensorsSender m_oSensorsSender;
	
	private int m_nInverted = 1;
	
	private double m_dblBaseSpeed = 100;

	public Arduino() {
		super(ArduinoTypes.AXLE_WIDTH, ArduinoTypes.MIN_VELOCITY, ArduinoTypes.MAX_VELOCITY, ArduinoTypes.MIN_RADIUS, ArduinoTypes.MAX_RADIUS);

		m_oController = new ArduinoController();
		m_oController.setSensorListener(this);
		
		m_oRemoteListener = new RobotDriveCommandListener(this);
		m_oRemoteHelper = new ZmqRemoteControlHelper();
		m_oRemoteHelper.setDriveControlListener(m_oRemoteListener);
		m_oRemoteHelper.setControlListener(this);
		m_oRemoteHelper.startReceiver("Arduino");

		m_oSensorsSender = new ZmqSensorsSender();
		
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_ARDUINO;
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
	public void destroy() {
		if (isConnected()) {
			disconnect();
		}
		m_oController.destroyConnection();
	}
	
	@Override
	public void setHandler(Handler i_oHandler) {
		super.setHandler(i_oHandler);
		m_oController.setHandler(i_oHandler);
	}

	public void setConnection(BluetoothConnection i_oConnection) {
		m_oController.setConnection(i_oConnection);
	}
	
	public BluetoothConnection getConnection() {
		return m_oController.getConnection();
	}

	@Override
	public void connect() {
		m_oController.connect();
	}

	@Override
	public void disconnect() {
		if (m_oController.isConnected()) {
			m_oController.disconnect();
		}
	}

	@Override
	public boolean isConnected() {
		return m_oController.isConnected();
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		m_oController.control(i_bEnable);
	}

	@Override
	public boolean toggleInvertDrive() {
		m_nInverted *= -1;
		return true;
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(nVelocity * m_nInverted, nVelocity * m_nInverted);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		m_oController.drive(oVelocity.left * m_nInverted, oVelocity.right * m_nInverted);
	}
	
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveForward(i_dblSpeed, nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(-nVelocity * m_nInverted, -nVelocity * m_nInverted);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		m_oController.drive(-oVelocity.left * m_nInverted, -oVelocity.right * m_nInverted);
	}
	
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveBackward(i_dblSpeed, nRadius);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(nVelocity, -nVelocity);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(-nVelocity, nVelocity);
	}

	@Override
	public void moveStop() {
		m_oController.driveStop();
	}
	
	@Override
	public void moveForward() {
		moveForward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSpeed() {
		return m_dblBaseSpeed;
	}

	@Override
	public void moveLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestSensorData() {
		m_oController.requestSensorData();
	}

	@Override
	public void onSensorData(String data) {
		SensorMessageObj sensorData = SensorMessageObj.decodeJSON(getID(), data);
		m_oSensorsSender.sendSensors(sensorData);
	}

}
