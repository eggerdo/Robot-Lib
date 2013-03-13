package org.dobots.robotalk.zmq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
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

	public ZmqHandler(Activity i_oActivity) {
		m_oActivity = i_oActivity;
		
		INSTANCE = this;
		
		m_strIPCpath = m_oActivity.getDir("tmp", Context.MODE_WORLD_WRITEABLE).toString();

        m_oZmqContext = new ZContext();
		
		m_oSettings = new ZmqSettings(m_oActivity);
		m_oSettings.checkSettings();
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

}
