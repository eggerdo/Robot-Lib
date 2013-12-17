package robots.arduino.gui;

import java.io.IOException;
import java.util.UUID;

import robots.gui.MessageTypes;
import robots.gui.comm.bluetooth.BluetoothConnection;
import android.bluetooth.BluetoothDevice;

public class ArduinoBluetooth extends BluetoothConnection {

	public ArduinoBluetooth(BluetoothDevice i_oDevice, UUID uuid) {
		super(i_oDevice, uuid);
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
			mConnected = false;
            sendState(MessageTypes.STATE_SENDERROR);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
