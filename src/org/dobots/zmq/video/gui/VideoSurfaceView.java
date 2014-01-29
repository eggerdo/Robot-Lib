/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief: VideoSurfaceView to display a Video.
* @file: VideoSurfaceView.java
*
* @desc:  receives video frames and draws them on the surface. depending on the current display mode
* 		  the frame will be scaled appropriately on the screen. The frame is also rotated if necessary
* 		  so that orientation is correct.
*
*
* Copyright (c) 2013 Dominik Egger <dominik@dobots.nl>
*
* @author:		Dominik Egger
* @date:		30.08.2013
* @project:		RobotLib
* @company:		Distributed Organisms B.V.
*/
package org.dobots.zmq.video.gui;

import java.io.ByteArrayInputStream;

import org.dobots.utilities.Utils;
import org.dobots.utilities.camera.ExifUtils;
import org.dobots.zmq.video.FpsCounter;
import org.dobots.zmq.video.IFpsListener;
import org.dobots.zmq.video.IRawVideoListener;
import org.dobots.zmq.video.IVideoListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;

public class VideoSurfaceView extends SurfaceView implements IVideoListener, IRawVideoListener, Callback {

	public static final String TAG = "VideoSurfaceView";

	/**
	 * Mode in which the frame should be displayed on the screen:
	 *   NORMAL			: the frame will be displayed with the dimensions as received
	 *   FILLED			: the frame will be scaled so that it fills the given space. proportions will be discarded.
	 *	 SCALED			: the frame will be scaled so that it fits inside the given space. proportions will be kept
	 *	 SCALED_WIDTH	: the frame will be scaled so that the width corresponds to the available width. the height
	 *					    of the view will be adjusted if necessary. proportions will be kept.  
	 *	 SCALED_HEIGHT	: the frame will be scaled so that the height corresponds to the available height. the width
	 *					    of the view will be adjusted if necessary. proportions will be kept.
	 */
	public enum DisplayMode {
		NORMAL, FILLED, SCALED, SCALED_WIDTH, SCALED_HEIGHT
	}
	
	private DisplayMode mMode;
	
	// limit the width and height if it is set to scale
	private int m_nMaxWidth = 0;
	private int m_nMaxHeight = 0;
	
	// dispHeight and dispWidth are the dimensions in which the video should be displayed on screen
	int mDispHeight = -1, mDispWidth = -1;
	// bmpWidth and bmpHeight are the actual dimensions of the received frame
	int mBmpWidth = -1, mBmpHeight = -1;
	
	private Paint mPainter;
	
	private Bitmap mCurrentFrame = null;
	private byte[] mCurrentRgb = null;
	private int mCurrentRotation = 0;
	private Object mLock = new Object();

	private boolean mSurfaceReady;	

	public VideoSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPainter = new Paint();
		mPainter.setAntiAlias(true);
		mPainter.setFilterBitmap(true);
		//		paint.setDither(true);
		mPainter.setColor(Color.BLACK);
		
		mFrameDecoder.start();
		
		getHolder().addCallback(this);
	}
	
	public void onDestroy() {
		mFrameDecoder.interrupt();
	}
	
	public void setDisplayMode(DisplayMode eMode) {
		mMode = eMode;
	}

	public DisplayMode getDisplayMode() {
		return mMode;
	}
	
	public void setMaxWidth(int i_nMaxWidth) {
		m_nMaxWidth = i_nMaxWidth;
	}

	public void setMaxHeight(int i_nMaxHeight) {
		m_nMaxHeight = i_nMaxHeight;
	}

	// with slower phones it is most likely that decoding, rotating and scaling
	// a frame takes longer than receiving frames. to avoid delay building up
	// the receiving of frames is decoupled from the decoding. the received frame
	// is stored in a variable. the decoder then always takes the currently stored
	// frame. if frames are received faster than they are decoded, the old ones will
	// be dropped.
	public void onFrame(byte[] rgb, int rotation) {
		
		synchronized (mLock) {
			mCurrentRgb = rgb;
			mCurrentRotation = rotation;
		}
	}

	// Thread responsible for creating a Bitmap out of the received Frame. 
	Thread mFrameDecoder = new Thread("FrameDecoder") {
		
		public void run() {
			
			byte[] rgb = null;
			int rotation = 0;

			while (!isInterrupted()) {
				synchronized (mLock) {
					// if no frame was received, skip
					// otherwise store it in a local variable so that
					// we don't have to worry about it being overwritten
					if (mCurrentRgb == null) {
						continue;
					} else {
						rgb = mCurrentRgb;
						rotation = mCurrentRotation;
						mCurrentRgb = null;
					}
				}
				
				try {
					ByteArrayInputStream stream = new ByteArrayInputStream(rgb);
					
					// if the rotation parameter provided by the callback function onFrame is
					// -1 we try to get the rotation from the EXIF metadata
					if (rotation == -1) {
						rotation = ExifUtils.getExifRotation(rgb);
					}
					Bitmap frame = BitmapFactory.decodeStream(stream);
					
					if (frame == null) {
						continue;
					}

					// update the size based on the display mode and the bitmap dimensions
					setSize(frame, rotation);
					
					// recycle the old frame before assigning the new
					if (mCurrentFrame != null) {
						mCurrentFrame.recycle();
					}
					mCurrentFrame = frame;
					
					// draw the frame on the screen
					drawVideoFrame(mCurrentFrame, rotation);
			
					counter.tick();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
	};
	
	FpsCounter counter = new FpsCounter(new IFpsListener() {
		
		@Override
		public void onFPS(double i_nFPS) {
			Log.d(TAG, String.format("fps dec: %.1f", i_nFPS));
		}
	});

	private void setSize(Bitmap bmp, int rotation) {
		
		int oldDispWidth = mDispWidth;
		int oldDispHeight = mDispHeight;

		int parentWidth = ((View)getParent()).getWidth();
		int parentHeight = ((View)getParent()).getHeight();
		
		// if frame is rotated by 90 or 270 we need to swap
		// height and width
		if (rotation % 180 == 0) {
			mBmpWidth = bmp.getWidth();
			mBmpHeight = bmp.getHeight();
		} else {
			mBmpHeight = bmp.getWidth();
			mBmpWidth = bmp.getHeight();
		}
		
		switch(mMode) {
		
		case SCALED_HEIGHT:
			// set the height equal to the parent (we cannot use MATCH_PARENT otherwise
			// calculation of the width will fail)
			mDispHeight = parentHeight;

			if (m_nMaxHeight != 0) {
				mDispHeight = Math.min(mDispHeight, m_nMaxHeight);
			}

			mDispWidth = (int)(mBmpWidth * (double)mDispHeight / mBmpHeight);
			break;
			
		case SCALED_WIDTH:
			// set the width equal to the parent (we cannot use MATCH_PARENT otherwise
			// calculation of the height will fail)
			mDispWidth = parentWidth;
			
			// check if max width / height is set
			if (m_nMaxWidth != 0) {
				mDispWidth = Math.min(mDispWidth, m_nMaxWidth);
			}
			
			// scale the height based on the width
			mDispHeight = (int)(mBmpHeight * (double)mDispWidth / mBmpWidth);
			break;
			
		case SCALED:
			// set the width equal to the parent (we cannot use MATCH_PARENT otherwise
			// calculation of the height will fail)
			mDispWidth = parentWidth;
			
			// check if max width / height is set
			if (m_nMaxWidth != 0) {
				mDispWidth = Math.min(mDispWidth, m_nMaxWidth);
			}
			if (m_nMaxHeight != 0) {
				mDispHeight = Math.min(mDispHeight, m_nMaxHeight);
			}
			
			// scale the height based on the width
			mDispHeight = (int)(mBmpHeight * (double)mDispWidth / mBmpWidth);
			
			// if the height would be greater than the parent height
			// we rescale the width based on the height
			if ((getHeight() != 0) && (mDispHeight > parentHeight)) {
				mDispHeight = parentHeight;
				mDispWidth = (int)(mBmpWidth * (double)mDispHeight / mBmpHeight);
			}
			break;

		case FILLED:
			// we use the parent's width and height
			// it's not possible to use MATCH_PARENT or the drawing (in particular calculating the
			// scaling factor) will fail
			mDispWidth = parentWidth;
			mDispHeight = parentHeight;
			break;
			
		default:
			// we display the frame as it is
			mDispHeight = mBmpHeight;
			mDispWidth = mBmpWidth;
			
		}
		
		// if the calculated display size changed we need to updated the layout with the new
		// width and height
		if (mDispHeight != oldDispHeight || mDispWidth != oldDispWidth) {
			Utils.runAsyncUiTask(new Runnable() {
				
				@Override
				public void run() {
					setLayoutParams(new LayoutParams(mDispWidth, mDispHeight, Gravity.CENTER));
				}
			});
		}
	}
	
	// draw the frame on the screen
	private void drawVideoFrame(Bitmap bmp, int rotation) {
		if (!mSurfaceReady) {
			return;
		}
		
		Canvas canvas = getHolder().lockCanvas();
		if (canvas != null) {
			try {
				// get the center of the available space
				float centerX, centerY;
				centerX = (float) getWidth() / 2;
				centerY = (float) getHeight() / 2;
				
				// calculate the top left corner where the (unrotated / unscaled) frame should
				// be drawn so that it is centered in the available space
				float left, top;
				left = centerX - (bmp.getWidth() / 2);
				top = centerY - (bmp.getHeight() / 2);

				// execution of the transformation is inverted. so first after drawing the image it is
				// rotated around the center of the image, then it is scaled (if necessary)
				canvas.scale((float) mDispWidth / mBmpWidth, (float) mDispHeight / mBmpHeight, centerX, centerY);
				canvas.rotate(rotation, centerX, centerY);
				
//				Log.d(TAG, "drawing video frame");
				canvas.drawColor(Color.BLACK);
				canvas.drawBitmap(bmp, left, top, mPainter);
			} finally {
				getHolder().unlockCanvasAndPost(canvas);
			}
		} else {
			Log.e(TAG, "failed to get canvas!");
		}
	}

	// if the frame is received as bmp, calculate the size, then
	// draw it on the screen.
	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		mCurrentFrame = bmp;
		setSize(bmp, rotation);
		drawVideoFrame(bmp, rotation);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceReady = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceReady = false;
	}

}
