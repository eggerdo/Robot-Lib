package robots.rover.ac13.gui;

import org.dobots.communication.video.IRawVideoListener;
import org.dobots.utilities.BaseActivity;

import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.gui.RoverBaseSensorGatherer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * This class provides foremost a function onFrame() with which it implements IRawVideoListener. This interface can be
 * used in the corresponding controller after the image has been actually extracted from for example a TCP message or
 * converted from another format. 
 */
public class AC13RoverSensorGatherer extends RoverBaseSensorGatherer implements IRawVideoListener {
	
	public static final String TAG = "AC13RoverSensorGatherer";

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
	public void onFrame(byte[] rgb, int rotation) {

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
					
					if (bmp != null) {
						m_ivVideo.setImageBitmap(bmp);
					} else {
						Log.w(TAG, "decode failed");
					}

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
