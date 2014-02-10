package robots.gui.comm.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import org.dobots.utilities.Utils;

import robots.gui.MessageTypes;
import robots.gui.comm.IRobotConnection;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

public class BluetoothConnection implements IRobotConnection {

//    public interface IMessageListener {
//    	public void onMessage(byte[] message);
//    }

//    protected IMessageListener m_oListener;
    
	protected BluetoothDevice m_oDevice = null;
	protected BluetoothSocket m_oSocket = null;
	
	protected InputStream m_oInStream = null;
	protected OutputStream m_oOutStream = null;
	
	protected DataOutputStream m_oDataOut = null;
	protected DataInputStream m_oDataIn = null;

	protected Handler m_oUiHandler = null;

	protected boolean mConnected = false;
	protected boolean mStopped = false;
	
	private UUID m_oUUID = null;
	
	protected String m_strMacAddress = "";
	
	public BluetoothConnection(BluetoothDevice i_oDevice, UUID uuid) {
		this.m_oDevice = i_oDevice;
		this.m_oUUID = uuid;
		this.m_strMacAddress = i_oDevice.getAddress();
	}

    public void setReceiveHandler(Handler i_oHandler) {
    	m_oUiHandler = i_oHandler;
    }

//	public void setMessageListener(IMessageListener listener) {
//		m_oListener = listener;
//	}
	
    public String getAddress() {
    	return m_strMacAddress;
    }
    
    public InputStream getInputStream() {
    	return m_oInStream;
    }
    
    public OutputStream getOutputStream() {
    	return m_oOutStream;
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
	
//	public void open() {
//
//        try {        
//            createConnection();
//            connect();
//        }
//        catch (IOException e) { 
//        	e.printStackTrace();
//            sendState(MessageTypes.STATE_CONNECTERROR);
//        }
//
//	}

	public boolean open() {
		// don't call this in the main thread, it just stalls the UI
		if (Looper.myLooper() == Looper.getMainLooper()) {
			Utils.runAsyncTask(new Runnable() {
				@Override
				public void run() {
					open();
				}
			});
			return false;
		} else {
			try {        
				createConnection();
				connect();
				return true;
			}
			catch (IOException e) { 
				e.printStackTrace();
				sendState(MessageTypes.STATE_CONNECTERROR);
				return false;
			}
		}
	}
	
//    @Override
//    public void run() {
//
//    	startUp();
//    
//    	while (connected && !m_bStopped) {
//    		try {
//    			execute();
//	    	} catch (IOException e) {
//				if (connected) {
//	            	connected = false;
//	                sendState(MessageTypes.STATE_RECEIVEERROR);
//	            }
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return;
//			}
//    	}
//    	
//    }
    	
//    public void execute() throws IOException {
//		byte[] message = receiveMessage();
//		if (message != null) {
//			if (m_oListener != null) {
//				m_oListener.onMessage(message);
//			}
//		}
//    }
    
//    protected abstract byte[] receiveMessage() throws IOException;

    public void createConnection() throws IOException {
        if (m_oDevice == null) {
            if (m_oUiHandler == null)
                throw new IOException();
            else {
                sendToast("No paired robot found!");
                sendState(MessageTypes.STATE_CONNECTERROR);
                return;
            }
        }
    	mConnected = false;
        m_oSocket = m_oDevice.createRfcommSocketToServiceRecord(m_oUUID);
    }

    /**
     * Closes the bluetooth connection. On error the method either sends a message
     * to it's owner or creates an exception in the case of no message handler.
     */
    public void close() throws IOException {
        try {
            if (m_oSocket != null) {
                mConnected = false;
//                stopThread();
                m_oSocket.close();
                m_oSocket = null;
            }

            m_oInStream = null;
            m_oOutStream = null;
            m_oDataOut = null;
            m_oDataIn = null;

        } catch (IOException e) {
            if (m_oUiHandler == null)
                throw e;
            else
                sendToast("Problem in closing the connection!");
        }
    }

    public void connect() throws IOException {
    	if (m_oSocket == null) {
//    		int i = 0;
    		return;
    	}
        try {
	    	try {
	    		m_oSocket.connect();
	        }
	        catch (IOException e) {  
//	            if (myOwner.isPairing()) {
//	                if (m_oUiHandler != null) {
//	                    sendToast(mResources.getString(R.string.pairing_message));
//	                    sendState(NXTTypes.STATE_CONNECTERROR_PAIRING);
//	                }
//	                else
//	                    throw e;
//	                return;
//	            }
	
	            // try another method for connection, this should work on the HTC desire, credits to Michael Biermann
	            try {
	                Method mMethod = m_oDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
	                m_oSocket = (BluetoothSocket) mMethod.invoke(m_oDevice, Integer.valueOf(1));            
	                m_oSocket.connect();
	            }
	            catch (Exception e1){
	            	e.printStackTrace();
	                if (m_oUiHandler == null)
	                    throw new IOException();
	                else
	                    sendState(MessageTypes.STATE_CONNECTERROR);
	                return;
	            }
	        }
	    	m_oInStream = m_oSocket.getInputStream();
	    	m_oOutStream = m_oSocket.getOutputStream();
	    	
	    	m_oDataOut = new DataOutputStream(m_oOutStream);
	    	m_oDataIn = new DataInputStream(m_oInStream);
	    	
	        mConnected = true;
	    } catch (IOException e) {
	    	e.printStackTrace();
	        if (m_oUiHandler == null)
	            throw e;
	        else {
//	            if (myOwner.isPairing())
//	                sendToast(mResources.getString(R.string.pairing_message));
	            sendState(MessageTypes.STATE_CONNECTERROR);
	            return;
	        }
	    }
	    // everything was OK
	    if (m_oUiHandler != null) {
	        sendState(MessageTypes.STATE_CONNECTED);
	    }
    }

    protected void sendToast(String toastText) {
    	Utils.sendMessage(m_oUiHandler, MessageTypes.DISPLAY_TOAST, toastText);
    }

    protected void sendState(int i_nCmd) {
    	Utils.sendMessage(m_oUiHandler, i_nCmd, null);
    }

	public void send(byte[] buffer) {
		try {
			m_oOutStream.write(buffer);
		} catch (IOException e) {
			onWriteError(e);
		}
	}

	public void send(String message) {
		try {
			m_oDataOut.writeBytes(message);
		} catch (IOException e) {
			onWriteError(e);
		}
	}
    
	public boolean isDataAvailable() {
		if (m_oDataIn != null) {
			try {
				return m_oDataIn.available() > 0;
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return false;
	}

	public String readLine() {
		if (m_oDataIn != null) {
			try {
				return m_oDataIn.readLine();
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return null;
	}

	public int read() {
		if (m_oDataIn != null) {
			try {
				return m_oDataIn.read();
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return -1;
	}

	public int read(byte[] buffer, int offset, int length) {
		if (m_oDataIn != null) {
			try {
				return m_oDataIn.read(buffer, offset, length);
			} catch (IOException e) {
				onReadError(e);
			}
		}
		return -1;
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
	
}
