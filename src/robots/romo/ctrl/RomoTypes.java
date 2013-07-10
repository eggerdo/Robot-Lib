package robots.romo.ctrl;

public class RomoTypes {
	
	public static final int MAX_VELOCITY	= 128;
	public static final int MIN_VELOCITY	= 32;
	public static final double AXLE_WIDTH	= 80.0;
    public static final int MAX_RADIUS 		= 1000;
	public static final int MIN_RADIUS 		= 1; 
	
	public static final int VELOCITY_OFFSET = 128;
	
	public static final int FORWARD_MAX		= 0xFF;
	public static final int FORWARD_MIN		= 0x81;
	public static final int BACKWARD_MIN	= 0x7F;
	public static final int BACKWARD_MAX	= 0x00;

	public static final int STOP			= 0x80;
	
	public static final int FORWARD_FAST	= 0xFF;
	public static final int FORWARD_SLOW	= 0xC0;
	public static final int BACKWARD_SLOW	= 0x40;
	public static final int BACKWARD_FAST	= 0x00;
	
	public static final String SSID_FILTER 	= "";
	
}
