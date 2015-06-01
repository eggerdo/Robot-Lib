package robots.arduino.gui;

import java.io.IOException;

import org.dobots.R;

import robots.RobotType;
import robots.arduino.ctrl.ArduinoTypes;
import robots.arduino.ctrl.IArduino;
import robots.ctrl.zmq.ZmqRemoteControlHelper;
import robots.ctrl.zmq.ZmqRemoteControlSender;
import robots.gui.BluetoothRobot;
import robots.gui.RobotInventory;
import robots.gui.SensorGatherer;
import robots.gui.comm.IRobotConnection;
import robots.gui.comm.bluetooth.BluetoothConnection;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ArduinoUI extends BluetoothRobot {

	private IArduino mArduino;
	private ArduinoSensorGatherer mSensorGatherer;
	private Button btnSensorRequest;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mArduino = (IArduino) getRobot();
//		mArduino.setLogListener(new AndroidLogger());
    	
    	// remote control helper, handles ui buttons and sends commands over zmq
		m_oRemoteCtrl = new ZmqRemoteControlHelper(m_oActivity);
		
		updateButtons(false);
	}

	@Override
	public void onRobotCtrlReady() {
    	m_oZmqRemoteSender = new ZmqRemoteControlSender(mArduino.getID());
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteSender);
		
		mArduino.setHandler(m_oUiHandler);
		mSensorGatherer = new ArduinoSensorGatherer(this, mArduino);

        if (mArduino.isConnected()) {
			updateButtons(true);
		} else {
			connectToRobot();
		}		
	}
	
	@Override
	public void onDestroy() {
		m_oRemoteCtrl.destroy();
		
		if (m_oZmqRemoteSender != null) {
			m_oZmqRemoteSender.close();
		}
		
		if (m_bOwnsRobot) {
			RobotInventory.getInstance().removeRobot(m_strRobotID);
		}
		
		super.onDestroy();
	}
	
	@Override
	public void setConnection(BluetoothDevice i_oDevice) {
		m_strAddress = i_oDevice.getAddress();
		showConnectingDialog();
		
		if (mArduino.getConnection() != null) {
			try {
				mArduino.getConnection().close();
			}
			catch (IOException e) { }
		}
		IRobotConnection connection = new BluetoothConnection(i_oDevice, ArduinoTypes.ARDUINO_UUID);
		connection.setReceiveHandler(m_oUiHandler);
		mArduino.setConnection(connection);
	}

	@Override
	public void connect() {
		mArduino.connect();
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
		m_oRemoteCtrl.setRemoteControl(true);
	}

	@Override
	protected void disconnect() {
		mArduino.disconnect();
	}

	@Override
	protected void onDisconnect() {
		resetLayout();
	}

	@Override
	protected void setLayout(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.arduino_main);
        
        btnSensorRequest = (Button) findViewById(R.id.btnSensorRequest);
        btnSensorRequest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mArduino.requestSensorData();
			}
		});
	}

	@Override
	protected void resetLayout() {
        m_oRemoteCtrl.resetLayout();
		mSensorGatherer.resetLayout();
        updateButtons(false);
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		m_oRemoteCtrl.setControlEnabled(i_bEnabled);
		
		btnSensorRequest.setEnabled(i_bEnabled);
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		return mSensorGatherer;
	}

}
