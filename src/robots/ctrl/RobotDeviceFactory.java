package robots.ctrl;


import robots.RobotType;
import robots.ispytank.ctrl.SpyTank;
import robots.romo.ctrl.Romo;
import robots.rover.rover2.ctrl.Rover2;

public class RobotDeviceFactory {
	
	public static IRobotDevice createRobotDevice(RobotType robot) throws Exception
	{
		IRobotDevice oRobot;
		switch (robot) {
			case RBT_ROMO:
				oRobot = new Romo();
				break;
			case RBT_ROVER2:
				oRobot = new Rover2();
				break;
			case RBT_SPYTANK:
				oRobot = new SpyTank();
				break;
			default: 		
				throw new Exception();
		}

		return oRobot;
	}
}