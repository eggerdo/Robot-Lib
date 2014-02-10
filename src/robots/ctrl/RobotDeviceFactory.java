package robots.ctrl;


import robots.RobotType;
import robots.dotty.ctrl.Dotty;
import robots.nxt.ctrl.NXT;
import robots.parrot.ctrl.Parrot;
import robots.piratedotty.ctrl.PirateDotty;
import robots.replicator.ctrl.Replicator;
import robots.robo40.ctrl.Robo40;
import robots.roboscooper.ctrl.RoboScooper;
import robots.romo.ctrl.Romo;
import robots.roomba.ctrl.Roomba;
import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.rover2.ctrl.Rover2;
import robots.spykee.ctrl.Spykee;
import robots.spytank.ctrl.SpyTank;

public class RobotDeviceFactory {
	
	public static IRobotDevice createRobotDevice(RobotType robot) throws Exception
	{
		IRobotDevice oRobot;
		switch (robot) {
			case RBT_ROMO:
				oRobot = new Romo();
				break;
			case RBT_AC13ROVER:
				oRobot = new AC13Rover();
				break;
			case RBT_ROVER2:
				oRobot = new Rover2();
				break;
			case RBT_SPYTANK:
				oRobot = new SpyTank();
				break;
			case RBT_PARROT:
				oRobot = new Parrot();
				break;
			case RBT_REPLICATOR:
				oRobot = new Replicator();
				break;
			case RBT_PIRATEDOTTY:
				oRobot = new PirateDotty();
				break;
			case RBT_ROOMBA:
				oRobot = new Roomba();
				break;
			case RBT_ROBOSCOOPER:
				oRobot = new RoboScooper();
				break;
			case RBT_DOTTY:
				oRobot = new Dotty();
				break;
			case RBT_NXT:
				oRobot = new NXT();
				break;
			case RBT_ROBO40:
				oRobot = new Robo40();
				break;
			case RBT_SPYKEE:
				oRobot = new Spykee();
				break;
			default: 		
				throw new Exception();
		}

		return oRobot;
	}
}