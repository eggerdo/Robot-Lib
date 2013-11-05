package robots.romo.gui;

import org.dobots.R;
import org.dobots.communication.msg.VideoMessage;
//import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.CameraPreview.CameraPreviewCallback;
import org.dobots.utilities.Utils;
//import org.zeromq.ZMQ;
//import org.zeromq.ZMsg;

import robots.gui.RobotInventory;
import robots.gui.SensorGatherer;
import robots.romo.ctrl.Romo;
import android.widget.TextView;

public class RomoSensorGatherer extends SensorGatherer implements CameraPreviewCallback {

//	private ZMQ.Socket m_oVideoSocket;

	private TextView m_lblFPS;

	private byte[] robotID;

    private int m_nFpsCounterPartner = 0;
    private long m_lLastTimePartner = System.currentTimeMillis();

    private boolean m_bDebug = true;
    
    private Romo mRomo = null;
    
	public RomoSensorGatherer(BaseActivity i_oActivity, String i_strRobotID) {
		super(i_oActivity, i_strRobotID + "-SensorGatherer");
		robotID = i_strRobotID.getBytes();
		mRomo = (Romo) RobotInventory.getInstance().getRobot(i_strRobotID);

//		m_oVideoSocket = ZmqHandler.getInstance().obtainVideoSendSocket();
		
		m_lblFPS = (TextView) i_oActivity.findViewById(R.id.lblFPS);

		start();
	}
	
	@Override
	public void onFrame(byte[] rgb, int width, int height, int rotation) {
		
		mRomo.onFrame(rgb, rotation);
		
//		VideoMessage oMsg = new VideoMessage(robotID, rgb, rotation);
//		
//		ZMsg zmsg = oMsg.toZmsg();
//		zmsg.send(m_oVideoSocket);
		
		if (m_bDebug) {
            ++m_nFpsCounterPartner;
            long now = System.currentTimeMillis();
            if ((now - m_lLastTimePartner) >= 1000)
            {
            	final int nFPS = m_nFpsCounterPartner;
				Utils.runAsyncUiTask(new Runnable() {
					
					@Override
					public void run() {
						m_lblFPS.setText("FPS: " + String.valueOf(nFPS));
					}
				});
	            
                m_lLastTimePartner = now;
                m_nFpsCounterPartner = 0;
            }
        }
	}

	@Override
	public void shutDown() {
//		m_oVideoSocket.close();
	}

}
