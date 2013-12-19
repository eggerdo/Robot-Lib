package robots.spytank.ctrl;

import robots.ctrl.ICameraRobot;
import robots.ctrl.IRobotDevice;

public interface ISpyTank extends IRobotDevice, ICameraRobot {

	public void setConnection(String address, int command_port, int media_port);
	
	public void cameraUp();

	public void cameraStop();

	public void cameraDown();
	
}
