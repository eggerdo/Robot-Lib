package org.dobots.robotalk.msg;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public abstract class BaseVideoMessage {

	public static int TRANSACTION_ID = 0;
	
	public static final int RAW_VIDEO = 0xae;
	public static final int BASE64_VIDEO = 0xaf;

	public int nHeaderID;
	public int nTransactionID;
	public long lTimeStamp;
	public String strRobotID;
	public String strVersion;
	public int nRotation;

	public BaseVideoMessage(int i_nID, String i_strRobot, int i_nRotation) {
		nHeaderID = i_nID;
		nTransactionID = TRANSACTION_ID++;
		lTimeStamp = System.currentTimeMillis();
		strRobotID = i_strRobot;
		strVersion = RoboCommandTypes.VERSION;
		nRotation = i_nRotation;
	}

	public BaseVideoMessage(JSONObject i_oHeader) throws JSONException {
		nHeaderID 		= i_oHeader.getInt(RoboCommandTypes.F_HEADER_ID);
		nTransactionID 	= i_oHeader.getInt(RoboCommandTypes.F_TID);
		lTimeStamp 		= i_oHeader.getLong(RoboCommandTypes.F_TIMESTAMP);
		strRobotID		= i_oHeader.getString(RoboCommandTypes.F_ROBOT_ID);
		nRotation		= i_oHeader.getInt(RoboCommandTypes.F_ROTATION);
		strVersion		= i_oHeader.getString(RoboCommandTypes.F_VERSION);
	}
	
	private JSONObject toJSON() {
		JSONObject header = new JSONObject();
		try {
			header.put(RoboCommandTypes.F_HEADER_ID, 	nHeaderID);
			header.put(RoboCommandTypes.F_TID, 			nTransactionID);
			header.put(RoboCommandTypes.F_TIMESTAMP, 	lTimeStamp);
			header.put(RoboCommandTypes.F_ROBOT_ID, 	strRobotID);
			header.put(RoboCommandTypes.F_ROTATION,		nRotation);
			header.put(RoboCommandTypes.F_VERSION,		strVersion);
			return header;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String toJSONString() {
		return toJSON().toString();
	}
	
	public String getHeader() {
		return toJSONString();
	}
	
	public String getRobotID() {
		return strRobotID;
	}
	
	public abstract byte[] getVideoData();
	
	public static BaseVideoMessage decodeVideoMessage(String i_strHeader, byte[] i_rgbData) {
		
		JSONObject header;
		try {
			header = new JSONObject(i_strHeader);
			
			switch(header.getInt(RoboCommandTypes.F_HEADER_ID)) {
			case RAW_VIDEO:
				return new RawVideoMessage(header, i_rgbData);
			case BASE64_VIDEO:
				return new Base64VideoMessage(header, i_rgbData);
			default: 
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	public abstract Bitmap getAsBmp();
	
}
