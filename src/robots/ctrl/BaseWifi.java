package robots.ctrl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.dobots.utilities.Utils;

import robots.gui.MessageTypes;
import android.os.Handler;

public abstract class BaseWifi extends Thread {
	
	public static final int CONNECT_TIMEOUT = 10000; // 10 seconds

    protected Socket m_oSocket = null;
	protected DataInputStream m_oDataIn = null;
	protected DataOutputStream m_oDataOut = null;

	protected Handler m_oReceiveHandler = null;

	protected boolean connected = false;
	protected boolean m_bStopped = false;
	
	protected String m_strRobotName = "";
	protected String m_strSSID_Filter = "";
	
	protected String m_strAddress = "";
	protected int m_nPort = 0;
	
	public BaseWifi(String i_strAddress, int i_nPort) {
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
        return connected;
    }

	public void startThread() {
		this.start();
	}
	
	public void stopThread() {
		m_bStopped = true;
	}
	
	protected void startUp() {

        try {        
            connect();
        }
        catch (IOException e) { 
        	e.printStackTrace();
            sendState(MessageTypes.STATE_CONNECTERROR);
        }

	}

    public void disconnect() throws IOException {
        try {
            if (m_oSocket != null) {
                connected = false;
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
        try {
	    	try {
	    		m_oSocket = new Socket();
	    		m_oSocket.connect(new InetSocketAddress(m_strAddress, m_nPort), CONNECT_TIMEOUT);
	        }
	        catch (IOException e) {  
                if (m_oReceiveHandler == null)
                    throw new IOException();
                else
                    sendState(MessageTypes.STATE_CONNECTERROR);
                return false;
            }
	    	
	    	m_oDataIn = new DataInputStream(m_oSocket.getInputStream());
	    	m_oDataOut = new DataOutputStream(m_oSocket.getOutputStream());
	        connected = true;
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

}
