package robots.gui;

import robots.RobotType;
import robots.arduino.ctrl.ArduinoTypes;
import robots.arduino.gui.ArduinoUI;
import robots.nxt.ctrl.NxtTypes;
import robots.nxt.gui.NxtRobot;
import robots.parrot.gui.ParrotUI;
import robots.piratedotty.ctrl.PirateDottyTypes;
import robots.piratedotty.gui.PirateDottyUI;
import robots.replicator.gui.ReplicatorUI;
import robots.romo.ctrl.RomoTypes;
import robots.romo.gui.RomoUI;
import robots.roomba.ctrl.RoombaTypes;
import robots.roomba.gui.RoombaRobot;
import robots.rover.ac13.gui.AC13RoverUI;
import robots.rover.rover2.gui.Rover2UI;
import robots.spytank.gui.SpyTankUI;

//import org.dobots.robots.dotty.DottyTypes;
//import org.dobots.robots.nxt.NXTTypes;
//import org.dobots.robots.parrot.ParrotTypes;
//import org.dobots.robots.roboscooper.RoboScooperTypes;
//import org.dobots.robots.roomba.RoombaTypes;
//import org.dobots.robots.spykee.SpykeeTypes;
//import org.dobots.swarmcontrol.robots.ac13.AC13RoverRobot;
//import org.dobots.swarmcontrol.robots.dotty.DottyRobot;
//import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
//import org.dobots.swarmcontrol.robots.parrot.ParrotRobot;
//import org.dobots.swarmcontrol.robots.roboscooper.RoboScooperRobot;
//import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;
//import org.dobots.swarmcontrol.robots.spykee.SpykeeRobot;

public class RobotViewFactory {
	
	public static Class getRobotViewClass(RobotType i_eRobot) {
		switch (i_eRobot) {
		case RBT_ROOMBA:
			return RoombaRobot.class;
		case RBT_NXT:
			return NxtRobot.class;
//		case RBT_DOTTY:
//			return DottyRobot.class;
		case RBT_ROBOSCOOPER:
			return RoboScooperRobot.class;
//		case RBT_SPYKEE:
//			return SpykeeRobot.class;
		case RBT_AC13ROVER:
			return AC13RoverUI.class;
		case RBT_ROMO:
			return RomoUI.class;
		case RBT_ROVER2:
			return Rover2UI.class;
		case RBT_SPYTANK:
			return SpyTankUI.class;
		case RBT_PARROT:
			return ParrotUI.class;
		case RBT_REPLICATOR:
			return ReplicatorUI.class;
		case RBT_PIRATEDOTTY:
			return PirateDottyUI.class;
		case RBT_ARDUINO:
			return ArduinoUI.class;
		default:
			return null;
//			return UnimplementedRobot.class;
		}
	}
	
	public static String getRobotAddressFilter(RobotType i_eRobot) {
		switch (i_eRobot) {
		case RBT_ROOMBA:
			return RoombaTypes.MAC_FILTER;
		case RBT_NXT:
			return NxtTypes.MAC_FILTER;
//		case RBT_DOTTY:
//			return DottyTypes.MAC_FILTER;
//		case RBT_PARROT:
//			return ParrotTypes.SSID_FILTER;
		case RBT_ROBOSCOOPER:
			return RoboScooperTypes.MAC_FILTER;
//		case RBT_SPYKEE:
//			return SpykeeTypes.SSID_FILTER;
//		case RBT_AC13ROVER:
//			return AC13RoverTypes.SSID_FILTER;
//		case RBT_ROMO:
//			return RomoTypes.SSID_FILTER;
		case RBT_PIRATEDOTTY:
			return PirateDottyTypes.MAC_FILTER;
		case RBT_ARDUINO:
			return ArduinoTypes.MAC_FILTER;
		default:
			return "";
		}
	}

}