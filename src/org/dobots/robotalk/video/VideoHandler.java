package org.dobots.robotalk.video;

import org.dobots.robotalk.zmq.ZmqSettings;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQStreamer;
import org.zeromq.ZMsg;

public class VideoHandler {

	private ZContext m_oZContext;
	private ZmqSettings m_oSettings;

	// the channel used to send our own video
	private ZMQ.Socket m_oVideoSender = null;
	
	private ZMQ.Socket m_oVideoReceiver = null;
	
	private ZMQStreamer m_oStreamer = null;
	
	// debug frame counters
    private int m_nFpsCounterUser = 0;
    private long m_lLastTimeUser = System.currentTimeMillis();

    private boolean m_bDebug;
    
    // on startup don't display the video of anybody, but wait until a partner is selected
    private String m_strPartner = "nobody";
    
	private boolean m_bConnected;
    
	public VideoHandler(ZContext i_oContext, ZmqSettings i_oSettings) {
		m_oZContext = i_oContext;
		m_oSettings = i_oSettings;
	}

	public String getPartner() {
		return m_strPartner;
	}
	
	public void setDebug(boolean i_bDebug) {
		m_bDebug = i_bDebug;
	}
	
	private void sendVideoMessage(byte[] rgb) {
		
		if (m_bConnected) {
			// create a video message from the rgb data
			VideoMessage videoMsg = new VideoMessage(m_oSettings.getRobotName(), rgb);
			// make a zmq message
			ZMsg outMsg = videoMsg.toZmsg();
			// send the zmq message out
			outMsg.send(m_oVideoSender);
		}
	}

	public void setupConnections() {

		if (!m_bConnected) {
			m_oVideoSender = m_oZContext.createSocket(ZMQ.PUB);
			m_oVideoReceiver = m_oZContext.createSocket(ZMQ.PULL);
	
			// obtain video ports from settings
			// receive port is always equal to send port + 1
			int nVideoSendPort = m_oSettings.getVideoPort();
			
			// set the output queue size down, we don't really want to have old video frames displayed
			// we only want the most recent ones
			m_oVideoSender.setHWM(20);
	
			m_oVideoSender.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nVideoSendPort));
			
			m_oVideoReceiver.bind("inproc://video");
			
			(new VideoReceiveThread()).start();
	
	//		m_oStreamer = new ZMQStreamer(m_oZContext.getContext(), m_oVideoReceiver, m_oVideoSender);
			
			m_bConnected = true;
		}
		
	}

	class VideoReceiveThread extends Thread {

		public boolean bRun = true;
		
		@Override
		public void run() {
			while(bRun) {
				ZMsg msg = ZMsg.recvMsg(m_oVideoReceiver);
				if (msg != null) {
					msg.send(m_oVideoSender);
				}
			}
		}
		
	}

	public void closeConnections() {
		
		if (m_oVideoSender != null) {
			m_oVideoSender.close();
			m_oVideoSender = null;
		}

		if (m_oVideoReceiver != null) {
			m_oVideoReceiver.close();
			m_oVideoReceiver = null;
		}

		if (m_oStreamer != null) {
			m_oStreamer = null;
		}
		
		m_bConnected = false;
	}
	
}
