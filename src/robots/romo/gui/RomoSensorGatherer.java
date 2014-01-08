package robots.romo.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.video.ZmqVideoReceiver;
import org.dobots.zmq.video.gui.VideoHelper;
import org.dobots.zmq.video.gui.VideoSurfaceView.DisplayMode;
import org.zeromq.ZMQ;

import robots.gui.SensorGatherer;
import android.view.ViewGroup;

public class RomoSensorGatherer extends SensorGatherer {

	private ZmqVideoReceiver m_oVideoDisplayer;
	private VideoHelper mVideoHelper;

	private String mRobotID;
	
	public RomoSensorGatherer(BaseActivity i_oActivity, String i_strRobotID) {
		super(i_oActivity, i_strRobotID + "-SensorGatherer");
		mRobotID = i_strRobotID;

		mVideoHelper = new VideoHelper(i_oActivity, (ViewGroup)m_oActivity.findViewById(R.id.layCameraContainer));
		mVideoHelper.setDisplayMode(DisplayMode.SCALED);
	}
	
	protected void startVideo() {
		setVideoListening(true);
		mVideoHelper.onStartVideoPlayback();
	}

	protected void stopVideo() {
		setVideoListening(false);
		mVideoHelper.onStopVideoPlayback();
	}
	
	private void setVideoListening(boolean i_bListening) {
		if (i_bListening) {
			setupVideoDisplay();
		} else {
			if (m_oVideoDisplayer != null) {
				m_oVideoDisplayer.close();
			}
		}
	}

    private void setupVideoDisplay() {
    	
		ZMQ.Socket oVideoRecvSocket = ZmqHandler.getInstance().obtainVideoRecvSocket();
		oVideoRecvSocket.subscribe(mRobotID.getBytes());

		// start a video display thread which receives video frames from the socket and displays them
		m_oVideoDisplayer = new ZmqVideoReceiver(oVideoRecvSocket);
		m_oVideoDisplayer.setFPSListener(mVideoHelper);
		m_oVideoDisplayer.setRawVideoListener(mVideoHelper);
		m_oVideoDisplayer.start();
	}

	@Override
	public void shutDown() {
		if (m_oVideoDisplayer != null) {
			m_oVideoDisplayer.close();
			m_oVideoDisplayer = null;
		}
		
		if (mVideoHelper != null) {
			mVideoHelper.destroy();
			mVideoHelper = null;
		}
	}

}
