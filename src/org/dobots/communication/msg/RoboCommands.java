package org.dobots.communication.msg;

import org.dobots.communication.msg.RoboCommandTypes.Request;
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
	
	public static BaseCommand decodeCommand(String i_strJson) {
		
		JSONObject oJSON;
		try {
			oJSON = new JSONObject(i_strJson);
			JSONObject header = oJSON.getJSONObject(RoboCommandTypes.S_HEADER);
			
			switch(header.getInt(RoboCommandTypes.F_HEADER_ID)) {
			case RoboCommandTypes.DRIVE_COMMAND:
				return getInstance().new DriveCommand(oJSON);
			case RoboCommandTypes.CAMERA_COMMAND:
				return getInstance().new CameraCommand(oJSON);
			default: 
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
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
			JSONObject header = i_oObj.getJSONObject(RoboCommandTypes.S_HEADER);
			nHeaderID 		= header.getInt(RoboCommandTypes.F_HEADER_ID);
			nTransactionID 	= header.getInt(RoboCommandTypes.F_TID);
			lTimeStamp 		= header.getLong(RoboCommandTypes.F_TIMESTAMP);
			strRobotID		= header.getString(RoboCommandTypes.F_ROBOT_ID);
			strVersion		= header.getString(RoboCommandTypes.F_VERSION);
		}
		
		public JSONObject toJSON() {
			JSONObject obj = new JSONObject();
			JSONObject header = new JSONObject();
			try {
				header.put(RoboCommandTypes.F_HEADER_ID, 	nHeaderID);
				header.put(RoboCommandTypes.F_TID, 			nTransactionID);
				header.put(RoboCommandTypes.F_TIMESTAMP, 	lTimeStamp);
				header.put(RoboCommandTypes.F_ROBOT_ID, 	strRobotID);
				header.put(RoboCommandTypes.F_VERSION,		strVersion);
				obj.put(RoboCommandTypes.S_HEADER, header);
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
	
	public class ControlCommand extends BaseCommand {
		
		Request eRequest;
		
		public ControlCommand(String i_strRobot, Request i_eRequest) {
			super(RoboCommandTypes.CONTROL_COMMAND, i_strRobot);
			
			eRequest = i_eRequest;
		}
		
		public ControlCommand(JSONObject i_oObj) throws JSONException {
			super(i_oObj);
			
			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
			eRequest = (Request)data.get(RoboCommandTypes.F_REQUEST);
		}
		
		public JSONObject toJSON() {
			JSONObject obj = super.toJSON();
			JSONObject data = new JSONObject();
			try {
				data.put(RoboCommandTypes.F_REQUEST, eRequest);
				obj.put(RoboCommandTypes.S_DATA, data);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}

}
