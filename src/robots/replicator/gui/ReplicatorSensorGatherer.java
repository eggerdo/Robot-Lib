package robots.replicator.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.R;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.gui.SensorGatherer;
import robots.replicator.ctrl.Replicator;
import robots.replicator.ctrl.ReplicatorTypes;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
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
	protected SurfaceView m_ivVideo;
	protected TextView m_lblFPS;

	protected final Handler m_oSensorDataUiUpdater = new Handler();

	protected ExecutorService executorSerive = Executors.newCachedThreadPool();

	private Paint mPainter;

	private boolean mSizeInitialized = false;
	private int mBmpWidth, mBmpHeight, mDispWidth, mDispHeight;

	public ReplicatorSensorGatherer(BaseActivity i_oActivity, Replicator i_oReplicator) {
		super(i_oActivity, "ReplicatorSensorGatherer");
		m_oReplicator = i_oReplicator;

		mPainter = new Paint();
		mPainter.setAntiAlias(true);
		mPainter.setFilterBitmap(true);
		// paint.setDither(true);
		mPainter.setColor(Color.BLACK);

		setProperties();

		initialize();

		start();
	}

	private void initialize() {
		m_bVideoConnected = false;
	}

	protected void setProperties() {
		m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
		m_ivVideo = (SurfaceView) m_oActivity.findViewById(R.id.ivCamera);

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
		m_oSensorDataUiUpdater.postDelayed(m_oTimeoutRunnable, 20000);
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

		Utils.writeToSurfaceView(m_oActivity, m_ivVideo, i_strMsg, true);
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
	public void onFrame(byte[] rgb, final int rotation) {

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
							if (!mSizeInitialized) {
								setSize(bmp, rotation);
							}
							drawVideoFrame(bmp, rotation);
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

	private void setSize(Bitmap bmp, int rotation) {
		// if frame is rotated by 90 or 270 we need to swap
		// height and width
		if (rotation % 180 == 0) {
			mBmpWidth = bmp.getWidth();
			mBmpHeight = bmp.getHeight();
		} else {
			mBmpHeight = bmp.getWidth();
			mBmpWidth = bmp.getHeight();
		}

		mDispWidth = m_ivVideo.getWidth();
		mDispHeight = m_ivVideo.getHeight();

		// scale the height based on the width
		mDispHeight = (int)(mBmpHeight * (double)mDispWidth / mBmpWidth);

		m_ivVideo.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, mDispHeight));
		m_ivVideo.requestLayout();

		mSizeInitialized = true;
	}

	// draw the frame on the screen
	private void drawVideoFrame(Bitmap bmp, int rotation) {
		Canvas canvas = m_ivVideo.getHolder().lockCanvas();

		Bitmap oldBitmap = m_ivVideo.getDrawingCache();
		if (oldBitmap != null) {
			oldBitmap.recycle();
		}

		if (canvas != null) {
			try {
				// get the center of the available space
				float centerX, centerY;
				centerX = (float) m_ivVideo.getWidth() / 2;
				centerY = (float) m_ivVideo.getHeight() / 2;

				// calculate the top left corner where the (unrotated / unscaled) frame should
				// be drawn so that it is centered in the available space
				float left, top;
				left = centerX - (bmp.getWidth() / 2);
				top = centerY - (bmp.getHeight() / 2);

				// execution of the transformation is inverted. so first after drawing the image it is
				// rotated around the center of the image, then it is scaled (if necessary)
				canvas.scale((float) mDispWidth / mBmpWidth, (float) mDispHeight / mBmpHeight, centerX, centerY);
				canvas.rotate(rotation, centerX, centerY);
				Log.i(TAG, "drawing bitmap");
				canvas.drawBitmap(bmp, left, top, mPainter);
			} finally {
				m_ivVideo.getHolder().unlockCanvasAndPost(canvas);
			}
		} else {
			Log.e(TAG, "failed to get canvas!");
			// invalidate();
			// requestLayout();
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
