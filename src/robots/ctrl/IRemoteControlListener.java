package robots.ctrl;

import robots.ctrl.RemoteControlHelper.Move;

public interface IRemoteControlListener {
	
	void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle);
	
	void onMove(Move i_oMove);
	
	void enableControl(boolean i_bEnable);
	
	void toggleInvertDrive();
	
}
