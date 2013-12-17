package robots.gui.comm.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface IBluetoothConnectionListener {

	public void setConnection(BluetoothDevice i_oDevice);
	
	public void connect();

}
