package robots.parrot.ctrl;

import robots.ctrl.ICameraRobot;
import robots.ctrl.IHolonomicRobotDevice;

import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavDataListener;

public interface IParrot extends ICameraRobot, IHolonomicRobotDevice {

	void setConnection(String address);
	
	public void setNavDataListener(NavDataListener i_oListener);
	public void removeNavDataListener(NavDataListener i_oListener);
	
	public void setVideoListener(DroneVideoListener i_oListener);
	public void removeVideoListener(DroneVideoListener i_oListener);
	
	public boolean isARDrone1();

	public void land();
	public void takeOff();
	
	public void stopAltitudeControl();
	public void setAltitude(double altitude);
	
	public void increaseAltitude();
	public void decreaseAltitude();
	public void increaseAltitude(double i_dblSpeed);
	public void decreaseAltitude(double i_dblSpeed);
	
	public void switchCamera();
	public VideoChannel getVidoeChannel();
}
