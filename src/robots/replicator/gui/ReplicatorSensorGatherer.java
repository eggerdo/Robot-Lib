package robots.replicator.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.R;
import org.dobots.communication.video.FpsCounter;
import org.dobots.communication.video.IFpsListener;
import org.dobots.communication.video.IVideoListener;
import org.dobots.communication.video.ZmqVideoReceiver;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;

import robots.gui.SensorGatherer;
import robots.replicator.ctrl.Replicator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
public class ReplicatorSensorGatherer extends SensorGatherer implements IVideoListener  {

	public static final String TAG = "ReplicatorSensorGatherer";

	protected Replicator m_oReplicator;
	
	protected boolean m_bVideoEnabled = true;
	protected boolean m_bVideoConnected = false;
	protected boolean m_bVideoStopped = false;
	
	private ProgressBar m_pbLoading;
	private FrameLayout m_layCamera;
	private ScalableImageView m_ivVideo;
	private TextView m_lblFPS;

	protected final Handler m_oSensorDataUiUpdater = new Handler();

	private ExecutorService executorSerive = Executors.newCachedThreadPool();
	
	private FpsCounter m_oFpsCounter;
	private boolean m_bDebug = false;
	
	public ReplicatorSensorGatherer(BaseActivity i_oActivity, Replicator i_oReplicator) {
		super(i_oActivity, "ReplicatorSensorGatherer");
		m_oReplicator = i_oReplicator;
		
		m_oFpsCounter = new FpsCounter(new IFpsListener() {
			
			@Override
			public void onFPS(int i_nFPS) {
				if (m_bDebug) {
					m_lblFPS.setText("FPS: " + String.valueOf(i_nFPS));
				}
			}
		});
		
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
		m_oReplicator.startVideo();
		m_bVideoConnected = false;
		m_bVideoStopped = false;
		showVideoLoading(true);
		m_oSensorDataUiUpdater.postDelayed(m_oTimeoutRunnable, 30000);
		setVideoListening(true);
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

	private ZmqVideoReceiver m_oVideoDisplayer;
	
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
		m_oReplicator.stopVideo();
		m_bVideoStopped = true;
		showVideoMsg("");
		setVideoListening(false);
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
		oVideoRecvSocket.subscribe(m_oReplicator.getID().getBytes());

		// start a video display thread which receives video frames from the socket and displays them
		m_oVideoDisplayer = new ZmqVideoReceiver(oVideoRecvSocket);
		m_oVideoDisplayer.setVideoListener(this);
		m_oVideoDisplayer.start();
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

	@Override
	public void onFrame(final Bitmap bmp, final int rotation) {
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
						Log.e(TAG, "bitmap is NULL!");
					}
					
					m_oFpsCounter.tick();
				}
			});
		}
	}

}
