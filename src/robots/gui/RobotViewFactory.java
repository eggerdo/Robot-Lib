package robots.gui;

import robots.RobotType;
import robots.dotty.ctrl.DottyTypes;
import robots.dotty.gui.DottyRobot;
import robots.nxt.ctrl.NXTTypes;
import robots.nxt.gui.NXTRobot;
import robots.parrot.gui.ParrotRobot;
import robots.piratedotty.ctrl.PirateDottyTypes;
import robots.piratedotty.gui.PirateDottyRobot;
import robots.replicator.gui.ReplicatorUI;
import robots.robo40.gui.Robo40Robot;
import robots.roboscooper.ctrl.RoboScooperTypes;
import robots.roboscooper.gui.RoboScooperRobot;
import robots.romo.gui.RomoRobot;
import robots.roomba.ctrl.RoombaTypes;
import robots.roomba.gui.RoombaRobot;
import robots.rover.ac13.gui.AC13RoverRobot;
import robots.rover.rover2.gui.Rover2Robot;
import robots.spykee.gui.SpykeeRobot;
import robots.spytank.gui.SpyTankRobot;

public class RobotViewFactory {
	
	public static Class getRobotViewClass(RobotType i_eRobot) {
		switch (i_eRobot) {
		case RBT_ROOMBA:
			return RoombaRobot.class;
		case RBT_NXT:
			return NXTRobot.class;
		case RBT_DOTTY:
			return DottyRobot.class;
		case RBT_ROBOSCOOPER:
			return RoboScooperRobot.class;
		case RBT_SPYKEE:
			return SpykeeRobot.class;
		case RBT_AC13ROVER:
			return AC13RoverRobot.class;
		case RBT_ROMO:
			return RomoRobot.class;
		case RBT_ROVER2:
			return Rover2Robot.class;
		case RBT_SPYTANK:
			return SpyTankRobot.class;
		case RBT_PARROT:
			return ParrotRobot.class;
		case RBT_REPLICATOR:
			return ReplicatorUI.class;
		case RBT_PIRATEDOTTY:
			return PirateDottyRobot.class;
		case RBT_ROBO40:
			return Robo40Robot.class;
		default:
			return null;
		}
	}
	
	// only useful for bluetooth robot where the bluetooth module
	// has a specific mac address prefix. not for wifi robots where
	// the address depends on the network.
	public static String getRobotAddressFilter(RobotType i_eRobot) {
		switch (i_eRobot) {
		case RBT_ROOMBA:
			return RoombaTypes.MAC_FILTER;
		case RBT_NXT:
			return NXTTypes.MAC_FILTER;
		case RBT_DOTTY:
			return DottyTypes.MAC_FILTER;
		case RBT_ROBOSCOOPER:
			return RoboScooperTypes.MAC_FILTER;
		case RBT_PIRATEDOTTY:
			return PirateDottyTypes.MAC_FILTER;
		case RBT_ROBO40:
			return PirateDottyTypes.MAC_FILTER;
		default:
			return "";
		}
	}

}