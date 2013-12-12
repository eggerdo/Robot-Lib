package robots.gui.comm.wifi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.dobots.utilities.Utils;

import robots.gui.MessageTypes;
import robots.gui.comm.IRobotConnection;
import android.os.Handler;
import android.util.Log;

public class WifiConnection implements IRobotConnection {

	private static final String TAG = "WifiConnection";

	public static final int CONNECT_TIMEOUT = 10000; // 10 seconds

	protected Socket m_oSocket = null;

	protected InputStream m_oInStream = null;
	protected OutputStream m_oOutStream = null;

	protected DataInputStream m_oDataIn = null;
	protected DataOutputStream m_oDataOut = null;

	protected Handler m_oReceiveHandler = null;

	protected boolean mConnected = false;
	protected boolean m_bStopped = false;

	protected String m_strSSID_Filter = "";

	protected String m_strAddress = "";
	protected int m_nPort = 0;

	public WifiConnection(String i_strAddress, int i_nPort) {
		this.m_strAddress = i_strAddress;
		this.m_nPort = i_nPort;
	}

	public void setReceiveHandler(Handler i_oHandler) {
		m_oReceiveHandler = i_oHandler;
	}

	public String getAddress() {
		return m_strAddress;
	}

	/**
	 * @return the current status of the connection
	 */            
	public boolean isConnected() {
		return mConnected;
	}

	//	public void startThread() {
	//		this.start();
	//	}
	//	
	//	public void stopThread() {
	//		m_bStopped = true;
	//	}

	public void disconnect() throws IOException {
		try {
			mConnected = false;

			if (m_oSocket != null) {
				m_oSocket.close();
				m_oSocket = null;
			}

			m_oDataIn = null;
			m_oDataOut = null;

		} catch (IOException e) {
			if (m_oReceiveHandler == null)
				throw e;
			else
				sendToast("Problem in closing the connection!");
		}
	}

	public boolean connect() throws IOException {
		if (m_strAddress == "") {
			Log.w(TAG, "There is no address defined");
		}

		try {
			try {
				m_oSocket = new Socket();
				Log.w(TAG, "Connection attempt to " + m_strAddress + " on port " + m_nPort);
				m_oSocket.connect(new InetSocketAddress(m_strAddress, m_nPort), CONNECT_TIMEOUT);
			}
			catch (IOException e) {  
				if (m_oReceiveHandler == null)
					throw new IOException();
				else
					sendState(MessageTypes.STATE_CONNECTERROR);
				return false;
			}

			m_oInStream = m_oSocket.getInputStream();
			m_oOutStream = m_oSocket.getOutputStream();

			m_oDataIn = new DataInputStream(new BufferedInputStream(m_oInStream));
			m_oDataOut = new DataOutputStream(new BufferedOutputStream(m_oOutStream));

			mConnected = true;
			Log.i(TAG, "Connection successful established");
		} catch (IOException e) {
			if (m_oReceiveHandler == null)
				throw e;
			else {
				sendState(MessageTypes.STATE_CONNECTERROR);
				return false;
			}
		}
		// everything was OK
		if (m_oReceiveHandler != null) {
			sendState(MessageTypes.STATE_CONNECTED);
		}

		return true;
	}

	protected void sendToast(String toastText) {
		Utils.sendMessage(m_oReceiveHandler, MessageTypes.DISPLAY_TOAST, toastText);
	}

	protected void sendState(int i_nCmd) {
		Utils.sendMessage(m_oReceiveHandler, i_nCmd, null);
	}

	protected void onConnectError() {
		sendState(MessageTypes.STATE_CONNECTERROR);
	}

	@Override
	public boolean open() {

		try {        
			return connect();
		}
		catch (IOException e) { 
			e.printStackTrace();
			sendState(MessageTypes.STATE_CONNECTERROR);
		}

		return false;
	}

	@Override
	public void close() throws IOException {
		disconnect();
	}

	@Override
	public OutputStream getOutputStream() {
		return m_oOutStream;
	}

	@Override
	public InputStream getInputStream() {
		return m_oInStream;
	}

	public void onReadError(IOException e) {
		if (mConnected) {
			mConnected = false;
			sendState(MessageTypes.STATE_RECEIVEERROR);
		}

		e.printStackTrace();
	}

	public void onWriteError(IOException e) {
		if (mConnected) {
			mConnected = false;
			sendState(MessageTypes.STATE_SENDERROR);
		}

		e.printStackTrace();
	}

	@Override
	public void send(byte[] message) {
		if (mConnected) {
			try {
				m_oOutStream.write(message);
				m_oOutStream.flush();
			} catch (IOException e) {
				onWriteError(e);
			}
		}
	}

	@Override
	public void send(String message) {
		if (mConnected) {
			try {
				m_oDataOut.writeBytes(message);
			} catch (IOException e) {
				onWriteError(e);
			}
		}
	}

	@Override
	public boolean isDataAvailable() {
		if (mConnected) {
			try {
				return m_oDataIn.available() > 0;
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return false;
	}

	@Override
	public String readLine() {
		if (mConnected) {
			try {
				return m_oDataIn.readLine();
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return null;
	}

	@Override
	public int read() {
		if (mConnected) {
			try {
				return m_oDataIn.read();
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return -1;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) {
		if (mConnected) {
			try {
				return m_oDataIn.read(buffer, offset, length);
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return -1;
	}

	public void setAddress(String address) {
		m_strAddress = address;
	}

	public void setPort(int port) {
		m_nPort = port;
	}

	public int getPort() {
		return m_nPort;
	}

}
