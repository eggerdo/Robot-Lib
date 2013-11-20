package robots.rover.rover2.ctrl;

import java.io.IOException;

import org.dobots.communication.control.ZmqRemoteControlHelper.IControlListener;
import org.dobots.communication.video.ZmqVideoSender;

import robots.RobotType;
import robots.ctrl.ICameraControlListener;
import robots.rover.base.ctrl.RoverBase;

public class Rover2 extends RoverBase implements ICameraControlListener {

	private static final String TAG = "Rover2";
	
	private boolean bInfrared;
	private boolean bLight;
	
	private ZmqVideoSender m_oVideoSender;

	public Rover2() {
		super(Rover2Types.AXLE_WIDTH, Rover2Types.MIN_SPEED, Rover2Types.MAX_SPEED, Rover2Types.MIN_RADIUS, Rover2Types.MAX_RADIUS);

		m_oController = new Rover2Controller();
		
		m_oVideoSender = new ZmqVideoSender(getID());
		m_oController.setVideoListener(m_oVideoSender);

		m_oRemoteHelper.setCameraControlListener(this);
		m_oRemoteHelper.setControlListener(this);
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_ROVER2;
	}

	@Override
	public String getAddress() {
		return m_oController.getAddress();
	}

	private Rover2Controller getController() {
		return (Rover2Controller) m_oController;
	}

	public void toggleInfrared() {

		if (bInfrared) {
			disableInfrared();
		} else {
			enableInfrared();
		}

	}

	public void enableInfrared() {
		try {
			getController().enableInfrared();
			bInfrared = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disableInfrared() {
		try {
			getController().disableInfrared();
			bInfrared = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void toggleLight() {

		if (bLight) {
			disableLight();
		} else {
			enableLight();
		}

	}

	public void enableLight() {
		try {
			getController().ledON();
			bLight = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disableLight() {
		try {
			getController().ledOFF();
			bLight = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cameraUp() {
		try {
			getController().cameraUp();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cameraStop() {
		try {
			getController().cameraStop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cameraDown() {
		try {
			getController().cameraDown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isStreaming() {
		return getController().isStreaming();
	}

	@Override
	public boolean startVideo() {
		try {
//			Log.w(TAG, "isStreaming");
			if (!isStreaming()) {
				return getController().enableVideo();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void stopVideo() {
		try {
			if (isStreaming()) {
				getController().disableVideo();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toggleCamera() {
		// rover has only one camera
	}

	@Override
	public void switchCameraOn() {
		startVideo();
	}

	@Override
	public void switchCameraOff() {
		stopVideo();
	}
	
	public double getBatteryPower() {
		return ((Rover2Controller)m_oController).getBatteryPower();
	}

}
