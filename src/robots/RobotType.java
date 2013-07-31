package robots;

import java.util.LinkedList;

public enum RobotType {
	
	RBT_ROOMBA		("Roomba", 			true,	true),
	RBT_NXT			("Mindstorm NXT", 	true,	true),
	RBT_DOTTY		("Dotty", 			true,	true), 
	RBT_PARROT		("AR Drone", 		true,	true),
	RBT_ROBOSCOOPER	("RoboScooper", 	true,	true),
	RBT_SPYKEE		("Spykee", 			true,	true), 
	RBT_AC13ROVER	("AC13 Rover",		true,	true),
	RBT_ROVER2		("Rover 2.0",		true,	true),
	RBT_SPYTANK		("I-Spy Tank",		true,	true),
	RBT_ROBO40		("Robo 40",			true,	false),
	RBT_FINCH		("Finch", 			false,	true),
	RBT_SURVEYOR	("Surveyor", 		false,	true),
	RBT_TRAKR		("Trakr", 			false,	true),
	RBT_ROMO		("Romo",			false,	true);
	
	private String strDisplayName;
	// enabled means that the robot is implemented and can be selected
	private boolean bEnabled;
	private boolean bAvailable;
	
	private RobotType(String i_strDisplayName, boolean i_bEnabled, boolean i_bAvailable) {
		this.strDisplayName = i_strDisplayName;
		this.bEnabled = i_bEnabled;
		this.bAvailable = i_bAvailable;
	}
	
	@Override
	public String toString() {
		return strDisplayName;
	}
	
	public boolean isEnabled() {
		return bEnabled;
	}
	
	public boolean isAvailable() {
		return bAvailable;
	}
	
	public static RobotType fromString(String name) {
		for (RobotType item : RobotType.values()) {
			if (item.name().equals(name)) {
				return item;
			}
		}
		return null;
	}
	
	public static RobotType[] getRobots() {
		RobotType[] values = RobotType.values();
		LinkedList<RobotType> temp = new LinkedList<RobotType>();
		
		for (RobotType item : values) {
			if (item.isAvailable()) {
				temp.add(item);
			} else {
				int i = 0;
			}
		}
		return temp.toArray(new RobotType[temp.size()]);
	}

}