package org.dobots.robotalk.video;

import org.dobots.robotalk.msg.BaseVideoMessage;
import org.dobots.robotalk.msg.RawVideoMessage;
import org.dobots.robotalk.msg.RobotVideoMessage;
import org.dobots.robotalk.zmq.ZmqReceiveThread;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class VideoDisplayThread extends ZmqReceiveThread {
	
	public interface VideoListener {
		/**
		 * NOTE THAT CHANGING UI ELEMENTS HAVE TO BE DONE IN THE MAIN THREAD, HOWEVER
		 * THIS FUNCTION IS NOT CALLED IN THE MAIN THREAD.
		 * @param i_oBmp
		 */
		public void onFrame(Bitmap i_oBmp, int i_nRotation);
	}
	
	public interface FPSListener {
		/**
		 * NOTE THAT CHANGING UI ELEMENTS HAVE TO BE DONE IN THE MAIN THREAD, HOWEVER
		 * THIS FUNCTION IS NOT CALLED IN THE MAIN THREAD.
		 * @param i_nFPS
		 */
		public void onFPS(int i_nFPS);
	}
	
	private VideoListener m_oVideoListener;
	private FPSListener m_oFPSListener;
	
	private Handler m_oUiHandler;

	// debug frame counters
    int m_nFpsCounter = 0;
    long m_lLastTime = System.currentTimeMillis();

	public VideoDisplayThread(ZMQ.Context i_oContext, Socket i_oInSocket, Handler i_oUiHandler) {
		super(i_oContext, i_oInSocket, "VideoDisplayer");
		
		m_oUiHandler = i_oUiHandler;
	}

	public VideoDisplayThread(ZMQ.Context i_oContext, Socket i_oInSocket) {
		super(i_oContext, i_oInSocket, "VideoDisplayer");
	}

	@Override
	protected void execute() {
		ZMsg oMsg = ZMsg.recvMsg(m_oInSocket);
		if (oMsg != null) {
			// create a video message out of the zmq message
			RobotVideoMessage oVideoMsg = RobotVideoMessage.fromZMsg(oMsg);
			BaseVideoMessage oBaseVideoMsg = BaseVideoMessage.decodeVideoMessage(oVideoMsg.header, oVideoMsg.data);
			
			if (oBaseVideoMsg instanceof RawVideoMessage) {
				
				Bitmap bmp = oBaseVideoMsg.getAsBmp();
	
//				// decode the received frame from jpeg to a bitmap
//				ByteArrayInputStream stream = new ByteArrayInputStream(oBaseVideoMsg.getVideoData());
//				Bitmap bmp = BitmapFactory.decodeStream(stream);
				
				if (m_oVideoListener != null) {
					m_oVideoListener.onFrame(bmp, oBaseVideoMsg.nRotation);
				}
				if (m_oUiHandler != null) {
					Message uiMsg = m_oUiHandler.obtainMessage();
					uiMsg.what = VideoTypes.INCOMING_VIDEO_MSG;
					uiMsg.obj = bmp;
					m_oUiHandler.dispatchMessage(uiMsg);
				}
			
	            ++m_nFpsCounter;
	            long now = System.currentTimeMillis();
	            if ((now - m_lLastTime) >= 1000)
	            {
	            	if (m_oFPSListener != null) {
	            		m_oFPSListener.onFPS(m_nFpsCounter);
	            	}
	            	if (m_oUiHandler != null) {
	            		Message uiMsg = m_oUiHandler.obtainMessage();
	    	        	uiMsg.what = VideoTypes.SET_FPS;
	    	        	uiMsg.obj = m_nFpsCounter;
	    	        	m_oUiHandler.dispatchMessage(uiMsg);
	            	}
		            
	                m_lLastTime = now;
	                m_nFpsCounter = 0;
	            }
			}
		}
	}
	
	public void setVideoListner(VideoListener i_oListener) {
		m_oVideoListener = i_oListener;
	}
	
	public void setFPSListener(FPSListener i_oListener) {
		m_oFPSListener = i_oListener;
	}
	
}