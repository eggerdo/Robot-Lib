package org.dobots.zmq;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dobots.lib.comm.Move;
import org.dobots.lib.comm.msg.RoboCommands;
import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;
import org.dobots.lib.comm.msg.RoboCommands.CameraCommand;
import org.dobots.lib.comm.msg.RoboCommands.ControlCommand;
import org.dobots.lib.comm.msg.RoboCommands.DriveCommand;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.zmq.comm.RobotMessage;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import robots.ctrl.control.RemoteControlHelper;
import android.util.Log;

public class ZmqRemoteControlHelper extends RemoteControlHelper {
	
	private static final String TAG = "ZmqRemoteControl";
	
	private ZContext m_oZContext;
	
	private ZMQ.Socket m_oCmdRecvSocket;
	
	private CommandReceiveThread m_oReceiver;

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
	 */
	public ZmqRemoteControlHelper(Object controlListener) {
		this((BaseActivity)null);
		m_oControlListener = controlListener;
	}

	/**
	 */
	public ZmqRemoteControlHelper() {
		this((BaseActivity)null);
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

	public void setControlListener(Object listener) {
		m_oControlListener = listener;
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
						if (m_oControlListener != null) {
							Method[] methods = m_oControlListener.getClass().getMethods();
							for (Method method : methods) {
								if (method.getName().equals(controlCommand.mCommand)) {
									Object[] args = controlCommand.getParameters();
									method.invoke(m_oControlListener, args);
									return;
								}
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

	@Override
	public void destroy() {
		super.destroy();
		
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
