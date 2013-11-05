package robots.factories;

import org.dobots.communication.control.RemoteControlReceiver;
import org.dobots.communication.control.zmq.ZmqRemoteControlReceiver;
import org.dobots.communication.control.zmq.ZmqRemoteControlSender;
import org.dobots.communication.video.ZmqVideoSender;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.BaseApplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.gui.RobotInventory;
import robots.gui.RobotView;

public class ZmqRobotFactory {
	
	public static void showRobot(Activity i_oActivity, RobotType i_eType) {
		try {
			String i_strRobotID = createRobotDevice(i_eType);
			createRobotView(i_oActivity, i_eType, i_strRobotID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String createRobotDevice(RobotType robot) throws Exception {
		IRobotDevice newRobot = RobotDeviceFactory.createRobotDevice(robot);
		
		ZmqRemoteControlReceiver m_oZmqRemoteReceiver = new ZmqRemoteControlReceiver(robot.name());
		newRobot.setRemoteReceiver(m_oZmqRemoteReceiver);
		m_oZmqRemoteReceiver.start();

		ZmqVideoSender m_oVideoSender = new ZmqVideoSender(newRobot.getID());
		newRobot.setVideoListener(m_oVideoSender);

		String i_strRobotID = RobotInventory.getInstance().addRobot(newRobot);
		return i_strRobotID;
	}
	
	public static void createRobotView(Activity i_oActivity, RobotType i_eType, String i_strRobotID) {
		Intent intent = new Intent(i_oActivity, RobotViewFactory.getRobotViewClass(i_eType));
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("RobotID", i_strRobotID);
		intent.putExtra("OwnsRobot", true);

        // Register for broadcasts when a robot view is loaded
        IntentFilter filter = new IntentFilter(RobotView.VIEW_LOADED);
        i_oActivity.registerReceiver(mReceiver, filter);
        
		i_oActivity.startActivity(intent);
	}

    private static final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (RobotView.VIEW_LOADED.equals(action)) {
            	RobotView view = (RobotView)((BaseApplication)context.getApplicationContext()).getCurrentActivity();
            	
            	ZmqRemoteControlSender m_oZmqRemoteSender = new ZmqRemoteControlSender(view.getRobot().getID());
            	view.setDriveControlListener(m_oZmqRemoteSender);
            	
            	RemoteControlReceiver m_oCameraCtrlReceiver = new ZmqRemoteControlReceiver("RomoUI");
				view.setCameraCtrlReceiver(m_oCameraCtrlReceiver);
				m_oCameraCtrlReceiver.start();
            };
        }
    };

}
