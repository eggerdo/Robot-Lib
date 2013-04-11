package robots.gui;

import robots.RobotInventory;
import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.ctrl.romo.Romo;
import android.app.Activity;
import android.content.Intent;

public class RobotLaunchHelper {

	public static void showRobot(Activity i_oActivity, RobotType i_eType) {
		try {
			String i_strRobotID = createRobot(i_eType);
			createRobotView(i_oActivity, i_eType, i_strRobotID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void createRobotView(Activity i_oActivity, RobotType i_eType, String i_strRobotID) {
		Intent intent = new Intent(i_oActivity, RobotViewFactory.getRobotViewClass(i_eType));
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("RobotID", i_strRobotID);
		intent.putExtra("OwnsRobot", true);
		i_oActivity.startActivity(intent);
	}
	
	public static String createRobot(RobotType i_eType) throws Exception {
		IRobotDevice oRobot = new Romo();
		String i_strRobotID = RobotInventory.getInstance().addRobot(oRobot);
		return i_strRobotID;
	}

}
