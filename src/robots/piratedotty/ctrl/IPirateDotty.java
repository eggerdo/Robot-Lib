package robots.piratedotty.ctrl;

import robots.ctrl.ICameraRobot;
import robots.ctrl.IRobotDevice;
import robots.gui.comm.IRobotConnection;

public interface IPirateDotty extends ICameraRobot, IRobotDevice {

	IRobotConnection getConnection();

	void setConnection(IRobotConnection connection);

	void shootGuns();

	void fireVolley();

	void dock(boolean isDocking);

}
