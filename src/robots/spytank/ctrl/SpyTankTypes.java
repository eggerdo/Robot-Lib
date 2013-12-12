package robots.spytank.ctrl;

public class SpyTankTypes {
	

	public static final String PREFS_SPYTANK_ADDRESS 		= "spytank_address";
	public static final String PREFS_SPYTANK_COMMANDPORT 	= "spytank_commandport";
	public static final String PREFS_SPYTANK_MEDIAPORT 		= "spytank_mediaport";


	public static final String ADDRESS 		= "10.10.1.1";
	public static final int COMMAND_PORT	= 8150;
	public static final int MEDIA_PORT		= 8196;
	public static final int AUDIO_PORT		= 7070;

	public static final int MIN_SPEED		= 0;
	public static final int MAX_SPEED 		= 100;
	public static final int MIN_RADIUS 		= 0;
	public static final int MAX_RADIUS 		= 1;
	public static final double AXLE_WIDTH  	= 160.0; // mm
	
	public static final String LEFT_STOP 	= "10";
	public static final String LEFT_FWD 	= "11";
	public static final String LEFT_BWD 	= "12";
	
	public static final String RIGHT_STOP	= "20";
	public static final String RIGHT_FWD	= "21";
	public static final String RIGHT_BWD	= "22";

	public static final String FORWARD		= "1121";
	public static final String BACKWARD		= "1222";
	public static final String ROTATE_LEFT	= "1221";
	public static final String ROTATE_RIGHT	= "1122";
	public static final String STOP_ALL		= "1020";
//	public static final String STOP_ALL		= "s";

	public static final String KEEP_ALIVE	= "KK";
	
	public static final int LEFT 	= 1;
	public static final int RIGHT 	= 2;
	public static final int CAMERA	= 3;
	
	public static final int STOP 	= 0;
	
	public static final int FWD 	= 1;
	public static final int BWD 	= 2;
	
	public static final int UP		= 1;
	public static final int DOWN	= 2;

	public static final int FRAME_MAX_LENGTH = 20100;
	public static final int HEADER_MAX_LENGTH = 100;
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final byte[] END_MARKER = { 45, 45, 97, 114, 102, 108, 101, 98, 97, 114, 102, 108, 101 };
	public static final byte[] EOF_MARKER = { -1, -39 };
	public static final byte[] SOI_MARKER = { -1, -40 };
	
	public static byte[] getMotorCommand(int id, int direction) {
		String cmd = String.format("%d%d", id, direction);
		return cmd.getBytes();
	}
	
	public static byte[] getKeepAlive() {
		String cmd = String.format("%s", SpyTankTypes.KEEP_ALIVE);
		return cmd.getBytes();
	}
	

}
