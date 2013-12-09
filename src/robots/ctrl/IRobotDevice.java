package robots.ctrl;

import java.io.IOException;

import robots.RobotType;
import android.os.Handler;

public interface IRobotDevice {
	
	public RobotType getType();
	public String getAddress();
	public String getID();

	public void destroy();
	
	public void setHandler(Handler i_oHandler);
	
	// connection methods
	public void connect();
	public void disconnect();
	public boolean isConnected();
	
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
	
//	public void executeCircle(double i_dblTime, double i_dblSpeed);
	
	public void setBaseSpeed(double i_dblSpeed);
	public double getBaseSpeed();
	
	public void moveForward();
	public void moveBackward();
	public void rotateCounterClockwise();
	public void rotateClockwise();
	public void moveLeft();
	public void moveRight();
	
}
