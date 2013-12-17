package robots.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.video.ZmqVideoReceiver;
import org.dobots.zmq.video.gui.VideoHelper;
import org.zeromq.ZMQ;

import robots.ctrl.IRobotDevice;
import android.view.ViewGroup;

public class VideoSensorGatherer extends SensorGatherer {

	protected IRobotDevice mRobot;
	
	protected ZmqVideoReceiver m_oVideoDisplayer;
	protected VideoHelper mVideoHelper;

	public VideoSensorGatherer(BaseActivity i_oActivity, IRobotDevice i_oRobot, String i_strThreadName) {
		super(i_oActivity, i_strThreadName);
		mRobot = i_oRobot;
		
		mVideoHelper = new VideoHelper(i_oActivity, (ViewGroup)m_oActivity.findViewById(R.id.layCameraContainer));
		//        mVideoHelper.setDisplayMode(DisplayMode.NORMAL);
	}

	public void resetLayout() {
		mVideoHelper.resetLayout();
	}

	public void startVideo() {
		mVideoHelper.onStartVideo();                
		setVideoListening(true);
	}

	public void stopVideo() {
		mVideoHelper.onStopVideo();
		setVideoListening(false);
	}
	
	public boolean isStopped() {
		return mVideoHelper.isStopped();
	}
	
	public void setVideoScaled(boolean scaled) {
		// TODO
	}
	
	public boolean isVideoScaled() {
		// TODO
		return false;
	}
	
	public void onConnect() {
		startVideo();
	}

	public void onDisconnect() {
		stopVideo();
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
		oVideoRecvSocket.subscribe(mRobot.getID().getBytes());

		// start a video display thread which receives video frames from the socket and displays them
		m_oVideoDisplayer = new ZmqVideoReceiver(oVideoRecvSocket);
		m_oVideoDisplayer.setRawVideoListener(mVideoHelper);
		m_oVideoDisplayer.setFPSListener(mVideoHelper);
		m_oVideoDisplayer.start();
	}

	@Override
	public void shutDown() {
		if (m_oVideoDisplayer != null) {
			m_oVideoDisplayer.close();
		}

		if (mVideoHelper != null) {
			mVideoHelper.destroy();
			mVideoHelper = null;
		}
	}

}
