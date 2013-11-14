package org.dobots.communication.dobots;

import org.json.JSONException;
import org.json.JSONObject;

public class DoBotsMessageEncoder {

	public static final int SENSOR_DATA 	= 0;
	public static final int DRIVE_COMMAND 	= 1;
	public static final int MOTOR_COMMAND 	= 2;
	public static final int CONTROL_COMMAND = 3;
	public static final int DISCONNECT		= 4;
	
	public static final int INT_T		= 0;
	public static final int DOUBLE_T	= 1;
	public static final int STRING_T	= 2;
	public static final int BOOL_T		= 3;

	/////////////////////////////////////////////////
	
	public static enum HeaderType { HEADER_SMALL, HEADER_BIG };
	
	public static HeaderType HEADER_TYPE = HeaderType.HEADER_BIG;
	
	private static final String JSON_VERSION = "v0.1"; 
	
	private static int mTransactionID = 0;

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Private
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void addHeader(JSONObject json, int message_id) throws JSONException {
		switch(HEADER_TYPE) {
		case HEADER_BIG:
			JSONObject header = new JSONObject();
			header.put("id", message_id);
			header.put("tid", mTransactionID++);
			header.put("timestamp", System.currentTimeMillis());
			header.put("version", JSON_VERSION);
			json.put("header", header);
			break;
		case HEADER_SMALL:
			json.put("id", message_id);
			break;
		}
	}

	private static JSONObject createJsonBase(int message_id) throws JSONException {
		JSONObject json = new JSONObject();
		addHeader(json, message_id);
		return json;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Public
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static JSONObject createControlCommand(boolean enable) throws JSONException {
		JSONObject json = createJsonBase(CONTROL_COMMAND);
		
		JSONObject data = new JSONObject();
		data.put("enabled", enable);
		json.put("data", data);
		
		return json;
	}
	
	public static byte[] getControlCommandPackage(boolean enable) {
		JSONObject json;
		try {
			json = createControlCommand(enable);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject createMotorCommand(int motor_id, int direction, int speed) throws JSONException {
		JSONObject json = createJsonBase(MOTOR_COMMAND);
		
		JSONObject data = new JSONObject();
		data.put("motor_id", motor_id);
		data.put("direction", direction);
		data.put("speed", speed);
		json.put("data", data);
		
		return json;
	}

	public static byte[] getMotorCommandPackage(int id, int direction, int speed) {
		JSONObject json;
		try {
			json = createMotorCommand(id, direction, speed);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject createDriveCommand(int left, int right) throws JSONException {
		JSONObject json = createJsonBase(DRIVE_COMMAND);
		
		JSONObject data = new JSONObject();
		data.put("left", left);
		data.put("right", right);
		json.put("data", data);
		
		return json;
	}

	public static byte[] getDriveCommandPackage(int left, int right) {
		JSONObject json;
		try {
			json = createDriveCommand(left, right);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject createDisconnectCommand() throws JSONException {
		return createJsonBase(DISCONNECT);
	}

	public static byte[] getDisconnectPackage() {
		JSONObject json;
		try {
			json = createDisconnectCommand();
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
