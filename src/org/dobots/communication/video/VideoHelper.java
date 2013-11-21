/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief: Helper class to display a video on the screen.
* @file: VideoHelper.java
*
* @desc:  includes loading progress bar and a FPS view. a watchdog checks if frames are 
* 		  received and in case of a timeout will reset connection status and display the 
* 		  loading screen. same applies when connecting and no frame is received
*
*
* Copyright (c) 2013 Dominik Egger <dominik@dobots.nl>
*
* @author:		Dominik Egger
* @date:		30.08.2013
* @project:		RobotLib
* @company:		Distributed Organisms B.V.
*/
package org.dobots.communication.video;

import org.dobots.R;
import org.dobots.communication.video.VideoSurfaceView.DisplayMode;
import org.dobots.utilities.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VideoHelper implements IVideoListener, IFpsListener, IRawVideoListener {
	
	private static final int CONNECTION_TIMEOUT = 15000; // 15 seconds
	private static final int WATCHDOG_TIMEOUT = 5000; // 5 seconds
	private static final int WATCHDOG_INTERVAL = 1000; // 1 second
	
	private Handler mHandler = new Handler();

	private boolean m_bVideoConnected;
	private boolean m_bVideoStopped;

	private VideoSurfaceView m_ivVideo;
	private ProgressBar m_pbLoading;
	private TextView m_lblFps;
	private FrameLayout m_layCamera;

	private Activity m_oActivity;
	
	private long mLastFrameRecv = 0;
	
	private boolean mDebug = false;

	/**
	 * takes an activity and a view container as input, then places a camera view, a progress bar
	 * and a FPS view inside the container
	 * @param activity the activity in which the video should be shown
	 * @param container the container where the video should be shown
	 */
	public VideoHelper(Activity activity, ViewGroup container) {
		m_oActivity = activity;
		
    	LayoutInflater.from(activity).inflate(R.layout.videoview, container);
    	m_layCamera = (FrameLayout) container.findViewById(R.id.layCamera);
    	m_ivVideo = (VideoSurfaceView) container.findViewById(R.id.ivCamera);
    	m_pbLoading = (ProgressBar) container.findViewById(R.id.pbLoading);
    	m_lblFps = (TextView) container.findViewById(R.id.lblFPS);
    	
    	m_ivVideo.setDisplayMode(DisplayMode.SCALED);
    	
    	initialize();
    	
    	mHandler.postDelayed(m_oWatchdog, WATCHDOG_INTERVAL);
	}
	
	/**
	 * call when activity is destroyed
	 */
	public void onDestroy() {
		m_ivVideo.onDestroy();
	}
	
	// the watchdog timer checks if new frames are received. if no new frame
	// is received within a given period, the VideoConnected flag is cleared
	// and the loading screen is shown until a frame is received again
	protected Runnable m_oWatchdog = new Runnable() {
		
		public void run() {
			if (m_bVideoConnected && !m_bVideoStopped) {
				if (System.currentTimeMillis() - mLastFrameRecv > WATCHDOG_TIMEOUT) {
					m_bVideoConnected = false;
					showVideoLoading(true);
				}
			}
			mHandler.postDelayed(this, WATCHDOG_INTERVAL);
		}
	};

	// the timeout runnable is used if connection is established. if no frame
	// is received when the timeout triggers we assume that video connection failed.
	protected Runnable m_oTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			if (!m_bVideoConnected) {
				m_bVideoStopped = true;
				showVideoLoading(false);
				showVideoMsg("Video Connection Failed");
			}
		}
	};

	// TODO: neccessary?
	public void resetLayout() {
		initialize();
		
		m_ivVideo.setVisibility(View.INVISIBLE);
	}
	
	private void initialize() {
		m_bVideoConnected = false;
		m_bVideoStopped = false;
	}


	/**
	 * call once video was started, check for incoming frames with timeout
	 * set to true 
	 */
	public void onStartVideo() {
		onStartVideo(true);
	}
	
	
	/**
	 * call once video was started, waits for incoming frames
	 * @param i_bTimeout if true and no frame is coming in within a timeout
	 * 					 then connection is assumed to have failed and the
	 * 					 flags are reset
	 */
	public void onStartVideo(boolean i_bTimeout) {
		m_bVideoConnected = false;
		m_bVideoStopped = false;
		showVideoLoading(true);
		if (i_bTimeout) {
			mHandler.postDelayed(m_oTimeoutRunnable, CONNECTION_TIMEOUT);
		}
	}

	/**
	 * call once video was stopped
	 */
	public void onStopVideo() {
		m_bVideoStopped = true;
		showVideoMsg("Video OFF");
	}
	
	/**
	 * shows/hides the video and video loading progress
	 * @param i_bShow if false, the video is shown and progress is hidden
	 * 					 true, the video is hidden and progress is shown
	 */
	protected void showVideoLoading(final boolean i_bShow) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Utils.showView(m_ivVideo, !i_bShow);
				Utils.showView(m_pbLoading, i_bShow);
			}
		});
	}

	/**
	 * write the given message on the video screen
	 * @param i_strMsg message to be displayed
	 */
	protected void showVideoMsg(String i_strMsg) {
		int width = m_layCamera.getWidth();
		int height = m_layCamera.getHeight();
		
		if (width == 0 || height == 0) {
			width = 380;
			height = 240;
		}
		
		Utils.writeToSurfaceView(m_oActivity, m_ivVideo, i_strMsg, true);
	}

	/**
	 * callback to update frames per second on display
	 */
	@Override
	public void onFPS(final int i_nFPS) {
		m_oActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!m_bVideoStopped && mDebug) {
					m_lblFps.setText("FPS: " + String.valueOf(i_nFPS));
				}
			}
		});
		
	}

	/**
	 * callback when already decoded bitmap should be shown
	 */
	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		if (!m_bVideoStopped) {
			if (!m_bVideoConnected) {
				// if this is the first frame received, cancel the timeout,
				// set flag to connected and show the video
				mHandler.removeCallbacks(m_oTimeoutRunnable);
				m_bVideoConnected = true;
				showVideoLoading(false);
			}
			
			// store current time for watchdog
			mLastFrameRecv = System.currentTimeMillis();
			m_ivVideo.onFrame(bmp, rotation);
		}
	}

	/**
	 * callback when a frame is received as a byte array. will be decoded
	 * by the VideoSurfaceView
	 */
	@Override
	public void onFrame(byte[] rgb, int rotation) {
		if (!m_bVideoStopped) {
			m_oActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (!m_bVideoConnected) {
						// if this is the first frame received, cancel the timeout,
						// set flag to connected and show the video
						mHandler.removeCallbacks(m_oTimeoutRunnable);
						m_bVideoConnected = true;
						showVideoLoading(false);
					}
				}
			});

			// store current time for watchdog
			mLastFrameRecv = System.currentTimeMillis();
			m_ivVideo.onFrame(rgb, rotation);

		}
	}
	
	/**
	 * Enable debug. in debug mode the FPS are displayed on the screen.
	 * @param debug true to enable, false to disable
	 */
	public void setDebug(boolean debug) {
		mDebug = debug;
	}
    
}
