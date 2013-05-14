package robots.gui.rover;

import org.dobots.R;
import org.dobots.robotalk.video.VideoTypes;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ.Socket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import robots.IVideoListener;
import robots.ctrl.rover.RoverBase;
import robots.ctrl.rover.RoverBaseTypes.VideoResolution;
import robots.ctrl.rover.rover2.Rover2;
import robots.gui.SensorGatherer;

public abstract class RoverBaseSensorGatherer extends SensorGatherer implements IVideoListener {

	protected RoverBase m_oRover;

	protected boolean m_bVideoEnabled = true;
	protected boolean m_bVideoConnected = false;
	private boolean m_bVideoScaled = false;

	private ProgressBar m_pbLoading;
	protected ScalableImageView m_ivVideo;
	
	private FrameLayout m_layCamera;

	protected final Handler m_oSensorDataUiUpdater = new Handler();
	
	private Socket videoSocket;

	// debug frame counters
    int m_nFpsCounter = 0;
    long m_lLastTime = System.currentTimeMillis();

	protected TextView m_lblFPS;

	public RoverBaseSensorGatherer(BaseActivity i_oActivity, RoverBase i_oRover, String i_strThreadName) {
		super(i_oActivity, i_strThreadName);
		m_oRover = i_oRover;
		
//		videoSocket = ZmqHandler.getInstance().getContext().createSocket(ZMQ.PUSH);
//		videoSocket.bind("inproc://video");
		
		setProperties();

		initialize();
		
		start();
	}
	
	private void initialize() {
		m_bVideoConnected = false;
	}

	public void resetLayout() {
		initialize();
		
		showView(m_ivVideo, false);
	}
	
	private void setProperties() {
		m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
		m_ivVideo = (ScalableImageView) m_oActivity.findViewById(R.id.ivCamera);
		
		m_layCamera = (FrameLayout) m_oActivity.findViewById(R.id.layCamera);

		m_lblFPS = (TextView) m_oActivity.findViewById(R.id.lblFPS);
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
	
	protected void showVideoLoading(final boolean i_bShow) {
		m_oSensorDataUiUpdater.post(new Runnable() {
			@Override
			public void run() {
				showView(m_ivVideo, !i_bShow);
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

	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}
	
	public void dispatchMessage(Message msg) {
		m_oSensorDataUiUpdater.dispatchMessage(msg);
	}

	public void setVideoEnabled(boolean i_bVideoEnabled) {
		
		m_bVideoEnabled = i_bVideoEnabled;
		if (m_bVideoEnabled) {
			m_oRover.setVideoListener(this);
			m_oRover.startVideo();
		} else {
			m_oRover.removeVideoListener(this);
			m_oRover.stopVideo();
		}
		
		if (i_bVideoEnabled) {
			startVideo();
		} else {
			m_bVideoConnected = false;
			showVideoMsg("Video OFF");
		}
	}
	
	protected void startVideo() {
		m_bVideoConnected = false;
		showVideoLoading(true);
		m_oSensorDataUiUpdater.postDelayed(m_oTimeoutRunnable, 15000);
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
		Bitmap bmp = Bitmap.createBitmap(m_layCamera.getWidth(), m_layCamera.getHeight(), Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		m_ivVideo.setImageBitmap(bmp);
	}

	public void onConnect() {
		setVideoEnabled(m_bVideoEnabled);
	}

	public boolean isVideoEnabled() {
		return m_bVideoEnabled;
	}

	public boolean isVideoScaled() {
		return m_bVideoScaled;
	}

	public void setVideoScaled(boolean i_bScaled) {
		m_bVideoScaled = i_bScaled;
		m_ivVideo.setScale(i_bScaled);
	}

	public void setResolution(final VideoResolution i_eResolution) {
		startVideo();
		m_oRover.setResolution(i_eResolution);
	}

	@Override
	public void shutDown() {
		
	}

}
