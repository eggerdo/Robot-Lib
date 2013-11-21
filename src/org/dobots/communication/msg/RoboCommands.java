package org.dobots.communication.msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import robots.ctrl.RemoteControlHelper;

public class RoboCommands {

	public static int TRANSACTION_ID = 0;
	private static RoboCommands INSTANCE = null;
	
	public static RoboCommands getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RoboCommands();
		}
		return INSTANCE;
	}
	
	public enum HeaderType {
		ht_small, ht_ext
	}
	
	private HeaderType mHeaderType = HeaderType.ht_small;
	
	public void setHeaderType(HeaderType type) {
		mHeaderType = type;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//// BASE command
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static BaseCommand decodeCommand(String i_strJson) {
		
		JSONObject oJSON;
		try {
			oJSON = new JSONObject(i_strJson);
			
			switch(getHeader(oJSON)) {
			case RoboCommandTypes.DRIVE_COMMAND:
				return getInstance().new DriveCommand(oJSON);
			case RoboCommandTypes.CAMERA_COMMAND:
				return getInstance().new CameraCommand(oJSON);
			case RoboCommandTypes.CONTROL_COMMAND:
				return getInstance().new ControlCommand(oJSON);
			default: 
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	private static int getHeader(JSONObject json) throws JSONException {
		if (getInstance().mHeaderType == HeaderType.ht_ext) {
			JSONObject header = json.getJSONObject(RoboCommandTypes.S_HEADER);
			return header.getInt(RoboCommandTypes.F_HEADER_ID);
		} else {
			return json.getInt(RoboCommandTypes.F_HEADER_ID);
		}
	}
	
	public abstract class BaseCommand {
		public int nHeaderID;
		public int nTransactionID;
		public long lTimeStamp;
		public String strRobotID;
		public String strVersion;
		
		public BaseCommand(int i_nID, String i_strRobot) {
			nHeaderID = i_nID;
			nTransactionID = TRANSACTION_ID++;
			lTimeStamp = System.currentTimeMillis();
			strRobotID = i_strRobot;
			strVersion = RoboCommandTypes.VERSION;
		}
		
		public BaseCommand(JSONObject i_oObj) throws JSONException {
			if (mHeaderType == HeaderType.ht_ext) {
				JSONObject header = i_oObj.getJSONObject(RoboCommandTypes.S_HEADER);
				nHeaderID 		= header.getInt(RoboCommandTypes.F_HEADER_ID);
				nTransactionID 	= header.getInt(RoboCommandTypes.F_TID);
				lTimeStamp 		= header.getLong(RoboCommandTypes.F_TIMESTAMP);
				strRobotID		= header.getString(RoboCommandTypes.F_ROBOT_ID);
				strVersion		= header.getString(RoboCommandTypes.F_VERSION);
			} else {
				nHeaderID = i_oObj.getInt(RoboCommandTypes.F_HEADER_ID);
			}
		}
		
		public JSONObject toJSON() {
			JSONObject obj = new JSONObject();
			JSONObject header = new JSONObject();
			try {
				if (mHeaderType == HeaderType.ht_ext) {
					header.put(RoboCommandTypes.F_HEADER_ID, 	nHeaderID);
					header.put(RoboCommandTypes.F_TID, 			nTransactionID);
					header.put(RoboCommandTypes.F_TIMESTAMP, 	lTimeStamp);
					header.put(RoboCommandTypes.F_ROBOT_ID, 	strRobotID);
					header.put(RoboCommandTypes.F_VERSION,		strVersion);
					obj.put(RoboCommandTypes.S_HEADER, header);
				} else {
					obj.put(RoboCommandTypes.F_HEADER_ID, nHeaderID);
				}
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public String toJSONString() {
			return toJSON().toString();
		}
		
		public String getRobotID() {
			return strRobotID;
		}
		
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//// DRIVE command
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public class DriveCommand extends BaseCommand {
		
		public RemoteControlHelper.Move eMove;
		public double dblSpeed;
		public double dblRadius;
		
		public DriveCommand(String i_strRobot, RemoteControlHelper.Move i_eMove, double i_dblSpeed, double i_dblAngle) {
			super(RoboCommandTypes.DRIVE_COMMAND, i_strRobot);
			
			this.eMove = i_eMove;
			this.dblSpeed = i_dblSpeed; 
			this.dblRadius = i_dblAngle;
		}
		
		public DriveCommand(JSONObject i_oObj) throws JSONException {
			super(i_oObj);
			
			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
			eMove 		= RemoteControlHelper.Move.valueOf(data.getString(RoboCommandTypes.F_MOVE));
			dblSpeed 	= data.getDouble(RoboCommandTypes.F_SPEED);
			dblRadius 	= data.getDouble(RoboCommandTypes.F_RADIUS);
		}
		
		public JSONObject toJSON() {
			JSONObject obj = super.toJSON();
			JSONObject data = new JSONObject();
			try {
				data.put(RoboCommandTypes.F_MOVE, 	eMove.toString());
				data.put(RoboCommandTypes.F_SPEED, 	dblSpeed);
				data.put(RoboCommandTypes.F_RADIUS, dblRadius);
				obj.put(RoboCommandTypes.S_DATA, data);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	
	}
	
	public static DriveCommand createDriveCommand(String i_strRobot, RemoteControlHelper.Move i_eMove, double i_dblSpeed, double i_dblAngle) {
		return getInstance().new DriveCommand(i_strRobot, i_eMove, i_dblSpeed, i_dblAngle);
	}
	
	public static DriveCommand createDriveCommand(String i_strRobot, RemoteControlHelper.Move i_eMove, double i_dblSpeed) {
		return getInstance().new DriveCommand(i_strRobot, i_eMove, i_dblSpeed, 0);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//// CAMERA command
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public enum CameraCommandType {
		TOGGLE,
		OFF,
		ON,
		UP,
		DOWN,
		STOP
	}
	
	public class CameraCommand extends BaseCommand {

		public CameraCommandType eType;
		
		public CameraCommand(String i_strRobot, CameraCommandType i_eType) {
			super(RoboCommandTypes.CAMERA_COMMAND, i_strRobot);
			
			eType = i_eType;
		}
		
		public CameraCommand(JSONObject i_oObj) throws JSONException {
			super(i_oObj);
			
			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
			eType 	= CameraCommandType.valueOf(data.getString(RoboCommandTypes.F_TYPE));
		}
		
		public JSONObject toJSON() {
			JSONObject obj = super.toJSON();
			JSONObject data = new JSONObject();
			try {
				data.put(RoboCommandTypes.F_TYPE, eType.toString());
				obj.put(RoboCommandTypes.S_DATA, data);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static CameraCommand createCameraCommand(String i_strRobot, CameraCommandType i_eType) {
		return getInstance().new CameraCommand(i_strRobot, i_eType);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//// CONTROL command
	///////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * control commands are used as RPC, the command is the name of the method to be called, 
	 * and the parameters are given to the method in the order that they are defined. works 
	 * only if the type and order of the parameters is correct and only for primitive types,
	 * not for objects!!
	 * return values from methods are not (yet?) supported
	 */
	public class ControlCommand extends BaseCommand {
		
		public String mCommand;
		public ArrayList<Object> mParameterList;
		
		public ControlCommand(String i_strRobot, String command, Object... parameters) {
			super(RoboCommandTypes.CONTROL_COMMAND, i_strRobot);

			mCommand = command;
			mParameterList = new ArrayList<Object>();
			if (parameters != null) {
				for (Object param : parameters) {
					mParameterList.add(param);
				}
			}
		}
		
		public ControlCommand(JSONObject i_oObj) throws JSONException {
			super(i_oObj);
			
			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
			
			mCommand = data.getString(RoboCommandTypes.F_COMMAND);
			
			JSONArray parameter = (JSONArray)data.get(RoboCommandTypes.F_PARAMETER);
			
			mParameterList = new ArrayList<Object>(parameter.length());
			for (int i = 0; i < parameter.length(); ++i) {
				mParameterList.add(parameter.get(i));
			}
		}
		
		public JSONObject toJSON() {
			JSONObject obj = super.toJSON();
			JSONObject data = new JSONObject();
			try {
				data.put(RoboCommandTypes.F_COMMAND, mCommand);
				
				JSONArray parameter = new JSONArray();
//				for (String key : mParameterList.keySet()) {
//					param.put(key, mParameterList.get(key));
//				}
				for (Object param : mParameterList.toArray()) {
					parameter.put(param);
				}
				data.put(RoboCommandTypes.F_PARAMETER, parameter);
				
				obj.put(RoboCommandTypes.S_DATA, data);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public Object getParameter(int index) {
			return mParameterList.get(index);
		}

		public Object[] getParameters() {
			return mParameterList.toArray();
		}
		
	}

	/**
	 * checks the object's class for it's public methods and compares them with the
	 * command name defined in the ControlCommand. If a method matches, it is invoked
	 * on the object with the parameters defined in the ControlCommand
	 * 
	 * @param command ControlCommand (contains method name and parameters)
	 * @param object object on which the method should be invoked
	 * 
	 * @return return value of the invoked function
	 */
	public static Object handleControlCommand(ControlCommand command, Object object) {
		try {
			Method[] methods = object.getClass().getMethods();
			for (Method method : methods) {
				
				// first compare the method name to find the correct method
				if (method.getName().equals(command.mCommand)) {
					
					Object[] args = command.getParameters();
					Class[] types = method.getParameterTypes();
					
					// make sure that the number of parameters is correct (overloaded methods)
					if (types.length == args.length) {
						
						// check parameters for enumerations. those parameters have to be converted
						// from string (received from json) to the correct enum
						for (int i = 0; i < types.length; ++i) {
							if (Enum.class.isAssignableFrom(types[i])) {
								args[i] = Enum.valueOf(types[i], (String)args[i]);
							}
						}
						
						try {
							// invoke the method
							return method.invoke(object, args);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							// if it failed because of the parameters, try again to find another 
							// method with the same name and a different parameter set ...
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
//	public class ControlCommand extends BaseCommand {
//		
//		public String mCommand;
////		KeyValue[] mParameters;
//		public HashMap<String, String> mParameterList;
//		
//		public ControlCommand(String i_strRobot, String command, Map.Entry<String, String>... parameters) {
//			super(RoboCommandTypes.CONTROL_COMMAND, i_strRobot);
//
//			mCommand = command;
//			for (Map.Entry<String, String> param : parameters) {
//				mParameterList.put(param.getKey(), param.getValue());
//			}
//		}
//		
//		public ControlCommand(String i_strRobot, String command, KeyValue... parameters) {
//			super(RoboCommandTypes.CONTROL_COMMAND, i_strRobot);
//			
//			mCommand = command;
////			mParameters = parameters;
//			
//			mParameterList = new HashMap<String, String>(parameters.length);
//			for (KeyValue param : parameters) {
//				mParameterList.put(param.mKey, param.mValue);
//			}
//		}
//		
//		public ControlCommand(JSONObject i_oObj) throws JSONException {
//			super(i_oObj);
//			
//			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
//			
//			mCommand = data.getString(RoboCommandTypes.F_COMMAND);
//			
//			JSONObject param = (JSONObject)data.get(RoboCommandTypes.F_PARAMETER);
//			JSONArray param_keys = param.names();
//			
//			if (param_keys != null) {
//				mParameterList = new HashMap<String, String>(param_keys.length());
//				for (int i = 0; i < param_keys.length(); ++i) {
//					String key = param_keys.getString(i);
//					String value = param.getString(key);
//					mParameterList.put(key, value);
//				}
//			} else {
//				mParameterList = new HashMap<String, String>();
//			}
//		}
//		
//		public JSONObject toJSON() {
//			JSONObject obj = super.toJSON();
//			JSONObject data = new JSONObject();
//			try {
//				data.put(RoboCommandTypes.F_COMMAND, mCommand);
//				
//				JSONObject param = new JSONObject();
////				for (String key : mParameterList.keySet()) {
////					param.put(key, mParameterList.get(key));
////				}
//				for (Map.Entry<String, String> entry : mParameterList.entrySet()) {
//					param.put(entry.getKey(), entry.getValue());
//				}
//				data.put(RoboCommandTypes.F_PARAMETER, param);
//				
//				obj.put(RoboCommandTypes.S_DATA, data);
//				return obj;
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return null;
//		}
//		
//		public String getParameter(String key) {
//			return mParameterList.get(key);
//		}
//		
//	}

	public static ControlCommand createControlCommand(String i_strRobot, String command, Object... parameters) {
		return getInstance().new ControlCommand(i_strRobot, command, parameters);
	}

}
