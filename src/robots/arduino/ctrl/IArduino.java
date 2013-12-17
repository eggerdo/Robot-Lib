package robots.arduino.ctrl;

import robots.ctrl.IRobotDevice;
import robots.gui.comm.IRobotConnection;

public interface IArduino extends IRobotDevice {
	
	public IRobotConnection getConnection();
	
	public void setConnection(IRobotConnection connection);

	public void requestSensorData();
	
}
