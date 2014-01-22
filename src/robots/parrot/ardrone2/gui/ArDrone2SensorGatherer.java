package robots.parrot.ardrone2.gui;

import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.video.IRawVideoListener;

import android.view.View;
import robots.parrot.ardrone2.ctrl.IArDrone2;
import robots.parrot.ctrl.IParrot;
import robots.parrot.gui.ParrotSensorGatherer;

public class ArDrone2SensorGatherer extends ParrotSensorGatherer implements IRawVideoListener {

	public ArDrone2SensorGatherer(BaseActivity i_oActivity, IParrot i_oParrot) {
		super(i_oActivity, i_oParrot);
	}

	@Override
	public void stopVideoPlayback() {
		super.stopVideoPlayback();
		
		// remove the image view from the video renderer, hide it
		// and display instead the elements provided by the VideoHelper
		((IArDrone2)m_oParrot).setVideoView(null);
		m_ivVideo.setVisibility(View.GONE);
		m_layCameraContainer.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void startVideoPlayback() {
		super.startVideoPlayback();

		// we use the video sent over zmq only to detect when the video
		// starts and can be displayed. once the first frame is received
		// we disconnect from the zmq video feed and instead let the 
		// ParrotVideoRenderer use the ImageView directly. To do so we
		// override the video listener set by the VideoSensorGatherer
		// to this
		m_oVideoDisplayer.setRawVideoListener(this);
	}

	@Override
	public void onFrame(byte[] rgb, int rotation) {
		// once the first frame is received we stop the
		// zmq video feed, give the image view to the
		// parrot and the video renderer, set the image view
		// to visible and hide the rest.
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// give the image view to the video renderer
				((IArDrone2)m_oParrot).setVideoView(m_ivVideo);
				
				// UI operations have to be handled by the UI thread
				m_ivVideo.setVisibility(View.VISIBLE);
				m_layCameraContainer.setVisibility(View.GONE);
			}
		});
		
		// stop the zmq video feed
		super.stopVideoPlayback();
	}
	
}
