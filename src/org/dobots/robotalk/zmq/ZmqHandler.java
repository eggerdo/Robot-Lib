package org.dobots.robotalk.zmq;

import org.dobots.robotalk.control.CommandHandler;
import org.dobots.robotalk.control.CommandHandler.CommandListener;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Poller;

import android.app.Activity;
import android.content.Context;

public class ZmqHandler {
	
	private static ZmqHandler INSTANCE;
	
	private ZContext m_oZmqContext;
	
	private Activity m_oActivity;

	private ZmqSettings m_oSettings;
	
	private String m_strIPCpath;
	
	private Poller m_oPoller;

	private CommandHandler m_oCommandHandler;

	private String m_strCommandOutAddr;
	private String m_strCommandInAddr;
	
	public ZmqHandler(Activity i_oActivity) {
		m_oActivity = i_oActivity;
		
		INSTANCE = this;
		
		m_strIPCpath = m_oActivity.getDir("tmp", Context.MODE_WORLD_WRITEABLE).toString();

        m_oZmqContext = new ZContext();
		
		m_oSettings = new ZmqSettings(m_oActivity);
		m_oSettings.checkSettings();
		
		m_oCommandHandler = new CommandHandler(this);
		
	}
	
	public void setupCommandConnections() {

		m_strCommandOutAddr = ZmqTypes.COMMAND_ADDRESS + "/out";
		ZMQ.Socket oCommandInternalSend = createSocket(ZMQ.PUB);
		oCommandInternalSend.bind(m_strCommandOutAddr);

		m_strCommandInAddr = ZmqTypes.COMMAND_ADDRESS + "/in";
		ZMQ.Socket oCommandInternalRecv = createSocket(ZMQ.SUB);
		oCommandInternalRecv.bind(m_strCommandInAddr);

		m_oCommandHandler.setupConnections(oCommandInternalRecv, oCommandInternalSend);

	}
	
	public static ZmqHandler getInstance() {
		return INSTANCE;
	}

	public ZContext getContext() {
		return m_oZmqContext;
	}

	public ZmqSettings getSettings() {
		return m_oSettings;
	}

	public ZMQ.Socket createSocket(int type) {
		return m_oZmqContext.createSocket(type);
	}
	
	public String getIPCpath() {
		return m_strIPCpath;
	}
	
	public Poller getPoller() {
		if (m_oPoller == null) {
			m_oPoller = m_oZmqContext.getContext().poller();
		}
		return m_oPoller;
	}

	public CommandHandler getCommandHandler() {
		return m_oCommandHandler;
	}
	
	public String getCommandOutAddr() {
		return m_strCommandOutAddr;
	}
	
	public String getCommandInAddr() {
		return m_strCommandInAddr;
	}

}
