package robots.piratedotty.gui;

import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.CameraPreview.CameraPreviewCallback;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.comm.VideoMessage;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import robots.gui.SensorGatherer;
import robots.piratedotty.ctrl.PirateDottyTypes;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class PirateDottySensorGatherer extends SensorGatherer implements CameraPreviewCallback {

//	private PirateDotty m_oPirateDotty;
	
//	private EnumMap<EPirateDottySensors, Boolean> m_oSensorEnabled;
//	private boolean m_bSensorRequestActive = false;
//	private int m_nEnableBitMask = 0;
//	
//	TextView txtDistanceValue;
//	TextView txtLightValue;
//	TextView txtSoundValue;
//	TextView txtBatteryValue;
//	TextView txtMotor1Value;
//	TextView txtMotor2Value;
//	TextView txtWheel1Value;
//	TextView txtWheel2Value;
//	TextView txtLed1Value;
//	TextView txtLed2Value;
//	TextView txtLed3Value;
//	
//	LinearLayout layDistanceValue;
//	LinearLayout layLightValue;
//	LinearLayout laySoundValue;
//	LinearLayout layBatteryValue;
//	LinearLayout layMotor1Value;
//	LinearLayout layMotor2Value;
//	LinearLayout layWheel1Value;
//	LinearLayout layWheel2Value;
//	LinearLayout layLed1Value;
//	LinearLayout layLed2Value;
//	LinearLayout layLed3Value;

	private ZMQ.Socket m_oVideoSocket;
	private byte[] mRobotID;

	public PirateDottySensorGatherer(BaseActivity i_oActivity, String i_strRobotID) {
		super(i_oActivity, "PirateDottySensorGatherer");

		mRobotID = i_strRobotID.getBytes();
		m_oVideoSocket = ZmqHandler.getInstance().obtainVideoSendSocket();
		
//		m_oPirateDotty = i_oPirateDotty;
		
//		m_oSensorEnabled = new EnumMap<EPirateDottySensors, Boolean>(EPirateDottySensors.class);
	
		setProperties();
		
		// set up the maps
		initialize();
		
		start();
	}

	@Override
	public void onFrame(byte[] rgb, int width, int height, int rotation) {

		VideoMessage oMsg = new VideoMessage(mRobotID, rgb, rotation);
		
		ZMsg zmsg = oMsg.toZmsg();
		zmsg.send(m_oVideoSocket);
		
//		if (m_bDebug) {
//            ++m_nFpsCounterPartner;
//            long now = System.currentTimeMillis();
//            if ((now - m_lLastTimePartner) >= 1000)
//            {
//            	final int nFPS = m_nFpsCounterPartner;
//				Utils.runAsyncUiTask(new Runnable() {
//					
//					@Override
//					public void run() {
//						m_lblFPS.setText("FPS: " + String.valueOf(nFPS));
//					}
//				});
//	            
//                m_lLastTimePartner = now;
//                m_nFpsCounterPartner = 0;
//            }
//        }
	}

	@Override
	public void shutDown() {
		m_oVideoSocket.close();
	}

	public void setProperties() {
//		txtDistanceValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_DistanceValue);
//		txtLightValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_LightValue);
//		txtSoundValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_SoundValue);
//		txtBatteryValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_BatteryValue);
//		txtMotor1Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_MotorSensor1Value);
//		txtMotor2Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_MotorSensor2Value);
//		txtWheel1Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Wheel1Value);
//		txtWheel2Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Wheel2Value);
//		txtLed1Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Led1Value);
//		txtLed2Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Led2Value);
//		txtLed3Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Led3Value);
//		
//		layDistanceValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_DistanceValue);
//		layLightValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_LightValue);
//		laySoundValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_SoundValue);
//		layBatteryValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_BatteryValue);
//		layMotor1Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_MotorSensor1Value);
//		layMotor2Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_MotorSensor2Value);
//		layWheel1Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Wheel1Value);
//		layWheel2Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Wheel2Value);
//		layLed1Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Led1Value);
//		layLed2Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Led2Value);
//		layLed3Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Led3Value);
	}

	public void initialize() {
		// set up the maps
//		for (EPirateDottySensors sensor : EPirateDottySensors.values()) {
//			m_oSensorEnabled.put(sensor, false);
//		}
//		m_bSensorRequestActive = false;
	}
	
	protected void execute() {

//		if (m_oPirateDotty.isConnected()) {
//			if (m_nEnableBitMask != 0 && !m_bSensorRequestActive) {
//			}
//		}
		
		Utils.waitSomeTime(500);
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler m_oSensorDataUiUpdater = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch(myMessage.what) {
			case PirateDottyTypes.SENSOR_DATA:
//				SensorData oData = (SensorData) myMessage.obj;
//				updateGUI(oData);
			}
		}
	};
	
	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}

//	private void updateGUI(SensorData oData) {
//		
//		for (EPirateDottySensors eSensor : EPirateDottySensors.values()) {
//			if (m_oSensorEnabled.get(eSensor)) {
//				switch(eSensor) {
//				case sensor_Battery:
//					setText(txtBatteryValue, oData.nBattery);
//					break;
//				case sensor_Dist:
//					setText(txtDistanceValue, oData.nDistance);
//					break;
//				case sensor_Light:
//					setText(txtLightValue, oData.nLight);
//					break;
//				case sensor_Motor1:
//					setText(txtMotor1Value, oData.nMotor1);
//					break;
//				case sensor_Motor2:
//					setText(txtMotor2Value, oData.nMotor2);
//					break;
//				case sensor_Sound:
//					setText(txtSoundValue, oData.nSound);
//					break;
//				case sensor_Wheel1:
//					setText(txtWheel1Value, oData.nWheel1);
//					break;
//				case sensor_Wheel2:
//					setText(txtWheel2Value, oData.nWheel2);
//					break;
//				case sensor_Led1:
//					setOnOffText(txtLed1Value, oData.bLed1ON);
//					break;
//				case sensor_Led2:
//					setOnOffText(txtLed2Value, oData.bLed2ON);
//					break;
//				case sensor_Led3:
//					setOnOffText(txtLed3Value, oData.bLed3ON);
//					break;
//				}
//			}
//		}
//		
//		m_bSensorRequestActive = false;
//	}
	
//	public void enableSensor(EPirateDottySensors i_eSensor, boolean i_bEnabled) {
//		m_oSensorEnabled.put(i_eSensor, i_bEnabled);
//		if (i_bEnabled) {
//			m_nEnableBitMask = Utils.setBit(m_nEnableBitMask, i_eSensor.ordinal());
//		} else {
//			m_nEnableBitMask = Utils.clearBit(m_nEnableBitMask, i_eSensor.ordinal());
//		}
//		
//		showSensor(i_eSensor, i_bEnabled);
//	}
	
//	public void showSensor(EPirateDottySensors i_eSensor, boolean i_bShow) {
//		switch(i_eSensor) {
//		case sensor_Battery:
//			showLayout(layBatteryValue, i_bShow);
//			break;
//		case sensor_Dist:
//			showLayout(layDistanceValue, i_bShow);
//			break;
//		case sensor_Light:
//			showLayout(layLightValue, i_bShow);
//			break;
//		case sensor_Motor1:
//			showLayout(layMotor1Value, i_bShow);
//			break;
//		case sensor_Motor2:
//			showLayout(layMotor2Value, i_bShow);
//			break;
//		case sensor_Sound:
//			showLayout(laySoundValue, i_bShow);
//			break;
//		case sensor_Wheel1:
//			showLayout(layWheel1Value, i_bShow);
//			break;
//		case sensor_Wheel2:
//			showLayout(layWheel2Value, i_bShow);
//			break;
//		case sensor_Led1:
//			showLayout(layLed1Value, i_bShow);
//			break;
//		case sensor_Led2:
//			showLayout(layLed2Value, i_bShow);
//			break;
//		case sensor_Led3:
//			showLayout(layLed3Value, i_bShow);
//			break;
//		}
//	}
	
	private void showLayout(View v, boolean i_bShow) {
		if (i_bShow) {
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
		}
	}

}
