package robots.piratedotty.ctrl;

import java.util.UUID;

public class PirateDottyTypes {

	/////////////////////////////////////////////////

	public static final UUID PIRATEDOTTY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public static final String MAC_FILTER = "00:06:66";
	
	public static final int MIN_VELOCITY = 0;
	public static final int MAX_VELOCITY = 150;
	public static final int MAX_RADIUS = 1000;
	public static final int MIN_RADIUS = 1;

	public static final double AXLE_WIDTH = 100.0; // in mm

	public static final int MIN_SENSOR_INTERVAL = 100;
	public static final int DEFAULT_SENSOR_INTERVAL = 500;

	/////////////////////////////////////////////////
	
}
