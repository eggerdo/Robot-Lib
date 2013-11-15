package robots.nxt.ctrl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import org.dobots.communication.msg.SensorMessageData;
import org.dobots.utilities.Utils;
import org.json.JSONException;

public class NXTTypes {
	public static final int MOTOR_A = 0;
    public static final int MOTOR_B = 1;
    public static final int MOTOR_C = 2;

    public static final int NO_DELAY = 0;
    
	public static String MAC_FILTER = "00:16:53";
	
	public static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // this is the only OUI registered by LEGO, see http://standards.ieee.org/regauth/oui/index.shtml
    public static final String OUI_LEGO = "00:16:53";
    
    public static final int MIN_VELOCITY = 0;
    public static final int MAX_VELOCITY = 100;
//    public static final int MAX_RADIUS = 2000;
    public static final int MAX_RADIUS = 1000;
	public static final int MIN_RADIUS = 1;
	public static final int STRAIGHT = 32768;
	public static final int CLOCKWISE = -1;
	public static final int COUNTER_CLOCKWISE = 1;

	public static final double AXLE_WIDTH = 160.0;
	
	public enum ENXTSensorType {
		sensType_None("None",						LCPMessage.NO_SENSOR,		LCPMessage.RAWMODE),
		sensType_Sound_DB("Sound_DB", 				LCPMessage.SOUND_DB,		LCPMessage.PCTFULLSCALEMODE),
		sensType_Sound_DBA("Sound_DBA", 			LCPMessage.SOUND_DBA,		LCPMessage.PCTFULLSCALEMODE),
		sensType_Push("Push Button", 				LCPMessage.SWITCH,			LCPMessage.BOOLEANMODE),
		sensType_Distance("Distance",				LCPMessage.LOWSPEED_9V,		LCPMessage.RAWMODE),
		sensType_Reflected_Light("Reflected Light", LCPMessage.LIGHT_ACTIVE,	LCPMessage.PCTFULLSCALEMODE),
		sensType_Ambient_Light("Ambient Light", 	LCPMessage.LIGHT_INACTIVE,	LCPMessage.PCTFULLSCALEMODE);
		private String strName;
		private byte byValue;
		private byte byDefaultMode;
		
		private ENXTSensorType(String name, byte value, byte mode) {
			this.strName = name;
			this.byValue = value;
			this.byDefaultMode = mode;
		}
		
		public String toString() {
			return strName;
		}

		public byte getValue() {
			return byValue;
		}
		
		public byte getDefaultMode() {
			return byDefaultMode;
		}
		
	}
	
	public enum ENXTSensorID {
		sens_unknown("Unknown", -1),
		sens_sensor1("Sensor 1", 0),
		sens_sensor2("Sensor 2", 1),
		sens_sensor3("Sensor 3", 2),
		sens_sensor4("Sensor 4", 3);
		private String strName;
		private int nValue;
		
		private ENXTSensorID(String name, int id) {
			this.strName = name;
			this.nValue = id;
		}
		
		public String toString() {
			return strName;
		}
		
		public int getValue() {
			return nValue;
		}
		
		public static ENXTSensorID fromValue(int id) {
			for (ENXTSensorID sensor : ENXTSensorID.values()) {
				if (sensor.getValue() == id) {
					return sensor;
				}
			}
			return null;
		}

	}
	
	public enum ENXTMotorID {
		motor_unknown("Unknown", -1),
		motor_1("Motor 1", 0),
		motor_2("Motor 2", 1),
		motor_3("Motor 3", 2);
		private String strName;
		private int nValue;
		
		private ENXTMotorID(String name, int id) {
			this.strName = name;
			this.nValue = id;
		}
		
		public String toString() {
			return strName;
		}
		
		public int getValue() {
			return nValue;
		}

		public static ENXTMotorID fromValue(int id) {
			for (ENXTMotorID motor : ENXTMotorID.values()) {
				if (motor.getValue() == id) {
					return motor;
				}
			}
			return null;
		}

	}
	
	public enum ENXTMotorSensorType {
		motor_degreee("Degree"),
		motor_rotation("Rotation");
		private String strName;
		
		private ENXTMotorSensorType(String name) {
			this.strName = name;
		}
		
		public String toString() {
			return strName;
		}
	}
	
	public class SensorData {
		public SensorMessageData sensorData;
		
		public int nTelegramType;
		public int nCommand;
		public int nStatus;
		public int nInputPort;
		public boolean bValid;
		public boolean bCalibrated;
		public int nSensorType;
		public int nSensorMode;
		public int nRawValue;
		public int nNormalizedValue;
		public int nScaledValue;
		public int nCalibratedValue;
		
		public SensorData(String robotID, byte[] buffer) {
			sensorData = new SensorMessageData(robotID, "sensor_data");
			
			ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
			DataInputStream data_in = new DataInputStream(byte_in);
			
			try {
				nTelegramType 		= data_in.readUnsignedByte();
				nCommand 			= data_in.readUnsignedByte();
				nStatus				= data_in.readUnsignedByte();
				nInputPort			= data_in.readUnsignedByte();
				bValid				= data_in.readBoolean();
				bCalibrated			= data_in.readBoolean();
				nSensorType			= data_in.readUnsignedByte();
				nSensorMode			= data_in.readUnsignedByte();
				nRawValue			= Utils.ConvertEndian(data_in.readShort());
				nNormalizedValue	= Utils.ConvertEndian(data_in.readShort());
				nScaledValue		= Utils.ConvertEndian(data_in.readShort());
				nCalibratedValue	= Utils.ConvertEndian(data_in.readShort());
				
				sensorData.addItem(nTelegramType);
				sensorData.addItem(nCommand);
				sensorData.addItem(nStatus);
				sensorData.addItem(nInputPort);
				sensorData.addItem(bValid);
				sensorData.addItem(bCalibrated);
				sensorData.addItem(nSensorType);
				sensorData.addItem(nSensorMode);
				sensorData.addItem(nRawValue);
				sensorData.addItem(nNormalizedValue);
				sensorData.addItem(nScaledValue);
				sensorData.addItem(nCalibratedValue);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sensorData = null;
			}
		}
		
		public SensorData(SensorMessageData sensorData) {
			
			try {
				nTelegramType 		= sensorData.getInt(0);
				nCommand 			= sensorData.getInt(1);
				nStatus				= sensorData.getInt(2);
				nInputPort			= sensorData.getInt(3);
				bValid				= sensorData.getBoolean(4);
				bCalibrated			= sensorData.getBoolean(5);
				nSensorType			= sensorData.getInt(6);
				nSensorMode			= sensorData.getInt(7);
				nRawValue			= sensorData.getInt(8);
				nNormalizedValue	= sensorData.getInt(9);
				nScaledValue		= sensorData.getInt(10);
				nCalibratedValue	= sensorData.getInt(11);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public static SensorData assembleSensorData(String id, byte[] sensorMessage) {
		NXTTypes types = new NXTTypes();
		return types.new SensorData(id, sensorMessage);
	}

	public static SensorData assembleSensorData(SensorMessageData sensorData) {
		NXTTypes types = new NXTTypes();
		return types.new SensorData(sensorData);
	}

	public class DistanceData {
		public SensorMessageData sensorData;
		
		public int nTelegramType;
		public int nCommand;
		public int nStatus;
		public int nInputPort;
		public int nDistance;
		
		public DistanceData(String robotID, int port, byte[] buffer) {
			sensorData = new SensorMessageData(robotID, "distance_data");
			
			ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
			DataInputStream data_in = new DataInputStream(byte_in);
			
			try {
				nTelegramType 		= data_in.readUnsignedByte();
				nCommand 			= data_in.readUnsignedByte();
				nStatus				= data_in.readUnsignedByte();
				nInputPort			= port;
				data_in.readUnsignedByte(); // discard the number of bytes read, for the distance data it is always 1
				nDistance 			= data_in.readUnsignedByte();
				
				sensorData.addItem(nTelegramType);
				sensorData.addItem(nCommand);
				sensorData.addItem(nStatus);
				sensorData.addItem(nInputPort);
				sensorData.addItem(nDistance);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sensorData = null;
			}
		}
		
		public DistanceData(SensorMessageData sensorData) {
			
			try {
				nTelegramType 		= sensorData.getInt(0);
				nCommand 			= sensorData.getInt(1);
				nStatus				= sensorData.getInt(2);
				nInputPort			= sensorData.getInt(3);
				nDistance			= sensorData.getInt(4);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public static DistanceData assembleDistanceData(String id, int port, byte[] sensorMessage) {
		NXTTypes types = new NXTTypes();
		return types.new DistanceData(id, port, sensorMessage);
	}

	public static DistanceData assembleDistanceData(SensorMessageData sensorData) {
		NXTTypes types = new NXTTypes();
		return types.new DistanceData(sensorData);
	}
	
	public class MotorData {
		SensorMessageData sensorData;
		
		public int nTelegramType;
		public int nCommand;
		public int nStatus;
		public int nOutputPort;
		public int nPowerSetpoint;
		public int nMode;
		public int nRegulationMode;
		public int nTurnRatio;
		public int nRunState;
		public long lTachoLimit;
		public int nTachoCount;
		public int nBlockTachoCount;
		public int nRotationCount;
		
		public MotorData(String robotID, byte[] buffer) {
			sensorData = new SensorMessageData(robotID, "motor_data");
			
			ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
			DataInputStream data_in = new DataInputStream(byte_in);
			
			try {
				nTelegramType		= data_in.readUnsignedByte();
				nCommand			= data_in.readUnsignedByte();
				nStatus				= data_in.readUnsignedByte();
				nOutputPort			= data_in.readUnsignedByte();
				nPowerSetpoint		= data_in.readByte();
				nMode				= data_in.readUnsignedByte();
				nRegulationMode		= data_in.readUnsignedByte();
				nTurnRatio			= data_in.readByte();
				nRunState			= data_in.readUnsignedByte();
				lTachoLimit			= Utils.convertEndian(data_in.readInt());
				nTachoCount			= Utils.convertEndian(data_in.readInt());
				nBlockTachoCount	= Utils.convertEndian(data_in.readInt());
				nRotationCount		= Utils.convertEndian(data_in.readInt());
				
				sensorData.addItem(nTelegramType);
				sensorData.addItem(nCommand);
				sensorData.addItem(nStatus);
				sensorData.addItem(nOutputPort);
				sensorData.addItem(nPowerSetpoint);
				sensorData.addItem(nMode);
				sensorData.addItem(nRegulationMode);
				sensorData.addItem(nTurnRatio);
				sensorData.addItem(nRunState);
				sensorData.addItem(lTachoLimit);
				sensorData.addItem(nTachoCount);
				sensorData.addItem(nBlockTachoCount);
				sensorData.addItem(nRotationCount);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sensorData = null;
			}
		}
		
		public MotorData(SensorMessageData sensorData) {
			
			try {
				nTelegramType		= sensorData.getInt(0);
				nCommand 			= sensorData.getInt(1);
				nStatus				= sensorData.getInt(2);
				nOutputPort			= sensorData.getInt(3);
				nPowerSetpoint		= sensorData.getInt(4);
				nMode				= sensorData.getInt(5);
				nRegulationMode		= sensorData.getInt(6);
				nTurnRatio			= sensorData.getInt(7);
				nRunState			= sensorData.getInt(8);
				lTachoLimit			= sensorData.getInt(9);
				nTachoCount			= sensorData.getInt(10);
				nBlockTachoCount	= sensorData.getInt(11);
				nRotationCount		= sensorData.getInt(12);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static MotorData assembleMotorData(String id, byte[] sensorMessage) {
		NXTTypes types = new NXTTypes();
		return types.new MotorData(id, sensorMessage);
	}

	public static MotorData assembleMotorData(SensorMessageData sensorData) {
		NXTTypes types = new NXTTypes();
		return types.new MotorData(sensorData);
	}

}
