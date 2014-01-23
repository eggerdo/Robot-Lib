package robots.parrot.ardrone1.gui;

import org.dobots.utilities.BaseActivity;

import robots.gui.comm.IConnectListener;
import robots.parrot.ctrl.IParrot;
import robots.parrot.gui.ParrotSensorGatherer;
import robots.parrot.gui.ParrotUI;

public class ArDrone1UI extends ParrotUI {

	public ArDrone1UI() {
		super();
	}

	public ArDrone1UI(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	@Override
	protected ParrotSensorGatherer createSensorGatherer(
			BaseActivity i_oActivity, IParrot i_oParrot) {
		return new ParrotSensorGatherer(i_oActivity, i_oParrot);
	}

	public static void connectToARDrone(final BaseActivity m_oOwner, IParrot i_oParrot, final IConnectListener i_oConnectListener) {
		ArDrone1UI m_oRobot = new ArDrone1UI(m_oOwner) {
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
