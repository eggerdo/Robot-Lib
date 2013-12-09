package robots.gui;

import android.bluetooth.BluetoothDevice;

public interface IBluetoothConnectionListener {

	public void setConnection(BluetoothDevice i_oDevice);
	
	public void connect();

}
