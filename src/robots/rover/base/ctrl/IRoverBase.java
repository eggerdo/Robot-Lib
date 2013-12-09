package robots.rover.base.ctrl;

import robots.ctrl.ICameraRobot;
import robots.ctrl.IRobotDevice;
import robots.rover.base.ctrl.RoverBaseTypes.VideoResolution;

public interface IRoverBase extends IRobotDevice, ICameraRobot {

	public void setConnection(String address, int port);

	public void toggleInfrared();

	public void enableInfrared();

	public void disableInfrared();

	public void setResolution(final VideoResolution i_eResolution);
	public VideoResolution getResolution();

}
