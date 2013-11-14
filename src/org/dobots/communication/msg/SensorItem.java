package org.dobots.communication.msg;

import java.lang.reflect.ParameterizedType;

import org.json.JSONException;
import org.json.JSONObject;

public class SensorItem {
	
	public class SensorValueTypeException extends Exception {

		/**
		 * 
		 */
		public static final long serialVersionUID = -1165979526914589510L;
	};

	public static final String FIELD_NAME = "name";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_VALUE = "value";
	
	public enum SensorType {
		UNKNOWN_T(-1),
		INT_T(0),
		DOUBLE_T(1),
		STRING_T(2),
		BOOL_T(3);
		int mValue;
		
		SensorType(int value) {
			mValue = value;
		}
		
		public int value() {
			return mValue;
		}
		
		public static SensorType fromValue(int value) {
			for (SensorType type : SensorType.values()) {
				if (type.value() == value) {
					return type;
				}
			}
			return null;
		}
	}
	

	private JSONObject mJsonItem;
	
	public SensorItem(String name) {
		mJsonItem = new JSONObject();
		try {
			mJsonItem.put(FIELD_NAME, name);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SensorItem(JSONObject item) {
		mJsonItem = item;
	}
	
	public SensorType getType() throws JSONException {
		int type = mJsonItem.getInt(FIELD_TYPE);
		return SensorType.fromValue(type);
	}
	
	public String getValue() throws JSONException {
		switch(getType()) {
		case INT_T:
			return Integer.toString(mJsonItem.getInt(FIELD_VALUE));
		case DOUBLE_T:
			return Double.toString(mJsonItem.getDouble(FIELD_VALUE));
		case STRING_T:
			return mJsonItem.getString(FIELD_VALUE);
		case BOOL_T:
			return Boolean.toString(mJsonItem.getBoolean(FIELD_VALUE));
		default:
			return "";
		}
	}

	public int getInt() throws JSONException, SensorValueTypeException {
		if (getType() != SensorType.INT_T) {
			throw new SensorValueTypeException();
		}
		return mJsonItem.getInt(FIELD_VALUE);
	}

	public double getDouble() throws JSONException, SensorValueTypeException {
		if (getType() != SensorType.DOUBLE_T) {
			throw new SensorValueTypeException();
		}
		return mJsonItem.getDouble(FIELD_VALUE);
	}

	public String getString() throws JSONException, SensorValueTypeException {
		if (getType() != SensorType.STRING_T) {
			throw new SensorValueTypeException();
		}
		return mJsonItem.getString(FIELD_VALUE);
	}

	public boolean getBoolean() throws JSONException, SensorValueTypeException {
		if (getType() != SensorType.BOOL_T) {
			throw new SensorValueTypeException();
		}
		return mJsonItem.getBoolean(FIELD_VALUE);
	}

	public String getName() throws JSONException {
		return mJsonItem.getString(FIELD_NAME);
	}

	public Class returnedClass() {
	     ParameterizedType parameterizedType = (ParameterizedType)getClass()
	                                                 .getGenericSuperclass();
	     return (Class) parameterizedType.getActualTypeArguments()[0];
	}
	
	public SensorType getTypeFromClass() {
		Class cls = returnedClass();
		if (cls.equals(int.class)) {
			return SensorType.INT_T;
		} else if (cls.equals(double.class)) {
			return SensorType.DOUBLE_T;
		} else if (cls.equals(String.class)) {
			return SensorType.STRING_T;
		} else if (cls.equals(boolean.class)) {
			return SensorType.BOOL_T;
		} else {
			return SensorType.UNKNOWN_T;
		}
	}
	
	public <T> void put(T value) throws JSONException {
		mJsonItem.put(FIELD_TYPE, getTypeFromClass().value());
		mJsonItem.put(FIELD_VALUE, value);
	}

//	public void put(int value) throws JSONException {
//		mJsonItem.put(FIELD_TYPE, SensorType.INT_T.value());
//		mJsonItem.put(FIELD_VALUE, value);
//	}
//
//	public void put(double value) throws JSONException {
//		mJsonItem.put(FIELD_TYPE, SensorType.DOUBLE_T.value());
//		mJsonItem.put(FIELD_VALUE, value);
//	}
//
//	public void put(String value) throws JSONException {
//		mJsonItem.put(FIELD_TYPE, SensorType.STRING_T.value());
//		mJsonItem.put(FIELD_VALUE, value);
//	}
//
//	public void put(boolean value) throws JSONException {
//		mJsonItem.put(FIELD_TYPE, SensorType.BOOL_T.value());
//		mJsonItem.put(FIELD_VALUE, value);
//	}
	
	public JSONObject toJSON() {
		return mJsonItem;
	}

}
