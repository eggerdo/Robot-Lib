package robots.replicator.ctrl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.communication.video.IRawVideoListener;

import android.os.Handler;
import android.util.Log;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.ctrl.ICameraControlListener;

public class Replicator extends DifferentialRobot implements ICameraControlListener {
	
	private static final String TAG = "Replicator";

	protected ExecutorService executorSerive = Executors.newCachedThreadPool();
	
	ReplicatorController m_oController;

	public Replicator() {
		super(ReplicatorTypes.AXLE_WIDTH, ReplicatorTypes.MIN_SPEED, ReplicatorTypes.MAX_SPEED, ReplicatorTypes.MIN_SPEED, ReplicatorTypes.MAX_RADIUS);

		m_oController = new ReplicatorController();
		
		Log.w(TAG, "Created Replicator controller");
		
	}
	
	public void setHandler(Handler m_oUiHandler) {
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
		return RobotType.RBT_REPLICATOR;
	}

	@Override
	public String getAddress() {
		return m_oController.getAddress();
	}


	@Override
	public void destroy() {
		m_oController.destroy();
	}

	public void setConnection(String address, int command_port, int video_port) {
		m_oController.setConnection(address, command_port, video_port);
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
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConnected() {
		return m_oController.isConnected();
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean toggleInvertDrive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		Log.w(TAG, "Move forward");
		// TODO Auto-generated method stub

	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeCircle(double i_dblTime, double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getBaseSped() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveForward() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBackward() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateCounterClockwise() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotateClockwise() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleCamera() {
		// not available
	}

	@Override
	public void switchCameraOn() {
		executorSerive.submit(new Runnable() {
			
			@Override
			public void run() {
				m_oController.startVideo();
			}
		});
	}

	@Override
	public void switchCameraOff() {
		m_oController.stopVideo();
	}
	
	@Override
	public void cameraUp() {
	}

	@Override
	public void cameraDown() {
	}

	@Override
	public void cameraStop() {
	}

}
