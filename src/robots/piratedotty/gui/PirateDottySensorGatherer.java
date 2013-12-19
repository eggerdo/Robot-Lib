package robots.piratedotty.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;

import android.view.View;
import android.view.ViewGroup;

import robots.gui.VideoSensorGatherer;
import robots.piratedotty.ctrl.IPirateDotty;

public class PirateDottySensorGatherer extends VideoSensorGatherer {
	
	private ViewGroup m_layCameraContainer;
	
	public PirateDottySensorGatherer(BaseActivity i_oActivity, IPirateDotty i_oPirateDotty) {
		super(i_oActivity, i_oPirateDotty, "PirateDottySensorGatherer");
		m_layCameraContainer = (ViewGroup)m_oActivity.findViewById(R.id.layCameraContainer);
	}
	
	@Override
	public void startVideoPlayback() {
		m_layCameraContainer.setVisibility(View.VISIBLE);
		super.startVideoPlayback();
	}
	
	@Override
	public void stopVideoPlayback() {
		super.stopVideoPlayback();
		m_layCameraContainer.setVisibility(View.GONE);
	}

}
