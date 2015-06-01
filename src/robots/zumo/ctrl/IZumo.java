package robots.zumo.ctrl;

import robots.ctrl.ICameraRobot;
import robots.ctrl.IRobotDevice;
import robots.gui.comm.IRobotConnection;

public interface IZumo extends ICameraRobot, IRobotDevice {

	IRobotConnection getConnection();

	void setConnection(IRobotConnection connection);

}
