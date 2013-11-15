package org.dobots.communication.msg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SensorMessageData {
	
	private String mRobotID;
	private String mSensorID;
	private JSONArray mValueList;
	
	private SensorMessageData(String robotID) {
		mRobotID = robotID;
		mValueList = new JSONArray();
	}
	
	public SensorMessageData(String robotID, String sensorID) {
		mRobotID = robotID;
		mSensorID = sensorID;
		mValueList = new JSONArray();
	}
	
	public static SensorMessageData decodeJSON(String robotID, String jsonItem) {

		SensorMessageData data = new SensorMessageData(robotID);

		try {
			JSONObject sensorData = new JSONObject(jsonItem);
			
			data.mSensorID = sensorData.getString("group_name");
			data.mValueList = (JSONArray)sensorData.get("data");
		} catch (JSONException e) {
			e.printStackTrace();
			
			data.mValueList = new JSONArray();
		}
		
		return data;
	}
	
	public String getRobotID() {
		return mRobotID;
	}
	
	public String getSensorID() {
		return mSensorID;
	}
	
	public int getInt(int index) throws JSONException {
		return mValueList.getInt(index);
	}
	
	public double getDouble(int index) throws JSONException {
		return mValueList.getDouble(index);
	}
	
	public boolean getBoolean(int index) throws JSONException {
		return mValueList.getBoolean(index);
	}
	
	public String getString(int index) throws JSONException {
		return mValueList.getString(index);
	}
	
	public <T> void addItem(T value) {
		mValueList.put(value);
	}

	public String toJSONString() {
		JSONObject data = new JSONObject();
		try {
			data.put("group_name", mSensorID);
			data.put("data", mValueList);
			return data.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
