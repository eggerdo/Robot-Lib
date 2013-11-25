package org.dobots.communication.video;

import org.dobots.communication.msg.VideoMessage;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqReceiveThread;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import android.os.AsyncTask;
import android.util.Log;

/**
 * The VideoDisplayThread supports two different video listeners:
 * 		1. RawVideoListener
 * 				On reception of a new frame, the onFrame callback will be triggered which
 * 				sends the frame as an byte array
 * 				Note: this will not be called in the main UI thread
 * 		2. VideoListener
 * 				On reception of a new frame, the onFrame callback will be triggered which
 * 				sends the frame as a bitmap
 * 				Note: this is called in the main UI thread and can be displayed directly.
 * 
 * It also provides a fps listener for debug purposes which is NOT called in the main UI thread.
 * 
 * Note that UI elements can only be changed in the main thread. so if the callback is not done
 * in the main thread directly, the callback function has to defer the work to the main thread
 * see Utils.runAsyncUITask
 * 
 */
public class ZmqVideoReceiver extends ZmqReceiveThread {
	
	private IRawVideoListener m_oRawVideoListener;
	private IVideoListener m_oVideoListener;
	
	private FpsCounter mFpsCounter = new FpsCounter();
	
	private FpsCounter mFpsInCounter = new FpsCounter();

	public ZmqVideoReceiver(Socket i_oInSocket) {
		super(ZmqHandler.getInstance().getContext().getContext(), i_oInSocket, "VideoReceiver");
		
		mFpsInCounter.setListener(new IFpsListener() {
			
			@Override
			public void onFPS(int i_nFPS) {
				Log.w("VideoDisplayThread", String.format("FPS in: %d", i_nFPS));
			}
		});
	}
	
	@Override
	protected void execute() {
		ZMsg oMsg = ZMsg.recvMsg(m_oInSocket);
		if (oMsg != null) {
			// create a video message out of the zmq message
			VideoMessage oVideoMsg = null;
			try {
				oVideoMsg = VideoMessage.fromZMsg(oMsg);
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
			if (oVideoMsg != null) {
				
				if (m_oVideoListener != null) {
					(new FrameDecoder()).execute(oVideoMsg);
				}
				
				if (m_oRawVideoListener != null) {
					m_oRawVideoListener.onFrame(oVideoMsg.getVideoData(), oVideoMsg.getRotation());
					
					// notify the fps counter about the new frame
					mFpsCounter.tick();
				}
			}
			
			mFpsInCounter.tick();
		}
	}

	private class FrameDecoder extends AsyncTask<VideoMessage, Integer, VideoMessage> {
		
		@Override
		protected VideoMessage doInBackground(VideoMessage... params) {
			// this will trigger the frame to be decoded as a bmp
			params[0].getAsBmp();
			return params[0];
		}

		@Override
		protected void onPostExecute(VideoMessage result) {

			if (m_oVideoListener != null) {
				// because we triggered the decoding in the background task we can now
				//  obtain the bmp
				m_oVideoListener.onFrame(result.getAsBmp(), result.getRotation());
			}
			
			// notify the fps counter about the new frame
			mFpsCounter.tick();
		}
	}
	
	public void setRawVideoListner(IRawVideoListener i_oListener) {
		m_oRawVideoListener = i_oListener;
	}
	
	public void setVideoListener(IVideoListener i_oListener) {
		m_oVideoListener = i_oListener;
	}
	
	public void setFPSListener(IFpsListener i_oListener) {
		mFpsCounter.setListener(i_oListener);
	}
	
}