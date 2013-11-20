package robots.ctrl;

import android.os.Handler;

public interface IRemoteRobot {

	public String getID();
	
	public void setConnection(String address, int port);
	public boolean isConnected();
	public void connect();
	public void disconnect();
	
	public void setHandler(Handler handler);
}
