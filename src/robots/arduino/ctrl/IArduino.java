package robots.arduino.ctrl;

import robots.ctrl.IRobotDevice;
import robots.gui.BluetoothConnection;

public interface IArduino extends IRobotDevice {
	
	public BluetoothConnection getConnection();
	
	public void setConnection(BluetoothConnection connection);

	public void requestSensorData();
	
}
