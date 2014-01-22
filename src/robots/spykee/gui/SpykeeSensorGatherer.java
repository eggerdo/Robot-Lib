package robots.spykee.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.video.gui.VideoSurfaceView.DisplayMode;

import robots.gui.VideoSensorGatherer;
import robots.spykee.ctrl.ISpykee;
import robots.spykee.ctrl.SpykeeController.DockState;
import robots.spykee.ctrl.SpykeeMessageTypes;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class SpykeeSensorGatherer extends VideoSensorGatherer {
	
//	private Spykee m_oSpykee;

	private TextView m_txtBattery;
	private TextView m_txtDockingState;
	
//	protected VideoHelper mVideoHelper;

	public SpykeeSensorGatherer(BaseActivity i_oActivity, ISpykee i_oSpykee) {
		super(i_oActivity, i_oSpykee, "SpykeeSensorGatherer");
//		m_oSpykee = i_oSpykee;
		
		mVideoHelper.setDisplayMode(DisplayMode.SCALED_WIDTH);
		
		setProperties();

		start();
	}
	
	public void setProperties() {
		m_txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBattery);
		m_txtDockingState = (TextView) m_oActivity.findViewById(R.id.txtDockingState);
	}
	
	public void resetLayout() {
		super.resetLayout();
		
		m_txtBattery.setText("-");
		m_txtDockingState.setText("-");
	}
	
	public void dispatchMessage(Message msg) {
		m_oSensorDataUiUpdater.dispatchMessage(msg);
	}

	final Handler m_oSensorDataUiUpdater = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SpykeeMessageTypes.BATTERY_LEVEL_RECEIVED:
				updateBatteryLevel(msg.arg1);
				break;
			case SpykeeMessageTypes.DOCKINGSTATE_RECEIVED:
				updateDockingState((DockState)msg.obj);
				break;
//			case SpykeeMessageTypes.VIDEO_FRAME_RECEIVED:
//				updateVideo((Bitmap)msg.obj);
//				break;
//			case SpykeeMessageTypes.AUDIO_RECEIVED:
//				if (mMediaPlayer == null) {
//					return;
//				}
//				sNumAudioBuffers += 1;
//				if (sNumAudioBuffers >= DROP_AUDIO_THRESHOLD) {
//					mNumSkips += 1;
//					sNumAudioBuffers -= 1;
//					mPlayingAudioNum += 1;
//					if (mPlayingAudioNum >= MAX_AUDIO_BUFFERS) {
//						mPlayingAudioNum = 0;
//					}
//					Log.d(TAG, "audio skips: " + mNumSkips + " waits: " + mNumWaits);
//				}
//				if (!mMediaPlayer.isPlaying() && sNumAudioBuffers == 1) {
//	    			playNextAudioFile();
//				}
			}
		}
	};

	private void updateBatteryLevel(int i_nBattery) {
		m_txtBattery.setText(String.format("%d", i_nBattery));
	}
	
	private void updateDockingState(DockState i_eDockingState) {
		switch (i_eDockingState) {
		case DOCKED:
			m_txtDockingState.setText("Docked");
			break;
		case UNDOCKED:
			m_txtDockingState.setText("Undocked");
			break;
		case DOCKING:
			m_txtDockingState.setText("Docking");
			break;
		}
	}
	
	private static final String PREFS_SCALEVIDEO = "scale_video";
	private static final boolean DEF_SCALEVIDEO = true;

	public void loadSettings(SharedPreferences prefs, String suffix) {
		boolean scaleVideo = prefs.getBoolean(String.format("%s_%s", suffix, PREFS_SCALEVIDEO), DEF_SCALEVIDEO);
		setVideoScaled(scaleVideo);
	}

	public void saveSettings(Editor editor, String suffix) {
		editor.putBoolean(String.format("%s_%s", suffix, PREFS_SCALEVIDEO), isVideoScaled());
	}
	
}
