package robots.piratedotty.gui;

import java.io.InputStream;

import org.dobots.R;
import org.dobots.utilities.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;

public class PirateDottySail extends View {
	
	private Movie mMovie = null;

	private long mMovieStart;
	
	public PirateDottySail(Context context) {
        super(context);
        load();
	}

    public PirateDottySail(Context context, AttributeSet attrs) {
        super(context, attrs);
        load();  
    }

    public PirateDottySail(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        load();
    }
    
    private void load() {
//        setFocusable(true);
        
    	Utils.runAsyncTask(new Runnable() {
			
			@Override
			public void run() {
				InputStream is = getResources().openRawResource(R.drawable.animated_sail);
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
