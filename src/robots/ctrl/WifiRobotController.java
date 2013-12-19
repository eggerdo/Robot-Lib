package robots.ctrl;

import java.io.IOException;

import org.dobots.utilities.log.Loggable;

import robots.gui.comm.wifi.WifiConnection;
import android.os.Handler;

public class WifiRobotController extends Loggable {

	protected WifiConnection m_oConnection;
	
	public WifiRobotController(String address, int port) {
		m_oConnection = new WifiConnection(address, port);
	}

	public void setConnection(String address, int commandPort) {
		m_oConnection.setAddress(address);
		m_oConnection.setPort(commandPort);
	}

	public boolean connect() throws IOException {
		return m_oConnection.connect();
	}

	public void destroy() {
		try {
			disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setReceiveHandler(Handler handler) {
		m_oConnection.setReceiveHandler(handler);
	}

	public String getAddress() {
		return m_oConnection.getAddress();
	}

	public void disconnect() throws IOException {
		m_oConnection.disconnect();
	}

	public boolean isConnected() {
		return m_oConnection.isConnected();
	}

	protected void send(byte[] buffer) {
		m_oConnection.send(buffer);
	}
	
}

