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
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoSurfaceView extends SurfaceView implements IVideoListener, IRawVideoListener {

	public static final String TAG = "VideoView";

	public boolean isScaled = false;
	public boolean isFullScreen = false;
	private int m_nMaxWidth = 0;
	
	int mHeight = -1, mWidth = -1;

	private Bitmap mCurrentFrame = null;
		private Paint paint;

		private SurfaceHolder mHolder;

	//	private float mScale = 1F;

//	public VideoSurfaceView(Context context) {
//		super(context);
//		paint = new Paint();
//		paint.setColor(Color.WHITE);
//		mHolder = getHolder();
//	}

	public VideoSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setAntiAlias(true);
		//		paint.setAntiAlias(true);
		//		paint.setFilterBitmap(true);
		//		paint.setDither(true);
		//		paint.setColor(Color.BLACK);
		paint.setColor(Color.WHITE);
		mHolder = getHolder();
	}

//	public VideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		paint = new Paint();
//		//		paint.setColor(Color.BLACK);
//		paint.setColor(Color.WHITE);
//	}

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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		try
		{
			if (mCurrentFrame != null) {
				if (isScaled) {
					int width;
					width = MeasureSpec.getSize(widthMeasureSpec);
					if (m_nMaxWidth != 0) {
						width = Math.min(width, m_nMaxWidth);
					}
					//	            	mScale = (float)width / mCurrentFrame.getWidth();
					int height = (int)(width * (double)mHeight / mWidth);
					setMeasuredDimension(width, height);
				} else if (isFullScreen) {
					int width = MeasureSpec.getSize(widthMeasureSpec);
					int height = MeasureSpec.getSize(heightMeasureSpec);
					setMeasuredDimension(width, height);
				} else {
					setMeasuredDimension(mWidth, mHeight);
				}
			} else {
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			}
		} catch (Exception e)
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public void setFrame(Bitmap bmp) {
		mCurrentFrame = bmp;
		invalidate();
	}

	BmpDecoder globalDecoder;
	@Override
	public void onFrame(byte[] rgb, int rotation) {
//		if (globalDecoder == null) {
			globalDecoder = new BmpDecoder(rgb, rotation);
			globalDecoder.execute();
//		}
	}

	FpsCounter counter = new FpsCounter(new IFpsListener() {
		
		@Override
		public void onFPS(int i_nFPS) {
			Log.w("ZmqVideo", String.format("fps dec: %d", i_nFPS));
		}
	});

	long dec_sum = 0;
	long dec_count = 0;
	private class BmpDecoder extends AsyncTask<Void, Integer, Void> {

		long start, end;
		byte[] rgbData;
		int nRotation;
		Bitmap frame;
		
		public BmpDecoder(byte[] rgb, int rotation) {
			rgbData = rgb;
			nRotation = rotation;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			start = System.currentTimeMillis();
//			frame = BitmapFactory.decodeByteArray(rgbData, 0, rgbData.length);
			ByteArrayInputStream stream = new ByteArrayInputStream(rgbData);
			frame = BitmapFactory.decodeStream(stream);
			
//			int dstWidth = getWidth();
//			int dstHeight = getHeight();
//
//			Matrix matrix = new Matrix();
//			
//			if (nRotation != 0) {
//				matrix.postRotate(nRotation, (float)frame.getWidth()/2, (float)frame.getHeight()/2);
//				matrix.postTranslate((mWidth - frame.getWidth())/2, (mHeight - frame.getHeight())/2);
//			}
//
//			if (isScaled || isFullScreen) {
//				// if the video should be scaled, determine the scaling factors
//				if ((mWidth != dstWidth) || (mHeight != dstHeight)) {
//					matrix.postScale((float) dstWidth / mWidth, (float) dstHeight / mHeight);
//				}
//			}
//
//			if (!matrix.isIdentity()) {
//				frame = Bitmap.createBitmap(frame, 0, 0, frame.getWidth(), frame.getHeight(), matrix, false);
//			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Bitmap oldBitmap = getDrawingCache();
			if (oldBitmap != null) {
				oldBitmap.recycle();
			}
			
			showFrame(frame, nRotation);
			
//			if (frame != null) {
//				Canvas canvas = getHolder().lockCanvas();
//				if (canvas != null) {
//					try {
//						canvas.drawBitmap(frame, 0, 0, null);
//					} finally {
//						getHolder().unlockCanvasAndPost(canvas);
//					}
//				}
//				mCurrentFrame = frame;
//			}
			
			counter.tick();
			
            end = System.currentTimeMillis();
            long dt = end - start;
            dec_sum += dt;
            dec_count += 1;
            Log.d("ZmqVideo", String.format("dec dt: %d (%.3f)", dt, (double)dec_sum / dec_count));
            
//            globalDecoder = null;
		}

	}
	
	private void setSize(Bitmap bmp, int rotation) {
//		if (rotation % 180 == 0) {
			mWidth = bmp.getWidth();
			mHeight = bmp.getHeight();
//		} else {
//			mHeight = bmp.getWidth();
//			mWidth = bmp.getHeight();
//		}
	}
	
	private void showFrame(Bitmap bmp, int rotation) {

		if (mCurrentFrame == null) {
			setSize(bmp, rotation);
			requestLayout();
		}
		mCurrentFrame = bmp;
		
//		int dstWidth = getWidth();
//		int dstHeight = getHeight();
//
//		Matrix matrix = new Matrix();
//		
//		if (rotation != 0) {
//			matrix.postRotate(rotation, (float)mCurrentFrame.getWidth()/2, (float)mCurrentFrame.getHeight()/2);
//			matrix.postTranslate((mWidth - mCurrentFrame.getWidth())/2, (mHeight - mCurrentFrame.getHeight())/2);
//		}
//
//		if (isScaled || isFullScreen) {
//			// if the video should be scaled, determine the scaling factors
//			if ((mWidth != dstWidth) || (mHeight != dstHeight)) {
//				matrix.postScale((float) dstWidth / mWidth, (float) dstHeight / mHeight);
//			}
//		}
		
		Canvas canvas = getHolder().lockCanvas();
		if (canvas != null) {
			try {
				canvas.drawBitmap(mCurrentFrame, 0, 0, paint);
			} finally {
				getHolder().unlockCanvasAndPost(canvas);
			}
		}
//		bmp.recycle();
	}

	@Override
	public void onFrame(Bitmap bmp, int rotation) {
//		mCurrentFrame = bmp;
		showFrame(bmp, rotation);
	}

}
