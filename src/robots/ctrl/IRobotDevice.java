package robots.ctrl;

import java.io.IOException;

import org.dobots.communication.control.RemoteControlReceiver;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.communication.video.ZmqVideoSender;

import robots.RobotType;

public interface IRobotDevice {
	
	public RobotType getType();
	public String getAddress();
	public String getID();

	public void destroy();
	
	// connection methods
	public void connect() throws IOException;
	public void disconnect();
	public boolean isConnected();
	
	public void setRemoteReceiver(RemoteControlReceiver i_oReceiver);
	public void setVideoListener(IRawVideoListener i_oVideoSender);
	
	// drive methods
	
	// enable robot to receive remote control commands (if necessary)
	public void enableControl(boolean i_bEnable);
	public boolean toggleInvertDrive();
	
	public void moveForward(double i_dblSpeed);
	public void moveForward(double i_dblSpeed, int i_nRadius);
	
	public void moveBackward(double i_dblSpeed);
	public void moveBackward(double i_dblSpeed, int i_nRadius);

	public void moveBackward(double i_dblSpeed, double i_dblAngle);
	public void moveForward(double i_dblSpeed, double i_dblAngle);
	
	public void rotateClockwise(double i_dblSpeed);
	public void rotateCounterClockwise(double i_dblSpeed);
	
	public void moveStop();
	
	public void executeCircle(double i_dblTime, double i_dblSpeed);
	
	public void setBaseSpeed(double i_dblSpeed);
	public double getBaseSped();
	
	public void moveForward();
	public void moveBackward();
	public void rotateCounterClockwise();
	public void rotateClockwise();
	public void moveLeft();
	public void moveRight();
	
}
