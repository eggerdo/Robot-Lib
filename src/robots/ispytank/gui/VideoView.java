package robots.ispytank.gui;

import org.dobots.communication.video.VideoDisplayThread.IVideoListener;
import org.dobots.communication.video.VideoDisplayThread.VideoListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

public class VideoView extends ImageView implements IVideoListener, VideoListener {

	public static final String TAG = "VideoView";

	public boolean isScaled = false;
	private int m_nMaxWidth = 0;

	private Bitmap mCurrentFrame = null;
	//	private Paint paint;

	//	private float mScale = 1F;

	public VideoView(Context context) {
		super(context);
		//		paint.setColor(Color.BLACK);
	}

	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//		paint = new Paint();
		//		paint.setAntiAlias(true);
		//		paint.setFilterBitmap(true);
		//		paint.setDither(true);
		//		paint.setColor(Color.BLACK);
	}

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//		paint.setColor(Color.BLACK);
	}

	public void setScale(boolean i_bScale) {
		isScaled = i_bScale;
		requestLayout();
	}

	public void setMaxWidth(int i_nMaxWidth) {
		m_nMaxWidth = i_nMaxWidth;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		try
		{
			if (isScaled) {
				int width;
				width = MeasureSpec.getSize(widthMeasureSpec);
				if (m_nMaxWidth != 0) {
					width = Math.min(width, m_nMaxWidth);
				}
				//	            	mScale = (float)width / mCurrentFrame.getWidth();
				int height = width * mCurrentFrame.getHeight() / mCurrentFrame.getWidth();
				setMeasuredDimension(width, height);
			} else {
				setMeasuredDimension(mCurrentFrame.getHeight(), mCurrentFrame.getHeight());
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
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mCurrentFrame != null) {
			setImageBitmap(mCurrentFrame);
		}
	}

	@Override
	public void onFrame(byte[] rgb, int rotation) {
//		(new BmpDecoder()).execute(rgb);
	}

	private class BmpDecoder extends AsyncTask<byte[], Integer, Bitmap> {

		@Override
		protected Bitmap doInBackground(byte[]... params) {
			Bitmap frame = BitmapFactory.decodeByteArray(params[0], 0, params[0].length);
			return frame;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			Bitmap oldBitmap = getDrawingCache();
			if (oldBitmap != null) {
				oldBitmap.recycle();
			}
			
			if (result != null) {
				mCurrentFrame = result;
				setImageBitmap(mCurrentFrame);
			}
		}

	}

	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		mCurrentFrame = bmp;
		setImageBitmap(mCurrentFrame);
	}

}
