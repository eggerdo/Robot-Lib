package robots.piratedotty.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.video.gui.VideoSurfaceView.DisplayMode;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import robots.gui.VideoSensorGatherer;
import robots.piratedotty.ctrl.IPirateDotty;

public class PirateDottySensorGatherer extends VideoSensorGatherer {
	
	private ViewGroup m_layCameraContainer;
	private ImageButton m_btnCameraToggle;
	private PirateDottySail m_vSail;
	
	public PirateDottySensorGatherer(BaseActivity i_oActivity, IPirateDotty i_oPirateDotty) {
		super(i_oActivity, i_oPirateDotty, "PirateDottySensorGatherer");
		m_layCameraContainer = (ViewGroup)m_oActivity.findViewById(R.id.layCameraContainer);
		mVideoHelper.setDisplayMode(DisplayMode.FILLED);

		m_btnCameraToggle = (ImageButton) m_oActivity.findViewById(R.id.btnCameraToggle);
		m_btnCameraToggle.setVisibility(View.GONE);
		
		m_vSail = (PirateDottySail) m_oActivity.findViewById(R.id.vSail);
		m_vSail.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void startVideoPlayback() {
		m_layCameraContainer.setVisibility(View.VISIBLE);
		m_btnCameraToggle.setVisibility(View.VISIBLE);
		
		m_vSail.setVisibility(View.GONE);
		
		super.startVideoPlayback();
	}
	
	@Override
	public void stopVideoPlayback() {
		super.stopVideoPlayback();
		m_layCameraContainer.setVisibility(View.GONE);
		m_btnCameraToggle.setVisibility(View.GONE);
		
		m_vSail.setVisibility(View.VISIBLE);
	}

}
