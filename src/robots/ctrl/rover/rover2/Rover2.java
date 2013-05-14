package robots.ctrl.rover.rover2;

import java.io.IOException;

import org.dobots.robotalk.msg.RawVideoMessage;
import org.dobots.robotalk.msg.RobotVideoMessage;
import org.dobots.robotalk.video.ZMQVideoSender;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import robots.IVideoListener;
import robots.RobotType;
import robots.ctrl.rover.RoverBase;

public class Rover2 extends RoverBase {

	private static final String TAG = "Rover2";
	
	private boolean bInfrared;
	private boolean bLight;
	
	public Rover2() {
		super(Rover2Types.AXLE_WIDTH, Rover2Types.MIN_SPEED, Rover2Types.MAX_SPEED, Rover2Types.MIN_RADIUS, Rover2Types.MAX_RADIUS);

		m_oController = new Rover2Controller();
		
//		m_oVideoSender = new ZMQVideoSender(getID());
//		m_oController.setVideoListener(m_oVideoSender);
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_ROVER2;
	}

	@Override
	public String getAddress() {
		return null;
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
			return getController().enableVideo();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void stopVideo() {
		try {
			getController().disableVideo();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
