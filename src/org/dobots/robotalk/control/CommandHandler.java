package org.dobots.robotalk.control;

import org.dobots.robotalk.msg.RoboCommands;
import org.dobots.robotalk.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.msg.RoboCommands.DriveCommand;
import org.dobots.robotalk.msg.RobotMessage;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqSettings;
import org.dobots.utilities.Utils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class CommandHandler {

	private ZContext m_oZContext;
	private ZmqSettings m_oSettings;
	
	// the channel on which messages are sent out
	private ZMQ.Socket m_oCmdPublisher = null;
	// the channel on which messages are coming in
	private ZMQ.Socket m_oCmdSubscriber = null;
	
	private CommandReceiveThread m_oRecvThread;
	
	private boolean m_bConnected;
	private ICommandReceiveListener m_oListener;
	
	private ZMQ.Socket m_oDriveCmdPublisher = null;
	
	public CommandHandler(ZmqHandler i_oZmqHandler) {
		m_oZContext = i_oZmqHandler.getContext();
		m_oSettings = i_oZmqHandler.getSettings();
		
		m_oCmdPublisher = m_oZContext.createSocket(ZMQ.PUSH);
		m_oCmdSubscriber = m_oZContext.createSocket(ZMQ.SUB);

		m_oDriveCmdPublisher = i_oZmqHandler.createSocket(ZMQ.PUSH);
	}
	
	public void setupConnections() {

		// obtain chat ports from settings
		// receive port is always equal to send port + 1
		int nCommandSendPort = m_oSettings.getCommandPort();
		int nCommandRecvPort = nCommandSendPort + 1;
		
		m_oCmdPublisher.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nCommandSendPort));
		m_oCmdSubscriber.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nCommandRecvPort));

		// subscribe to messages which are targeted at us directly
		m_oCmdSubscriber.subscribe("".getBytes());

		m_oDriveCmdPublisher.connect(String.format("ipc://%s/remotectrl", ZmqHandler.getInstance().getIPCpath()));
		
		m_oRecvThread = new CommandReceiveThread();
		m_oRecvThread.start();
		
		m_bConnected = true;
		
	}

	public void closeConnections() {

		if (m_oRecvThread != null) {
			m_oRecvThread.bRun = false;
			m_oRecvThread.interrupt();
			m_oRecvThread = null;
		}
		
		if (m_oCmdPublisher != null) {
			m_oCmdPublisher.close();
			m_oCmdPublisher = null;
		}
		
		if (m_oCmdSubscriber != null) {
			m_oCmdSubscriber.close();
			m_oCmdSubscriber = null;
		}

		m_bConnected = false;
	}

	class CommandReceiveThread extends Thread {

		public boolean bRun = true;
		
		@Override
		public void run() {
			while(bRun) {
				try {
					ZMsg oZMsg = ZMsg.recvMsg(m_oCmdSubscriber);
					if (oZMsg != null) {
						// create a chat message out of the zmq message
						RobotMessage oCmdMsg = RobotMessage.fromZMsg(oZMsg);
						
						String strJson = Utils.byteArrayToString(oCmdMsg.data);
						BaseCommand oCmd = RoboCommands.decodeCommand(strJson);
						
						if (oCmd instanceof DriveCommand) {
							RobotMessage rm = new RobotMessage("t", oCmd.toJSONString().getBytes());
							ZMsg zm = rm.toZmsg();
							zm.send(m_oDriveCmdPublisher);
						} else {
							if (m_oListener != null) {
								m_oListener.onCommandReceived(oCmd);
							}
						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendCommand(BaseCommand i_oCmd) {
		if (m_bConnected) {
			String strJSON = i_oCmd.toJSONString();
			RobotMessage oMsg = new RobotMessage(m_oSettings.getRobotName(), strJSON.getBytes());
			ZMsg oZMsg = oMsg.toZmsg();
			oZMsg.send(m_oCmdPublisher);
		}
	}

	public void setReceiveListener(ICommandReceiveListener i_oListener) {
		m_oListener = i_oListener;
	}

}
