package robots.rover.ac13.gui;

import org.dobots.utilities.BaseActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import robots.IVideoListener;
import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.gui.RoverBaseSensorGatherer;

public class AC13RoverSensorGatherer extends RoverBaseSensorGatherer implements IVideoListener {

	// debug frame counters
    int m_nFpsCounter = 0;
    long m_lLastTime = System.currentTimeMillis();

	public AC13RoverSensorGatherer(BaseActivity i_oActivity, AC13Rover i_oRover) {
		super(i_oActivity, i_oRover, "AC13RoverSensorGatherer");
	}

	@Override
	protected void startVideo() {
		super.startVideo();
		m_oRover.setVideoListener(this);
	}

	@Override
	protected void stopVideo() {
		super.stopVideo();
		m_oRover.removeVideoListener(this);
	}

	@Override
	public void frameReceived(byte[] rgb) {

		final Bitmap bmp = BitmapFactory.decodeByteArray(rgb, 0, rgb.length);
		
//		RawVideoMessage vmsg = new RawVideoMessage(m_oRover.getType().toString(), bmp, 0);
//		RobotVideoMessage oMsg = new RobotVideoMessage(vmsg.getRobotID(), vmsg.getHeader(), vmsg.getVideoData());
//		ZMsg zmsg = oMsg.toZmsg();
//		zmsg.send(videoSocket);

		if (m_bVideoEnabled) {
			m_oSensorDataUiUpdater.post(new Runnable() {
				@Override
				public void run() {

					if (!m_bVideoConnected) {
						m_oSensorDataUiUpdater.removeCallbacks(m_oTimeoutRunnable);
						m_bVideoConnected = true;
						showVideoLoading(false);
					}
					
					m_ivVideo.setImageBitmap(bmp);

		            ++m_nFpsCounter;
		            long now = System.currentTimeMillis();
		            if ((now - m_lLastTime) >= 1000)
		            {
						m_lblFPS.setText("FPS: " + String.valueOf(m_nFpsCounter));
			            
		                m_lLastTime = now;
		                m_nFpsCounter = 0;
		            }
				}
			});
		}
	}
	
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
