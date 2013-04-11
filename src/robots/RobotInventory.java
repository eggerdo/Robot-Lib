package robots;

import java.util.HashMap;

import robots.ctrl.IRobotDevice;

public class RobotInventory {
	
	private static RobotInventory m_oInstance;

	private HashMap<String, IRobotDevice> m_oRobotList = new HashMap<String, IRobotDevice>();

	public static RobotInventory getInstance() {
		if (m_oInstance == null) {
			m_oInstance = new RobotInventory();
		}
		return m_oInstance;
	}
	
	public String addRobot(IRobotDevice i_oRobot) {
		m_oRobotList.put(i_oRobot.getID(), i_oRobot);
		return i_oRobot.getID();
	}
	
	public IRobotDevice getRobot(String i_strRobotID) {
		return m_oRobotList.get(i_strRobotID);
	}

	public void removeRobot(String i_strRobotID) {
		IRobotDevice oRobot = m_oRobotList.remove(i_strRobotID);
		oRobot.destroy();
	}

}
