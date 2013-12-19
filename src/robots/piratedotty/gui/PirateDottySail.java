package robots.piratedotty.gui;

import java.io.InputStream;

import org.dobots.utilities.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PirateDottySail extends View {
	
	private static final String TAG = "PirateDottySail";
	
	private Movie mMovie = null;

	private long mMovieStart;
	
	private Paint mPainter;
	
	public PirateDottySail(Context context) {
        super(context);
        initialize();
	}

    public PirateDottySail(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PirateDottySail(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }
    
    private void initialize() {
        load();

        mPainter = new Paint();
        mPainter.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, mPainter);
    }
    
    public void load() {
//        setFocusable(true);
        
    	Utils.runAsyncTask(new Runnable() {
			
			@Override
			public void run() {
				int resourceID = getResources().getIdentifier("animated_sail", "drawable", getContext().getPackageName());
				if (resourceID != 0) {
					InputStream is = getResources().openRawResource(resourceID);
					mMovie = Movie.decodeStream(is);
				} else {
					Log.e(TAG, "animated_sail not found in drawables");
				}
			}
		});
    }
    
//    private void load() {
//    	Utils.runAsyncTask(new Runnable() {
//    		@Override
//    		public void run() {
//    			InputStream is = getResources().openRawResource(R.drawable.animated_sail);
//    			mMovie = Movie.decodeStream(is);
//    		}
//    	});
//    }
    
    @Override 
    protected void onDraw(Canvas canvas) {
    	if (mMovie == null) {
    		invalidate();
    		return;
    	}
    	
        canvas.drawColor(0xFFCCCCCC);  
        
        canvas.scale((float)getWidth() / mMovie.width(), (float)getHeight() / mMovie.height());
       
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {   // first time
            mMovieStart = now;
        }
        if (mMovie != null) {
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int)((now - mMovieStart) % dur);
            mMovie.setTime(relTime);
//            mMovie.draw(canvas, (getWidth() - mMovie.width()) / 2,
//                        (getHeight() - mMovie.height()) / 2);
            mMovie.draw(canvas, 0, 0, mPainter);
            invalidate();
        }
    }


}
