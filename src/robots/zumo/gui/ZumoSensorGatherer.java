package robots.zumo.gui;

import org.dobots.R;
import org.dobots.comm.msg.ISensorDataListener;
import org.dobots.comm.msg.SensorMessageObj;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.sensors.ZmqSensorsReceiver;
import org.dobots.zmq.video.gui.VideoSurfaceView.DisplayMode;
import org.zeromq.ZMQ.Socket;

import android.media.AudioManager;
import android.media.SoundPool;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import robots.gui.VideoSensorGatherer;
import robots.zumo.ctrl.IZumo;

public class ZumoSensorGatherer extends VideoSensorGatherer {
	
	private ViewGroup m_layCameraContainer;
	private ImageButton m_btnCameraToggle;

//	private ZmqSensorsReceiver m_oSensorsReceiver;
//	private Socket m_oSensorsRecvSocket;
	
//	private SoundPool mSoundPool;
//	private int mHitID;
//	private int mHitsDetected = 0;
//	private int mShotsFired;
	
//	private TextView txtShotsFired;
//	private TextView txtHitsDetected;
	
//	private IZumo mZumo;
	
	public ZumoSensorGatherer(BaseActivity i_oActivity, IZumo i_oZumo) {
		super(i_oActivity, i_oZumo, "ZumoSensorGatherer");
		m_layCameraContainer = (ViewGroup)m_oActivity.findViewById(R.id.layCameraContainer);
		mVideoHelper.setDisplayMode(DisplayMode.FILLED);

		m_btnCameraToggle = (ImageButton) m_oActivity.findViewById(R.id.btnCameraToggle);
		m_btnCameraToggle.setVisibility(View.GONE);
		
//		m_oSensorsRecvSocket = ZmqHandler.getInstance().obtainSensorsRecvSocket();
//		m_oSensorsRecvSocket.subscribe(mRobot.getID().getBytes());
//		m_oSensorsReceiver = new ZmqSensorsReceiver(m_oSensorsRecvSocket, "ArduinoSensorsReceiver");
//		m_oSensorsReceiver.setSensorDataListener(this);
//		m_oSensorsReceiver.start();

//        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
//        mHitID = mSoundPool.load(m_oActivity, R.raw.explosion, 1);
        
//        txtShotsFired = (TextView) m_oActivity.findViewById(R.id.txtShotsFired);
//        txtHitsDetected = (TextView) m_oActivity.findViewById(R.id.txtHitsDetected);
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

//	@Override
//	public void onSensorData(String data) {
//		SensorMessageObj sensorData = SensorMessageObj.decodeJSON(mRobot.getID(), data);
//		switch(ZumoEncoder.getType(sensorData)) {
//		case ZumoEncoder.HIT_DETECTED:
//			onHitDetected();
//		}
//	}
	
//	public void onHitDetected() {
//		mHitsDetected ++;
//		updateHits();
//		
//		mSoundPool.play(mHitID, 1, 1, 1, 0, 1f);
//	}
	
//	private void updateHits() {
//		Utils.runAsyncUiTask(new Runnable() {
//			@Override
//			public void run() {
//				txtHitsDetected.setText("" + mHitsDetected);
//			}
//		});
//	}
	
//	public void onShotFired(int num) {
//		mShotsFired += num;
//		updateShots();
//	}
	
//	private void updateShots() {
//		Utils.runAsyncUiTask(new Runnable() {
//			@Override
//			public void run() {
//				txtShotsFired.setText("" + mShotsFired);
//			}
//		});
//	}

//	public void resetStats() {
//		mShotsFired = 0;
//		mHitsDetected = 0;
//		updateHits();
//		updateShots();
//	}

}
