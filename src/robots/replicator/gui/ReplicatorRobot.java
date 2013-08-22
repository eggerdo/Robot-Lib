package robots.replicator.gui;

import org.dobots.R;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import robots.replicator.ctrl.Replicator;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

public class ReplicatorRobot extends WifiRobot {

	private static final String TAG = "ReplicatorRobot";

	private Replicator m_oReplicator;

	private ReplicatorSensorGatherer m_oSensorGatherer;

//	private int m_nCommandPort;
//	private int m_nVideoPort;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.w(TAG, "Creating Replicator robot");

		m_oReplicator = (Replicator) getRobot();
		m_oReplicator.setHandler(m_oUiHandler);

		m_oSensorGatherer = new ReplicatorSensorGatherer(this, m_oReplicator);

		updateButtons(false);

		//m_oReplicator.setConnection(m_strAddress, m_nCommandPort, m_nVideoPort);

		if (m_oReplicator.isConnected()) {
			updateButtons(true);
		} else {
			connectToRobot();
		}
	}
	
	
	@Override
	protected void connect() {
		m_oReplicator.connect();
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
		m_oSensorGatherer.onConnect();
	}

	@Override
	protected void onDisconnect() {
		updateButtons(false);
		m_oSensorGatherer.onDisconnect();
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
		m_oActivity.setContentView(R.layout.replicator_main);
	}

	@Override
	protected void disconnect() {
		m_oReplicator.disconnect();
	}

	@Override
	protected void resetLayout() {
		m_oSensorGatherer.resetLayout();
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		//		m_oRemoteCtrl.updateButtons(i_bEnabled);

		Utils.setEnabledRecursive((ViewGroup)m_oActivity.findViewById(R.id.layCameraControl), i_bEnabled);
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

}
