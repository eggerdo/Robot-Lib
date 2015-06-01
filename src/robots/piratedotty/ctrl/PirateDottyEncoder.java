package robots.piratedotty.ctrl;

import org.dobots.comm.JsonEncoder;
import org.dobots.comm.protocol.BaseMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class PirateDottyEncoder extends JsonEncoder {
	
	private static PirateDottyEncoder INSTANCE;
	public static int TIMESTAMP = 0;
	
	public static final int DOCK = USER + 1;
	public static final int SHOOT_GUNS = USER + 2;
	public static final int FIRE_VOLLEY = USER + 3;
	public static final int HIT_DETECTED = USER + 4;
	
	public PirateDottyEncoder getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PirateDottyEncoder();
		}
		return INSTANCE;
	}

	public JSONObject createSimpleCommand(int message_id) throws JSONException {
		return assembleBaseMessage(new BaseMessage(message_id, 0));
	}

	public byte[] getSimpleCommandPackage(int message_id) {
		JSONObject json;
		try {
			json = createSimpleCommand(message_id);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject createDockingCommand(boolean isDocking) throws JSONException {
		JSONObject json = assembleBaseMessage(new BaseMessage(DOCK, 0));

		JSONObject data = new JSONObject();
		data.put("enable", isDocking);
		json.put("data", data);
		
		return json;
	}

	public byte[] getDockingCommandPackage(boolean isDocking) {
		JSONObject json;
		try {
			json = createDockingCommand(isDocking);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
