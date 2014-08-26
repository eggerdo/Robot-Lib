package robots.piratedotty.gui;

import org.dobots.R;
import org.dobots.comm.msg.ISensorDataListener;
import org.dobots.comm.msg.SensorMessageObj;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.sensors.ZmqSensorsReceiver;
import org.dobots.zmq.video.gui.VideoSurfaceView.DisplayMode;
import org.zeromq.ZMQ.Socket;

import android.media.AudioManager;
import android.media.SoundPool;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import robots.gui.VideoSensorGatherer;
import robots.piratedotty.ctrl.IPirateDotty;
import robots.piratedotty.ctrl.PirateDottyTypes;

public class PirateDottySensorGatherer extends VideoSensorGatherer implements ISensorDataListener {
	
	private ViewGroup m_layCameraContainer;
	private ImageButton m_btnCameraToggle;
	private PirateDottySail m_vSail;

	private ZmqSensorsReceiver m_oSensorsReceiver;
	private Socket m_oSensorsRecvSocket;
	
	private SoundPool mSoundPool;
	private int mHitID;
	
//	private IPirateDotty mPirateDotty;
	
	public PirateDottySensorGatherer(BaseActivity i_oActivity, IPirateDotty i_oPirateDotty) {
		super(i_oActivity, i_oPirateDotty, "PirateDottySensorGatherer");
		m_layCameraContainer = (ViewGroup)m_oActivity.findViewById(R.id.layCameraContainer);
		mVideoHelper.setDisplayMode(DisplayMode.FILLED);

		m_btnCameraToggle = (ImageButton) m_oActivity.findViewById(R.id.btnCameraToggle);
		m_btnCameraToggle.setVisibility(View.GONE);
		
		m_vSail = (PirateDottySail) m_oActivity.findViewById(R.id.vSail);
//		m_vSail.setVisibility(View.VISIBLE);
		m_vSail.setVisibility(View.GONE);
		
		m_oSensorsRecvSocket = ZmqHandler.getInstance().obtainSensorsRecvSocket();
		m_oSensorsRecvSocket.subscribe(mRobot.getID().getBytes());
		m_oSensorsReceiver = new ZmqSensorsReceiver(m_oSensorsRecvSocket, "ArduinoSensorsReceiver");
		m_oSensorsReceiver.setSensorDataListener(this);
		m_oSensorsReceiver.start();

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mHitID = mSoundPool.load(m_oActivity, R.raw.explosion, 1);
	}
	
	@Override
	public void startVideoPlayback() {
		m_layCameraContainer.setVisibility(View.VISIBLE);
		m_btnCameraToggle.setVisibility(View.VISIBLE);
		
//		m_vSail.setVisibility(View.GONE);
		
		super.startVideoPlayback();
	}
	
	@Override
	public void stopVideoPlayback() {
		super.stopVideoPlayback();
		m_layCameraContainer.setVisibility(View.GONE);
		m_btnCameraToggle.setVisibility(View.GONE);
		
//		m_vSail.setVisibility(View.VISIBLE);
	}

	@Override
	public void onSensorData(String data) {
		SensorMessageObj sensorData = SensorMessageObj.decodeJSON(mRobot.getID(), data);
		switch(PirateDottyTypes.getType(sensorData)) {
		case PirateDottyTypes.HIT_DETECTED:
			mSoundPool.play(mHitID, 1, 1, 1, 0, 1f);
		}
	}

}
