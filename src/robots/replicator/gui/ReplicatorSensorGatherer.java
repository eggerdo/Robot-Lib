package robots.replicator.gui;

import org.dobots.R;
import org.dobots.communication.video.IFpsListener;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.communication.video.IVideoListener;
import org.dobots.communication.video.VideoDisplayThread;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import robots.gui.SensorGatherer;
import robots.replicator.ctrl.Replicator;

/**
 * The ReplicatorSensorGatherer used to derive from the same thing as the SpyTank, but it's smarter to use the
 * AC13 thing as inspiration.
 * @author anne
 *
 */
public class ReplicatorSensorGatherer extends SensorGatherer implements IRawVideoListener  {

	protected Replicator m_oReplicator;

	// debug frame counters
    int m_nFpsCounter = 0;
    long m_lLastTime = System.currentTimeMillis();
    
	protected boolean m_bVideoEnabled = true;
	protected boolean m_bVideoConnected = false;
	private boolean m_bVideoScaled = false;
	protected boolean m_bVideoStopped = false;

	private ProgressBar m_pbLoading;
	private VideoView mVideo;
	
	protected ScalableImageView m_ivVideo;
	
	private FrameLayout m_layCamera;

	protected final Handler m_oSensorDataUiUpdater = new Handler();
	
	private Socket videoSocket;

	protected TextView m_lblFPS;

	private VideoDisplayThread m_oVideoDisplayer;
	
	public ReplicatorSensorGatherer(BaseActivity i_oActivity, Replicator robot) {
		super(i_oActivity, "ReplicatorSensorGatherer");
		m_oReplicator = robot;
	
		setProperties();

		initialize();
	}

	private void initialize() {
		m_bVideoConnected = false;
	}

	public void resetLayout() {
		initialize();
		
		showView(mVideo, false);
	}
	
	protected void setProperties() {
		m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
		mVideo = (VideoView) m_oActivity.findViewById(R.id.ivRepCamera);
		
		m_layCamera = (FrameLayout) m_oActivity.findViewById(R.id.layCamera);

		m_lblFPS = (TextView) m_oActivity.findViewById(R.id.lblFPS);
	}
	
	protected void showVideoLoading(final boolean i_bShow) {
		m_oSensorDataUiUpdater.post(new Runnable() {
			@Override
			public void run() {
				showView(mVideo, !i_bShow);
				showView(m_pbLoading, i_bShow);
			}
		});
	}
	
	private void showView(View i_oView, boolean i_bShow) {
		if (i_bShow) {
			i_oView.setVisibility(View.VISIBLE);
		} else {
			i_oView.setVisibility(View.INVISIBLE);
		}
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
	
	protected void stopVideo() {
		m_oReplicator.switchCameraOff();
		m_bVideoStopped = true;
		showVideoMsg("");

		setVideoListening(false);
	}
	
	protected void startVideo() {
		m_oReplicator.switchCameraOn();
		m_bVideoConnected = false;
		m_bVideoStopped = false;
		showVideoLoading(true);
		m_oSensorDataUiUpdater.postDelayed(m_oTimeoutRunnable, 15000);

		setVideoListening(true);
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
		int width = m_layCamera.getWidth();
		int height = m_layCamera.getHeight();
		
		if (width == 0 || height == 0) {
			width = 380;
			height = 240;
		}
		
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		mVideo.setImageBitmap(bmp);

	}

	public void onConnect() {
		setVideoEnabled(m_bVideoEnabled);
	}

	public void onDisconnect() {
		stopVideo();
	}
	
	public boolean isVideoEnabled() {
		return m_bVideoEnabled;
	}

	public boolean isVideoScaled() {
		return m_bVideoScaled;
	}

	public void setVideoScaled(boolean i_bScaled) {
		m_bVideoScaled = i_bScaled;
		mVideo.setScale(i_bScaled);
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
		m_oVideoDisplayer = new VideoDisplayThread(ZmqHandler.getInstance().getContext().getContext(), oVideoRecvSocket);
		m_oVideoDisplayer.setVideoListener(this);
		m_oVideoDisplayer.setFPSListener(this);
		m_oVideoDisplayer.start();
	}

	@Override
	public void onFPS(final int i_nFPS) {

		// since the function is not called from the main thread we have to call the main thread from
		// here in order to update the fps display
		Utils.runAsyncUiTask(new Runnable() {

			@Override
			public void run() {
				if (!m_bVideoStopped) {
					m_lblFPS.setText("FPS: " + String.valueOf(i_nFPS));
				}
			}
		});
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
	public void onFrame(Bitmap bmp, int rotation) {
		if (!m_bVideoStopped) {
			if (!m_bVideoConnected) {
				m_oSensorDataUiUpdater.removeCallbacks(m_oTimeoutRunnable);
				m_bVideoConnected = true;
				showVideoLoading(false);
			}

			mVideo.onFrame(bmp, rotation);
		}
	}

	@Override
	public void shutDown() {
		if (m_oVideoDisplayer != null) {
			m_oVideoDisplayer.close();
		}
	}

}
