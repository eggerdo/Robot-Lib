package org.dobots.communication.video;

import android.graphics.Bitmap;

public interface IVideoListener {
	
	/**
	 * callback when a frame is received. in case the frame is rotated before being sent, it has to be
	 * rotated by the given rotation before being displayed 
	 * @param bmp received frame decoded as a bitmap
	 * @param rotation rotation so that it can be displayed correctly. 0 if it doesn't have to be rotated.
	 */
	public void onFrame(final Bitmap bmp, final int rotation);
}

