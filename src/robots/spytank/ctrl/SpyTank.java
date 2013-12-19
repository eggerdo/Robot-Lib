package robots.spytank.ctrl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.video.IRawVideoListener;
import org.dobots.zmq.video.ZmqVideoSender;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.ctrl.control.ICameraControlListener;
import robots.ctrl.control.RobotDriveCommandListener;
import android.os.Handler;

public class SpyTank extends DifferentialRobot implements ISpyTank, ICameraControlListener {

	private SpyTankController m_oController;
	private ZmqVideoSender m_oVideoSender;
	private RobotDriveCommandListener m_oRemoteListener;
	private ZmqRemoteControlHelper m_oRemoteHelper;

	protected ExecutorService executorSerive = Executors.newCachedThreadPool();

	public SpyTank() {
		super(SpyTankTypes.AXLE_WIDTH, SpyTankTypes.MIN_SPEED, SpyTankTypes.MAX_SPEED, SpyTankTypes.MIN_RADIUS, SpyTankTypes.MAX_RADIUS);
		
		m_oController = new SpyTankController();
		
		m_oVideoSender = new ZmqVideoSender(getID());
		m_oController.setVideoListener(m_oVideoSender);

		m_oRemoteListener = new RobotDriveCommandListener(this);
		m_oRemoteHelper = new ZmqRemoteControlHelper(this);
		m_oRemoteHelper.setDriveControlListener(m_oRemoteListener);
		m_oRemoteHelper.setCameraControlListener(this);
		m_oRemoteHelper.startReceiver(getID());
	}

	public void setHandler(Handler m_oUiHandler) {
		super.setHandler(m_oUiHandler);
		
		m_oController.setReceiveHandler(m_oUiHandler);
	}

	public void setVideoListener(IRawVideoListener listener) {
		m_oController.setVideoListener(listener);
	}
	
	public void removeVideoListener(IRawVideoListener listener) {
		m_oController.removeVideoListener(listener);
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_SPYTANK;
	}

	@Override
	public String getAddress() {
		return m_oController.getAddress();
	}

	@Override
	public void destroy() {
		m_oRemoteHelper.destroy();
		m_oController.destroy();
	}
	
	public void setConnection(String address, int command_port, int media_port) {
		m_oController.setConnection(address, command_port, media_port);
	}

	@Override
	public void connect() {
		executorSerive.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					m_oController.connect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void disconnect() {
		try {
			m_oController.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConnected() {
		return m_oController.isConnected();
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// nothing to do
	}

	@Override
	public boolean toggleInvertDrive() {
		// not available
		return false;
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		m_oController.moveForward((int)i_dblSpeed);
	}
	
	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		// there is only one speed available, so we make a threshold
		// if the angle is > threshold, we set the velocity
		// of the left wheel to 0 and the right wheel to i_dblSpeed
		// and vice versa if it is < -threshold
		if (i_dblAngle > 30) {
			m_oController.moveForward(0, (int)i_dblSpeed);
		} else if (i_dblAngle < -30) {
			m_oController.moveForward((int)i_dblSpeed, 0);
		} else {
			m_oController.moveForward((int)i_dblSpeed);
		}
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		// there is only one speed available, so we make a threshold
		// if the radius is > 0, we set the velocity
		// of the left wheel to 0 and the right wheel to i_dblSpeed
		// and vice versa if it is < 0
		if (i_nRadius > 0) {
			m_oController.moveForward(0, (int)i_dblSpeed);
		} else if (i_nRadius < 0) {
			m_oController.moveForward((int)i_dblSpeed, 0);
		} else {
			m_oController.moveForward((int)i_dblSpeed);
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		m_oController.moveBackward((int)i_dblSpeed);
	}
	
	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		// there is only one speed available, so we make a threshold
		// if the angle is > threshold, we set the velocity
		// of the left wheel to 0 and the right wheel to i_dblSpeed
		// and vice versa if it is < -threshold
		if (i_dblAngle > 30) {
			m_oController.moveBackward(0, (int)i_dblSpeed);
		} else if (i_dblAngle < -30) {
			m_oController.moveBackward((int)i_dblSpeed, 0);
		} else {
			m_oController.moveBackward((int)i_dblSpeed);
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		// there is only one speed available, so we make a threshold
		// if the radius is over the threshold, we set the velocity
		// of the left wheel to 0 and the right wheel to 100
		if (i_nRadius > 0) {
			m_oController.moveBackward(0, (int)i_dblSpeed);
		} else if (i_nRadius < 0) {
			m_oController.moveBackward((int)i_dblSpeed, 0);
		} else {
			m_oController.moveBackward((int)i_dblSpeed);
		}
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		m_oController.rotateRight((int)i_dblSpeed);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		m_oController.rotateLeft((int)i_dblSpeed);
	}

	@Override
	public void moveStop() {
		m_oController.moveStop();
	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		// nothing to do, there is only one speed
	}

	@Override
	public double getBaseSpeed() {
		return 100.0;
	}

	@Override
	public void moveForward() {
		moveForward(100);
	}

	@Override
	public void moveBackward() {
		moveBackward(100);
	}

	@Override
	public void rotateCounterClockwise() {
		rotateCounterClockwise(100);
	}

	@Override
	public void rotateClockwise() {
		rotateClockwise(100);
	}

	@Override
	public void moveLeft() {
		// not available
	}

	@Override
	public void moveRight() {
		// not available
	}

	@Override
	public void toggleCamera() {
		// not available
	}

	@Override
	public void startVideo() {
		executorSerive.submit(new Runnable() {
			
			@Override
			public void run() {
				m_oController.startVideo();
			}
		});
	}

	@Override
	public void stopVideo() {
		m_oController.stopVideo();
	}

	@Override
	public void cameraUp() {
		try {
			m_oController.cameraUp();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void cameraDown() {
		try {
			m_oController.cameraDown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void cameraStop() {
		try {
			m_oController.cameraStop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isStreaming() {
		return m_oController.isStreaming();
	}

}
