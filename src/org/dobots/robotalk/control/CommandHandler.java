package org.dobots.robotalk.control;

import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RobotMessage;
import org.dobots.robotalk.zmq.ZmqForwarderThread;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqSettings;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class CommandHandler {
	
	private static CommandHandler INSTANCE;

	private ZContext m_oZContext;
	private ZmqSettings m_oSettings;
	
	// the channel on which messages are sent out
	private ZMQ.Socket m_oExt_CommandOut = null;
	// the channel on which messages are coming in
	private ZMQ.Socket m_oExt_CommandIn = null;
	
	private ZMQ.Socket m_oInt_CommandOut = null;
	private String m_strIntCommandAddr;
	
//	private CommandReceiveThread m_oCommandRecvThread;
	private ZmqForwarderThread m_oCmdRecvThread = null;
	
	private ICommandReceiveListener m_oCommandListener;
	
	private ZMQ.Socket m_oDriveCmdPublisher = null;
	private boolean m_bReceiverConnected;
	private boolean m_bSenderConnected;

	public CommandHandler(ZmqHandler i_oZmqHandler) {
		m_oZContext = i_oZmqHandler.getContext();
		m_oSettings = i_oZmqHandler.getSettings();
		
		INSTANCE = this;
		
		m_strIntCommandAddr = "inproc://command";
		m_oInt_CommandOut = i_oZmqHandler.createSocket(ZMQ.PUB);
		m_oInt_CommandOut.bind(m_strIntCommandAddr);
	}

	public static CommandHandler getInstance() {
		return INSTANCE;
	}
	
	public String getIntCommandAddr() {
		return m_strIntCommandAddr;
	}

	/**
	 * If only incoming video should be handled, supply null as the OutSockete parameter (or vice versa)
	 * @param i_oInSocket
	 * @param i_oOutSocket
	 */
	public void setupConnections(ZMQ.Socket i_oInSocket, ZMQ.Socket i_oOutSocket) {
		
		if (i_oInSocket != null) {
			m_oExt_CommandIn = i_oInSocket;

			m_bReceiverConnected = true;
			
			m_oCmdRecvThread = new ZmqForwarderThread(m_oZContext.getContext(), m_oExt_CommandIn, m_oInt_CommandOut, "IncomingCommandForwarder");
			m_oCmdRecvThread.start();
		}
		
		if (i_oOutSocket != null) {
			m_oExt_CommandOut = i_oOutSocket;
			
			m_bSenderConnected = true;
		}
	}
	

	public void closeConnections() {

		m_bReceiverConnected = false;
		m_bSenderConnected = false;
		
		if (m_oExt_CommandOut != null) {
			m_oExt_CommandOut.close();
			m_oExt_CommandOut = null;
		}
		
		if (m_oExt_CommandIn != null) {
			m_oExt_CommandIn.close();
			m_oExt_CommandIn = null;
		}

	}

	public void sendCommand(BaseCommand i_oCmd) {
		if (m_bSenderConnected) {
			String strJSON = i_oCmd.toJSONString();
			RobotMessage oMsg = new RobotMessage(m_oSettings.getRobotName(), strJSON.getBytes());
			ZMsg oZMsg = oMsg.toZmsg();
			oZMsg.send(m_oExt_CommandOut);
		}
	}

	public void setReceiveListener(ICommandReceiveListener i_oListener) {
		m_oCommandListener = i_oListener;
	}

}
