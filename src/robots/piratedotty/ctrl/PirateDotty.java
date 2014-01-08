package robots.piratedotty.ctrl;

import org.dobots.utilities.Utils;
import org.dobots.utilities.camera.CameraPreview;
import org.dobots.utilities.camera.CameraPreview.CameraPreviewCallback;
import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.video.ZmqVideoSender;

import android.content.Context;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.ctrl.control.ICameraControlListener;
import robots.ctrl.control.RobotDriveCommandListener;
import robots.gui.comm.IRobotConnection;

public class PirateDotty extends DifferentialRobot implements IPirateDotty, ICameraControlListener {

	private static final String TAG = "PirateDotty";
	
	private PirateDottyController m_oController;

	private double m_dblBaseSpeed = 100.0;

	private RobotDriveCommandListener m_oRemoteListener;

	private ZmqRemoteControlHelper m_oRemoteHelper;

	private CameraPreview mCamera;

	private ZmqVideoSender mVideoSender;

	// inverted = -1
	// normal 	= +1
	// inverts backward/forward by multiplying the inversion
	// factor with the speed for each wheel
	private int m_nInverted = 1;

	public PirateDotty() {
		super(PirateDottyTypes.AXLE_WIDTH, PirateDottyTypes.MIN_VELOCITY, PirateDottyTypes.MAX_VELOCITY, PirateDottyTypes.MIN_RADIUS, PirateDottyTypes.MAX_RADIUS);

		m_oController = new PirateDottyController();
		
		m_oRemoteListener = new RobotDriveCommandListener(this);
		m_oRemoteHelper = new ZmqRemoteControlHelper(this);
		m_oRemoteHelper.setDriveControlListener(m_oRemoteListener);
		m_oRemoteHelper.startReceiver("PirateDotty");

		mVideoSender = new ZmqVideoSender(getID());
		
		m_oRemoteHelper.setCameraControlListener(this);
	}

	public void startCamera(Context context) {
		debug(TAG, "startCamera...");
		mCamera = CameraPreview.createCameraWithoutSurface(context);
		mCamera.setPreviewSize(320, 240);
		mCamera.setFrameListener(new CameraPreviewCallback() {
			@Override
			public void onFrame(byte[] rgb, int width, int height, int rotation) {
				mVideoSender.onFrame(rgb, rotation);
			}
		});
	}

	public void startCamera(CameraPreview camera) {
		mCamera = camera;
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_PIRATEDOTTY;
	}

	@Override
	public String getAddress() {
		if (m_oController.getConnection() != null) {
			return m_oController.getConnection().getAddress();
		} else {
			return "";
		}
	}

	@Override
	public void destroy() {
		if (mCamera != null) {
			mCamera.destroy();
			mCamera = null;
		}

		m_oRemoteHelper.destroy();
		if (isConnected()) {
			disconnect();
		}
	}

	public void setConnection(IRobotConnection i_oConnection) {
		debug(TAG, "setConnection(%s)", i_oConnection.getAddress());
		m_oController.setConnection(i_oConnection);
	}
	
	public IRobotConnection getConnection() {
		return m_oController.getConnection();
	}

	@Override
	public void connect() {
		debug(TAG, "connect...");
		m_oController.connect();
	}

	@Override
	public void disconnect() {
		debug(TAG, "disconnect...");
		if (m_oController.isConnected()) {
			m_oController.disconnect();
		}
	}

	@Override
	public boolean isConnected() {
		return m_oController.isConnected();
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		if (isConnected()) {
			m_oController.control(i_bEnable);
		}
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(nVelocity * m_nInverted, nVelocity * m_nInverted);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		m_oController.drive(oVelocity.left * m_nInverted, oVelocity.right * m_nInverted);
	}
	
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveForward(i_dblSpeed, nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(-nVelocity * m_nInverted, -nVelocity * m_nInverted);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		m_oController.drive(-oVelocity.left * m_nInverted, -oVelocity.right * m_nInverted);
	}
	
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveBackward(i_dblSpeed, nRadius);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(nVelocity, -nVelocity);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(-nVelocity, nVelocity);
	}

	@Override
	public void moveStop() {
		m_oController.driveStop();
	}
	
	public void requestSensorData() {
//		m_oController.requestSensorData();
	}
	
	public void startStreaming(int i_nInterval) {
//		m_oController.startStreaming(i_nInterval);
	}
	
	public void stopStreaming() {
//		m_oController.stopStreaming();
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
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSpeed() {
		return m_dblBaseSpeed;
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
	public boolean toggleInvertDrive() {
		m_nInverted *= -1;
		return true;
	}

	@Override
	public boolean isStreaming() {
		return mCamera.isStopped();
	}

	@Override
	public void startVideo() {
		debug(TAG, "startVideo...");
		mCamera.startCamera();
	}

	@Override
	public void stopVideo() {
		debug(TAG, "stopVideo...");
		mCamera.stopCamera();
	}

	@Override
	public void toggleCamera() {
		debug(TAG, "toggleCamera...");
		// toggle camera only works if it is executed by the UI thread
		// so we check if the calling thread is the main thread, otherwise
		// we call the function again inside the main thread.
		Utils.runAsyncUiTask(new Runnable() {
			@Override
			public void run() {
				mCamera.toggleCamera();
			}
		});
	}

	@Override
	public void cameraUp() {
		// NOT APPLICABLE
	}

	@Override
	public void cameraDown() {
		// NOT APPLICABLE
	}

	@Override
	public void cameraStop() {
		// NOT APPLICABLE
	}

}
