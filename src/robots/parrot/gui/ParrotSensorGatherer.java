package robots.parrot.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;
import org.dobots.zmq.video.IRawVideoListener;
import org.dobots.zmq.video.IVideoListener;
import org.dobots.zmq.video.gui.VideoSurfaceView.DisplayMode;

import robots.gui.VideoSensorGatherer;
import robots.parrot.ctrl.IParrot;
import robots.parrot.ctrl.ParrotTypes;
import robots.parrot.ctrl.ParrotVideoProcessor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

/*
 * See ParrotVideoRenderer for explanation why ZMQ is not used to display the video feed from the parrot
 */
public class ParrotSensorGatherer extends VideoSensorGatherer implements NavDataListener {

	protected IParrot m_oParrot;

	protected Handler mHandler = new Handler(Looper.getMainLooper());

	private boolean m_bSensorsEnabled = false;
	
	private TextView m_txtControlState;
	private TextView m_txtBattery;
	private TextView m_txtAltitude;
	private TextView m_txtPitch;
	private TextView m_txtRoll;
	private TextView m_txtYaw;
	private TextView m_txtVX;
	private TextView m_txtVY;
	private TextView m_txtVZ;

	private LinearLayout m_laySensors;

	protected ScalableImageView m_ivVideo;
	
	public ParrotSensorGatherer(BaseActivity i_oActivity, IParrot i_oParrot) {
		super(i_oActivity, i_oParrot, "ParrotSensorGatherer");
		m_oParrot = i_oParrot;
		
		setProperties();
		
		initialize();
	}
	
	public void setProperties() {
		m_txtControlState = (TextView) m_oActivity.findViewById(R.id.txtControlState);
		m_txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBattery);
		m_txtAltitude = (TextView) m_oActivity.findViewById(R.id.txtAltitude);
		m_txtPitch = (TextView) m_oActivity.findViewById(R.id.txtPitch);
		m_txtRoll = (TextView) m_oActivity.findViewById(R.id.txtRoll);
		m_txtYaw = (TextView) m_oActivity.findViewById(R.id.txtYaw);
		m_txtVX = (TextView) m_oActivity.findViewById(R.id.txtVX);
		m_txtVY = (TextView) m_oActivity.findViewById(R.id.txtVY);
		m_txtVZ = (TextView) m_oActivity.findViewById(R.id.txtVZ);
		
		m_laySensors = (LinearLayout) m_oActivity.findViewById(R.id.laySensors);

        m_ivVideo = (ScalableImageView) m_oActivity.findViewById(R.id.ivParrot_Video);
        m_ivVideo.setMaxWidth(ParrotTypes.VIDEO_WIDTH);
	}

	public void initialize() {
		m_bSensorsEnabled = false;
	}
	
	@Override
	public void navDataReceived(final NavData nd) {
		if (m_bSensorsEnabled) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					setText(m_txtControlState, nd.getControlState().toString());
					setText(m_txtBattery, nd.getBattery());
					setText(m_txtAltitude, nd.getAltitude());
					setText(m_txtPitch, nd.getPitch());
					setText(m_txtRoll, nd.getRoll());
					setText(m_txtYaw, nd.getYaw());
					setText(m_txtVX, nd.getVx());
					setText(m_txtVY, nd.getLongitude());
					setText(m_txtVZ, nd.getVz());
				}
			});
		}
	}

	public void enableSensors(boolean i_bEnabled) {
		m_bSensorsEnabled = i_bEnabled;
		Utils.showView(m_laySensors, i_bEnabled);
	}
	
	// call when robot connected
	public void onConnect() {
		super.onConnect();
		m_oParrot.setNavDataListener(this);
	}

	// call when robot disconnected
	public void onDisconnect() {
		super.onDisconnect();
		m_oParrot.removeNavDataListener(this);
	}

}
