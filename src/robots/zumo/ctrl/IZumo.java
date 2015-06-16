package robots.zumo.ctrl;

import robots.ctrl.ICameraRobot;
import robots.ctrl.IRobotDevice;
import robots.gui.comm.IRobotConnection;

public interface IZumo extends ICameraRobot, IRobotDevice {

	IRobotConnection getConnection();

	void setConnection(IRobotConnection connection);
	
	public void initMazeSolver();

	public void startMazeSolving();

	public void stopMazeSolving();

	public void repeatMaze();

	public void calibrateCompass();

	public void resetHeading();

	public void turnDegrees(int angle);

	boolean isMazeSolving();
	
}
