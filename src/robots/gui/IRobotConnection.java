package robots.gui;

import java.util.concurrent.TimeoutException;

public interface IRobotConnection {
	
	public void send(byte[] buffer);
	
	public byte[] read(int i_nBytes) throws TimeoutException;
	
}
