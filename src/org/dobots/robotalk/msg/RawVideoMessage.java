package org.dobots.robotalk.msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RawVideoMessage extends BaseVideoMessage {
	
	private byte[] videoData;
	private Bitmap bmp = null;

	public RawVideoMessage(String i_strRobotName, Bitmap i_oBmp, int i_nRotation) {
		super(BaseVideoMessage.RAW_VIDEO, i_strRobotName, i_nRotation);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		i_oBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

		videoData  = stream.toByteArray();
	}
	
	public RawVideoMessage(String i_strRobotName, byte[] i_data, int i_nRotation) {
		super(BaseVideoMessage.RAW_VIDEO, i_strRobotName, i_nRotation);
		
		videoData  = i_data;
	}
	
	public RawVideoMessage(JSONObject i_oHeader, byte[] i_data) throws JSONException {
		super(i_oHeader);
		
		videoData = i_data;
	}
	
	public byte[] getVideoData() {
		return videoData;
	}

	@Override
	public Bitmap getAsBmp() {
		if (bmp == null) {
			// decode the received frame from jpeg to a bitmap
			ByteArrayInputStream stream = new ByteArrayInputStream(getVideoData());
			bmp = BitmapFactory.decodeStream(stream);
		}
		return bmp;
	}

}

