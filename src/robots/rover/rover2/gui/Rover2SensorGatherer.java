package robots.rover.rover2.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.video.ZmqVideoReceiver;
import org.zeromq.ZMQ;

import robots.rover.base.gui.RoverBaseSensorGatherer;
import robots.rover.rover2.ctrl.IRover2;
import android.widget.TextView;

public class Rover2SensorGatherer extends RoverBaseSensorGatherer {

	private ZmqVideoReceiver m_oVideoDisplayer;
	private TextView m_txtBattery;

	public Rover2SensorGatherer(BaseActivity i_oActivity, IRover2 i_oRover) {
		super(i_oActivity, i_oRover, "Rover2SensorGatherer");
		
		setProperties();
	}
	
	protected void setProperties() {
    	m_txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBattery);
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
