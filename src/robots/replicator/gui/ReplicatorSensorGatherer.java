package robots.replicator.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.video.ZmqVideoReceiver;
import org.dobots.zmq.video.gui.VideoHelper;
import org.zeromq.ZMQ;

import robots.gui.SensorGatherer;
import robots.replicator.ctrl.Replicator;
import android.widget.LinearLayout;

/**
 * The ReplicatorSensorGatherer used to derive from the same thing as the SpyTank, but it's smarter to use the
 * AC13 thing as inspiration.
 * 
 * @author Anne C. van Rossum
 * @date Aug. 22, 2013
 * @license LGPLv3
 * @copyright Distributed Organisms B.V., Rotterdam, The Netherlands
 */
public class ReplicatorSensorGatherer extends SensorGatherer {

	public static final String TAG = "ReplicatorSensorGatherer";

	private Replicator m_oReplicator;
	
	private LinearLayout m_layVideo;

	private ZmqVideoReceiver m_oVideoDisplayer;
	
	private VideoHelper mVideoHelper;
	private ZMQ.Socket m_oVideoRecvSocket;
	
	private boolean m_bVideoEnabled = true;
	
	private boolean m_bDebug = false;
	
	public ReplicatorSensorGatherer(BaseActivity i_oActivity, Replicator i_oReplicator) {
		super(i_oActivity, "ReplicatorSensorGatherer");
		m_oReplicator = i_oReplicator;
		
		setProperties();
		
		setupVideoDisplay();
	}
	
	protected void setProperties() {
		m_layVideo = (LinearLayout) m_oActivity.findViewById(R.id.layVideo);
	}
	
	public void resetLayout() {
//		mVideoHelper.resetLayout();
	}

	public void setVideoEnabled(boolean i_bEnabled) {
		m_bVideoEnabled = i_bEnabled;
		
		if (i_bEnabled) {
			startVideo();
		} else {
			stopVideo();
		}
	}

    private void setupVideoDisplay() {
        mVideoHelper = new VideoHelper(m_oActivity, m_layVideo);
        mVideoHelper.setDebug(m_bDebug);
	}

    private void startVideo() {
		m_oReplicator.switchCameraOn();

		m_oVideoRecvSocket = ZmqHandler.getInstance().obtainVideoRecvSocket();
		m_oVideoRecvSocket.subscribe(m_oReplicator.getID().getBytes());
		
		// start a video display thread which receives video frames from the socket and displays them
		m_oVideoDisplayer = new ZmqVideoReceiver(m_oVideoRecvSocket);
		m_oVideoDisplayer.setRawVideoListener(mVideoHelper);
		m_oVideoDisplayer.setFPSListener(mVideoHelper);
		m_oVideoDisplayer.start();
		
		mVideoHelper.onStartVideoPlayback(false);
    }

    private void stopVideo() {
		m_oReplicator.switchCameraOff();
    	
		if (m_oVideoDisplayer != null) {
			m_oVideoDisplayer.setVideoListener(null);
			m_oVideoDisplayer.close();
			m_oVideoDisplayer = null;
		}

    	mVideoHelper.onStopVideoPlayback();
    	
    	if (m_oVideoRecvSocket != null) {
    		m_oVideoRecvSocket.close();
    		m_oVideoRecvSocket = null;
    	}
    }

    
	public void onConnect() {
		setVideoEnabled(m_bVideoEnabled);
	}

	public void onDisconnect() {
		stopVideo();
	}
	
	@Override
	public void shutDown() {
		stopVideo();
	}

}
