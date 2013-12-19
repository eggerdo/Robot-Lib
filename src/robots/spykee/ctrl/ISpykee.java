package robots.spykee.ctrl;

import robots.ctrl.ICameraRobot;
import robots.ctrl.IRobotDevice;
import robots.spykee.ctrl.SpykeeController.DockState;
import robots.spykee.ctrl.SpykeeTypes.SpykeeSound;

public interface ISpykee extends IRobotDevice, ICameraRobot {

	public void dock();
	
	public void undock();
	
	public void cancelDock();
	
	public void playSound(SpykeeSound sound);
	
	public DockState getDockState();
	
	public void setLed(int led, boolean on);
	
	public int getBatteryLevel();
	
	public boolean isCharging();

	public void setConnection(String address, String port, String login, String password);

	public boolean isInverted();

	public void setInverted(boolean inverted);
	
}
