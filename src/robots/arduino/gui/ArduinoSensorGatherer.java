package robots.arduino.gui;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dobots.R;
import org.dobots.lib.comm.msg.ISensorDataListener;
import org.dobots.lib.comm.msg.SensorMessageObj;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.sensors.ZmqSensorsReceiver;
import org.json.JSONArray;
import org.json.JSONException;
import org.zeromq.ZMQ.Socket;

import robots.arduino.ctrl.IArduino;
import robots.gui.SensorGatherer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

class SensorListAdapter extends ArrayAdapter<Map.Entry<String, String>> {
	
	private final BaseActivity context;
	private final List<Map.Entry<String, String>> list;
	
	public SensorListAdapter(BaseActivity context, List<Map.Entry<String, String>> list) {
		super(context, R.layout.sensor_entry, list);
		this.context = context;
		this.list = list;
	}
	
	private class ViewHolder {
		protected TextView lblSensorName;
		protected TextView txtSensorValue;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		Map.Entry<String, String> oEntry = list.get(position);
		
		if (convertView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.sensor_entry, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.lblSensorName = (TextView) view.findViewById(R.id.lblSensorName);
			viewHolder.txtSensorValue = (TextView) view.findViewById(R.id.txtSensorValue);
			
			view.setTag(viewHolder);
		} else {
			view = convertView;
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		
		holder.lblSensorName.setText(oEntry.getKey());
		holder.txtSensorValue.setText(oEntry.getValue());
		return view;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}
}

public class ArduinoSensorGatherer extends SensorGatherer implements ISensorDataListener {

	private List<Map.Entry<String, String>> mSensorList;
	private ListView mListView;
	private TextView mNoData;

	private ZmqSensorsReceiver m_oSensorsReceiver;
	private Socket m_oSensorsRecvSocket;
	
	private IArduino mArduino;
	
	public ArduinoSensorGatherer(BaseActivity i_oActivity, IArduino arduino) {
		super(i_oActivity, "ArduinoSensorGatherer");
		mArduino = arduino;
		
		mSensorList = new ArrayList<Map.Entry<String,String>>();

		m_oSensorsRecvSocket = ZmqHandler.getInstance().obtainSensorsRecvSocket();
		m_oSensorsRecvSocket.subscribe(mArduino.getID().getBytes());
		m_oSensorsReceiver = new ZmqSensorsReceiver(m_oSensorsRecvSocket, "ArduinoSensorsReceiver");
		m_oSensorsReceiver.setSensorDataListener(this);
		m_oSensorsReceiver.start();
		
		setLayout();
	}
	
	private void setLayout() {
		mListView = (ListView) m_oActivity.findViewById(R.id.lvSensorList);
		SensorListAdapter mAdapter = new SensorListAdapter(m_oActivity, mSensorList);
		mListView.setAdapter(mAdapter);
		
		mNoData = (TextView) m_oActivity.findViewById(R.id.lblNoData);
	}
	
	public void resetLayout() {
		mSensorList.clear();
		Utils.showView(mNoData, true);
		Utils.setListViewHeightBasedOnChildren(mListView);
	}
	
	public void updateItem(String name, String value) {
		boolean found = false;
		for (int i = 0; i < mSensorList.size(); ++i) {
			Map.Entry<String, String> entry = mSensorList.get(i);
			if (entry.getKey().equals(name)) {
				entry.setValue(value);
				found = true;
				break;
			}
		}
		
		if (!found) {
			mSensorList.add(new AbstractMap.SimpleEntry<String, String>(name, value));
		}
	}
	
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub

	}

	private void updateGUI(SensorMessageObj json) {
		try {
			JSONArray names = json.names();
			for (int i = 0; i < names.length(); ++i) {
				String name = names.getString(i);
				String value = json.get(name).toString();
				updateItem(name, value);
			}

			m_oActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					mListView.invalidateViews();
					Utils.setListViewHeightBasedOnChildren(mListView);
			
					Utils.showView(mNoData, false);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSensorData(String data) {
		SensorMessageObj sensorData = SensorMessageObj.decodeJSON(mArduino.getID(), data);
		updateGUI(sensorData);
	}
	
}
