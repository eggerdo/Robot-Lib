package robots.replicator.gui;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.R;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;

import robots.gui.SensorGatherer;
import robots.replicator.ctrl.Replicator;
import robots.replicator.ctrl.ReplicatorTypes;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The ReplicatorSensorGatherer used to derive from the same thing as the SpyTank, but it's smarter to use the
 * AC13 thing as inspiration.
 * 
 * @author Anne C. van Rossum
 * @date Aug. 22, 2013
 * @license LGPLv3
 * @copyright Distributed Organisms B.V., Rotterdam, The Netherlands
 */
public class ReplicatorSensorGatherer extends SensorGatherer implements IRawVideoListener  {

	public static final String TAG = "ReplicatorSensorGatherer";

	protected Replicator m_oReplicator;
	
	protected boolean m_bVideoEnabled = true;
	protected boolean m_bVideoConnected = false;
	protected boolean m_bVideoStopped = false;
	
	// debug frame counters
    int m_nFpsCounter = 0;
    long m_lLastTime = System.currentTimeMillis();

	private ProgressBar m_pbLoading;
	private FrameLayout m_layCamera;
	protected ScalableImageView m_ivVideo;
	protected TextView m_lblFPS;

	protected final Handler m_oSensorDataUiUpdater = new Handler();

	protected ExecutorService executorSerive = Executors.newCachedThreadPool();
	
	public ReplicatorSensorGatherer(BaseActivity i_oActivity, Replicator i_oReplicator) {
		super(i_oActivity, "ReplicatorSensorGatherer");
		m_oReplicator = i_oReplicator;
		
		setProperties();
		
		initialize();
		
		start();
	}
	
	private void initialize() {
		m_bVideoConnected = false;
	}

	protected void setProperties() {
		m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
		m_ivVideo = (ScalableImageView) m_oActivity.findViewById(R.id.ivCamera);
		
		m_layCamera = (FrameLayout) m_oActivity.findViewById(R.id.layCamera);

		m_lblFPS = (TextView) m_oActivity.findViewById(R.id.lblFPS);
	}
	
	public void resetLayout() {
		initialize();
		
		showView(m_ivVideo, false);
	}
	
	protected void startVideo() {
		m_oReplicator.switchCameraOn();
		m_bVideoConnected = false;
		m_bVideoStopped = false;
		showVideoLoading(true);
//		m_oSensorDataUiUpdater.postDelayed(m_oTimeoutRunnable, 15000);
		m_oReplicator.setVideoListener(this);
	}

	public void setVideoEnabled(boolean i_bEnabled) {
		m_bVideoEnabled = i_bEnabled;
		
		if (i_bEnabled) {
			startVideo();
		} else {
			stopVideo();
			showVideoMsg("Video OFF");
		}
	}
	
	protected Runnable m_oTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			if (!m_bVideoConnected) {
				setVideoEnabled(false);
				showVideoLoading(false);
				showVideoMsg("Video Connection Failed");
			}
		}
	};
	
	protected void showVideoMsg(String i_strMsg) {
		if (m_layCamera == null) {
			Log.w(TAG, "Camera is null");
			return;
		}
		int width = m_layCamera.getWidth();
		int height = m_layCamera.getHeight();
		
		if (width == 0 || height == 0) {
			width = 380;
			height = 240;
		}
		
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		if (m_ivVideo == null) {
			Log.w(TAG, "Imageview ivVideo is null");
			return;
		}
		m_ivVideo.setImageBitmap(bmp);
	}
	
	private void showView(View i_oView, boolean i_bShow) {
		if (i_bShow) {
			i_oView.setVisibility(View.VISIBLE);
		} else {
			i_oView.setVisibility(View.INVISIBLE);
		}
	}

	protected void stopVideo() {
		m_oReplicator.switchCameraOff();
		m_bVideoStopped = true;
		showVideoMsg("");
		m_oReplicator.removeVideoListener(this);
	}

	protected void showVideoLoading(final boolean i_bShow) {
		m_oSensorDataUiUpdater.post(new Runnable() {
			@Override
			public void run() {
				showView(m_ivVideo, !i_bShow);
				showView(m_pbLoading, i_bShow);
			}
		});
	}
	
	private boolean decoding = false;
	@Override
	public void onFrame(byte[] rgb, int rotation) {
		
		if (!decoding) {
			decoding = true;
		
					if (rgb == null) {
						Log.w(TAG, "Byte array is null!");
						return;
					}
					if (rgb.length == 0) {
						Log.w(TAG, "Byte array is empty!");
						return;
					}
					
					Log.i(TAG, "Try to decode array to bitmap with size " + rgb.length);			
									
					int argb8888[] = new int[ReplicatorTypes.IMAGE_WIDTH * ReplicatorTypes.IMAGE_HEIGHT];
					for (int i = 0, j = 0; i < ReplicatorTypes.IMAGE_SIZE; i+=3, j++) {
						int r = rgb[i+0]; int g = rgb[i+1]; int b = rgb[i+2];
						argb8888[j] = 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
					}
					final Bitmap bmp = Bitmap.createBitmap(argb8888, ReplicatorTypes.IMAGE_WIDTH, ReplicatorTypes.IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
					
					decoding = false;

					if (bmp == null) {
						return;
					}
					
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
									Log.i(TAG, "Write bitmap to screen");
									m_ivVideo.setImageBitmap(bmp);
								} else {
									Log.w(TAG, "Decode failed, bmp is null");
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
		} else {
			Log.w(TAG, "skip frame");
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
		// TODO Auto-generated method stub
		
	}
}
