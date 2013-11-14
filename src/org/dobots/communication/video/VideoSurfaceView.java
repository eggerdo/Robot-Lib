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
package org.dobots.communication.video;

import java.io.ByteArrayInputStream;

import org.dobots.utilities.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout.LayoutParams;

public class VideoSurfaceView extends SurfaceView implements IVideoListener, IRawVideoListener {

	public static final String TAG = "VideoSurfaceView";

	/**
	 * Mode in which the frame should be displayed on the screen:
	 *   NORMAL: the frame will be displayed with the dimensions as received
	 *	 SCALED: the frame will be scaled so that it fits inside the given space. proportions will be kept
	 *   FILLED: the frame will be scaled so that it fills the given space. proportions will be discarded.
	 */
	public enum DisplayMode {
		NORMAL, SCALED, FILLED
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
	
	private boolean mSizeInitialized = false;

	private Bitmap mCurrentFrame = null;
	private byte[] mCurrentRgb = null;
	private int mCurrentRotation = 0;
	private Object mLock = new Object();
	
	public VideoSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPainter = new Paint();
		mPainter.setAntiAlias(true);
		mPainter.setFilterBitmap(true);
		//		paint.setDither(true);
		mPainter.setColor(Color.BLACK);
		
		mFrameDecoder.start();
	}
	
	public void onDestroy() {
		mFrameDecoder.interrupt();
	}
	
	public void setDisplayMode(DisplayMode eMode) {
		mMode = eMode;
		mSizeInitialized = false;
//		requestLayout();
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
					Bitmap frame = BitmapFactory.decodeStream(stream);
					
					// if not already, initialize the size in which the frame should
					// be displayed
					if (!mSizeInitialized) {
						setSize(frame, rotation);
//						invalidate();
//						requestLayout();
					} 
					
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
			
		};
	};
	
	FpsCounter counter = new FpsCounter(new IFpsListener() {
		
		@Override
		public void onFPS(int i_nFPS) {
			Log.d("ZmqVideo", String.format("fps dec: %d", i_nFPS));
		}
	});	

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
		
		mDispWidth = getWidth();
		mDispHeight = getHeight();

		// if height and width is 0 then the surface was not created yet (or the
		// view has wrong layout parameters). in any case we keep trying to
		// initialize the sizes.
		if ((mDispHeight == 0) && (mDispWidth == 0)) {
			mSizeInitialized = false;
			return;
		}
		
		switch(mMode) {
		case SCALED:
			// check if max width / height is set
			if (m_nMaxWidth != 0) {
				mDispWidth = Math.min(mDispWidth, m_nMaxWidth);
			}
			if (m_nMaxHeight != 0) {
				mDispHeight = Math.min(mDispHeight, m_nMaxHeight);
			}
			
			// scale the height based on the width
			mDispHeight = (int)(mBmpHeight * (double)mDispWidth / mBmpWidth);
			
			// if the height would be greater than the available height
			// we rescale the width based on the height
			
			if ((getHeight() != 0) && (mDispHeight > getHeight())) {
				mDispHeight = getHeight();
				mDispWidth = (int)(mBmpWidth * (double)mDispHeight / mBmpHeight);
			}
			break;
		case FILLED:
			// nothing to do, we just keep the width and height values already assigned above
			break;
		default:
			// we display the frame as it is
			mDispHeight = mBmpHeight;
			mDispWidth = mBmpWidth;
			
		}
		
		// if only the height is 0, then the layout was set to wrap_content, so we force
		// the view to take the height we calculated
		if (getHeight() == 0) {
			Utils.runAsyncUiTask(new Runnable() {
				
				@Override
				public void run() {
					setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, mDispHeight));
				}
			});
		}
		
		mSizeInitialized = true;
	}
	
	// draw the frame on the screen
	private void drawVideoFrame(Bitmap bmp, int rotation) {
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
				
				Log.d(TAG, "drawing video frame");
				canvas.drawBitmap(bmp, left, top, mPainter);
			} finally {
				getHolder().unlockCanvasAndPost(canvas);
			}
		} else {
			Log.e(TAG, "failed to get canvas!");
//			invalidate();
//			requestLayout();
		}
	}

	// if the frame is received as bmp, calculate the size, then
	// draw it on the screen.
	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		mCurrentFrame = bmp;
		if (!mSizeInitialized) {
			setSize(bmp, rotation);
		}
		drawVideoFrame(bmp, rotation);
	}

}
