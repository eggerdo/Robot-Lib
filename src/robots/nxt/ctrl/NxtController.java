/**
 *   Copyright 2010 Guenther Hoelzl, Shawn Brown
 *
 *   This file is part of MINDdroid.
 *
 *   MINDdroid is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   MINDdroid is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with MINDdroid.  If not, see <http://www.gnu.org/licenses/>.
**/

package robots.nxt.ctrl;

import java.io.IOException;

import org.dobots.utilities.Utils;

import robots.ctrl.comm.OldProtocolHandler;
import robots.ctrl.comm.OldProtocolHandler.ICommHandler;
import robots.gui.comm.IRobotConnection;
import robots.nxt.MsgTypes;
import android.os.Handler;

/**
 * This class is for talking to a LEGO NXT robot via bluetooth.
 * The communciation to the robot is done via LCP (LEGO communication protocol).
 * Objects of this class can either be run as standalone thread or controlled
 * by the owners, i.e. calling the send/recive methods by themselves.
 */
public class NxtController implements ICommHandler<byte[]> {

	private IRobotConnection m_oConnection;

    private byte[] returnMessage;

	private Handler mHandler;

    private class NXTProtocolHandler extends OldProtocolHandler {

		public NXTProtocolHandler(IRobotConnection connection, ICommHandler handler) {
			super(connection, handler);
		}

	    /**
	     * Creates the connection, waits for incoming messages and dispatches them. The thread will be terminated
	     * on closing of the connection.
	     * @throws IOException 
	     */
		@Override
		public void execute() {
			try {
				returnMessage = receiveMessage();
		        if ((returnMessage.length >= 2) && ((returnMessage[0] == LCPMessage.REPLY_COMMAND) ||
		            (returnMessage[0] == LCPMessage.DIRECT_COMMAND_NOREPLY)))
		            mMessageHandler.onMessage(returnMessage);
			} catch (IOException e) {
				m_oConnection.onReadError(e);
				return;
			}
		}

		@Override
		public void shutDown() {
			// TODO Auto-generated method stub
			
		}
    }
    
    private OldProtocolHandler mProtocolHandler = new NXTProtocolHandler(m_oConnection, this);

	public void setHandler(Handler handler) {
		mHandler = handler;
		if (mHandler == null) {
			int i = 0;
		}
	}
	
	public void setConnection(IRobotConnection i_oConnection) {
		m_oConnection = i_oConnection;
		mProtocolHandler = new NXTProtocolHandler(m_oConnection, this);
	}
	
	public IRobotConnection getConnection() {
		return m_oConnection;
	}

	public void destroyConnection() {
		if (mProtocolHandler != null) {
			mProtocolHandler.destroy();
			mProtocolHandler = null;
		}
		
		if (m_oConnection != null) {
			try {
				m_oConnection.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_oConnection = null;
		
	}

	public void connect() {
		m_oConnection.open();
		mProtocolHandler.start();
	}
	
	public void disconnect() {
		destroyConnection();
	}
	
	public boolean isConnected() {
		if (m_oConnection != null) {
			return m_oConnection.isConnected();
		} else {
			return false;
		}
	}

	public byte[] getReturnMessage() {
        return returnMessage;
    }

    /**
     * Sends a message on the opened OutputStream
     * @param message, the message as a byte array
     */
    public void sendMessage(byte[] message) throws IOException {
        if (m_oConnection == null)
            throw new IOException();

        // send message length
        int messageLength = message.length;
        m_oConnection.getOutputStream().write(messageLength);
        m_oConnection.getOutputStream().write(messageLength >> 8);
        m_oConnection.getOutputStream().write(message, 0, message.length);
    }  

    /**
     * Receives a message on the opened InputStream
     * @return the message
     */                
    public byte[] receiveMessage() throws IOException {
        if (m_oConnection == null)
            throw new IOException();

        int length = m_oConnection.getInputStream().read();
        if (length < 0) {
            throw new IOException();
        }
        
        length = (m_oConnection.getInputStream().read() << 8) + length;
        byte[] returnMessage = new byte[length];
        m_oConnection.getInputStream().read(returnMessage);
        return returnMessage;
    }    

    /**
     * Sends a message on the opened OutputStream. In case of 
     * an error the state is sent to the handler.
     * @param message, the message as a byte array
     */
    private void sendMessageAndState(byte[] message) {
        if (m_oConnection == null)
            return;

        try {
            sendMessage(message);
        }
        catch (IOException e) {
        	m_oConnection.onWriteError(e);
        }
    }

	@Override
	public void onMessage(byte[] message) {
        switch (message[1]) {

            case LCPMessage.GET_OUTPUT_STATE:

                if (message.length >= 25)
                    sendStateAndData(NxtMessageTypes.MOTOR_STATE, message);

                break;

            case LCPMessage.GET_FIRMWARE_VERSION:

                if (message.length >= 7)
                    sendStateAndData(NxtMessageTypes.FIRMWARE_VERSION, message);

                break;

            case LCPMessage.FIND_FIRST:
            case LCPMessage.FIND_NEXT:

                if (message.length >= 28) {
                    // Success
                    if (message[2] == 0)
                        sendStateAndData(NxtMessageTypes.FIND_FILES, message);
                }

                break;
                
            case LCPMessage.GET_CURRENT_PROGRAM_NAME:

                if (message.length >= 23) {
                    sendStateAndData(NxtMessageTypes.PROGRAM_NAME, message);
                }
                
                break;
                
            case LCPMessage.SAY_TEXT:
                
                if (message.length == 22) {
                    sendStateAndData(NxtMessageTypes.SAY_TEXT, message);
                }
                
            case LCPMessage.VIBRATE_PHONE:
                if (message.length == 3) {
                    sendStateAndData(NxtMessageTypes.VIBRATE_PHONE, message);
                }               
                
            case LCPMessage.GET_INPUT_VALUES:
            	if (message.length == 16) {
            		sendStateAndData(NxtMessageTypes.GET_INPUT_VALUES, message);
            	}
            	
            case LCPMessage.LS_GET_STATUS:
            	if (message.length == 4) {
            		sendStateAndData(NxtMessageTypes.LS_GET_STATUS, message);
            	}
            	
            case LCPMessage.LS_READ:
            	if (message.length == 20) {
            		sendStateAndData(NxtMessageTypes.LS_READ, message);
            	}
            
        }
    }
    
    public void keepAlive() {
    	byte[] message = LCPMessage.getKeepAliveMessage();
    	sendMessageAndState(message);
    }

    public void doBeep(int frequency, int duration) {
        byte[] message = LCPMessage.getBeepMessage(frequency, duration);
        sendMessageAndState(message);
        Utils.waitSomeTime(20);
    }
    
    public void doAction(int actionNr) {
        byte[] message = LCPMessage.getActionMessage(actionNr);
        sendMessageAndState(message);
    }

    public void startProgram(String programName) {
        byte[] message = LCPMessage.getStartProgramMessage(programName);
        sendMessageAndState(message);
    }

    public void stopProgram() {
        byte[] message = LCPMessage.getStopProgramMessage();
        sendMessageAndState(message);
    }
    
    public void requestProgramName() {
        byte[] message = LCPMessage.getProgramNameMessage();
        sendMessageAndState(message);
    }
    
    public void setMotorSpeed(int motor, int speed) {
        if (speed > 100)
            speed = 100;

        else if (speed < -100)
            speed = -100;

        byte[] message = LCPMessage.getMotorMessage(motor, speed);
        sendMessageAndState(message);
    }

    public void rotateTo(int motor, int end) {
        byte[] message = LCPMessage.getMotorMessage(motor, -80, end);
        sendMessageAndState(message);
    }

    public void requestMotorState(int motor) {
        byte[] message = LCPMessage.getOutputStateMessage(motor);
        sendMessageAndState(message);
    }

    public void requestFirmwareVersion() {
        byte[] message = LCPMessage.getFirmwareVersionMessage();
        sendMessageAndState(message);
    }

    public void findFiles(boolean findFirst, int handle) {
        byte[] message = LCPMessage.getFindFilesMessage(findFirst, handle, "*.*");
        sendMessageAndState(message);
    }
    
    public void setInputMode(int port, byte sensorType, byte sensorMode) {
    	byte[] message = LCPMessage.getInputModeMessage(port, sensorType, sensorMode);
    	sendMessageAndState(message);
    }
    
    public void requestInputValues(int port) {
    	byte[] message = LCPMessage.getInputValuesMessage(port);
    	sendMessageAndState(message);
    }
    
    public void LSWrite(int port, byte[] data, int expectedBytes) {
    	byte[] message = LCPMessage.getLSWriteMessage(port, expectedBytes, data.length, data);
    	sendMessageAndState(message);
    }
    
    public void LSGetStatus(int port) {
    	byte[] message = LCPMessage.getLSGetStatusMessage(port);
    	sendMessageAndState(message);
    }
    
    public void LSRead(int port) {
    	byte[] message = LCPMessage.getLSReadMessage(port);
    	sendMessageAndState(message);
    }
    
    public void resetInputScale(int port) {
    	byte[] message = LCPMessage.getResetInputScaledValueMessage(port);
    	sendMessageAndState(message);
    }
    
    public void resetMotorPosition(int motor, boolean relative) {
    	byte[] message = LCPMessage.getResetMessage(motor, relative);
    	sendMessageAndState(message);
    }
    
    public void requestBatteryLevel() {
    	byte[] message = LCPMessage.getBatteryLevelMessage();
    	sendMessageAndState(message);
    }

    private void sendStateAndData(int i_nCmd, byte[] i_rgbyData) {
    	if (mHandler != null) {
    		Utils.sendMessage(mHandler, i_nCmd, MsgTypes.assembleRawDataMsg(i_rgbyData));
    	}
    }

}
