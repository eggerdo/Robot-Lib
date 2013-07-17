package org.dobots.communication.video;

import org.dobots.communication.msg.VideoMessage;
import org.dobots.communication.zmq.ZmqReceiveThread;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class VideoDisplayThread extends ZmqReceiveThread {
	
	public interface VideoListener {
		/**
		 * NOTE THAT CHANGING UI ELEMENTS HAVE TO BE DONE IN THE MAIN THREAD, HOWEVER
		 * THIS FUNCTION IS NOT CALLED IN THE MAIN THREAD.
		 * @param i_oBmp
		 */
		public void onFrame(byte[] rgb, int rotation);
	}
	
	public interface IVideoListener {
		/**
		 * NOTE THAT CHANGING UI ELEMENTS HAVE TO BE DONE IN THE MAIN THREAD, HOWEVER
		 * THIS FUNCTION IS NOT CALLED IN THE MAIN THREAD.
		 * @param i_oBmp
		 */
		public void onFrame(Bitmap bmp, int rotation);
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
	public IVideoListener m_oBmpListener;
	
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
			VideoMessage oVideoMsg = VideoMessage.fromZMsg(oMsg);
			
			if (oVideoMsg != null) {
				
				(new BmpDecoder()).execute(oVideoMsg);

//				if (m_oVideoListener != null) {
//					m_oVideoListener.onFrame(oVideoMsg.getVideoData(), oVideoMsg.nRotation);
//				}
				
				// decoding the rgb array to a bmp slows down the zmq receiver.
				// we continue with the rgb array until we want to display. then we
				// can do the decoding in an AsyncTask
				//				if (m_oVideoListener != null) {
				//					Bitmap bmp = oVideoMsg.getAsBmp();
				//					m_oVideoListener.onFrame(bmp, oVideoMsg.nRotation);
				//				}
				//				if (m_oUiHandler != null) {
				//					Message uiMsg = m_oUiHandler.obtainMessage();
				//					uiMsg.what = VideoTypes.INCOMING_VIDEO_MSG;
				//					uiMsg.obj = bmp;
				//					m_oUiHandler.dispatchMessage(uiMsg);
				//				}
			
//	            ++m_nFpsCounter;
//	            long now = System.currentTimeMillis();
//	            if ((now - m_lLastTime) >= 1000)
//	            {
//	            	if (m_oFPSListener != null) {
//	            		m_oFPSListener.onFPS(m_nFpsCounter);
//	            	}
//	            	if (m_oUiHandler != null) {
//	            		Message uiMsg = m_oUiHandler.obtainMessage();
//	    	        	uiMsg.what = VideoTypes.SET_FPS;
//	    	        	uiMsg.obj = m_nFpsCounter;
//	    	        	m_oUiHandler.dispatchMessage(uiMsg);
//	            	}
//		            
//	                m_lLastTime = now;
//	                m_nFpsCounter = 0;
//	            }
			}
		}
	}

	private class BmpDecoder extends AsyncTask<VideoMessage, Integer, VideoMessage> {

		@Override
		protected VideoMessage doInBackground(VideoMessage... params) {
			params[0].getAsBmp();
			return params[0];
		}

		@Override
		protected void onPostExecute(VideoMessage result) {

			if (m_oBmpListener != null) {
				m_oBmpListener.onFrame(result.getAsBmp(), result.getRotation());
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
	
	public void setVideoListner(VideoListener i_oListener) {
		m_oVideoListener = i_oListener;
	}
	
	public void setFPSListener(FPSListener i_oListener) {
		m_oFPSListener = i_oListener;
	}
	
}