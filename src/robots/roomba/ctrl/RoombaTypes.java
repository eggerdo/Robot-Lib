package robots.roomba.ctrl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.dobots.comm.msg.SensorMessageArray;
import org.dobots.utilities.Utils;
import org.json.JSONException;

public class RoombaTypes {

	public static final UUID ROOMBA_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public static final String MAC_FILTER = "00:06:66";

    public static int MAX_SPEED = 100;
	
	public static int MAX_VELOCITY = 500; // -500 - 500 mm/s
//	public static int MAX_RADIUS = 2000;  // -2000 - 2000 mm/s
	public static int MAX_RADIUS = 1000;  // -2000 - 2000 mm/s
	public static int STRAIGHT = 32768;
	public static int CLOCKWISE = -1;
	public static int COUNTER_CLOCKWISE = 1;
	
	public enum ERoombaModes {
		mod_Unknown,
		mod_Passive,
		mod_Safe,
		mod_Full,
		mod_PowerOff
	}
	
	public enum ERoombaBaudRates {
		baud_300(0),
		baud_600(1),
		baud_1200(2),
		baud_2400(3),
		baud_4800(4),
		baud_9600(5),
		baud_14400(6),
		baud_19200(7),
		baud_28800(8),
		baud_38400(9),
		baud_57600(10),
		baud_115200(11);
		private int id;
		
		private ERoombaBaudRates(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public enum ERoombaMotors {
		motor_SideBrush(0),
		motor_Vacuum(1),
		motor_MainBrush(2);
		private int id;
		
		private ERoombaMotors(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public enum ERoombaOnOffLEDs {
		led_DirtDetect(0),
		led_Max(1),
		led_Clean(2),
		led_Spot(3);
		private int id;
		
		private ERoombaOnOffLEDs(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public static final int STATUS_LED_LOW_BIT 	= 4;
	public static final int STATUS_LED_HIGH_BIT	= 5;
	
	public enum ERoombaStatusLEDColours {
		ledCol_Red,
		ledCol_Green,
		ledCol_Amber
	}
	
	public static final int POWER_LED_GREEN = 0;
	public static final int POWER_LED_RED	= 255;
	
	public enum ERoombaSensorPackages {
		sensPkg_None(-1, "Nothing"),
		sensPkg_1(1, "Environment"),
		sensPkg_2(2, "Actuators"),
		sensPkg_3(3, "Power"),
		sensPkg_All(100, "Everything");
		private int id;
		private String strName;
		
		private ERoombaSensorPackages(int id, String name) {
			this.id = id;
			this.strName = name;
		}
		
		public int getID() {
			return id;
		}
		
		public String toString() {
			return strName;
		}
		
		public static ERoombaSensorPackages fromString(String name) {
			for (ERoombaSensorPackages pkg : ERoombaSensorPackages.values()) {
				if (pkg.toString().equals(name)) {
					return pkg;
				}
			}
			return sensPkg_None;
		}
	}
	
	public static class BumpsWheeldrops {
		private static final int CASTER_WHEELDROP 	= 4;
		private static final int LEFT_WHEELDROP 	= 3;
		private static final int RIGHT_WHEELDROP 	= 2;
		private static final int LEFT_BUMP 			= 1;
		private static final int RIGHT_BUMP		 	= 0;
		
		public boolean bCaster_Wheeldrop;
		public boolean bLeft_Wheeldrop;
		public boolean bRight_Wheeldrop;
		public boolean bLeft_Bump;
		public boolean bRight_Bump;
	
		BumpsWheeldrops(int i_nVal) {
			bCaster_Wheeldrop 	= Utils.IsBitSet(i_nVal, CASTER_WHEELDROP);
			bLeft_Wheeldrop		= Utils.IsBitSet(i_nVal, LEFT_WHEELDROP);
			bRight_Wheeldrop	= Utils.IsBitSet(i_nVal, RIGHT_WHEELDROP);
			bLeft_Bump			= Utils.IsBitSet(i_nVal, LEFT_BUMP);
			bRight_Bump			= Utils.IsBitSet(i_nVal, RIGHT_BUMP);
		}

		public String toString() {
			return "Caster_Wheeldrop=" + bCaster_Wheeldrop + ", " +
				   "Left_Wheeldrop=" + bLeft_Wheeldrop + ", " +
				   "Right_Wheeldrop=" + bRight_Wheeldrop + ", " +
				   "Left_Bump=" + bLeft_Bump + ", " +
				   "Right_Bump=" + bRight_Bump;
		}

		public void addToSensorData(SensorMessageArray sensorData) {
			sensorData.addItem(bCaster_Wheeldrop);
			sensorData.addItem(bLeft_Wheeldrop);
			sensorData.addItem(bRight_Wheeldrop);
			sensorData.addItem(bLeft_Bump);
			sensorData.addItem(bRight_Bump);
		}
		
		public int fromSensorData(SensorMessageArray sensorData, int offset) throws JSONException {
			bCaster_Wheeldrop = sensorData.getBoolean(offset++);
			bLeft_Wheeldrop = sensorData.getBoolean(offset++);
			bRight_Wheeldrop = sensorData.getBoolean(offset++);
			bLeft_Bump = sensorData.getBoolean(offset++);
			bRight_Bump = sensorData.getBoolean(offset++);
			return offset;
		}
	}
	
	public static class MotorOvercurrents {
		private static final int DRIVE_LEFT		= 4;
		private static final int DRIVE_RIGHT	= 3;
		private static final int MAIN_BRUSH		= 2;
		private static final int VACUUM			= 1;
		private static final int SIDE_BRUSH		= 0;
		
		public boolean bDriveLeft;
		public boolean bDriveRight;
		public boolean bMainBrush;
		public boolean bVacuum;
		public boolean bSideBrush;
		
		MotorOvercurrents(int i_nVal) {
			bDriveLeft	= Utils.IsBitSet(i_nVal, DRIVE_LEFT);
			bDriveRight	= Utils.IsBitSet(i_nVal, DRIVE_RIGHT);
			bMainBrush	= Utils.IsBitSet(i_nVal, MAIN_BRUSH);
			bVacuum		= Utils.IsBitSet(i_nVal, VACUUM);
			bSideBrush	= Utils.IsBitSet(i_nVal, SIDE_BRUSH);
		}

		public String toString() {
			return "DriveLeft=" + bDriveLeft + ", " +
				   "DriveRight=" + bDriveRight + ", " +
				   "MainBrush=" + bMainBrush + ", " +
				   "Vacuum=" + bVacuum + ", " +
				   "bSideBrush=" + bSideBrush;
		}

		public void addToSensorData(SensorMessageArray sensorData) {
			sensorData.addItem(bDriveLeft);
			sensorData.addItem(bDriveRight);
			sensorData.addItem(bMainBrush);
			sensorData.addItem(bVacuum);
			sensorData.addItem(bSideBrush);
		}
		
		public int fromSensorData(SensorMessageArray sensorData, int offset) throws JSONException {
			bDriveLeft = sensorData.getBoolean(offset++);
			bDriveRight = sensorData.getBoolean(offset++);
			bMainBrush = sensorData.getBoolean(offset++);
			bVacuum = sensorData.getBoolean(offset++);
			bSideBrush = sensorData.getBoolean(offset++);
			return offset;
		}
	}
	
	public static class ButtonsPressed {
		private static final int POWER	= 3;
		private static final int SPOT	= 2;
		private static final int CLEAN	= 1;
		private static final int MAX	= 0;
		
		public boolean bPower;
		public boolean bSpot;
		public boolean bClean;
		public boolean bMax;
		
		ButtonsPressed(int i_nVal) {
			bPower	= Utils.IsBitSet(i_nVal, POWER);
			bSpot	= Utils.IsBitSet(i_nVal, SPOT);
			bClean	= Utils.IsBitSet(i_nVal, CLEAN);
			bMax	= Utils.IsBitSet(i_nVal, MAX);
		}
		
		public String toString() {
			return "Power=" + bPower + ", " +
				   "Spot=" + bSpot + ", " +
				   "Clean=" + bClean + ", " +
				   "Max=" + bMax;
		}

		public void addToSensorData(SensorMessageArray sensorData) {
			sensorData.addItem(bPower);
			sensorData.addItem(bSpot);
			sensorData.addItem(bClean);
			sensorData.addItem(bMax);
		}
		
		public int fromSensorData(SensorMessageArray sensorData, int offset) throws JSONException {
			bPower = sensorData.getBoolean(offset++);
			bSpot = sensorData.getBoolean(offset++);
			bClean = sensorData.getBoolean(offset++);
			bMax = sensorData.getBoolean(offset++);
			return offset;
		}
	}
	
	public static class LightBumper {
		private static final int LT_BUMPER_RIGHT 		= 5;
		private static final int LT_BUMPER_FRONT_RIGHT 	= 4;
		private static final int LT_BUMPER_CENTER_RIGHT	= 3;
		private static final int LT_BUMPER_CENTER_lEFT 	= 2;
		private static final int LT_BUMPER_FRONT_LEFT 	= 1;
		private static final int LT_BUMPER_LEFT 		= 0;
		
		public boolean bLtBumperRight;
		public boolean bLtBumperFrontRight;
		public boolean bLtBumperCenterRight;
		public boolean bLtBumperCenterLeft;
		public boolean bLtBumperFrontLeft;
		public boolean bLtBumperLeft;
		
		LightBumper(int i_nVal) {
			bLtBumperRight			= Utils.IsBitSet(i_nVal, LT_BUMPER_RIGHT);
			bLtBumperFrontRight		= Utils.IsBitSet(i_nVal, LT_BUMPER_FRONT_RIGHT);
			bLtBumperCenterRight	= Utils.IsBitSet(i_nVal, LT_BUMPER_CENTER_RIGHT);
			bLtBumperCenterLeft		= Utils.IsBitSet(i_nVal, LT_BUMPER_CENTER_lEFT);
			bLtBumperFrontLeft		= Utils.IsBitSet(i_nVal, LT_BUMPER_FRONT_LEFT);
			bLtBumperLeft			= Utils.IsBitSet(i_nVal, LT_BUMPER_LEFT);
		}

		public void addToSensorData(SensorMessageArray sensorData) {
			sensorData.addItem(bLtBumperRight);
			sensorData.addItem(bLtBumperFrontRight);
			sensorData.addItem(bLtBumperCenterRight);
			sensorData.addItem(bLtBumperCenterLeft);
			sensorData.addItem(bLtBumperFrontLeft);
			sensorData.addItem(bLtBumperLeft);
		}
		
		public int fromSensorData(SensorMessageArray sensorData, int offset) throws JSONException {
			bLtBumperRight = sensorData.getBoolean(offset++);
			bLtBumperFrontRight = sensorData.getBoolean(offset++);
			bLtBumperCenterRight = sensorData.getBoolean(offset++);
			bLtBumperCenterLeft = sensorData.getBoolean(offset++);
			bLtBumperFrontLeft = sensorData.getBoolean(offset++);
			bLtBumperLeft = sensorData.getBoolean(offset++);
			return offset;
		}
	}
	
	public enum EChargingState {
		chg_notCharging("Not Charging"),
		chg_chargingRecovery("Recovery Charging"),
		chg_charging("Charging"),
		chg_trickleCharging("Trickle Charging"),
		chg_Waiting("Waiting"),
		chg_ChargingError("Charging Error"),
		chg_Unknown("Unknown");
		private String strName;
		
		EChargingState(String i_strName) {
			this.strName = i_strName;
		}
		
		public static EChargingState OrdToEnum(int i_nVal) {
			try {
				return EChargingState.values()[i_nVal];
			} catch (ArrayIndexOutOfBoundsException e) {
				return chg_Unknown;
			}
		}
		
		public String toString() {
			return strName;
		}

		public static EChargingState fromString(String name) {
			for (EChargingState state : EChargingState.values()) {
				if (state.toString().equals(name)) {
					return state;
				}
			}
			return chg_Unknown;
		}
	}

	public enum OIMode {
		oi_off("Off"),
		oi_passive("Passive"),
		oi_safe("Safe"),
		oi_full("Full"),
		oi_unknown("Unknown");
		private String strName;
		
		OIMode(String i_strName) {
			this.strName = i_strName;
		}
		
		public static OIMode OrdToEnum(int i_nVal) {
			try {
				return OIMode.values()[i_nVal];
			} catch (ArrayIndexOutOfBoundsException e) {
				return oi_unknown;
			}
		}
		
		public String toString() {
			return strName;
		}

		public static OIMode fromString(String name) {
			for (OIMode mode : OIMode.values()) {
				if (mode.toString().equals(name)) {
					return mode;
				}
			}
			return oi_unknown;
		}
	}
	
	public enum ChargeMode {
		chgmode_HomeBase("Home Base", 2),
		chgmode_Internal("Internal", 1),
		chgmode_None("None", 0),
		chgmode_Unknown("Unknown", -1);
		private String strName;
		private int nValue;
		
		ChargeMode(String i_strName, int i_nValue) {
			this.strName = i_strName;
			this.nValue = i_nValue;
		}
		
		public static ChargeMode valToEnum(int i_nVal) {
			for (ChargeMode eMode : ChargeMode.values()) {
				if (eMode.toValue() == i_nVal) {
					return eMode;
				}
			}
			return chgmode_Unknown;
		}
		
		public String toString() {
			return strName;
		}
		
		public int toValue() {
			return nValue;
		}

		public static ChargeMode fromString(String name) {
			for (ChargeMode mode : ChargeMode.values()) {
				if (mode.toString().equals(name)) {
					return mode;
				}
			}
			return chgmode_Unknown;
		}
	}
	
	public enum IROpCode {
		irop_none("None", 0),
		irop_left("Left", 129),
		irop_forward("Forward", 130),
		irop_right("Right", 131),
		irop_spot("Spot", 132),
		irop_max("Max", 133),
		irop_small("Small", 134),
		irop_medium("Medium", 135),
		irop_clean("Clean", 136),
		irop_stop("Stop", 137),
		irop_power("Power", 138),
		irop_arcleft("Arc Left", 139),
		irop_arcright("Arc Right", 140),
		irop_stop2("Stop", 141),
		irop_download("Download", 142),
		irop_seekdock("Seek Dock", 143),
		irop_discovery_reserved("Reserved", 240),
		irop_discovery_redbuoy("Red Buoy", 248),
		irop_discovery_greenbuoy("Green Buoy", 244),
		irop_discovery_forcefield("Force Field", 242),
		irop_discovery_rbgb("Red Buoy and Green Buoy", 252),
		irop_discovery_rbff("Red Buoy and Force Field", 250),
		irop_discovery_gbff("Green Buoy and Force Field", 246),
		irop_discovery_rbgbff("Red Buoy, Green Buoy and Force Field", 254),
		irop_charge_reserved("Reserved", 160),
		irop_charge_redbuoy("Red Buoy", 168),
		irop_charge_greenbuoy("Green Buoy", 164),
		irop_charge_forcefield("Force Field", 161),
		irop_charge_rbgb("Red Buoy and Green Buoy", 172),
		irop_charge_rbff("Red Buoy and Force Field", 169),
		irop_charge_gbff("Green Buoy and Force Field", 165),
		irop_charge_rbgbff("Red Buoy, Green Buoy and Force Field", 173),
		irop_virtualwall("Virtual Wall", 162),
//		irop_lighthouse("...", ...)
		irop_unknown("Unknown", -1);
		private String strName;
		private int nValue;
		
		private IROpCode(String i_strName, int i_nVal) {
			strName = i_strName;
			nValue = i_nVal;
		}
		
		public String toString() {
			return strName;
		}
		
		public int toValue() {
			return nValue;
		}
		
		public static IROpCode valToEnum(int i_nVal) {
			for (IROpCode eCode : IROpCode.values()) {
				if (eCode.toValue() == i_nVal) {
					return eCode;
				}
			}
			return irop_unknown;
		}
		
		public static IROpCode fromString(String name) {
			for (IROpCode code : IROpCode.values()) {
				if (code.toString().equals(name)) {
					return code;
				}
			}
			return irop_unknown;
		}
	}
	
	public interface SensorPackage{
		public String toString();
		public SensorMessageArray getSensorData();
	};
	
	public static class SensorPackage1 implements SensorPackage {
		private SensorMessageArray oSensorData;
		
		public static final int IDX_BUMPWHEELDROPS		= 0;
		public static final int IDX_WALL				= 1;
		public static final int IDX_CLIFFLEFT			= 2;
		public static final int IDX_CLIFFFRONTLEFT		= 3;
		public static final int IDX_CLIFFFRONTRIGHT		= 4;
		public static final int IDX_CLIFFRIGHT			= 5;
		public static final int IDX_VIRTUALWALL			= 6;
		public static final int IDX_MOTOROVERCURRENTS	= 7;
		public static final int IDX_DIRTDETECTIONLEFT	= 8;
		public static final int IDX_DIRTDETECTIONRIGHT	= 9;
		
		public BumpsWheeldrops oBumpsWheeldrops;
		public boolean bWall;
		public boolean bCliffLeft;
		public boolean bCliffFrontLeft;
		public boolean bCliffFrontRight;
		public boolean bCliffRight;
		public boolean bVirtualWall;
		public MotorOvercurrents oMotorOvercurrents;
		public byte byDirtDetectionLeft;
		public byte byDirtDetectionRight;
		
		public SensorPackage1(String robotID, byte[] i_rgbyValues) {
			oSensorData = new SensorMessageArray(robotID, ERoombaSensorPackages.sensPkg_1.toString());
			
			for (int i = 0; i < i_rgbyValues.length; i++) {
				switch (i) {
					case IDX_BUMPWHEELDROPS:
						oBumpsWheeldrops = new BumpsWheeldrops(i_rgbyValues[i]);
						break;
					case IDX_WALL:
						bWall = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFLEFT:
						bCliffLeft = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFFRONTLEFT:
						bCliffFrontLeft = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFFRONTRIGHT:
						bCliffFrontRight = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFRIGHT:
						bCliffRight = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_VIRTUALWALL:
						bVirtualWall = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_MOTOROVERCURRENTS:
						oMotorOvercurrents = new MotorOvercurrents(i_rgbyValues[i]);
						break;
					case IDX_DIRTDETECTIONLEFT:
						byDirtDetectionLeft = i_rgbyValues[i];
						break;
					case IDX_DIRTDETECTIONRIGHT:
						byDirtDetectionRight = i_rgbyValues[i];
						break;
					default:
						throw new IndexOutOfBoundsException("Array has too many fields");
				}
			}
			
			oBumpsWheeldrops.addToSensorData(oSensorData);
			oSensorData.addItem(bWall);
			oSensorData.addItem(bCliffLeft);
			oSensorData.addItem(bCliffFrontLeft);
			oSensorData.addItem(bCliffFrontRight);
			oSensorData.addItem(bCliffRight);
			oSensorData.addItem(bVirtualWall);
			oMotorOvercurrents.addToSensorData(oSensorData);
			oSensorData.addItem(byDirtDetectionLeft);
			oSensorData.addItem(byDirtDetectionRight);
		}
		
		public SensorPackage1(SensorMessageArray sensorData) {
			try {
				int offset = 0;
				
				oBumpsWheeldrops = new BumpsWheeldrops(0);
				offset = oBumpsWheeldrops.fromSensorData(sensorData, offset);
				bWall = sensorData.getBoolean(offset++);
				bCliffLeft = sensorData.getBoolean(offset++);
				bCliffFrontLeft = sensorData.getBoolean(offset++);
				bCliffFrontRight = sensorData.getBoolean(offset++);
				bCliffRight = sensorData.getBoolean(offset++);
				bVirtualWall = sensorData.getBoolean(offset++);
				oMotorOvercurrents = new MotorOvercurrents(0);
				offset = oMotorOvercurrents.fromSensorData(sensorData, offset);
				byDirtDetectionLeft = (byte) sensorData.getInt(offset++);
				byDirtDetectionRight = (byte) sensorData.getInt(offset++);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String toString() {
			return oBumpsWheeldrops.toString() + ", " +
			       "Wall=" + bWall + ", " +
			       "CliffLeft=" + bCliffLeft + ", " +
			       "CliffFrontLeft=" + bCliffFrontLeft + ", " +
			       "CliffFrontRight=" + bCliffFrontRight + ", " +
			       "CliffRight=" + bCliffRight + ", " +
			       "VirtualWall=" + bVirtualWall + ", " +
			       oMotorOvercurrents.toString() + ", " +
			       "DirtDetectionLeft=" + byDirtDetectionLeft + ", " +
			       "DirtDetectionRight=" + byDirtDetectionRight;
		}

		@Override
		public SensorMessageArray getSensorData() {
			return oSensorData;
		}
	}
	
	public static class SensorPackage2 implements SensorPackage {
		private SensorMessageArray oSensorData;
		
		private static final int IDX_REMOTEOPCODE		= 0;
		private static final int IDX_BUTTONSPRESSED		= 1;
		private static final int IDX_DISTANCE			= 2; // 2 bytes
		private static final int IDX_ANGLE				= 4; // 2 bytes
		
		public byte byRemoteOpCode;
		public ButtonsPressed oButtonsPressed;
		public short sDistance;
		public short sAngle;
		
		public SensorPackage2(String robotID, byte[] i_rgbyValues) {
			oSensorData = new SensorMessageArray(robotID, ERoombaSensorPackages.sensPkg_2.toString());
			
			for (int i = 0; i < i_rgbyValues.length; i++) {
				switch (i) {
					case IDX_REMOTEOPCODE:
						byRemoteOpCode = i_rgbyValues[i];
						break;
					case IDX_BUTTONSPRESSED:
						oButtonsPressed = new ButtonsPressed(i_rgbyValues[i]);
						break;
					case IDX_DISTANCE:
						sDistance = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_DISTANCE+1:
						break;
					case IDX_ANGLE:
						sAngle = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
					case IDX_ANGLE+1:
						break;
					default:
						throw new IndexOutOfBoundsException("Array has too many fields");
				}
			}
			
			oSensorData.addItem(byRemoteOpCode);
			oButtonsPressed.addToSensorData(oSensorData);
			oSensorData.addItem(sDistance);
			oSensorData.addItem(sAngle);
		}
		
		public SensorPackage2(SensorMessageArray sensorData) {
			try {
				int offset = 0;
				
				byRemoteOpCode = (byte) sensorData.getInt(offset++);
				oButtonsPressed = new ButtonsPressed(0);
				offset = oButtonsPressed.fromSensorData(sensorData, offset);
				sDistance = (short) sensorData.getInt(offset++);
				sAngle = (short) sensorData.getInt(offset++);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String toString() {
			return "RemoteOpCode=" + byRemoteOpCode + ", " +
				   oButtonsPressed.toString() + ", " +
				   "Distance=" + sDistance + ", " +
				   "Angle=" + sAngle;
		}

		@Override
		public SensorMessageArray getSensorData() {
			return oSensorData;
		}
	}
	
	public static class SensorPackage3 implements SensorPackage {
		private SensorMessageArray oSensorData;
		
		private static final int IDX_CHARGINGSTATE		= 0;
		private static final int IDX_VOLTAGE			= 1; // 2 bytes
		private static final int IDX_CURRENT			= 3; // 2 bytes
		private static final int IDX_TEMPERATURE		= 5;
		private static final int IDX_CHARGE				= 6; // 2 bytes
		private static final int IDX_CAPACITY			= 8; // 2 bytes
		
		public EChargingState eChargingState;
		public short sVoltage;
		public short sCurrent;
		public byte byTemperature;
		public short sCharge;
		public short sCapacity;
		
		public SensorPackage3(String robotID, byte[] i_rgbyValues) {
			oSensorData = new SensorMessageArray(robotID, ERoombaSensorPackages.sensPkg_3.toString());
			
			for (int i = 0; i < i_rgbyValues.length; i++) {
				switch (i) {
					case IDX_CHARGINGSTATE:
						eChargingState = EChargingState.OrdToEnum(i_rgbyValues[i]);
						break;
					case IDX_VOLTAGE:
						sVoltage = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_VOLTAGE+1:
						break;
					case IDX_CURRENT:
						sCurrent = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_CURRENT+1:
						break;
					case IDX_TEMPERATURE:
						byTemperature = i_rgbyValues[i];
						break;
					case IDX_CHARGE:
						sCharge = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_CHARGE+1:
						break;
					case IDX_CAPACITY:
						sCapacity = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_CAPACITY+1:
						break;
					default:
						throw new IndexOutOfBoundsException("Array has too many fields");
				}
			}
			
			oSensorData.addItem(eChargingState);
			oSensorData.addItem(sVoltage);
			oSensorData.addItem(sCurrent);
			oSensorData.addItem(byTemperature);
			oSensorData.addItem(sCharge);
			oSensorData.addItem(sCapacity);
		}

		public SensorPackage3(SensorMessageArray sensorData) {
			try {
				int offset = 0;
				
				eChargingState = EChargingState.fromString(sensorData.getString(offset++));
				sVoltage = (short) sensorData.getInt(offset++);
				sCurrent = (short) sensorData.getInt(offset++);
				byTemperature = (byte) sensorData.getInt(offset++);
				sCharge = (short) sensorData.getInt(offset++);
				sCapacity = (short) sensorData.getInt(offset++);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String toString() {
			return "ChargingState=" + eChargingState + ", " +
				   "Voltage=" + sVoltage + ", " +
				   "Current=" + sCurrent + ", " +
				   "Temperature=" + byTemperature + ", " +
				   "Charge=" + sCharge + ", " +
				   "Capacity=" + sCapacity;
		}

		@Override
		public SensorMessageArray getSensorData() {
			return oSensorData;
		}
	}

	public enum SensorType {
		WHEELDROP_CASTER("Caster Wheel Drop"),
		WHEELDROP_LEFT("Left Wheel Drop"),
		WHEELDROP_RIGHT("Right Wheel Drop"),
		BUMP_LEFT("Left Bumper"),
		BUMP_RIGHT("Right Bumper"),
		WALL("Wall"),
		CLIFF_LEFT("Cliff Left"),
		CLIFF_FRONT_LEFT("Cliff Front Left"),
		CLIFF_FRONT_RIGHT("Cliff Front Right"),
		CLIFF_RIGHT("Cliff Right"),
		VIRTUAL_WALL("Virtual Wall"),
		OVERCURRENT_LEFT("Motor OC Left Whel"),
		OVERCURRENT_RIGHT("Motor OC Right Wheel"),
		OVERCURRENT_MAIN_BRUSH("Motor OC Main Brush"),
		OVERCURRENT_VACUUM("Motor OC Vacuum"),
		OVERCURRENT_SIDEBRUSH("Motor OC Side Brush"),
		DIRT_DETECTOR_LEFT("Dirt Detector Left"),
		DIRT_DETECTOR_RIGHT("Dirt Detector Right"),
		IR_OPCODE_OMNI("IR Op-Code Omni"),
		PRESSED_POWER("Power Button pressed"),
		PRESSED_SPOT("Spot Button pressed"),
		PRESSED_CLEAN("Clean Button pressed"),
		PRESSED_MAX("Max button pressed"),
		DISTANCE("Distance [mm]"),
		ANGLE("Angle [mm]"),
		CHARGING_STATE("Charging State"),
		VOLTAGE("Voltage [mV]"),
		CURRENT("Current [mA]"),
		BATTERY_TEMPRERATURE("Battery Temperature [C]"),
		CHARGE("Charge [mAh]"),
		CAPACITY("Capacity [mAh]"),
		WALL_SIGNAL("Wall Signal"),
		CLIFF_LEFT_SIGNAL("Cliff Left Signal"),
		CLIFF_FRONT_LEFT_SIGNAL("Cliff Front Left Signal"),
		CLIFF_FRONT_RIGHT_SIGNAL("Cliff Front Right Signal"),
		CLIFF_RIGHT_SIGNAL("Cliff Right Signal"),
		USER_DIGITAL_INPUTS("User Digital Inputs"),
		USER_ANALOG_INPUT("User Analog Input"),
		CHARGING_SOURCES_AVAILABLE("Charging Sources Available"),
		OI_MODE("Open Interface Mode"),
		SONG_NUMBER("Song Number"),
		SONG_PLAYING("Song Playing"),
		NUMBER_OF_STREAM_PACKETS("Number of Stream Packets"),
		REQUESTED_VELOCITY("Requested Velocity [mm/s]"),
		REQUESTED_RADIUS("Requested Velocity [mm/s]"),
		REQUESTED_RIGHT_VELOCITY("Requested Velocity Right [mm/s]"),
		REQUESTED_LEFT_VELOCITY("Requested Velocity Left [mm/s]"),
		ENCODER_COUNTS_LEFT("Encoder Counts Left"),
		ENCODER_COUNTS_RIGHT("Encoder Counts Right"),
		LIGHT_BUMPER_LEFT("Light Bumper Left"),
		LIGHT_BUMPER_FRONT_LEFT("Light Bumper Front Left"),
		LIGHT_BUMPER_CENTER_LEFT("Light Bumper Center Left"),
		LIGHT_BUMPER_CENTER_RIGHT("Light Bumper Center Right"),
		LIGHT_BUMPER_FRONT_RIGHT("Light Bumper Front Right"),
		LIGHT_BUMPER_RIGHT("Light Bumper Right"),
		LIGHT_BUMP_LEFT_SIGNAL("Light Bump Left Signal"),
		LIGHT_BUMP_FRONT_LEFT_SIGNAL("Light Bump Front Left Signal"),
		LIGHT_BUMP_CENTER_LEFT_SIGNAL("Light Bump Center Left Signal"),
		LIGHT_BUMP_CENTER_RIGHT_SIGNAL("Light Bump Center Right Signal"),
		LIGHT_BUMP_FRONT_RIGHT_SIGNAL("Light Bump Front Right Signal"),
		LIGHT_BUMP_RIGHT_SIGNAL("Light Bump Right Signal"),
		IR_OPCODE_LEFT("IR Op-Code Left"),
		IR_OPCODE_RIGHT("IR Op-Code Right"),
		LEFT_MOTOR_CURRENT("Left Motor Current [mA]"),
		RIGHT_MOTOR_CURRENT("Right Motor Current [mA]"),
		MAIN_BRUSH_CURRENT("Main Brush Current [mA]"),
		SIDE_BRUSH_CURRENT("Side Brush Current [mA]"),
		STASIS("Stasis"),
		ALL("Show All");
		private String strName;
		
		SensorType(String i_strName) {
			this.strName = i_strName;
		}
		
		public String toString() {
			return strName;
		}
	}
	
	public static class SensorPackageAll implements SensorPackage {
		private SensorMessageArray oSensorData;
		
		public BumpsWheeldrops bumps_wheeldrops;
		public boolean wall;
		public boolean cliff_left;
		public boolean cliff_front_left;
		public boolean cliff_front_right;
		public boolean cliff_right;
		public boolean virtual_wall;
		public MotorOvercurrents motor_overcurrents;
		public short dirt_detector_left;
		public short dirt_detector_right;
		public IROpCode remote_opcode;
		public ButtonsPressed buttons;
		public short distance;
		public short angle;
		public EChargingState charging_state;
		public int voltage;
		public short current;
		public byte temprerature;
		public int charge;
		public int capacity;
		public int wall_signal;
		public int cliff_left_signal;
		public int cliff_front_left_signal;
		public int cliff_front_right_signal;
		public int cliff_right_signal;
		public short user_digital_inputs;
		public int user_analog_input;
		public ChargeMode charging_sources_available;
		public OIMode oi_mode;
		public short song_number;
		public boolean song_playing;
		public short number_of_stream_packets;
		public short requested_velocity;
		public short requested_radius;
		public short requested_right_velocity;
		public short requested_left_velocity;
		public int encoder_counts_left;
		public int encoder_counts_right;
		public LightBumper light_bumper;
		public int light_bump_left_signal;
		public int light_bump_front_left_signal;
		public int light_bump_center_left_signal;
		public int light_bump_center_right_signal;
		public int light_bump_front_right_signal;
		public int light_bump_right_signal;
		public IROpCode ir_opcode_left;
		public IROpCode ir_opcode_right;
		public short left_motor_current;
		public short right_motor_current;
		public short main_brush_current;
		public short side_brush_current;
		public byte stasis;
		
		
		public SensorPackageAll(String robotID, byte[] i_rgbyValues) {
			oSensorData = new SensorMessageArray(robotID, ERoombaSensorPackages.sensPkg_All.toString());
			
			ByteBuffer buffer = ByteBuffer.wrap(i_rgbyValues);
			buffer.order(ByteOrder.BIG_ENDIAN);
			
			bumps_wheeldrops = new BumpsWheeldrops(Utils.getUnsignedByte(buffer));
			wall = Utils.getBoolean(buffer);
			cliff_left = Utils.getBoolean(buffer);
			cliff_front_left = Utils.getBoolean(buffer);
			cliff_front_right = Utils.getBoolean(buffer);
			cliff_right = Utils.getBoolean(buffer);
			virtual_wall = Utils.getBoolean(buffer);
			motor_overcurrents = new MotorOvercurrents(Utils.getUnsignedByte(buffer));
			dirt_detector_left = Utils.getUnsignedByte(buffer);
			dirt_detector_right = Utils.getUnsignedByte(buffer);
			remote_opcode = IROpCode.valToEnum(Utils.getUnsignedByte(buffer));
			buttons = new ButtonsPressed(Utils.getUnsignedByte(buffer));
			distance = buffer.getShort();
			angle = buffer.getShort();
			charging_state = EChargingState.OrdToEnum(Utils.getUnsignedByte(buffer));
			voltage = Utils.getUnsignedShort(buffer);
			current = buffer.getShort();
			temprerature = buffer.get();
			charge = Utils.getUnsignedShort(buffer);
			capacity = Utils.getUnsignedShort(buffer);
			wall_signal = Utils.getUnsignedShort(buffer);
			cliff_left_signal = Utils.getUnsignedShort(buffer);
			cliff_front_left_signal = Utils.getUnsignedShort(buffer);
			cliff_front_right_signal = Utils.getUnsignedShort(buffer);
			cliff_right_signal = Utils.getUnsignedShort(buffer);
			user_digital_inputs = Utils.getUnsignedByte(buffer);
			user_analog_input = Utils.getUnsignedShort(buffer);
			charging_sources_available = ChargeMode.valToEnum(Utils.getUnsignedByte(buffer));
			oi_mode = OIMode.OrdToEnum(Utils.getUnsignedByte(buffer));
			song_number = Utils.getUnsignedByte(buffer);
			song_playing = Utils.getBoolean(buffer);
			number_of_stream_packets = Utils.getUnsignedByte(buffer);
			requested_velocity = buffer.getShort();
			requested_radius = buffer.getShort();
			requested_right_velocity = buffer.getShort();
			requested_left_velocity = buffer.getShort();
			encoder_counts_left = Utils.getUnsignedShort(buffer);
			encoder_counts_right = Utils.getUnsignedShort(buffer);
			light_bumper = new LightBumper(Utils.getUnsignedByte(buffer));
			light_bump_left_signal = Utils.getUnsignedShort(buffer);
			light_bump_front_left_signal = Utils.getUnsignedShort(buffer);
			light_bump_center_left_signal = Utils.getUnsignedShort(buffer);
			light_bump_center_right_signal = Utils.getUnsignedShort(buffer);
			light_bump_front_right_signal = Utils.getUnsignedShort(buffer);
			light_bump_right_signal = Utils.getUnsignedShort(buffer);
			ir_opcode_left = IROpCode.valToEnum(Utils.getUnsignedByte(buffer));
			ir_opcode_right = IROpCode.valToEnum(Utils.getUnsignedByte(buffer));
			left_motor_current = buffer.getShort();
			right_motor_current = buffer.getShort();
			main_brush_current = buffer.getShort();
			side_brush_current = buffer.getShort();
			stasis = buffer.get();
			

			bumps_wheeldrops.addToSensorData(oSensorData);
			oSensorData.addItem(wall);
			oSensorData.addItem(cliff_left);
			oSensorData.addItem(cliff_front_left);
			oSensorData.addItem(cliff_front_right);
			oSensorData.addItem(cliff_right);
			oSensorData.addItem(virtual_wall);
			motor_overcurrents.addToSensorData(oSensorData);
			oSensorData.addItem(dirt_detector_left);
			oSensorData.addItem(dirt_detector_right);
			oSensorData.addItem(remote_opcode);
			buttons.addToSensorData(oSensorData);
			oSensorData.addItem(distance);
			oSensorData.addItem(angle);
			oSensorData.addItem(charging_state);
			oSensorData.addItem(voltage);
			oSensorData.addItem(current);
			oSensorData.addItem(temprerature);
			oSensorData.addItem(charge);
			oSensorData.addItem(capacity);
			oSensorData.addItem(wall_signal);
			oSensorData.addItem(cliff_left_signal);
			oSensorData.addItem(cliff_front_left_signal);
			oSensorData.addItem(cliff_front_right_signal);
			oSensorData.addItem(cliff_right_signal);
			oSensorData.addItem(user_digital_inputs);
			oSensorData.addItem(user_analog_input);
			oSensorData.addItem(charging_sources_available);
			oSensorData.addItem(oi_mode);
			oSensorData.addItem(song_number);
			oSensorData.addItem(song_playing);
			oSensorData.addItem(number_of_stream_packets);
			oSensorData.addItem(requested_velocity);
			oSensorData.addItem(requested_radius);
			oSensorData.addItem(requested_right_velocity);
			oSensorData.addItem(requested_left_velocity);
			oSensorData.addItem(encoder_counts_left);
			oSensorData.addItem(encoder_counts_right);
			light_bumper.addToSensorData(oSensorData);
			oSensorData.addItem(light_bump_left_signal);
			oSensorData.addItem(light_bump_front_left_signal);
			oSensorData.addItem(light_bump_center_left_signal);
			oSensorData.addItem(light_bump_center_right_signal);
			oSensorData.addItem(light_bump_front_right_signal);
			oSensorData.addItem(light_bump_right_signal);
			oSensorData.addItem(ir_opcode_left);
			oSensorData.addItem(ir_opcode_right);
			oSensorData.addItem(left_motor_current);
			oSensorData.addItem(right_motor_current);
			oSensorData.addItem(main_brush_current);
			oSensorData.addItem(side_brush_current);
			oSensorData.addItem(stasis);
		}
		
		public SensorPackageAll(SensorMessageArray sensorData) {
			try {
				int offset = 0;
				bumps_wheeldrops = new BumpsWheeldrops(0);
				offset = bumps_wheeldrops.fromSensorData(sensorData, offset);
				wall = sensorData.getBoolean(offset++);
				cliff_left = sensorData.getBoolean(offset++);
				cliff_front_left = sensorData.getBoolean(offset++);
				cliff_front_right = sensorData.getBoolean(offset++);
				cliff_right = sensorData.getBoolean(offset++);
				virtual_wall = sensorData.getBoolean(offset++);
				motor_overcurrents = new MotorOvercurrents(0);
				offset = motor_overcurrents.fromSensorData(sensorData, offset);
				dirt_detector_left = (short) sensorData.getInt(offset++);
				dirt_detector_right = (short) sensorData.getInt(offset++);
				remote_opcode = IROpCode.fromString(sensorData.getString(offset++));
				buttons = new ButtonsPressed(0);
				offset = buttons.fromSensorData(sensorData, offset);
				distance = (short) sensorData.getInt(offset++);
				angle = (short) sensorData.getInt(offset++);
				charging_state = EChargingState.fromString(sensorData.getString(offset++));
				voltage = sensorData.getInt(offset++);
				current = (short) sensorData.getInt(offset++);
				temprerature = (byte) sensorData.getInt(offset++);
				charge = sensorData.getInt(offset++);
				capacity = sensorData.getInt(offset++);
				wall_signal = sensorData.getInt(offset++);
				cliff_left_signal = sensorData.getInt(offset++);
				cliff_front_left_signal = sensorData.getInt(offset++);
				cliff_front_right_signal = sensorData.getInt(offset++);
				cliff_right_signal = sensorData.getInt(offset++);
				user_digital_inputs = (short) sensorData.getInt(offset++);
				user_analog_input = sensorData.getInt(offset++);
				charging_sources_available = ChargeMode.fromString(sensorData.getString(offset++));
				oi_mode = OIMode.fromString(sensorData.getString(offset++));
				song_number = (short) sensorData.getInt(offset++);
				song_playing = sensorData.getBoolean(offset++);
				number_of_stream_packets = (short) sensorData.getInt(offset++);
				requested_velocity = (short) sensorData.getInt(offset++);
				requested_radius = (short) sensorData.getInt(offset++);
				requested_right_velocity = (short) sensorData.getInt(offset++);
				requested_left_velocity = (short) sensorData.getInt(offset++);
				encoder_counts_left = sensorData.getInt(offset++);
				encoder_counts_right = sensorData.getInt(offset++);
				light_bumper = new LightBumper(0);
				offset = light_bumper.fromSensorData(sensorData, offset);
				light_bump_left_signal = sensorData.getInt(offset++);
				light_bump_front_left_signal = sensorData.getInt(offset++);
				light_bump_center_left_signal = sensorData.getInt(offset++);
				light_bump_center_right_signal = sensorData.getInt(offset++);
				light_bump_front_right_signal = sensorData.getInt(offset++);
				light_bump_right_signal = sensorData.getInt(offset++);
				ir_opcode_left = IROpCode.fromString(sensorData.getString(offset++));
				ir_opcode_right = IROpCode.fromString(sensorData.getString(offset++));
				left_motor_current = (short) sensorData.getInt(offset++);
				right_motor_current = (short) sensorData.getInt(offset++);
				main_brush_current = (short) sensorData.getInt(offset++);
				side_brush_current = (short) sensorData.getInt(offset++);
				stasis = (byte) sensorData.getInt(offset++);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public String toString() {
			return "";
		}

		@Override
		public SensorMessageArray getSensorData() {
			return oSensorData;
		}
	}
	
	public static SensorPackage assembleSensorPackage(String robotID, ERoombaSensorPackages i_ePackage, byte[] i_bySensorData) {
		switch (i_ePackage) {
			case sensPkg_All:
				return new SensorPackageAll(robotID, i_bySensorData);
			case sensPkg_1:
				return new SensorPackage1(robotID, i_bySensorData);
			case sensPkg_2:
				return new SensorPackage2(robotID, i_bySensorData);
			case sensPkg_3:
				return new SensorPackage3(robotID, i_bySensorData);
			default:
				return null;
		}
	}

	public static SensorPackage assembleSensorPackage(SensorMessageArray sensorData) {
		ERoombaSensorPackages ePackage = ERoombaSensorPackages.fromString(sensorData.getSensorID()); 
		switch (ePackage) {
			case sensPkg_All:
				return new SensorPackageAll(sensorData);
			case sensPkg_1:
				return new SensorPackage1(sensorData);
			case sensPkg_2:
				return new SensorPackage2(sensorData);
			case sensPkg_3:
				return new SensorPackage3(sensorData);
			default:
				return null;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	/// Private Functions
	/////////////////////////////////////////////////////////////////////////

}
