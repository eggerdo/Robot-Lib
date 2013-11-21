package robots.rover.rover2.ctrl;

import robots.rover.base.ctrl.IRoverBase;

public interface IRover2 extends IRoverBase {
	
	public void toggleLight();

	public void enableLight();

	public void disableLight();

	public void cameraUp();

	public void cameraStop();

	public void cameraDown();

	public double getBatteryPower();

}
