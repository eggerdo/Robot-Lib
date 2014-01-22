package robots.parrot.ardrone2.ctrl;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Semaphore;

import org.dobots.utilities.DoBotsThread;
import org.dobots.utilities.Utils;
import org.dobots.zmq.video.FpsCounter;
import org.dobots.zmq.video.IFpsListener;
import org.dobots.zmq.video.IRawVideoListener;

import robots.gui.comm.IConnectListener;
import robots.parrot.ctrl.ParrotTypes;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

/*
 * The Parrot sends video encoded as H264. The ParrotVideoRender uses native code and the ffmpeg library
 * to decode the H264 video stream into bitmaps for each received frame. This bitmap can be directly given
 * to an ImageView and displayed on screen. Doing so we achieve a frame rate of ~30 Hz.
 * In order to use the framework set up for zmq and dodedodo, the ParrotVideoRenderer should provide the video
 * as a compressed jpeg feed, so that it can be sent over zmq and dodedodo. But compressing the frames to jpeg
 * then send them over zmq, receive them and decode them again as bmp results in a frame rate of ~15 Hz,
 * depending on the processing power of the android device. In addition, we observe a slight delay in the video
 * feed due to the compression / decompression. To avoid this delay and lower frame rate, we instead provide the
 * possibility to give an ImageView to the renderer so that it can display the bitmap decoded from the H264 
 * directly, without the need of compressing back and forth from jpeg. The ParrotVideoRenderer still provides 
 * the video feed as jpeg in a separate thread for any other element that wants to receive the parrot's video. 
 * This does not have any effect on the displaying of the video as bmp directly. Doing this hack around we can 
 * display the video on the android device with 30 Hz while still having the video feed as compressed
 * jpeg for dodedodo with 15 Hz
 */
public class ArDrone2VideoRenderer extends Thread {

	private static final String TAG = ArDrone2VideoRenderer.class.getName();

    private Bitmap mFrame;
    private boolean mRun = false;
    private boolean mPause = false;
	private boolean mClose = false;

    private ImageView m_oImage = null;
	private Handler m_oUiHandler = new Handler(Looper.getMainLooper());

//    private BaseActivity m_oActivity;
    
    private IConnectListener m_oConnectListener;
    private IRawVideoListener m_oVideoListener;
//    private IVideoListener m_oVideoListener;
    
    private boolean m_bVideoConnected = false;
    
	private int m_nErrorCount = 0;
	
	private String m_strAddress = ParrotTypes.PARROT_IP;

	private ByteArrayOutputStream bos = new ByteArrayOutputStream();
	
	public ArDrone2VideoRenderer() {
		super("Parrot Video Renderer");
//		m_oImage = i_oImage;
		
		mFrame = Bitmap.createBitmap(ParrotTypes.VIDEO_WIDTH, ParrotTypes.VIDEO_HEIGHT, Bitmap.Config.RGB_565); //ARGB_8888
//		m_oImage.setImageBitmap(m_bmpVideo);
	}

	public void setConnectListener(IConnectListener i_oListener) {
		m_oConnectListener = i_oListener;
	}
	
	public void setVideoListener(IRawVideoListener listener) {
		m_oVideoListener = listener;
	}
	
	public void setVideoView(ImageView view) {
		m_oImage = view;
		if (m_oImage != null) {
			m_oImage.setImageBitmap(mFrame);
		}
	}
	
	public void setConnection(String address) {
		m_strAddress = address;
	}
	
	private void onConnect(boolean connected) {
		if (m_oConnectListener != null) {
			m_oConnectListener.onConnect(connected);
		}
	}
	
	public void connect() {

		String strVideoAddr = String.format("http://%s:%d", m_strAddress, ParrotTypes.MEDIA_PORT);
		
        if (nativeOpenFromURL(strVideoAddr, ParrotTypes.VIDEO_CODEC) != ParrotTypes.SUCCESS)
        {
            nativeClose();
            Log.i(TAG, "nativeOpen() failed, throwing RuntimeException");
            onConnect(false);
            return;
        }

        if (nativeOpenVideo(mFrame) != ParrotTypes.SUCCESS) 
        {
            nativeCloseVideo();
	        Log.i(TAG, "unable to open a stream, throwing RuntimeException");
	        onConnect(false);
            return;
        }

        startProcess();
	}
	
	private void startProcess() {
		
		if (!mRun) {
			mRun = true;
			start();
		} else if (mPause) {
			// nothing to do
			mPause = false;
		}
		
	}
	
	Semaphore mSemaphore = new Semaphore(1);
	Bitmap mCurrentFrame;
	FpsCounter counter = new FpsCounter(new IFpsListener() {
		
		@Override
		public void onFPS(int i_nFPS) {
			Log.d(TAG, "fps ffmpeg: " + i_nFPS);
		}
	});
	
	FpsCounter counter2 = new FpsCounter(new IFpsListener() {
		
		@Override
		public void onFPS(int i_nFPS) {
			Log.d(TAG, "fps tojpg: " + i_nFPS);
		}
	});
	
	private boolean mNew = false;
	
	DoBotsThread mConverter = new DoBotsThread("JpegConverter") {
		
		@Override
		public void shutDown() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected void execute() {
			if (mNew) {
				try {
	//				Loggable.startTimeMeasurement(0);
					mSemaphore.acquire();
//					counter2.tick();
					
					bos.reset();
					mCurrentFrame.compress(CompressFormat.JPEG, 50, bos);
					byte[] rgb = bos.toByteArray();
					m_oVideoListener.onFrame(rgb, 0);
//					int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
//					bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

					mNew = false;
					mSemaphore.release();
	//				Loggable.stopTimeMeasurement(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	public void run() {
        Log.d(TAG, "entering run()");
        
        // give the ffmpeg library some time to settle before
        // starting to decode the frames
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        mConverter.start();
        
        while (mRun)
        {
        	if (mPause) {
        		try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		continue;
        	}
        	
//        	Loggable.startTimeMeasurement(0);
        	int nResult = nativeDecodeFrame();
//        	Loggable.stopTimeMeasurement(0);
    		if (nResult == ParrotTypes.SUCCESS)
            {
    			if (!m_bVideoConnected) {
    		        onConnect(true);
    		        m_bVideoConnected = true;
    			}

//    			Loggable.startTimeMeasurement(1);
    			nResult = nativeUpdateBitmap();
//    			Loggable.stopTimeMeasurement(1);
    			if (nResult == ParrotTypes.SUCCESS) {
					m_nErrorCount = 0;

//					counter.tick();
					
//					Loggable.startTimeMeasurement(0);
//					int[] pixels = new int[m_bmpVideo.getWidth() * m_bmpVideo.getHeight()];
//					m_bmpVideo.getPixels(pixels, 0, m_bmpVideo.getWidth(), 0, 0, m_bmpVideo.getWidth(), m_bmpVideo.getHeight());
//					Loggable.stopTimeMeasurement(0);
					
					if (m_oImage != null) {
						m_oUiHandler.post(new Runnable() {
							
							@Override
							public void run() {
								if (m_oImage != null) {
									// image must be updated by GUI thread (main thread)
									m_oImage.invalidate();
								}
							}
						});
					}
						
					if (mSemaphore.tryAcquire()) {
						mCurrentFrame = mFrame;
						mNew = true;
////						Utils.runAsyncTask(new Runnable() {
////							
////							@Override
////							public void run() {
////								try {
//////									Loggable.startTimeMeasurement(0);
////									sema.acquire();
//////									counter2.tick();
////									bos.reset();
////									bmp.compress(CompressFormat.JPEG, 50, bos);
////									byte[] rgb = bos.toByteArray();
////									m_oVideoListener.onFrame(rgb, 0);
////									sema.release();
//////									Loggable.stopTimeMeasurement(0);
////								} catch (InterruptedException e) {
////									// TODO Auto-generated catch block
////									e.printStackTrace();
////								}
////							}
////						});
						mSemaphore.release();
					}
					
    			} else if (nResult == ParrotTypes.CODEC_DIMENSION_ERROR) {

    				// NOTE: I have spent several days trying to find out why the ffmpeg library
    				// suddenly crashes after some time but without success. I tried recompiling
    				// the library without success. The only thing I found out is that it always
    				// happens after receiving some erroneous frames which result in codec dimensions
    				// that don't correspond with the 640x360. To avoid crashes of the whole app because
    				// of that I now close the library if the codec dimension error is detected and
    				// then reconnect to continue displaying the video which seems to work so far.
    				m_nErrorCount++;
    				if (m_nErrorCount >= 3) {
	                	nativeClose();
	                	
	                	Utils.waitSomeTime(2000);
	                	
	                	connect();
	                	
	                	m_nErrorCount = 0;
    				}
    			} else if (nResult == ParrotTypes.BITMAP_LOCKPIXELS_FAILED) {
    				m_nErrorCount++;
    				if (m_nErrorCount >= 3) {
    					mFrame = null;
    					mPause = true;

    					mFrame = Bitmap.createBitmap(ParrotTypes.VIDEO_WIDTH, ParrotTypes.VIDEO_HEIGHT, Bitmap.Config.RGB_565); //ARGB_8888
    					
    					nativeClose();
	                	
	                	Utils.waitSomeTime(2000);
	                	
	                	connect();
	                	
	                	m_nErrorCount = 0;
	                	
    					mPause = false;
    				}
                }
            } else if (nResult == ParrotTypes.READ_FRAME_FAILED) {

				m_nErrorCount++;
				if (m_nErrorCount >= 3) {
	            	mRun = false;
	            	onConnect(false);
				}
            } 
        }
        
        mConverter.destroy();
        
        // close video
        nativeClose();
        
        mClose = true;

        Log.d(TAG, "leaving run()");
	}
	
	public void close() {
        mRun = false;
        while (!mClose) {
        	Utils.waitSomeTime(20);
        }
	}
	
	public void pauseThread() {
		mPause = true;
	}
	
	public void resumeThread() {
		mPause = false;
	}
	
	static {
    	System.loadLibrary("ffmpeg");
    	System.loadLibrary("ardrone2");
	}

    //native methods are described in jni/avjni.c
    private native int nativeOpenFromURL(String url, String format);
    private native void nativeClose();
    private native int nativeOpenVideo(Object aBitmapRef);
    private native void nativeCloseVideo();
    private native int nativeDecodeFrame(); //never touch the bitmap here
    private native int nativeUpdateBitmap();

}
