package robots.rover.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ.Socket;

import robots.IVideoListener;
import robots.gui.SensorGatherer;
import robots.rover.ctrl.RoverBase;
import robots.rover.ctrl.RoverBaseTypes.VideoResolution;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class RoverBaseSensorGatherer extends SensorGatherer {

	protected RoverBase m_oRover;

	protected boolean m_bVideoEnabled = true;
	protected boolean m_bVideoConnected = false;
	private boolean m_bVideoScaled = false;
	protected boolean m_bVideoStopped = false;

	private ProgressBar m_pbLoading;
	protected ScalableImageView m_ivVideo;
	
	private FrameLayout m_layCamera;

	protected final Handler m_oSensorDataUiUpdater = new Handler();
	
	private Socket videoSocket;

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
	
	protected void setProperties() {
		m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
		m_ivVideo = (ScalableImageView) m_oActivity.findViewById(R.id.ivCamera);
		
		m_layCamera = (FrameLayout) m_oActivity.findViewById(R.id.layCamera);

		m_lblFPS = (TextView) m_oActivity.findViewById(R.id.lblFPS);
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
		m_oRover.stopVideo();
		m_bVideoStopped = true;
		showVideoMsg("");
	}
	
	protected void startVideo() {
		m_oRover.startVideo();
		m_bVideoConnected = false;
		m_bVideoStopped = false;
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
		int width = m_layCamera.getWidth();
		int height = m_layCamera.getHeight();
		
		if (width == 0 || height == 0) {
			width = 380;
			height = 240;
		}
		
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		m_ivVideo.setImageBitmap(bmp);
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
		m_ivVideo.setScale(i_bScaled);
	}

	public void setResolution(final VideoResolution i_eResolution) {
		m_oRover.setResolution(i_eResolution);
	}

}
