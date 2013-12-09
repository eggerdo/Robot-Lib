package robots.replicator.ctrl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.video.IRawVideoListener;
import org.dobots.zmq.video.ZmqVideoSender;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.ctrl.ICameraControlListener;
import robots.gui.RobotDriveCommandListener;
import android.os.Handler;
import android.util.Log;

public class Replicator extends DifferentialRobot implements ICameraControlListener {
	
	private static final String TAG = "Replicator";

	protected ExecutorService executorSerive = Executors.newCachedThreadPool();
	
	ReplicatorController m_oController;
	
	private double m_dblBaseSpeed = 50;

	private RobotDriveCommandListener m_oDriveCommandListener;
	private ZmqRemoteControlHelper m_oRemoteCtrl;

	private ZmqVideoSender m_oVideoSender;

	public Replicator() {
		super(ReplicatorTypes.AXLE_WIDTH, ReplicatorTypes.MIN_SPEED, ReplicatorTypes.MAX_SPEED, ReplicatorTypes.MIN_SPEED, ReplicatorTypes.MAX_RADIUS);

		m_oController = new ReplicatorController();

		m_oDriveCommandListener = new RobotDriveCommandListener(this);
		m_oRemoteCtrl = new ZmqRemoteControlHelper();
		m_oRemoteCtrl.setDriveControlListener(m_oDriveCommandListener);
		m_oRemoteCtrl.startReceiver("Replicator");

		m_oVideoSender = new ZmqVideoSender(getID());
		m_oController.setVideoListener(m_oVideoSender);
		
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
	
	public boolean isStreaming() {
		return m_oController.isStreaming();
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
		Log.i(TAG, String.format("Move forward (%.3f)", i_dblSpeed));
		m_oController.drive((int)i_dblSpeed, 1000);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		Log.i(TAG, String.format("Move forward (%.3f, %d)", i_dblSpeed, i_nRadius));
		m_oController.drive((int)i_dblSpeed, i_nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		Log.i(TAG, String.format("Move backward (%.3f)", i_dblSpeed));
		m_oController.drive(-(int)i_dblSpeed, 1000);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		Log.i(TAG, String.format("Move backward (%.3f, %d)", i_dblSpeed, i_nRadius));
		m_oController.drive(-(int)i_dblSpeed, i_nRadius);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		Log.i(TAG, String.format("Move rotate clock (%.3f)", i_dblSpeed));
		m_oController.drive((int)i_dblSpeed, -1);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		Log.i(TAG, String.format("Move rotate counter clock (%.3f)", i_dblSpeed));
		m_oController.drive((int)i_dblSpeed, 1);
	}

	@Override
	public void moveStop() {
		Log.i(TAG, "Move stop");
		m_oController.drive(0, 0);
	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSpeed() {
		// TODO Auto-generated method stub
		return m_dblBaseSpeed;
	}

	@Override
	public void moveForward() {
		moveForward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
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
	}

	@Override
	public void cameraDown() {
	}

	@Override
	public void cameraStop() {
	}

}
