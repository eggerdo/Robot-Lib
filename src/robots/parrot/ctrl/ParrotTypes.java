package robots.parrot.ctrl;

public class ParrotTypes {

	public static final String PREFS_ADDRESS 		= "parrot_address";
	public static final String PREFS_COMMANDPORT 	= "parrot_commandport";
	public static final String PREFS_MEDIAPORT 		= "parrot_mediaport";

	public static final String SSID_FILTER 				= "ardrone";

    public static final long CONNECTION_TIMEOUT 		= 10000;

    public static final String PARROT_IP				= "192.168.1.1";
//	public static final int PORT 						= 80;
    public static final int COMMAND_PORT				= 5554;
    public static final int MEDIA_PORT 					= 5555;
    
    public static final String VIDEO_CODEC 				= "h264";
    public final static int VIDEO_WIDTH 				= 640;
    public final static int VIDEO_HEIGHT 				= 360;
    
    public final static int SUCCESS						= 0;
    public final static int NOTHING_FOUND				= -1;
    public final static int READ_FRAME_FAILED			= -2;
    public final static int CODEC_DIMENSION_ERROR		= -14;

    public final static int BITMAP_LOCKPIXELS_FAILED 	= -3;    

    public enum ParrotMove {
    	MOVE_UP, MOVE_DOWN, MOVE_FWD, MOVE_BWD, MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT
    }

}
