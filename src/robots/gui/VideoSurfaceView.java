package robots.gui;


import java.io.ByteArrayInputStream;

import org.dobots.communication.video.FpsCounter;
import org.dobots.communication.video.IFpsListener;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.communication.video.IVideoListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class VideoSurfaceView extends SurfaceView implements IVideoListener, IRawVideoListener {

	public static final String TAG = "VideoView";

	public boolean isScaled = false;
	public boolean isFullScreen = false;
	private int m_nMaxWidth = 0;
	private int m_nMaxHeight = 0;
	
	int mHeight = -1, mWidth = -1;
	
	private Bitmap mCurrentFrame = null;
	private Paint paint;

	public VideoSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		//		paint.setDither(true);
		paint.setColor(Color.BLACK);
		
		mWorkerThread.start();
	}

	public void setScale(boolean i_bScale) {
		isScaled = i_bScale;
		requestLayout();
	}
	
	public void setFullScreen(boolean i_bValue) {
		isFullScreen = true;
		requestLayout();
	}

	public void setMaxWidth(int i_nMaxWidth) {
		m_nMaxWidth = i_nMaxWidth;
	}

	public void setMaxHeight(int i_nMaxHeight) {
		m_nMaxHeight = i_nMaxHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		synchronized(lock) {
			try
			{
				if (mCurrentFrame != null) {
					if (isScaled) {
						int width, height;
						int newWidth, newHeight;
						width = MeasureSpec.getSize(widthMeasureSpec);
						height = MeasureSpec.getSize(heightMeasureSpec);
	
						if (m_nMaxWidth != 0) {
							width = Math.min(width, m_nMaxWidth);
						}
						if (m_nMaxHeight != 0) {
							height = Math.min(height, m_nMaxHeight);
						}
						
						newHeight = (int)(width * (double)mHeight / mWidth);
						
						if (newHeight > height) {
							newHeight = height;
							newWidth = (int)(height * (double)mWidth / mHeight);
						} else {
							newWidth = width;
						}
						
						setMeasuredDimension(newWidth, newHeight);
					} else if (isFullScreen) {
						int width = MeasureSpec.getSize(widthMeasureSpec);
						int height = MeasureSpec.getSize(heightMeasureSpec);
						setMeasuredDimension(width, height);
					} else {
						setMeasuredDimension(mWidth, mHeight);
					}
				} else {
					Log.e(TAG, "current frame is NULL");
					super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				}
			} catch (Exception e)
			{
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			}
		}
	}

	public void setFrame(Bitmap bmp) {
		mCurrentFrame = bmp;
		requestLayout();
	}
	
	byte[] mCurrentRgb = null;
	int mCurrentRotation = 0;
	Object lock = new Object();
	
	int count = 0;
	public void onFrame(byte[] rgb, int rotation) {
		
		synchronized (lock) {
			if (mCurrentRgb != null) {
				count++;
			}
			mCurrentRgb = rgb;
			mCurrentRotation = rotation;
		}
	}

	Thread mWorkerThread = new Thread("VideoSurfaceView-Worker") {
		
		public void run() {
			
			byte[] rgb = null;
			int rotation = 0;

			while (!isInterrupted()) {
				synchronized (lock) {
					if (mCurrentRgb == null) {
						continue;
					} else {
						rgb = mCurrentRgb;
						rotation = mCurrentRotation;
						mCurrentRgb = null;
					}
					
					if (count > 0) {
						Log.d(TAG, "skipped frames " + count);
						count = 0;
					}
				}
				
				try {
					ByteArrayInputStream stream = new ByteArrayInputStream(rgb);
					Bitmap frame = BitmapFactory.decodeStream(stream);
					
					if (mCurrentFrame == null) {
						setSize(frame, rotation);
						invalidate();
						requestLayout();
					} else {
						mCurrentFrame.recycle();
					}
					mCurrentFrame = frame;
					
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
			Log.w("ZmqVideo", String.format("fps dec: %d", i_nFPS));
		}
	});	

	private void setSize(Bitmap bmp, int rotation) {
		if (rotation % 180 == 0) {
			mWidth = bmp.getWidth();
			mHeight = bmp.getHeight();
		} else {
			mHeight = bmp.getWidth();
			mWidth = bmp.getHeight();
		}
	}
	
	private void drawVideoFrame(Bitmap bmp, int rotation) {
		Canvas canvas = getHolder().lockCanvas();
		if (canvas != null) {
			try {
				canvas.scale((float) getWidth() / mWidth, (float) getHeight() / mHeight);
				canvas.translate((mWidth - bmp.getWidth())/2, (mHeight - bmp.getHeight())/2);
				canvas.rotate(rotation, (float)bmp.getWidth()/2, (float)bmp.getHeight()/2);
				canvas.drawBitmap(bmp, 0, 0, paint);
			} finally {
				getHolder().unlockCanvasAndPost(canvas);
			}
		} else {
			invalidate();
			requestLayout();
		}
	}

	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		mCurrentFrame = bmp;
		drawVideoFrame(bmp, rotation);
	}

}
