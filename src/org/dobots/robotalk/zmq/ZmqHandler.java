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

	private ZmqMessageHandler m_oCommandHandler;

	private String m_strCommandOutAddr;
	private String m_strCommandInAddr;
	
	private ZmqMessageHandler m_oVideoHandler;
	
	private String m_strVideoOutAddr;
	private String m_strVideoInAddr;

	private String m_strVideoBase64OutAddr;
	private String m_strVideoBase64InAddr;

	private ZmqMessageHandler m_oVideoBase64Handler;
	
	public ZmqHandler(Activity i_oActivity) {
		m_oActivity = i_oActivity;
		
		INSTANCE = this;
		
		m_strIPCpath = m_oActivity.getDir("tmp", Context.MODE_WORLD_WRITEABLE).toString();

        m_oZmqContext = new ZContext();
		
		m_oSettings = new ZmqSettings(m_oActivity);
		m_oSettings.checkSettings();
		
		m_oCommandHandler = new ZmqMessageHandler();
		setupCommandConnections();
		
		m_oVideoHandler = new ZmqMessageHandler();
		m_oVideoBase64Handler = new ZmqMessageHandler();
		setupVideoConnections();
	}
	
	public void setupCommandConnections() {

		m_strCommandOutAddr = ZmqTypes.COMMAND_ADDRESS + "/out";
		ZMQ.Socket oCommandSendSocket = createSocket(ZMQ.PUB);
		oCommandSendSocket.bind(m_strCommandOutAddr);

		m_strCommandInAddr = ZmqTypes.COMMAND_ADDRESS + "/in";
		ZMQ.Socket oCommandRecvSocket = createSocket(ZMQ.SUB);
		oCommandRecvSocket.subscribe("".getBytes());
		oCommandRecvSocket.bind(m_strCommandInAddr);

		m_oCommandHandler.setupConnections(oCommandRecvSocket, oCommandSendSocket);

	}
	
	public void setupVideoConnections() {

		m_strVideoOutAddr = ZmqTypes.VIDEO_ADDRESS + "/out";
		ZMQ.Socket oVideoSendSocket = createSocket(ZMQ.PUB);
		oVideoSendSocket.bind(m_strVideoOutAddr);

		m_strVideoInAddr = ZmqTypes.VIDEO_ADDRESS + "/in";
		ZMQ.Socket oVideoRecvSocket = createSocket(ZMQ.SUB);
		oVideoRecvSocket.subscribe("".getBytes());
		oVideoRecvSocket.bind(m_strVideoInAddr);

		m_oVideoHandler.setupConnections(oVideoRecvSocket, oVideoSendSocket);
		
		m_strVideoBase64OutAddr = ZmqTypes.VIDEO_BASE64_ADDRESS + "/out";
		ZMQ.Socket oVideoBase64SendSocket = createSocket(ZMQ.PUB);
		oVideoBase64SendSocket.bind(m_strVideoBase64OutAddr);

		m_strVideoBase64InAddr = ZmqTypes.VIDEO_BASE64_ADDRESS + "/in";
		ZMQ.Socket oVideoBase64RecvSocket = createSocket(ZMQ.SUB);
		oVideoBase64RecvSocket.subscribe("".getBytes());
		oVideoBase64RecvSocket.bind(m_strVideoBase64InAddr);

		m_oVideoBase64Handler.setupConnections(oVideoBase64RecvSocket, oVideoBase64SendSocket);
		
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

	public ZmqMessageHandler getCommandHandler() {
		return m_oCommandHandler;
	}

	// creates and returns a socket to which an internal module
	// can send commands (connects to the In Socket of the Command Handler)
	public ZMQ.Socket obtainCommandSendSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.PUB);
		socket.connect(m_strCommandInAddr);
		return socket;
	}

	// creates and returns a socket from which an internal module
	// can receive commands (connects to the Out Socket of the Command Handler)
	public ZMQ.Socket obtainCommandRecvSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.SUB);
		socket.connect(m_strCommandOutAddr);
		return socket;
	}
	

	public ZmqMessageHandler getVideoHandler() {
		return m_oVideoHandler;
	}

	// creates and returns a socket to which an internal module
	// can send video (connects to the In Socket of the Video Handler)
	public ZMQ.Socket obtainVideoSendSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.PUB);
		socket.connect(m_strVideoInAddr);
		return socket;
	}

	// creates and returns a socket from which an internal module
	// can receive video (connects to the Out Socket of the Video Handler)
	public ZMQ.Socket obtainVideoRecvSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.SUB);
		socket.connect(m_strVideoOutAddr);
		return socket;
	}

	public ZmqMessageHandler getVideoBase64Handler() {
		return m_oVideoBase64Handler;
	}

	// creates and returns a socket to which an internal module
	// can send video (connects to the In Socket of the Video Handler)
	public ZMQ.Socket obtainVideoBase64SendSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.PUB);
		socket.connect(m_strVideoBase64InAddr);
		return socket;
	}

	// creates and returns a socket from which an internal module
	// can receive video (connects to the Out Socket of the Video Handler)
	public ZMQ.Socket obtainVideoBase64RecvSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.SUB);
		socket.connect(m_strVideoBase64OutAddr);
		return socket;
	}
	

}
