package robots.piratedotty.gui;

import java.io.InputStream;

import org.dobots.utilities.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PirateDottySail extends View {
	
	private Movie mMovie = null;

	private long mMovieStart;
	
	public PirateDottySail(Context context) {
        super(context);
	}

    public PirateDottySail(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PirateDottySail(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void load(final int resourceId) {
//        setFocusable(true);
        
    	Utils.runAsyncTask(new Runnable() {
			
			@Override
			public void run() {
				InputStream is = getResources().openRawResource(resourceId);
				mMovie = Movie.decodeStream(is);
			}
		});
    }
    
    @Override 
    protected void onDraw(Canvas canvas) {
    	if (mMovie == null) {
    		invalidate();
    		return;
    	}
    	
        canvas.drawColor(0xFFCCCCCC);  
        
        canvas.scale((float)getWidth() / mMovie.width(), (float)getHeight() / mMovie.height());
       
        Paint p = new Paint();
        p.setAntiAlias(true);
       
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
            mMovie.draw(canvas, 0, 0, p);
            invalidate();
        }
    }


}
