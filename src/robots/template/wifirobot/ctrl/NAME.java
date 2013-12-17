package robots.template.wifirobot.ctrl;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;

public class NAME extends DifferentialRobot {
	
	NAMEController m_oController;

	public NAME() {
		super(NAMETypes.AXLE_WIDTH, NAMETypes.MIN_SPEED, NAMETypes.MAX_SPEED, NAMETypes.MIN_SPEED, NAMETypes.MAX_RADIUS);

		m_oController = new NAMEController();
		// TODO Auto-generated constructor stub
	}

	@Override
	public RobotType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean toggleInvertDrive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getBaseSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveForward() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBackward() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateCounterClockwise() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateClockwise() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveRight() {
		// TODO Auto-generated method stub

	}

}
