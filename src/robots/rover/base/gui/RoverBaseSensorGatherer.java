package robots.rover.base.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.video.gui.VideoHelper;
import org.dobots.zmq.video.gui.VideoSurfaceView.DisplayMode;

import robots.gui.SensorGatherer;
import robots.rover.base.ctrl.IRoverBase;
import robots.rover.base.ctrl.RoverBaseTypes.VideoResolution;
import android.os.Handler;
import android.view.ViewGroup;

public abstract class RoverBaseSensorGatherer extends SensorGatherer {

	protected IRoverBase m_oRover;

	protected VideoHelper mVideoHelper;
	
	protected final Handler m_oSensorDataUiUpdater = new Handler();
	
	public RoverBaseSensorGatherer(BaseActivity i_oActivity, IRoverBase i_oRover, String i_strThreadName) {
		super(i_oActivity, i_strThreadName);
		m_oRover = i_oRover;
		
//		videoSocket = ZmqHandler.getInstance().getContext().createSocket(ZMQ.PUSH);
//		videoSocket.bind("inproc://video");
		
		mVideoHelper = new VideoHelper(i_oActivity, (ViewGroup)m_oActivity.findViewById(R.id.layCameraContainer));
		mVideoHelper.setDisplayMode(DisplayMode.NORMAL);
		
		start();
	}
	
	public void resetLayout() {
		mVideoHelper.resetLayout();
	}

	protected void startVideo() {
		mVideoHelper.onStartVideoPlayback();
	}

	protected void stopVideo() {
		mVideoHelper.onStopVideoPlayback();
	}
		
	public void onConnect() {
		startVideo();
	}

	public void onDisconnect() {
		stopVideo();
	}
	
	public void setResolution(final VideoResolution i_eResolution) {
		m_oRover.setResolution(i_eResolution);
	}

}
