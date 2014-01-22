package robots.parrot.ardrone2.gui;

import org.dobots.utilities.BaseActivity;

import android.os.Bundle;

import robots.gui.comm.IConnectListener;
import robots.parrot.ctrl.IParrot;
import robots.parrot.gui.ParrotSensorGatherer;
import robots.parrot.gui.ParrotUI;

public class ArDrone2UI extends ParrotUI {

	public ArDrone2UI() {
		super();
	}

	public ArDrone2UI(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        m_oRemoteCtrl.setJoystickControlAvailable(true);
	}
	
	@Override
	protected void onConnect() {
		super.onConnect();
        m_oRemoteCtrl.setRemoteControl(true);
    }

	@Override
	protected ParrotSensorGatherer createSensorGatherer(BaseActivity i_oActivity,
			IParrot i_oParrot) {
		return new ArDrone2SensorGatherer(i_oActivity, i_oParrot);
	}
	
	public static void connectToARDrone(final BaseActivity m_oOwner, IParrot i_oParrot, final IConnectListener i_oConnectListener) {
		ArDrone2UI m_oRobot = new ArDrone2UI(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oParrot.isConnected()) {
			i_oParrot.disconnect();
		}

		i_oParrot.setHandler(m_oRobot.getUIHandler());
		i_oParrot.connect();
	}
}
