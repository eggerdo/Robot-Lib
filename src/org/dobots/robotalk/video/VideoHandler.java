package org.dobots.robotalk.video;

import java.util.HashMap;

import org.dobots.robotalk.zmq.ZmqForwarderThread;
import org.dobots.robotalk.zmq.ZmqReceiveThread;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class VideoHandler {
	
	private static final String TAG = "VideoHandler";
	
	private static VideoHandler INSTANCE;

	ZContext m_oZContext;

	// the channel used to send our own video
	private ZMQ.Socket m_oExt_VideoOut = null;
	private ZMQ.Socket m_oExt_VideoIn = null;
	
	private String m_strInt_VideoAddr = null;
	private ZMQ.Socket m_oInt_VideoOut = null;

    private boolean m_bDebug;
    
	private HashMap<String, VideoPublisherEntry> m_mVideoPublisher;

	private ZmqForwarderThread m_oVideoRecvThread;

	private boolean m_bReceiverConnected;

	private boolean m_bSenderConnected;
	
	public VideoHandler(ZContext i_oContext) {
		m_oZContext = i_oContext;
		
		INSTANCE = this;
		
		m_mVideoPublisher = new HashMap<String, VideoPublisherEntry>();
		
		m_strInt_VideoAddr = "inproc://video_out";
		m_oInt_VideoOut = m_oZContext.createSocket(ZMQ.PUB);
		m_oInt_VideoOut.bind(m_strInt_VideoAddr);
		m_oInt_VideoOut.setHWM(20);
	}

	public static VideoHandler getInstance() {
		return INSTANCE;
	}
	
	public String getIntVideoAddr() {
		return m_strInt_VideoAddr;
	}
	
	public void setDebug(boolean i_bDebug) {
		m_bDebug = i_bDebug;
	}

	/**
	 * If only incoming video should be handled, supply null as the OutSockete parameter (or vice versa)
	 * @param i_oInSocket
	 * @param i_oOutSocket
	 */
	public void setupConnections(ZMQ.Socket i_oInSocket, ZMQ.Socket i_oOutSocket) {
		
		if (i_oInSocket != null) {
			m_oExt_VideoIn = i_oInSocket;

			m_oVideoRecvThread = new ZmqForwarderThread(m_oZContext.getContext(), m_oExt_VideoIn, m_oInt_VideoOut, "VideoReceiver");
			m_oVideoRecvThread.start();
			
			m_bReceiverConnected = true;
		}
		
		if (i_oOutSocket != null) {
			m_oExt_VideoOut = i_oOutSocket;
			
			m_bSenderConnected = true;
		}
	}
	
	public void closeConnections() {

		m_bReceiverConnected = false;
		m_bSenderConnected = false;
		
		if (m_oVideoRecvThread != null) {
			m_oVideoRecvThread.interrupt();
		}

		if (m_oExt_VideoOut != null) {
			m_oExt_VideoOut.close();
			m_oExt_VideoOut = null;
		}

		if (m_oExt_VideoIn != null) {
			m_oExt_VideoIn.close();
			m_oExt_VideoIn = null;
		}

	}
	
	public void destroy() {

		for (VideoPublisherEntry entry : m_mVideoPublisher.values()) {
			entry.destroy();
		}
		m_mVideoPublisher.clear();
		
	}

	private class VideoPublisherEntry {
		public ZMQ.Socket inSocket;
		public Thread streamer;
		
		public VideoPublisherEntry(ZMQ.Socket i_oInSocket, Thread i_oStreamer) {
			inSocket = i_oInSocket;
			streamer = i_oStreamer;
		}
		
		public void destroy() {
			streamer.interrupt();
			inSocket.close();
		}
	}
	
	public void publishVideo(String i_strVideoAddr) {
		
		ZMQ.Socket oSocket = m_oZContext.createSocket(ZMQ.PULL);
		oSocket.connect(i_strVideoAddr);
		
		// it would be possible to use the ZMQStreamer class for forwarding ZMsg, however because
		// we want to have to possibility to change the connection to the outside, without having
		// to recreate all the forwarder threads we implemented our own forwarder
		
		//		ZMQStreamer oZmqStreamer = new ZMQStreamer(m_oZContext.getContext(), oSocket, m_oVideoSender);
		//		Thread oStreamer = new Thread(oZmqStreamer);
		//		oStreamer.start();
		
		ZMQForwarderThread oStreamer = new ZMQForwarderThread(oSocket);
		oStreamer.start();
		
		VideoPublisherEntry entry = new VideoPublisherEntry(oSocket, oStreamer);
		m_mVideoPublisher.put(i_strVideoAddr, entry);
		
	}
	
	public void unpublishVideo(String i_strVideoAddr) {
		
		VideoPublisherEntry entry = m_mVideoPublisher.get(i_strVideoAddr);
		if (entry != null) {
			entry.destroy();
		}
		
	}
	
	class ZMQForwarderThread extends ZmqReceiveThread {
		
		public ZMQForwarderThread(ZMQ.Socket i_oInSocket) {
			super(m_oZContext.getContext(), i_oInSocket, "ZMQForwarder");
		}
		
		@Override
		protected void execute() {
			ZMsg msg = ZMsg.recvMsg(m_oInSocket);
			
			// while disconnected from external "world"
			// skip sending messages
			if (msg != null && m_bSenderConnected) {
				msg.send(m_oExt_VideoOut);
			}
		}
		
	}

}
