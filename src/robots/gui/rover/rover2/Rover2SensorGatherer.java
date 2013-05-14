package robots.gui.rover.rover2;

import org.dobots.robotalk.video.VideoDisplayThread;
import org.dobots.robotalk.video.VideoDisplayThread.FPSListener;
import org.dobots.robotalk.video.VideoDisplayThread.VideoListener;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.utilities.BaseActivity;
import org.zeromq.ZMQ;

import android.graphics.Bitmap;

import robots.ctrl.rover.rover2.Rover2;
import robots.gui.rover.RoverBaseSensorGatherer;

public class Rover2SensorGatherer extends RoverBaseSensorGatherer { // implements VideoListener, FPSListener {

	private VideoDisplayThread m_oVideoDisplayer;

	public Rover2SensorGatherer(BaseActivity i_oActivity, Rover2 i_oRover) {
		super(i_oActivity, i_oRover, "Rover2SensorGatherer");
	}

//	@Override
//	public void setVideoEnabled(boolean i_bVideoEnabled) {
//		
//		m_bVideoEnabled = i_bVideoEnabled;
//		if (m_bVideoEnabled) {
////			m_oRover.setVideoListener(this);
//			setVideoListening(true);
//			m_oRover.startVideo();
//		} else {
//			setVideoListening(false);
////			m_oRover.removeVideoListener(this);
//			m_oRover.stopVideo();
//		}
//		
//		if (i_bVideoEnabled) {
//			startVideo();
//		} else {
//			m_bVideoConnected = false;
//			showVideoMsg("Video OFF");
//		}
//	}
//	
//	private void setVideoListening(boolean listening) {
//		if (listening) {
//			setupVideoDisplay();
//		} else {
//			m_oVideoDisplayer.close();
//		}
//	}
//
//    private void setupVideoDisplay() {
//    	
//		ZMQ.Socket oVideoRecvSocket = ZmqHandler.getInstance().obtainVideoRecvSocket();
//		oVideoRecvSocket.subscribe("".getBytes());
//
//		// start a video display thread which receives video frames from the socket and displays them
//		m_oVideoDisplayer = new VideoDisplayThread(ZmqHandler.getInstance().getContext().getContext(), oVideoRecvSocket);
//		m_oVideoDisplayer.setVideoListner(this);
//		m_oVideoDisplayer.setFPSListener(this);
//		m_oVideoDisplayer.start();
//		
//	}
//
//	@Override
//	public void onFPS(int i_nFPS) {
//		m_lblFPS.setText("FPS: " + String.valueOf(i_nFPS));
//	}
//
//	@Override
//	public void onFrame(Bitmap i_oBmp, int i_nRotation) {
//
//		if (!m_bVideoConnected) {
//			m_oSensorDataUiUpdater.removeCallbacks(m_oTimeoutRunnable);
//			m_bVideoConnected = true;
//			showVideoLoading(false);
//		}
//		
//		m_ivVideo.setImageBitmap(i_oBmp);
//
//	}
    
}
