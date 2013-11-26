package org.dobots.communication.video;

import java.util.concurrent.Semaphore;

import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.utilities.DoBotsThread;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;

import android.graphics.Bitmap;

public class VideoThrottle extends DoBotsThread implements IRawVideoListener, IVideoListener {

	private ZmqVideoReceiver mVideoReceiver;
	private ZMQ.Socket mVideoSocket;
	
	private byte[] mRgbFrame;
	private int mRotation;
	private Bitmap mBmpFrame;
	
	private Semaphore mSemaphore;

	private double mFrameRate = 0.0;
	
	private IRawVideoListener mRawVideoListener;
	private IVideoListener mVideoListener;
	
	public VideoThrottle(String threadName) {
		super(threadName);
		
		mSemaphore = new Semaphore(1);
		
		mVideoSocket = ZmqHandler.getInstance().obtainVideoRecvSocket();
		mVideoSocket.subscribe("".getBytes());
		
		mVideoReceiver = new ZmqVideoReceiver(mVideoSocket);
		mVideoReceiver.setRawVideoListner(this);
		mVideoReceiver.start();
	}
	
	public void setRawVideoListener(IRawVideoListener listener) {
		mRawVideoListener = listener;
	}
	
	public void setVideoListener(IVideoListener listener) {
		mVideoListener = listener;
	}

	@Override
	public void shutDown() {
		mVideoReceiver.close();
	}

	@Override
	protected void execute() {

		long start = System.currentTimeMillis();
		
		if (mRawVideoListener != null && mRgbFrame != null) {
			
			try {
				mSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			
			mRawVideoListener.onFrame(mRgbFrame, mRotation);
			mRgbFrame = null;
			
			mSemaphore.release();
			
		} else if (mVideoListener != null && mBmpFrame != null) {

			try {
				mSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			
			mVideoListener.onFrame(mBmpFrame, mRotation);
			mBmpFrame = null;
			
			mSemaphore.release();
		}

		long end = System.currentTimeMillis();
		
		if (mFrameRate > 0) {
			int sleep = (int) ((1000 / mFrameRate) - (end - start));
			if (sleep > 0) {
				Utils.waitSomeTime(sleep);
			}
		}
	}

	@Override
	public void onFrame(byte[] rgb, int rotation) {
		// if semaphore is available, we assign the frame
		// otherwise if a frame is being processed we
		// drop this frame
		if (mSemaphore.tryAcquire()) {
			mRgbFrame = rgb;
			mRotation = rotation;
			mSemaphore.release();
		}
	}
	
	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		// if semaphore is available, we assign the frame
		// otherwise if a frame is being processed we
		// drop this frame
		if (mSemaphore.tryAcquire()) {
			mBmpFrame = bmp;
			mRotation = rotation;
			mSemaphore.release();
		}
	}

	public void setFrameRate(double rate) {
		mFrameRate = rate;
	}
	
	public double getFrameRate() {
		return mFrameRate;
	}

}
