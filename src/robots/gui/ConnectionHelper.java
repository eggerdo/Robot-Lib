package robots.gui;

import org.dobots.utilities.BaseActivity;

import robots.ctrl.IRobotDevice;
import robots.gui.BluetoothConnectionHelper.BTEnableCallback;
import robots.nxt.ctrl.NXT;
import robots.nxt.gui.NXTRobot;
import robots.parrot.ctrl.Parrot;
import robots.parrot.gui.ParrotRobot;
import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.ac13.gui.AC13RoverRobot;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

public class ConnectionHelper {
	
	public static boolean establishConnection(BaseActivity i_oActivity, IRobotDevice i_oRobot, IConnectListener i_oListener) {
		
		switch(i_oRobot.getType()) {
		case RBT_DOTTY:
		case RBT_PIRATEDOTTY:
		case RBT_NXT:
		case RBT_ROOMBA:
		case RBT_ROBOSCOOPER:
		case RBT_ROBO40:
			return establishBluetoothConnection(i_oActivity, i_oRobot, i_oListener);
		case RBT_PARROT:
		case RBT_SPYKEE:
		case RBT_AC13ROVER:
		case RBT_ROVER2:
		case RBT_SPYTANK:
			return establishWifiConnection(i_oActivity, i_oRobot, i_oListener);
		}

		return false;
		
	}
	
	public static boolean establishBluetoothConnection(final BaseActivity i_oActivity, final IRobotDevice i_oRobot, final IConnectListener i_oListener) {
		
		final BluetoothConnectionHelper oBTHelper = new BluetoothConnectionHelper(i_oActivity, RobotViewFactory.getRobotAddressFilter(i_oRobot.getType()));
		oBTHelper.SetOnConnectListener(new IBluetoothConnectionListener() {
			
			BluetoothDevice mDevice;
			
			@Override
			public void setConnection(BluetoothDevice i_oDevice) {
				mDevice = i_oDevice;
			}

			@Override
			public void connect() {
				try {
					ConnectionHelper.connectToBluetoothRobot(i_oActivity, i_oRobot, mDevice, i_oListener);
				} catch (Exception e) {
					Toast.makeText(i_oActivity, "Robot not available", Toast.LENGTH_LONG);
				}
			}
			
			
		});
		
		oBTHelper.initBluetooth(new BTEnableCallback() {
			
			@Override
			public void onEnabled() {
				oBTHelper.selectRobot();
			}
		});
		
		return true;
	}
	
	public static boolean establishWifiConnection(final BaseActivity i_oActivity, final IRobotDevice i_oRobot, final IConnectListener i_oListener) {
		
		final WifiConnectionHelper oWifiHelper = new WifiConnectionHelper(i_oActivity, RobotViewFactory.getRobotAddressFilter(i_oRobot.getType()));
		
		if (oWifiHelper.initWifi()) {
			try {
				ConnectionHelper.connectToWifiRobot(i_oActivity, i_oRobot, i_oListener);
			} catch (Exception e) {
				Toast.makeText(i_oActivity, "Robot not available", Toast.LENGTH_LONG);
			}
		}
		
		return true;
	}
	

	public static void connectToBluetoothRobot(BaseActivity context, IRobotDevice oRobot,
			BluetoothDevice i_oDevice, final IConnectListener oListener) throws Exception {
		switch (oRobot.getType()) {
		case RBT_NXT:
			NXTRobot.connectToNXT(context, (NXT)oRobot, i_oDevice, oListener);
			break;
//		case RBT_ROOMBA:
//			RoombaRobot.connectToRoomba(context, (Roomba)oRobot, i_oDevice, oListener);
//			break;
//		case RBT_DOTTY:
//			DottyRobot.connectToDotty(context, (Dotty)oRobot, i_oDevice, oListener);
//			break;
//		case RBT_ROBOSCOOPER:
//			RoboScooperRobot.connectToRoboScooper(context, (RoboScooper)oRobot, i_oDevice, oListener);
//			break;
//		case RBT_ROBO40:
//			Robo40Robot.connectToRobo40(context, (Robo40)oRobot, i_oDevice, oListener);
//			break;
		default:
			throw new Exception();
		}
	}

	private static void connectToWifiRobot(BaseActivity context, IRobotDevice oRobot,
			IConnectListener oListener) throws Exception {
		switch (oRobot.getType()) {
		case RBT_PARROT:
			ParrotRobot.connectToARDrone(context, (Parrot)oRobot, oListener);
//		case RBT_SPYKEE:
//			SpykeeRobot.connectToSpykee(context, (Spykee)oRobot, oListener);
		case RBT_AC13ROVER:
			AC13RoverRobot.connectToAC13Rover(context, (AC13Rover)oRobot, oListener);
//		case RBT_ROVER2:
//			Rover2Robot.connectToRover2(context, (Rover2)oRobot, oListener);
		default:
			throw new Exception();
		}
	}


}
