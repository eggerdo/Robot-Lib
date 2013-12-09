package robots.rover.ac13.gui;

import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.video.FpsCounter;
import org.dobots.zmq.video.IFpsListener;
import org.dobots.zmq.video.IRawVideoListener;

import robots.rover.ac13.ctrl.IAC13Rover;
import robots.rover.base.gui.RoverBaseSensorGatherer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
    
    private FpsCounter mFpsCounter;

	public AC13RoverSensorGatherer(BaseActivity i_oActivity, IAC13Rover i_oRover) {
		super(i_oActivity, i_oRover, "AC13RoverSensorGatherer");
		
		mFpsCounter = new FpsCounter(new IFpsListener() {
			
			@Override
			public void onFPS(int i_nFPS) {
				mVideoHelper.onFPS(i_nFPS);
			}
		});
	}

	@Override
	protected void startVideo() {
		super.startVideo();
//		m_oRover.setVideoListener(this);
	}

	@Override
	protected void stopVideo() {
		super.stopVideo();
//		m_oRover.removeVideoListener(this);
	}

	@Override
	public void onFrame(byte[] rgb, int rotation) {

		final Bitmap bmp = BitmapFactory.decodeByteArray(rgb, 0, rgb.length);
		
		mVideoHelper.onFrame(bmp, rotation);
		mFpsCounter.tick();
	}
	
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
