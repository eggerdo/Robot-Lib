package robots.spytank.ctrl;


public class CommandEncoder {
	
	public static byte[] getMotorCommand(int id, int direction) {
		String cmd = String.format("%d%d", id, direction);
		return cmd.getBytes();
	}
	
	public static byte[] getKeepAlive() {
		String cmd = String.format("%s", SpyTankTypes.KEEP_ALIVE);
		return cmd.getBytes();
	}
	
}
