package robots.rover.ac13.gui;

import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.video.FpsCounter;
import org.dobots.zmq.video.IFpsListener;
import org.dobots.zmq.video.IRawVideoListener;
import org.dobots.zmq.video.ZmqVideoReceiver;
import org.zeromq.ZMQ;

import robots.rover.ac13.ctrl.IAC13Rover;
import robots.rover.base.gui.RoverBaseSensorGatherer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This class provides foremost a function onFrame() with which it implements IRawVideoListener. This interface can be
 * used in the corresponding controller after the image has been actually extracted from for example a TCP message or
 * converted from another format. 
 */
public class AC13RoverSensorGatherer extends RoverBaseSensorGatherer {
	
	public static final String TAG = "AC13RoverSensorGatherer";

	private ZmqVideoReceiver m_oVideoDisplayer;

	public AC13RoverSensorGatherer(BaseActivity i_oActivity, IAC13Rover i_oRover) {
		super(i_oActivity, i_oRover, "AC13RoverSensorGatherer");
		mVideoHelper.setVideoTimeout(false);
	}

	@Override
	protected void startVideo() {
		super.startVideo();
		setVideoListening(true);
	}

	@Override
	protected void stopVideo() {
		super.stopVideo();
		setVideoListening(false);
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
		oVideoRecvSocket.subscribe(m_oRover.getID().getBytes());

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
