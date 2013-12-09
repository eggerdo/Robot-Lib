package org.dobots.zmq;

import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqMessageHandler.ZmqMessageListener;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMsg;

import android.content.Context;

public class ZmqHandler {

	// !! IMPORTANT: Zmq HWM has to be set before calling connect, otherwise it doesn't
	//    have any effect !!

	private static final String TAG = "ZmqHandler";

	private static ZmqHandler INSTANCE;

	private ZContext m_oZmqContext;

	//	private BaseActivity m_oActivity;

	private ZmqSettings m_oSettings;

	private String m_strIPCpath;

	private Poller m_oPoller;

	private ZmqMessageHandler m_oCommandHandler;

	private String m_strCommandOutAddr;
	private String m_strCommandInAddr;

	private ZmqMessageHandler m_oVideoHandler;

	private String m_strVideoOutAddr;
	private String m_strVideoInAddr;

	private ZmqMessageHandler m_oSensorsHandler;

	private String m_strSensorsOutAddr;
	private String m_strSensorsInAddr;

	// use the static functions ZmqHandler.initialize
	private ZmqHandler(Context i_oContext) {
		m_strIPCpath = i_oContext.getDir("tmp", Context.MODE_WORLD_WRITEABLE).toString();

		m_oZmqContext = new ZContext();

//		m_oActivity.addMenuListener(m_oSettings);
//		m_oActivity.addDialogListener(m_oSettings);

		m_oCommandHandler = new ZmqMessageHandler(m_oZmqContext);
		setupCommandConnections();

		m_oVideoHandler = new ZmqMessageHandler(m_oZmqContext);
		setupVideoConnections();

		m_oSensorsHandler = new ZmqMessageHandler(m_oZmqContext);
		setupSensorsConnections();
	}

	// settings are only initialized if an activity is provided
	public static ZmqHandler initialize(BaseActivity i_oActivity) {
		if (getInstance() == null) {
			INSTANCE = new ZmqHandler(i_oActivity);
		}

		if (getInstance().getSettings() == null) {
			getInstance().setSettings(new ZmqSettings(i_oActivity));
		}

//		if (!getInstance().getSettings().checkSettings()) {
//			getInstance().getSettings().showDialog(i_oActivity);
//		}

		return getInstance();
	}

	// if only a context is provided, then settings are not initialized
	public static ZmqHandler initialize(Context i_oContext) {
		if (getInstance() == null) {
			INSTANCE = new ZmqHandler(i_oContext);
		}

		return getInstance();
	}

	public static ZmqHandler getInstance() {
		return INSTANCE;
	}

	// use the static function destroyInstance()
	private void onDestroy() {
		m_oCommandHandler.close();
		m_oVideoHandler.close();
		m_oSensorsHandler.close();
	}

	public static void destroyInstance() {
		if (getInstance() != null) {
			getInstance().onDestroy();
		}

		INSTANCE = null;
	}

	public void setupCommandConnections() {

		m_strCommandOutAddr = ZmqTypes.COMMAND_ADDRESS + "/out";
		ZMQ.Socket oCommandSendSocket = createSocket(ZMQ.PUB);
		oCommandSendSocket.bind(m_strCommandOutAddr);

		m_strCommandInAddr = ZmqTypes.COMMAND_ADDRESS + "/in";
		ZMQ.Socket oCommandRecvSocket = createSocket(ZMQ.SUB);
		oCommandRecvSocket.bind(m_strCommandInAddr);
		oCommandRecvSocket.subscribe("".getBytes());

		m_oCommandHandler.setupConnections(oCommandRecvSocket, oCommandSendSocket);
		m_oCommandHandler.addIncomingMessageListener(new ZmqMessageListener() {

			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oCommandHandler.sendZmsg(i_oMsg);
			}
		});

	}

	public void setupVideoConnections() {

		m_strVideoOutAddr = ZmqTypes.VIDEO_ADDRESS + "/out";
		ZMQ.Socket oVideoSendSocket = createSocket(ZMQ.PUB);
		oVideoSendSocket.setHWM(1);
		oVideoSendSocket.bind(m_strVideoOutAddr);

		m_strVideoInAddr = ZmqTypes.VIDEO_ADDRESS + "/in";
		ZMQ.Socket oVideoRecvSocket = createSocket(ZMQ.SUB);
		oVideoRecvSocket.setHWM(1);
		oVideoRecvSocket.bind(m_strVideoInAddr);
		oVideoRecvSocket.subscribe("".getBytes());

		m_oVideoHandler.setupConnections(oVideoRecvSocket, oVideoSendSocket);
		m_oVideoHandler.addIncomingMessageListener(new ZmqMessageListener() {

			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oVideoHandler.sendZmsg(i_oMsg);
			}
		});

	}

	public void setupSensorsConnections() {

		m_strSensorsOutAddr = ZmqTypes.SENSORS_ADDRESS + "/out";
		ZMQ.Socket oSensorsSendSocket = createSocket(ZMQ.PUB);
		//		oSensorsSendSocket.setHWM(1);
		oSensorsSendSocket.bind(m_strSensorsOutAddr);

		m_strSensorsInAddr = ZmqTypes.SENSORS_ADDRESS + "/in";
		ZMQ.Socket oSensorsRecvSocket = createSocket(ZMQ.SUB);
		//		oSensorsRecvSocket.setHWM(1);
		oSensorsRecvSocket.bind(m_strSensorsInAddr);
		oSensorsRecvSocket.subscribe("".getBytes());

		m_oSensorsHandler.setupConnections(oSensorsRecvSocket, oSensorsSendSocket);
		m_oSensorsHandler.addIncomingMessageListener(new ZmqMessageListener() {

			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oSensorsHandler.sendZmsg(i_oMsg);
			}
		});

	}

	public ZContext getContext() {
		return m_oZmqContext;
	}

	private void setSettings(ZmqSettings i_oSettings) {
		m_oSettings = i_oSettings;
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
		socket.setHWM(1);
		socket.connect(m_strVideoInAddr);
		return socket;
	}

	// creates and returns a socket from which an internal module
	// can receive video (connects to the Out Socket of the Video Handler)
	public ZMQ.Socket obtainVideoRecvSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.SUB);
		socket.setHWM(1);
		socket.connect(m_strVideoOutAddr);
		return socket;
	}

	public ZmqMessageHandler getSensorsHandler() {
		return m_oSensorsHandler;
	}

	public ZMQ.Socket obtainSensorsSendSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.PUB);
		socket.setHWM(1);
		socket.connect(m_strSensorsInAddr);
		return socket;
	}

	public ZMQ.Socket obtainSensorsRecvSocket() {
		ZMQ.Socket socket = createSocket(ZMQ.SUB);
		socket.setHWM(1);
		socket.connect(m_strSensorsOutAddr);
		return socket;
	}

}
