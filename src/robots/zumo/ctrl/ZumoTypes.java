package robots.zumo.ctrl;

import java.util.UUID;

import org.dobots.comm.MessageEncoder;

public class ZumoTypes {

	/////////////////////////////////////////////////

	public static final UUID ZUMO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

//	public static final String MAC_FILTER = "00:06:66";
	
	public static final int MIN_VELOCITY = 0;
	public static final int MAX_VELOCITY = 400;
	public static final int MAX_RADIUS = 1000;
	public static final int MIN_RADIUS = 1;

	public static final double AXLE_WIDTH = 85.0; // in mm

//	public static final int MIN_SENSOR_INTERVAL = 100;
//	public static final int DEFAULT_SENSOR_INTERVAL = 500;

	/////////////////////////////////////////////////

	public static final int INIT_MAZE 			= MessageEncoder.USER + 0;	
	public static final int START_MAZE 			= MessageEncoder.USER + 1;
	public static final int STOP_MAZE 			= MessageEncoder.USER + 2;
	public static final int REPEAT_MAZE 		= MessageEncoder.USER + 3;
	public static final int CALIBRATE_COMPSS 	= MessageEncoder.USER + 4;
	public static final int RESET_HEADING 		= MessageEncoder.USER + 5;
	public static final int TURN_DEG 			= MessageEncoder.USER + 6;
	
}
