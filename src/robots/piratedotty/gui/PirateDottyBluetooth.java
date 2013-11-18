package robots.piratedotty.gui;

import java.io.IOException;
import java.util.UUID;

import robots.gui.BluetoothConnection;
import robots.gui.MessageTypes;
import robots.piratedotty.ctrl.PirateDottyTypes;
import android.bluetooth.BluetoothDevice;

public class PirateDottyBluetooth extends BluetoothConnection {
	
//	public PirateDottyBluetooth(BluetoothDevice i_oDevice) {
//		super(i_oDevice, PirateDottyTypes.PIRATEDOTTY_UUID);
//	}
	
	public PirateDottyBluetooth(BluetoothDevice i_oDevice, UUID uuid) {
		super(i_oDevice, uuid);
		// TODO Auto-generated constructor stub
	}

	protected byte[] receiveMessage() throws IOException {
		if (m_oDataIn.available() > 0) {
			String jsonString = m_oDataIn.readLine();
			return jsonString.getBytes();
		}
		return null;
	}
	
	public void sendMessage(byte[] buffer) {
		try {
			m_oOutStream.write(buffer);
		} catch (IOException e) {
			connected = false;
            sendState(MessageTypes.STATE_SENDERROR);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}