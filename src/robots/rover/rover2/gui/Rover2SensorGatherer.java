package robots.rover.rover2.gui;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.video.IFpsListener;
import org.dobots.zmq.video.IVideoListener;
import org.dobots.zmq.video.ZmqVideoReceiver;
import org.zeromq.ZMQ;

import robots.rover.base.gui.RoverBaseSensorGatherer;
import robots.rover.rover2.ctrl.Rover2;
import android.graphics.Bitmap;
import android.widget.TextView;

public class Rover2SensorGatherer extends RoverBaseSensorGatherer  implements IFpsListener, IVideoListener {

	private ZmqVideoReceiver m_oVideoDisplayer;
	private TextView m_txtBattery;

	public Rover2SensorGatherer(BaseActivity i_oActivity, Rover2 i_oRover) {
		super(i_oActivity, i_oRover, "Rover2SensorGatherer");
		
		setProperties();
		
		startThread();
	}
	
	@Override
	protected void setProperties() {
		super.setProperties();

    	m_txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBattery);
	}
	
	@Override
	protected void execute() {
		final double battery = ((Rover2)m_oRover).getBatteryPower();
		
		Utils.runAsyncUiTask(new Runnable() {
			@Override
			public void run() {
				m_txtBattery.setText(String.format("%.0f %%", battery));
			}
		});
	}
	
	@Override
	protected void startVideo() {
		super.startVideo();
		setVideoListening(true);
	}

	@Override
	protected void stopVideo() {
		super.stopVideo();
		setVideoListening(false);
	}
	
	private void setVideoListening(boolean i_bListening) {
		if (i_bListening) {
			setupVideoDisplay();
		} else {
			if (m_oVideoDisplayer != null) {
				m_oVideoDisplayer.close();
			}
		}
	}

    private void setupVideoDisplay() {
    	
		ZMQ.Socket oVideoRecvSocket = ZmqHandler.getInstance().obtainVideoRecvSocket();
		oVideoRecvSocket.subscribe(m_oRover.getID().getBytes());

		// start a video display thread which receives video frames from the socket and displays them
		m_oVideoDisplayer = new ZmqVideoReceiver(oVideoRecvSocket);
		m_oVideoDisplayer.setVideoListener(this);
		m_oVideoDisplayer.setFPSListener(this);
		m_oVideoDisplayer.start();
	}

	@Override
	public void onFPS(final double fps) {
		Utils.runAsyncUiTask(new Runnable() {

			@Override
			public void run() {
				if (!m_bVideoStopped) {
					m_lblFPS.setText("FPS: " + String.valueOf(fps));
				}
			}
		});
	}

	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		if (!m_bVideoStopped) {
			if (!m_bVideoConnected) {
				m_oSensorDataUiUpdater.removeCallbacks(m_oTimeoutRunnable);
				m_bVideoConnected = true;
				showVideoLoading(false);
			}

			m_ivVideo.setImageBitmap(bmp);
		}
	}
    
	@Override
	public void shutDown() {
		if (m_oVideoDisplayer != null) {
			m_oVideoDisplayer.close();
		}
	}

}
