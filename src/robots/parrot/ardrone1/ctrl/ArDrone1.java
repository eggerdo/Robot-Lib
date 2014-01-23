package robots.parrot.ardrone1.ctrl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.dobots.utilities.Utils;
import org.dobots.zmq.video.FpsCounter;
import org.dobots.zmq.video.IFpsListener;

import robots.RobotType;
import robots.parrot.ctrl.Parrot;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;

import com.codeminders.ardrone.DroneVideoListener;

public class ArDrone1 extends Parrot implements DroneVideoListener {

	private static final String TAG = ArDrone1.class.getSimpleName();
	
	@Override
	public RobotType getType() {
		// TODO Auto-generated method stub
		return RobotType.RBT_ARDRONE1;
	}

	@Override
	public void startVideo() {
		debug(TAG, "startVideo()");
		Utils.runAsyncTask(new Runnable() {

			@Override
			public void run() {
				try {
					m_oController.connectVideoArDrone1();
					setVideoListener(ArDrone1.this);
				} catch (IOException e) {
					e.printStackTrace();
					error(TAG, "... failed");
				}
			}
		});
	}

	@Override
	public void stopVideo() {
		removeVideoListener(this);
	}

	@Override
    public void frameReceived(final int startX, final int startY, final int w, final int h, final
            int[] rgbArray, final int offset, final int scansize) {
		
			Utils.runAsyncTask(new Runnable() {
				@Override
				public void run() {

					(new VideoDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute();
				}
			});
	}
	
	private class VideoDisplayer extends AsyncTask<Void, Integer, Void> {
        
        public Bitmap b;
        public int[]rgbArray;
        public int offset;
        public int scansize;
        public int w;
        public int h;
        public ByteArrayOutputStream bos;
        
        public VideoDisplayer(int x, int y, int width, int height, int[] arr, int off, int scan) {
            // do stuff
            rgbArray = arr;
            offset = off;
            scansize = scan;
            w = width;
            h = height;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            b =  Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
            bos = new ByteArrayOutputStream();
            b.compress(CompressFormat.JPEG, 50, bos);
            return null;
        }
        
        @Override
        protected void onPostExecute(Void param) {
        	m_oVideoSender.onFrame(bos.toByteArray(), 0);
        }
    }

}
