package robots.zumo.ctrl;

import java.nio.ByteBuffer;

import org.dobots.comm.ByteEncoder;
import org.dobots.comm.protocol.BaseMessage;

public class ZumoEncoder extends ByteEncoder {

	public  byte[] getTurnDegreespackage(int angle) {
		
		BaseMessage msg = new BaseMessage(ZumoTypes.TURN_DEG, 2);

		ByteBuffer buffer = assembleBaseMessage(msg);

		buffer.putShort((short)angle);
		
		return buffer.array();
	}
	
	
	
//	public JSONObject createSimpleCommand(int message_id) throws JSONException {
//		return createJsonBase(message_id);
//	}
//
//	public byte[] getSimpleCommandPackage(int message_id) {
//		JSONObject json;
//		try {
//			json = createSimpleCommand(message_id);
//			return json.toString().getBytes();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public JSONObject createDockingCommand(boolean isDocking) throws JSONException {
//		JSONObject json = createJsonBase(DOCK);
//
//		JSONObject data = new JSONObject();
//		data.put("enable", isDocking);
//		json.put("data", data);
//		
//		return json;
//	}
//
//	public byte[] getDockingCommandPackage(boolean isDocking) {
//		JSONObject json;
//		try {
//			json = createDockingCommand(isDocking);
//			return json.toString().getBytes();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
}
