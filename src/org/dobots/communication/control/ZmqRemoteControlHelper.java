package org.dobots.communication.control;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dobots.communication.msg.RoboCommands;
import org.dobots.communication.msg.RoboCommands.BaseCommand;
import org.dobots.communication.msg.RoboCommands.CameraCommand;
import org.dobots.communication.msg.RoboCommands.ControlCommand;
import org.dobots.communication.msg.RoboCommands.DriveCommand;
import org.dobots.communication.msg.RobotMessage;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqReceiveThread;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import robots.ctrl.ICameraControlListener;
import robots.ctrl.RemoteControlHelper;
import android.util.Log;

public class ZmqRemoteControlHelper extends RemoteControlHelper {
	
	public interface IControlListener {
		public void onCommand(ControlCommand command);
	}

	private static final String TAG = "ZmqRemoteControl";
	
	private ZContext m_oZContext;
	
	private ZMQ.Socket m_oCmdRecvSocket;
	
	private CommandReceiveThread m_oReceiver;

	private ICameraControlListener m_oCameraListener;
	
	private Object m_oControlListener;

	/**
	 * Starts a Helper object which handles remote control over zmq. If the parameter Activity
	 * is provided, the helper class expects a remote control layout to be part of the activity.
	 * The helper class then handles button presses and forwards them to the remote control
	 * listener. 
	 * @param i_oActivity The activity with the remote control layout
	 */
	public ZmqRemoteControlHelper(BaseActivity i_oActivity) {
		super(i_oActivity);
		m_oZContext = ZmqHandler.getInstance().getContext();
	}
	
	/**
	 * Starts a helper object without activity. the helper class now only serves as a hub to forward
	 * remote controls. Optionally, the receiver can be started which listens for incoming zmq messages
	 * parses them and forwards them to the listener.
	 */
	public ZmqRemoteControlHelper() {
		this(null);
	}
	
	/**
	 * starts the zmq message receive thread. incoming messages will be parsed and forwarded
	 * to the respective listener (camera vs control).
	 * NOTE: do not start the receiver together with a zmq remote control listener. otherwise the
	 * receiver will forward the messages to the listener which sends them back as a zmq message to the 
	 * handler which sends them out again to the receiver, resulting in an endless loop. to avoid that the
	 * receiver only forwards messages if the listener is not an instance of ZmqRemoteControlSender.
	 * @param i_strName name used to identify the thread
	 */
	public void startReceiver(String i_strName) {
		
		m_oCmdRecvSocket = ZmqHandler.getInstance().obtainCommandRecvSocket();
//		m_oCmdRecvSocket.subscribe(i_strName.getBytes());
		m_oCmdRecvSocket.subscribe("".getBytes());

		m_oReceiver = new CommandReceiveThread(m_oZContext.getContext(), m_oCmdRecvSocket, i_strName + "ZmqRC");
		m_oReceiver.start();
	}
	
	/**
	 * assign a camera control listener which will handle camera on/off, toggle, and up/down commands
	 * @param i_oCameraListener object implementing the ICameraControlListener interface
	 */
	public void setCameraControlListener(ICameraControlListener i_oCameraListener) {
		m_oCameraListener = i_oCameraListener;
	}
	
	public void setControlListener(Object listener) {
		m_oControlListener = listener;
	}
	
	public void toggleCamera() {
		if (m_oCameraListener != null) {
			m_oCameraListener.toggleCamera();
		}
	}
	
	public void startVideo() {
		if (m_oCameraListener != null) {
			m_oCameraListener.startVideo();
		}
	}

	public void stopVideo() {
		if (m_oCameraListener != null) {
			m_oCameraListener.stopVideo();
		}
	}
	
	public void cameraUp() {
		if (m_oCameraListener != null) {
			m_oCameraListener.cameraUp();
		}
	}

	public void cameraDown() {
		if (m_oCameraListener != null) {
			m_oCameraListener.cameraDown();
		}
	}

	public void cameraStop() {
		if (m_oCameraListener != null) {
			m_oCameraListener.cameraStop();
		}
	}
	
	public void doMove(Move i_eMove, double i_dblSpeed, double i_dblRadius) {
		if (m_oRemoteControlListener != null) {
			m_oRemoteControlListener.onMove(i_eMove, i_dblSpeed, i_dblRadius);
		}
	}
	
	public void toggleInvertDrive() {
		if (m_oRemoteControlListener != null) {
			m_oRemoteControlListener.toggleInvertDrive();
		}
	}
	
	// in contrast to a normal RemoteControlHelper, the ZmqRemoteControl can receive remote commands
	// over Zmq messages. 
	class CommandReceiveThread extends ZmqReceiveThread {

		public CommandReceiveThread(Context i_oContext, Socket i_oInSocket,	String i_strThreadName) {
			super(i_oContext, i_oInSocket, i_strThreadName);
		}

		@Override
		protected void execute() {
			ZMsg oZMsg = ZMsg.recvMsg(m_oCmdRecvSocket);
			if (oZMsg != null) {

				// create a chat message out of the zmq message
				RobotMessage oCmdMsg = RobotMessage.fromZMsg(oZMsg);
				
				String strJson = Utils.byteArrayToString(oCmdMsg.data);
				BaseCommand oCmd = RoboCommands.decodeCommand(strJson);

				if (oCmd instanceof DriveCommand) {
					
					if (m_oRemoteControlListener instanceof ZmqRemoteControlSender) {
						Log.e(TAG, "receiver started with ZmqRemoteSender as listener. Abort!");
						return;
					} else {
				
						DriveCommand oDriveCmd = (DriveCommand)oCmd;
						doMove(oDriveCmd.eMove, oDriveCmd.dblSpeed, oDriveCmd.dblRadius);
					}
					
				} else if (oCmd instanceof CameraCommand) {
					
					if (m_oCameraListener instanceof ZmqRemoteControlSender) {
						Log.e(TAG, "receiver started with ZmqRemoteSender as listener. Abort!");
						return;
					} else {
				
						CameraCommand oCameraCmd = (CameraCommand)oCmd;
						switch(oCameraCmd.eType) {
						case OFF:
							stopVideo();
							break;
						case ON:
							startVideo();
							break;
						case TOGGLE:
							toggleInvertDrive();
							toggleCamera();
							break;
						case UP:
							cameraUp();
							break;
						case DOWN:
							cameraDown();
							break;
						case STOP:
							cameraStop();
						}
					}
				} else if (oCmd instanceof ControlCommand) {

					try {
	//					m_oControlListener.onCommand((ControlCommand)oCmd);
						ControlCommand controlCommand = (ControlCommand)oCmd;
						Method[] methods = m_oControlListener.getClass().getMethods();
						for (Method method : methods) {
							if (method.getName().equals(controlCommand.mCommand)) {
								Object[] args = controlCommand.getParameters();
								method.invoke(m_oControlListener, args);
								return;
							}
						}
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	public void close() {
		if (m_oCmdRecvSocket != null) {
			m_oCmdRecvSocket.close();
			m_oCmdRecvSocket = null;
		}
		if (m_oReceiver != null) {
			m_oReceiver.close();
			m_oReceiver = null;
		}
	}


}
