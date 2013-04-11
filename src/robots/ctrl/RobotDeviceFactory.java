package robots.ctrl;


import robots.RobotType;
import robots.ctrl.romo.Romo;

public class RobotDeviceFactory {
	
	public static IRobotDevice createRobotDevice(RobotType robot) throws Exception
	{
		IRobotDevice oRobot;
		switch (robot) {
			case RBT_ROMO:
				oRobot = new Romo();
				break;
			default: 		
				throw new Exception();
		}

		return oRobot;
	}
}