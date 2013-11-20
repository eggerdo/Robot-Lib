package robots.rover.base.gui;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

public class RoverBaseMessenger {
	
	private static final int SET_CONNECTION = 3000;
	
	public void setConnection(String address, int port) {
		
//		Message msgPort = Message.obtain(null, SET_CONNECTION);
//		msgPort.replyTo = ;
//		Bundle bundlePort = new Bundle();
//		bundlePort.putString("module", MODULE_NAME);
//		bundlePort.putInt("id", 0); // TODO: adjustable id, multiple modules
//		bundlePort.putString("port", "Bmp");
//		msgPort.setData(bundlePort);
//		msgSend(mToMsgService, msgPort);
	}

}
