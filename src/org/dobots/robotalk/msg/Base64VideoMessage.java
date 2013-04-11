package org.dobots.robotalk.msg;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Base64;

public class Base64VideoMessage extends BaseVideoMessage {

	public static final String F_ROTATION 	= "rotation";
	public static final String F_FRAME 		= "frame";

	public String strVideoBase64; 

	public Base64VideoMessage(String i_strRobotName, byte[] i_data, int i_nRotation) {
		super(BaseVideoMessage.BASE64_VIDEO, i_strRobotName, i_nRotation);
		
		strVideoBase64 = Base64.encodeToString(i_data, Base64.DEFAULT);
	}
	
	public Base64VideoMessage(JSONObject i_oHeader, byte[] i_data) throws JSONException {
		super(i_oHeader);
		
		strVideoBase64 = new String(i_data);
	}

	public byte[] getVideoData() {
		return strVideoBase64.getBytes();
	}

	@Override
	public Bitmap getAsBmp() {
		return null;
	}
}
