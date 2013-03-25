package org.dobots.robotalk.control;

import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RobotMessage;
import org.dobots.robotalk.zmq.ZmqForwarderThread;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqReceiveThread;
import org.dobots.robotalk.zmq.ZmqSettings;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class CommandHandler {
	
	public interface CommandListener {
		public void onCommand(ZMsg i_oMsg);
	}
	
	private ZContext m_oZContext;
	private ZmqSettings m_oSettings;
	
	// the channel on which messages are sent out
	private ZMQ.Socket m_oOutSocket = null;
	// the channel on which messages are coming in
	private ZMQ.Socket m_oInSocket = null;
	
//	private CommandReceiveThread m_oCommandRecvThread;
	private CommandReceiveThread m_oCmdRecvThread = null;
	
	private ICommandReceiveListener m_oCommandListener;
	
	private ZMQ.Socket m_oDriveCmdPublisher = null;
	private boolean m_bOutConnected;
	private boolean m_bInConnected;
	
	private CommandListener m_oCommandReceiveListener;

	public CommandHandler(ZmqHandler i_oZmqHandler) {
		m_oZContext = i_oZmqHandler.getContext();
		m_oSettings = i_oZmqHandler.getSettings();
	}

	/**
	 * If only incoming video should be handled, supply null as the OutSockete parameter (or vice versa)
	 * @param i_oInSocket
	 * @param i_oOutSocket
	 */
	public void setupConnections(ZMQ.Socket i_oInSocket, ZMQ.Socket i_oOutSocket) {
		
		if (i_oInSocket != null) {
			m_oInSocket = i_oInSocket;

			m_bOutConnected = true;
			
			m_oCmdRecvThread = new CommandReceiveThread(m_oZContext.getContext(), m_oInSocket, "IncomingCommandForwarder");
			m_oCmdRecvThread.start();
		}
		
		if (i_oOutSocket != null) {
			m_oOutSocket = i_oOutSocket;
			
			m_bInConnected = true;
		}
	}

	public void setCommandListener(CommandListener i_oListener) {
		if (m_oCmdRecvThread != null) {
			m_oCmdRecvThread.setCommandListener(i_oListener);
		}
	}

	public class CommandReceiveThread extends ZmqReceiveThread {
		
		private CommandListener m_oCommandReceiveListener;

		public CommandReceiveThread(Context i_oContext, Socket i_oInSocket,
				String i_strThreadName) {
			super(i_oContext, i_oInSocket, i_strThreadName);
		}
		
		public void setCommandListener(CommandListener i_oListener) {
			m_oCommandReceiveListener = i_oListener;
		}

		@Override
		protected void execute() {
			ZMsg msg = ZMsg.recvMsg(m_oInSocket);
			if (msg != null) {
				if (m_oCommandReceiveListener != null) {
					m_oCommandReceiveListener.onCommand(msg);
				}
			}
		}
		
	}

	public void closeConnections() {

		m_bOutConnected = false;
		m_bInConnected = false;
		
		if (m_oOutSocket != null) {
			m_oOutSocket.close();
			m_oOutSocket = null;
		}
		
		if (m_oInSocket != null) {
			m_oInSocket.close();
			m_oInSocket = null;
		}

	}

	public void sendCommand(BaseCommand i_oCmd) {
		if (m_bInConnected) {
			String strJSON = i_oCmd.toJSONString();
			RobotMessage oMsg = new RobotMessage(m_oSettings.getRobotName(), strJSON.getBytes());
			ZMsg oZMsg = oMsg.toZmsg();
			oZMsg.send(m_oOutSocket);
		}
	}

	public void sendZmsg(ZMsg i_oMsg) {
		i_oMsg.send(m_oOutSocket);
	}

}
