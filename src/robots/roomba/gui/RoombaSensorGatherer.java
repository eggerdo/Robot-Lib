package robots.roomba.gui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.dobots.R;
import org.dobots.lib.comm.msg.ISensorDataListener;
import org.dobots.lib.comm.msg.SensorMessageArray;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.sensors.ZmqSensorsReceiver;
import org.zeromq.ZMQ.Socket;

import robots.gui.SensorGatherer;
import robots.roomba.ctrl.Roomba;
import robots.roomba.ctrl.RoombaTypes;
import robots.roomba.ctrl.RoombaTypes.ERoombaSensorPackages;
import robots.roomba.ctrl.RoombaTypes.SensorPackage;
import robots.roomba.ctrl.RoombaTypes.SensorPackage1;
import robots.roomba.ctrl.RoombaTypes.SensorPackage2;
import robots.roomba.ctrl.RoombaTypes.SensorPackage3;
import robots.roomba.ctrl.RoombaTypes.SensorPackageAll;
import robots.roomba.ctrl.RoombaTypes.SensorType;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

public class RoombaSensorGatherer extends SensorGatherer implements ISensorDataListener {

	private ERoombaSensorPackages m_eSensor;
	private Roomba m_oRoomba;
	
	private SensorPackage m_oSensorData;
	
	private TableLayout m_tblSensorPackage1;
	private TableLayout m_tblSensorPackage2;
	private TableLayout m_tblSensorPackage3;
	
	private LinearLayout m_laySensorDataAll;
	private ListView m_lvSensorDataAll;

	private ArrayList<SensorEntry> m_oSensorList;
	private EnumMap<SensorType, SensorEntry> m_oSensorSelected;
	private ArrayAdapter<SensorType> oSensorTypeAdapter;
	
	private Button m_btnSensorItem;

	private ZmqSensorsReceiver m_oSensorsReceiver;
	
	private boolean m_bShowRemoveElement = false;
	
	private TextView txtCasterWD;
	private TextView txtLeftWD;
	private TextView txtRightWD;
	private TextView txtBumpLeft;
	private TextView txtBumpRight;
	private TextView txtWall;
	private TextView txtCliffLeft;
	private TextView txtCliffFrontLeft;
	private TextView txtCliffFrontRight;
	private TextView txtCliffRight;
	private TextView txtVirtualWall;
	private TextView txtDriveLeft;
	private TextView txtDriveRight;
	private TextView txtMainBrush;
	private TextView txtVacuum;
	private TextView txtSideBrush;
	private TextView txtDirtLeft;
	private TextView txtDirtRight;
	private TextView txtRemoteOpCode;
	private TextView txtPowerBtn;
	private TextView txtSpotBtn;
	private TextView txtCleanBtn;
	private TextView txtMaxBtn;
	private TextView txtDistance;
	private TextView txtAngle;
	private TextView txtChargingState;
	private TextView txtCharge;
	private TextView txtPower;
	private TextView txtTemperature;
	private SensorListAdapter oSensorEntryAdapter;
	
	public RoombaSensorGatherer(BaseActivity i_oActivity, Roomba i_oRoomba) {
		super(i_oActivity, "RoombaSensorGatherer");
		m_oRoomba = i_oRoomba;
		
		m_oSensorList = new ArrayList<SensorEntry>();
		m_oSensorSelected = new EnumMap<SensorType, SensorEntry>(SensorType.class);

		Socket sensorsRecvSocket = ZmqHandler.getInstance().obtainSensorsRecvSocket();
		sensorsRecvSocket.subscribe(m_oRoomba.getID().getBytes());
		m_oSensorsReceiver = new ZmqSensorsReceiver(sensorsRecvSocket, "RoombaSensorsReceiver");
		m_oSensorsReceiver.setSensorDataListener(this);
		m_oSensorsReceiver.start();
		
		setProperties();
	}
	
	private void setProperties() {
		m_tblSensorPackage1 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData1);
		m_tblSensorPackage2 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData2);
		m_tblSensorPackage3 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData3);
		
		m_laySensorDataAll = (LinearLayout) m_oActivity.findViewById(R.id.laySensorDataAll);
		m_lvSensorDataAll = (ListView) m_oActivity.findViewById(R.id.lvSensorDataAll);
		oSensorEntryAdapter = new SensorListAdapter(m_oActivity, m_oSensorList);
//		adapter.setNotifyOnChange(true);
		m_lvSensorDataAll.setAdapter(oSensorEntryAdapter);
		m_oActivity.registerForContextMenu(m_lvSensorDataAll);
//		m_lvSensorPackageAll.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		  
        m_btnSensorItem = (Button) m_oActivity.findViewById(R.id.btnSensorItem);
        m_btnSensorItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSensorItemSelectionDialog();
			}
		});
		
		oSensorTypeAdapter = new ArrayAdapter<SensorType>(m_oActivity, 
				android.R.layout.select_dialog_item, SensorType.values());
        oSensorTypeAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        
        setLayoutSensorData1();
        setLayoutSensorData2();
        setLayoutSensorData3();
	}
	
	private void setLayoutSensorData1() {

		txtCasterWD = (TextView) m_oActivity.findViewById(R.id.txtCasterWD);
		txtLeftWD = (TextView) m_oActivity.findViewById(R.id.txtLeftWD);
		txtRightWD = (TextView) m_oActivity.findViewById(R.id.txtRightWD);
		txtBumpLeft = (TextView) m_oActivity.findViewById(R.id.txtBumpLeft);
		txtBumpRight = (TextView) m_oActivity.findViewById(R.id.txtBumpRight);
		txtWall = (TextView) m_oActivity.findViewById(R.id.txtWall);
		txtCliffLeft = (TextView) m_oActivity.findViewById(R.id.txtCliffLeft);
		txtCliffFrontLeft = (TextView) m_oActivity.findViewById(R.id.txtCliffFrontLeft);
		txtCliffFrontRight = (TextView) m_oActivity.findViewById(R.id.txtCliffFrontRight);
		txtCliffRight = (TextView) m_oActivity.findViewById(R.id.txtCliffRight);
		txtVirtualWall = (TextView) m_oActivity.findViewById(R.id.txtVirtualWall);
		txtDriveLeft = (TextView) m_oActivity.findViewById(R.id.txtDriveLeft);
		txtDriveRight = (TextView) m_oActivity.findViewById(R.id.txtDriveRight);
		txtMainBrush = (TextView) m_oActivity.findViewById(R.id.txtMainBrush);
		txtVacuum = (TextView) m_oActivity.findViewById(R.id.txtVacuum);
		txtSideBrush = (TextView) m_oActivity.findViewById(R.id.txtSideBrush);
		txtDirtLeft = (TextView) m_oActivity.findViewById(R.id.txtDirtLeft);
		txtDirtRight = (TextView) m_oActivity.findViewById(R.id.txtDirtRight);
		
	}
	
	private void setLayoutSensorData2() {

		txtRemoteOpCode = (TextView) m_oActivity.findViewById(R.id.txtRemoteOpCode);
		txtPowerBtn = (TextView) m_oActivity.findViewById(R.id.txtPowerBtn);
		txtSpotBtn = (TextView) m_oActivity.findViewById(R.id.txtSpotBtn);
		txtCleanBtn = (TextView) m_oActivity.findViewById(R.id.txtCleanBtn);
		txtMaxBtn = (TextView) m_oActivity.findViewById(R.id.txtMaxBtn);
		txtDistance = (TextView) m_oActivity.findViewById(R.id.txtDistance);
		txtAngle = (TextView) m_oActivity.findViewById(R.id.txtAngle);
		
	}
	
	private void setLayoutSensorData3() {

		txtChargingState = (TextView) m_oActivity.findViewById(R.id.txtChargingState);
		txtCharge = (TextView) m_oActivity.findViewById(R.id.txtCharge);
		txtPower = (TextView) m_oActivity.findViewById(R.id.txtPower);
		txtTemperature = (TextView) m_oActivity.findViewById(R.id.txtTemperature);
		
	}
	
	private void showSensorItemSelectionDialog() {
		AlertDialog dialog = Utils.CreateAdapterDialog(m_oActivity, "Choose a sensor", oSensorTypeAdapter,
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SensorType eSensor = oSensorTypeAdapter.getItem(which);
				if (eSensor == SensorType.ALL) {
					addAllSensors();
				} else {
					addSensor(eSensor, true);
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void addSensor(SensorType i_eSensor, boolean i_bUpdate) {
		
		if (m_oSensorSelected.get(i_eSensor) == null) {
			SensorEntry entry = new SensorEntry(i_eSensor);
			m_oSensorList.add(entry);
			m_oSensorSelected.put(i_eSensor, entry);

			if (i_bUpdate) {
				updateListView();
			}
		}
	}
	
	public void addAllSensors() {
		for (SensorType eSensor : SensorType.values()) {
			if (eSensor != SensorType.ALL) {
				addSensor(eSensor, false);
			}
		}
		// all is not a sensor, remove it again. all is only
		// used to be able to display all sensors at once
		
		updateListView();
	}

	public void setSensorPackage(ERoombaSensorPackages i_eSensor) {
		m_eSensor = i_eSensor;
		
		if (m_eSensor == ERoombaSensorPackages.sensPkg_None) {
			m_oRoomba.stopSensorStreaming();
		} else {
			m_oRoomba.startSensorStreaming(m_eSensor);
		}
	}

	public void showSensorPackage(ERoombaSensorPackages eSensorPkg) {

		m_tblSensorPackage1.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		m_tblSensorPackage2.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		m_tblSensorPackage3.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		
		Utils.showLayout(m_laySensorDataAll, false);
		
		if (eSensorPkg != m_eSensor) {
			switch (eSensorPkg) {
			case sensPkg_1:
				m_tblSensorPackage1.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_2:
				m_tblSensorPackage2.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_3:
				m_tblSensorPackage3.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_All:
				clearSensorSelection();
				updateListView();
				Utils.showLayout(m_laySensorDataAll, true);
				break;
			default:
				break;
			}
			
		}

		setSensorPackage(eSensorPkg);
	}

	private class UpdateSensorDataTask implements Runnable {

		public void run() {
			
			if (m_oRoomba.isPowerOn()) {
			
				switch (m_eSensor) {
				case sensPkg_1:
					if (SensorPackage1.class.isInstance(m_oSensorData)) 
						updateSensorData1();
					break;
				case sensPkg_2:
					if (SensorPackage2.class.isInstance(m_oSensorData))
						updateSensorData2();
					break;
				case sensPkg_3:
					if (SensorPackage3.class.isInstance(m_oSensorData))
						updateSensorData3();
					break;
				case sensPkg_All:
					if (SensorPackageAll.class.isInstance(m_oSensorData))
						updateSensorDataAll();
				}
//			    oHandler.postDelayed(this, 100);
			    
			} else {
				resetSensorData1();
				resetSensorData2();
				resetSensorData3();
				resetSensorDataAll();
			}
		}
		
		private void resetSensorData1() {

			txtCasterWD.setText("---");
			txtLeftWD.setText("---");
			txtRightWD.setText("---");
			txtBumpLeft.setText("---");
			txtBumpRight.setText("---");
			txtWall.setText("---");
			txtCliffLeft.setText("---");
			txtCliffFrontLeft.setText("---");
			txtCliffFrontRight.setText("---");
			txtCliffRight.setText("---");
			txtVirtualWall.setText("---");
			txtDriveLeft.setText("---");
			txtDriveRight.setText("---");
			txtMainBrush.setText("---");
			txtVacuum.setText("---");
			txtSideBrush.setText("---");
			txtDirtLeft.setText("---");
			txtDirtRight.setText("---");
			
		}
			
		private void updateSensorData1() {
	
			SensorPackage1 oSensorData1 = (SensorPackage1)m_oSensorData;
			
			setBoolElement(txtCasterWD, oSensorData1.oBumpsWheeldrops.bCaster_Wheeldrop);
			setBoolElement(txtLeftWD, oSensorData1.oBumpsWheeldrops.bLeft_Wheeldrop);
			setBoolElement(txtRightWD, oSensorData1.oBumpsWheeldrops.bRight_Wheeldrop);
			setBoolElement(txtBumpLeft, oSensorData1.oBumpsWheeldrops.bLeft_Bump);
			setBoolElement(txtBumpRight, oSensorData1.oBumpsWheeldrops.bRight_Bump);
			setBoolElement(txtWall, oSensorData1.bWall);
			setBoolElement(txtCliffLeft, oSensorData1.bCliffLeft);
			setBoolElement(txtCliffFrontLeft, oSensorData1.bCliffFrontLeft);
			setBoolElement(txtCliffFrontRight, oSensorData1.bCliffFrontRight);
			setBoolElement(txtCliffRight, oSensorData1.bCliffRight);
			setBoolElement(txtVirtualWall, oSensorData1.bVirtualWall);
			setBoolElement(txtDriveLeft, oSensorData1.oMotorOvercurrents.bDriveLeft);
			setBoolElement(txtDriveRight, oSensorData1.oMotorOvercurrents.bDriveRight);
			setBoolElement(txtMainBrush, oSensorData1.oMotorOvercurrents.bMainBrush);
			setBoolElement(txtVacuum, oSensorData1.oMotorOvercurrents.bVacuum);
			setBoolElement(txtSideBrush, oSensorData1.oMotorOvercurrents.bSideBrush);
			txtDirtLeft.setText(String.valueOf(oSensorData1.byDirtDetectionLeft));
			txtDirtRight.setText(String.valueOf(oSensorData1.byDirtDetectionRight));
			
		}
		
		private void resetSensorData2() {

			txtRemoteOpCode.setText("---");
			txtPowerBtn.setText("---");
			txtSpotBtn.setText("---");
			txtCleanBtn.setText("---");
			txtMaxBtn.setText("---");
			txtDistance.setText("---");
			txtAngle.setText("---");
			
		}
		
		private void updateSensorData2() {
	
			SensorPackage2 oSensorData2 = (SensorPackage2)m_oSensorData;
			
			txtRemoteOpCode.setText(String.valueOf(oSensorData2.byRemoteOpCode));
			setBoolElement(txtPowerBtn, oSensorData2.oButtonsPressed.bPower);
			setBoolElement(txtSpotBtn, oSensorData2.oButtonsPressed.bSpot);
			setBoolElement(txtCleanBtn, oSensorData2.oButtonsPressed.bClean);
			setBoolElement(txtMaxBtn, oSensorData2.oButtonsPressed.bMax);
			txtDistance.setText(String.valueOf(oSensorData2.sDistance) + " mm");
			txtAngle.setText(String.valueOf(oSensorData2.sAngle) + " mm");
			
		}
		
		private void resetSensorData3() {

			txtChargingState.setText("---");
			txtCharge.setText("---");
			txtPower.setText("---");
			txtTemperature.setText("---");
			
		}
		
		private void updateSensorData3() {
	
			SensorPackage3 oSensorData3 = (SensorPackage3)m_oSensorData;
			
			txtChargingState.setText(oSensorData3.eChargingState.toString());
			CharSequence strTmp = String.format("%.2f", (float)oSensorData3.sCharge / oSensorData3.sCapacity * 100) + "%" + 
								  " ( " + oSensorData3.sCharge + " / " + oSensorData3.sCapacity + " mAh )";
			txtCharge.setText(strTmp);
			strTmp = String.format("%.2f", (float)oSensorData3.sCurrent / 1000 * oSensorData3.sVoltage / 1000) + "W" +
					 " ( " + oSensorData3.sCurrent + " mA, " + oSensorData3.sVoltage + " mV )";
			txtPower.setText(strTmp);
			txtTemperature.setText(oSensorData3.byTemperature + " C");
			
		}
		
		private void updateSensorDataAll() {
			
			SensorPackageAll oData = (SensorPackageAll) m_oSensorData;
			for (SensorType eType : SensorType.values()) {
				SensorEntry oEntry = m_oSensorSelected.get(eType);
				if (oEntry != null) {
					switch (oEntry.eSensor) {
					case WHEELDROP_CASTER:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bCaster_Wheeldrop);
						break;
					case WHEELDROP_LEFT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bLeft_Wheeldrop);
						break;
					case WHEELDROP_RIGHT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bRight_Wheeldrop);
						break;
					case BUMP_LEFT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bLeft_Bump);
						break;
					case BUMP_RIGHT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bRight_Bump);
						break;
					case WALL:
						oEntry.strValue = String.valueOf(oData.wall);
						break;
					case CLIFF_LEFT:
						oEntry.strValue = String.valueOf(oData.cliff_left);
						break;
					case CLIFF_FRONT_LEFT:
						oEntry.strValue = String.valueOf(oData.cliff_front_left);
						break;
					case CLIFF_FRONT_RIGHT:
						oEntry.strValue = String.valueOf(oData.cliff_front_right);
						break;
					case CLIFF_RIGHT:
						oEntry.strValue = String.valueOf(oData.cliff_right);
						break;
					case VIRTUAL_WALL:
						oEntry.strValue = String.valueOf(oData.virtual_wall);
						break;
					case OVERCURRENT_LEFT:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bDriveLeft);
						break;
					case OVERCURRENT_RIGHT:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bDriveRight);
						break;
					case OVERCURRENT_MAIN_BRUSH:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bMainBrush);
						break;
					case OVERCURRENT_VACUUM:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bVacuum);
						break;
					case OVERCURRENT_SIDEBRUSH:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bSideBrush);
						break;
					case DIRT_DETECTOR_LEFT:
						oEntry.strValue = String.valueOf(oData.dirt_detector_left);
						break;
					case DIRT_DETECTOR_RIGHT:
						oEntry.strValue = String.valueOf(oData.dirt_detector_right);
						break;
					case IR_OPCODE_OMNI:
						oEntry.strValue = oData.remote_opcode.toString();
						break;
					case PRESSED_POWER:
						oEntry.strValue = String.valueOf(oData.buttons.bPower);
						break;
					case PRESSED_SPOT:
						oEntry.strValue = String.valueOf(oData.buttons.bSpot);
						break;
					case PRESSED_CLEAN:
						oEntry.strValue = String.valueOf(oData.buttons.bClean);
						break;
					case PRESSED_MAX:
						oEntry.strValue = String.valueOf(oData.buttons.bMax);
						break;
					case DISTANCE:
						oEntry.strValue = String.valueOf(oData.distance);
						break;
					case ANGLE:
						oEntry.strValue = String.valueOf(oData.angle);
						break;
					case CHARGING_STATE:
						oEntry.strValue = oData.charging_state.toString();
						break;
					case VOLTAGE:
						oEntry.strValue = String.valueOf(oData.voltage);
						break;
					case CURRENT:
						oEntry.strValue = String.valueOf(oData.current);
						break;
					case BATTERY_TEMPRERATURE:
						oEntry.strValue = String.valueOf(oData.temprerature);
						break;
					case CHARGE:
						oEntry.strValue = String.valueOf(oData.charge);
						break;
					case CAPACITY:
						oEntry.strValue = String.valueOf(oData.capacity);
						break;
					case WALL_SIGNAL:
						oEntry.strValue = String.valueOf(oData.wall_signal);
						break;
					case CLIFF_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_left_signal);
						break;
					case CLIFF_FRONT_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_front_left_signal);
						break;
					case CLIFF_FRONT_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_front_right_signal);
						break;
					case CLIFF_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_right_signal);
						break;
					case USER_DIGITAL_INPUTS:
						oEntry.strValue = String.valueOf(oData.user_digital_inputs);
						break;
					case USER_ANALOG_INPUT:
						oEntry.strValue = String.valueOf(oData.user_analog_input);
						break;
					case CHARGING_SOURCES_AVAILABLE:
						oEntry.strValue = oData.charging_sources_available.toString();
						break;
					case OI_MODE:
						oEntry.strValue = oData.oi_mode.toString();
						break;
					case SONG_NUMBER:
						oEntry.strValue = String.valueOf(oData.song_number);
						break;
					case SONG_PLAYING:
						oEntry.strValue = String.valueOf(oData.song_playing);
						break;
					case NUMBER_OF_STREAM_PACKETS:
						oEntry.strValue = String.valueOf(oData.number_of_stream_packets);
						break;
					case REQUESTED_VELOCITY:
						oEntry.strValue = String.valueOf(oData.requested_velocity);
						break;
					case REQUESTED_RADIUS:
						oEntry.strValue = String.valueOf(oData.requested_radius);
						break;
					case REQUESTED_RIGHT_VELOCITY:
						oEntry.strValue = String.valueOf(oData.requested_right_velocity);
						break;
					case REQUESTED_LEFT_VELOCITY:
						oEntry.strValue = String.valueOf(oData.requested_left_velocity);
						break;
					case ENCODER_COUNTS_LEFT:
						oEntry.strValue = String.valueOf(oData.encoder_counts_left);
						break;
					case ENCODER_COUNTS_RIGHT:
						oEntry.strValue = String.valueOf(oData.encoder_counts_right);
						break;
					case LIGHT_BUMPER_CENTER_LEFT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperCenterLeft);
						break;
					case LIGHT_BUMPER_CENTER_RIGHT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperCenterRight);
						break;
					case LIGHT_BUMPER_FRONT_LEFT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperFrontLeft);
						break;
					case LIGHT_BUMPER_FRONT_RIGHT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperFrontRight);
						break;
					case LIGHT_BUMPER_LEFT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperLeft);
						break;
					case LIGHT_BUMPER_RIGHT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperRight);
						break;
					case LIGHT_BUMP_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_left_signal);
						break;
					case LIGHT_BUMP_FRONT_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_front_left_signal);
						break;
					case LIGHT_BUMP_CENTER_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_center_left_signal);
						break;
					case LIGHT_BUMP_CENTER_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_center_right_signal);
						break;
					case LIGHT_BUMP_FRONT_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_front_right_signal);
						break;
					case LIGHT_BUMP_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_right_signal);
						break;
					case IR_OPCODE_LEFT:
						oEntry.strValue = oData.ir_opcode_left.toString();
						break;
					case IR_OPCODE_RIGHT:
						oEntry.strValue = oData.ir_opcode_right.toString();
						break;
					case LEFT_MOTOR_CURRENT:
						oEntry.strValue = String.valueOf(oData.left_motor_current);
						break;
					case RIGHT_MOTOR_CURRENT:
						oEntry.strValue = String.valueOf(oData.right_motor_current);
						break;
					case MAIN_BRUSH_CURRENT:
						oEntry.strValue = String.valueOf(oData.main_brush_current);
						break;
					case SIDE_BRUSH_CURRENT:
						oEntry.strValue = String.valueOf(oData.side_brush_current);
						break;
					case STASIS:
						oEntry.strValue = String.valueOf(oData.stasis);
						break;
					}
				}
			}

			m_lvSensorDataAll.invalidateViews();
		}
		
		private void resetSensorDataAll() {

			for (SensorType eType : SensorType.values()) {
				SensorEntry oEntry = m_oSensorSelected.get(eType);
				if (oEntry != null) {
					oEntry.strValue = "---";
				}
			}

			m_lvSensorDataAll.invalidateViews();
		}

		private void setBoolElement(TextView io_oElement, boolean i_bBool) {
			io_oElement.setText(Boolean.toString(i_bBool));
			if (i_bBool) {
				io_oElement.setBackgroundColor(Color.RED);
				io_oElement.setTextColor(Color.LTGRAY);
			} else {
				io_oElement.setBackgroundColor(Color.GREEN);
				io_oElement.setTextColor(Color.BLACK);
			}
		}
		
	}
	
	public void initialize() {
		showSensorPackage(ERoombaSensorPackages.sensPkg_None);
		clearSensorSelection();
	}

	public void clearSensorSelection() {
		m_oSensorList.clear();
		m_oSensorSelected.clear();
	}
	

	public class SensorEntry {
		
		SensorType eSensor;
		String strValue;
		boolean bRemove;
		
		public SensorEntry(SensorType i_eSensor) {
			this.eSensor = i_eSensor;
			this.bRemove = false;
		}
		
	}

	private class SensorListAdapter extends ArrayAdapter<SensorEntry> {
		
		private final Activity context;
		private final List<SensorEntry> list;
		
		public SensorListAdapter(Activity context, List<SensorEntry> list) {
			super(context, R.layout.roomba_sensor_entry, list);
			this.context = context;
			this.list = list;
		}
		
		private class ViewHolder {
			protected TextView lblSensorName;
			protected TextView txtSensorValue;
			protected CheckBox cbRemove;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			SensorEntry oEntry = list.get(position);
			
			if (convertView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.roomba_sensor_entry, null);
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.lblSensorName = (TextView) view.findViewById(R.id.lblSensorName);
				viewHolder.txtSensorValue = (TextView) view.findViewById(R.id.txtSensorValue);
				
				viewHolder.cbRemove = (CheckBox) view.findViewById(R.id.cbRemoveSensor);
				viewHolder.cbRemove.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						SensorEntry oEntry = (SensorEntry) viewHolder.cbRemove.getTag();
						oEntry.bRemove = true;
					}
				});
				
				view.setTag(viewHolder);
				viewHolder.cbRemove.setTag(oEntry);
			} else {
				view = convertView;
				((ViewHolder) view.getTag()).cbRemove.setTag(oEntry);
			}
			
			ViewHolder holder = (ViewHolder) view.getTag();
			
			// the buttons and all other items which can get a focus need to be set
			// to focusable=false so that the onClick event of the listViewItem gets
			// fired!!
			holder.cbRemove.setFocusable(false);
			
			holder.lblSensorName.setText(oEntry.eSensor.toString());
			holder.txtSensorValue.setText(oEntry.strValue);
			
			if (m_bShowRemoveElement) {
				Utils.showView(holder.cbRemove, true);
			} else {
				holder.cbRemove.setChecked(false);
				Utils.showView(holder.cbRemove, false);
			}
			
			return view;
		}
		
	}
	
	private void removeEntries() {
		for (int i = m_oSensorList.size()-1; i >= 0 ; i--) {
			SensorEntry entry = m_oSensorList.get(i);
			if (entry.bRemove) {
				m_oSensorList.remove(entry);
				m_oSensorSelected.put(entry.eSensor, null);
			}
		}
		updateListView();
	}
	
	private void updateListView() {
		m_lvSensorDataAll.invalidateViews();
		Utils.setListViewHeightBasedOnChildren(m_lvSensorDataAll);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.lvSensorDataAll) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			SensorEntry oEntry = m_oSensorList.get(info.position);
			menu.setHeaderTitle(String.format("%s", oEntry.eSensor.toString()));
			menu.add("Remove");
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		SensorEntry oEntry = m_oSensorList.get(info.position);
		m_oSensorList.remove(oEntry);
		m_oSensorSelected.put(oEntry.eSensor, null);
		updateListView();
		return true;
	}

	public void updateButtons(boolean enabled) {
		m_btnSensorItem.setEnabled(enabled);
	}

	@Override
	public void shutDown() {
		m_oSensorsReceiver.close();
	}

	public void handleSensorMessage(Message msg) {
		m_oSensorData = (SensorPackage) msg.obj;
		m_oUiHandler.post(new UpdateSensorDataTask());
	}

	@Override
	public void onSensorData(String json) {
		// TODO Auto-generated method stub
		SensorMessageArray data = SensorMessageArray.decodeJSON(m_oRoomba.getID(), json);
		m_oSensorData = RoombaTypes.assembleSensorPackage(data);
		m_oUiHandler.post(new UpdateSensorDataTask());
	}

}
