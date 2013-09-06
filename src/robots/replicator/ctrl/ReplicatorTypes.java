package robots.replicator.ctrl;

public class ReplicatorTypes {

	public static final int MIN_SPEED		= 0;
	public static final int MAX_SPEED 		= 100;
	public static final int MIN_RADIUS 		= 0;
	public static final int MAX_RADIUS 		= 1000;
	public static final double AXLE_WIDTH  	= 20.0; // mm

	public static final String ADDRESS 		= "192.168.52.207";
	public static final int COMMAND_PORT	= 10001;
	public static final int VIDEO_PORT		= 10002;


	public static final int IMAGE_WIDTH     = 640;
	public static final int IMAGE_HEIGHT    = 480;
	public static final int IMAGE_CHANNELS  = 3;
	public static final int IMAGE_SIZE      = 921600; // 640x480x3
	public static final int HEADER_SIZE     = 54;
	public static final int FRAME_SIZE      = 921600; // actually, 921654, 640x480x3 + 54 for header
	
	public static final String PREFS_REPLICATOR_ADDRESS 		= "replicator.address";
	public static final String PREFS_REPLICATOR_COMMANDPORT 	= "replicator.commandport";
	public static final String PREFS_REPLICATOR_VIDEOPORT 		= "replicator.videoport";

}
