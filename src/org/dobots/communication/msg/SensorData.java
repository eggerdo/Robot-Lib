package org.dobots.communication.msg;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

public class SensorData {
	
//	private JSONArray mJsonSensorList;
	
	private String mRobotID;
	private ArrayList<SensorItem> mSensorList;
	
	public SensorData(String robotID) {
//		mJsonSensorList = new JSONArray();
		mRobotID = robotID;
		mSensorList = new ArrayList<SensorItem>();
	}
	
	public SensorData(String robotID, String jsonItem) {
		mRobotID = robotID;
		try {
			JSONArray jsonSensorList = new JSONArray(jsonItem);
			mSensorList = new ArrayList<SensorItem>(jsonSensorList.length());
					
			for (int i = 0; i < jsonSensorList.length(); ++i) {
				mSensorList.add(i, new SensorItem(jsonSensorList.getJSONObject(i)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			
			mSensorList = new ArrayList<SensorItem>();
		}
	}
	
	public String getRobotID() {
		return mRobotID;
	}
	
	public SensorItem getItem(int index) {
		return mSensorList.get(index);
	}
	
	public SensorItem getItem(String name) {
		try {
			for (SensorItem item : mSensorList) {
				if (item.getName().equals(name)) {
					return item;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addItem(SensorItem item) {
		mSensorList.add(item);
	}

	public <T> void addItem(String name, T value) {
		SensorItem item = new SensorItem(name);
		try {
			item.put(value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	public void addItem(String name, int value) {
//		SensorItem item = new SensorItem(name);
//		try {
//			item.put(value);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void addItem(String name, double value) {
//		SensorItem item = new SensorItem(name);
//		try {
//			item.put(value);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void addItem(String name, String value) {
//		SensorItem item = new SensorItem(name);
//		try {
//			item.put(value);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void addItem(String name, boolean value) {
//		SensorItem item = new SensorItem(name);
//		try {
//			item.put(value);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public String toJSONString() {
		JSONArray list = new JSONArray();
		for (SensorItem item : mSensorList) {
			list.put(item.toJSON());
		}
		return list.toString();
	}

}
