package robots.zumo.ctrl;

import org.dobots.comm.Move;
import org.dobots.comm.msg.ISensorDataListener;
import org.dobots.comm.msg.SensorMessageObj;
import org.dobots.utilities.Utils;
import org.dobots.utilities.camera.CameraPreview;
import org.dobots.utilities.camera.CameraPreview.CameraPreviewCallback;
import org.dobots.zmq.sensors.ZmqSensorsSender;
import org.dobots.zmq.video.ZmqVideoSender;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.ctrl.control.ICameraControlListener;
import robots.ctrl.control.IMoveRepeaterListener;
import robots.ctrl.control.MoveRepeater;
import robots.ctrl.control.RobotDriveCommandListener;
import robots.ctrl.zmq.ZmqRemoteControlHelper;
import robots.gui.comm.IRobotConnection;
import robots.spykee.ctrl.SpykeeController.DockState;
import android.content.Context;
import android.util.Log;

public class Zumo extends DifferentialRobot implements IZumo, ICameraControlListener, IMoveRepeaterListener {

	private static final String TAG = "Zumo";
	
	private ZumoController m_oController;

	private double m_dblBaseSpeed = 50.0;

	private RobotDriveCommandListener m_oRemoteListener;

	private ZmqRemoteControlHelper m_oRemoteHelper;

	private CameraPreview mCamera = null;

	private ZmqVideoSender mVideoSender;
	private ZmqSensorsSender m_oSensorsSender;

	private MoveRepeater m_oRepeater;
	
	private Context mCameraContext;
	
	private boolean mMazeSolving = false;

	// inverted = -1
	// normal 	= +1
	// inverts backward/forward by multiplying the inversion
	// factor with the speed for each wheel
	private int m_nInverted = 1;

	public Zumo() {
		super(ZumoTypes.AXLE_WIDTH, ZumoTypes.MIN_VELOCITY, ZumoTypes.MAX_VELOCITY, ZumoTypes.MIN_RADIUS, ZumoTypes.MAX_RADIUS);

		m_oController = new ZumoController();
//		m_oController.setSensorListener(this);
		
		m_oRemoteListener = new RobotDriveCommandListener(this);
		m_oRemoteHelper = new ZmqRemoteControlHelper(this);
		m_oRemoteHelper.setDriveControlListener(m_oRemoteListener);
		m_oRemoteHelper.startReceiver("Zumo");

		mVideoSender = new ZmqVideoSender(getID());
		
		m_oRemoteHelper.setCameraControlListener(this);

		m_oSensorsSender = new ZmqSensorsSender();

		m_oRepeater = new MoveRepeater(this, 500);
	}

	public void setCamera(Context context) {
		mCameraContext = context;
		debug(TAG, "startCamera...");
		mCamera = CameraPreview.createCameraWithoutSurface(mCameraContext);
		mCamera.setPreviewSize(320, 240);
		mCamera.setFrameListener(new CameraPreviewCallback() {
			@Override
			public void onFrame(byte[] rgb, int width, int height, int rotation) {
				mVideoSender.onFrame(rgb, rotation);
			}
		});
	}
	
	public void setCamera(CameraPreview camera) {
		mCamera = camera;
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_ZUMO;
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
		m_oRepeater.startMove(Move.FORWARD, i_dblSpeed, true);
	}
	
	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		m_oRepeater.startMove(Move.FORWARD, i_dblSpeed, i_nRadius, true);
	}
	
	public void moveForward(double i_dblSpeed, double i_dblAngle) {

		if (Math.abs(i_dblAngle) < 2) {
			moveForward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveForward(i_dblSpeed, nRadius);
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		m_oRepeater.startMove(Move.BACKWARD, i_dblSpeed, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		m_oRepeater.startMove(Move.BACKWARD, i_dblSpeed, i_nRadius, true);
	}

	public void moveBackward(double i_dblSpeed, double i_dblAngle) {

		if (Math.abs(i_dblAngle) < 2) {
			moveBackward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveBackward(i_dblSpeed, nRadius);
		}
	}
	
	@Override
	public void rotateClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(Move.ROTATE_RIGHT, i_dblSpeed, true);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(Move.ROTATE_LEFT, i_dblSpeed, true);
	}
	
	@Override
	public void moveStop() {
		m_oRepeater.stopMove();
		m_oController.driveStop();
	}

	@Override
	public void onDoMove(Move i_eMove, double i_dblSpeed) {
		switch(i_eMove) {
		case BACKWARD:
			executeMoveBackward(i_dblSpeed);
			break;
		case FORWARD:
			executeMoveForward(i_dblSpeed);
			break;
		case ROTATE_LEFT:
			executeRotateCounterClockwise(i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			executeRotateClockwise(i_dblSpeed);
			break;
		default:
			error(TAG, "Move not available");
			return;
		}
	}

	public void executeMoveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(nVelocity * m_nInverted, nVelocity * m_nInverted);
	}

	public void executeMoveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(-nVelocity * m_nInverted, -nVelocity * m_nInverted);
	}

	public void executeRotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(nVelocity, -nVelocity);
	}

	public void executeRotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(-nVelocity, nVelocity);
	}

	@Override
	public void onDoMove(Move i_eMove, double i_dblSpeed, double i_dblRadius) {
		switch(i_eMove) {
		case BACKWARD:
			executeMoveBackward(i_dblSpeed, i_dblRadius);
			break;
		case FORWARD:
			executeMoveForward(i_dblSpeed, i_dblRadius);
			break;
		default:
			error(TAG, "Move not available");
			return;
		}
	}

	public void executeMoveForward(double i_dblSpeed, double i_dblRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_dblRadius);

		m_oController.drive(oVelocity.left * m_nInverted, oVelocity.right * m_nInverted);
	}

	public void executeMoveBackward(double i_dblSpeed, double i_dblRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_dblRadius);

		m_oController.drive(-oVelocity.left * m_nInverted, -oVelocity.right * m_nInverted);
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
		Utils.runAsyncUiTask(new Runnable() {
			@Override
			public void run() {
				mCamera.startCamera();
			}
		});
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

	@Override
	public boolean isMazeSolving() {
		return mMazeSolving;
	}
	
	public void initMazeSolver() {
		m_oController.initMazeSolver();
	}

	public void startMazeSolving() {
		m_oController.startMazeSolving();
		mMazeSolving = true;
	}

	public void stopMazeSolving() {
		m_oController.stopMazeSolving();
		mMazeSolving = false;
	}

	public void repeatMaze() {
		m_oController.repeatMaze();
	}

	public void calibrateCompass() {
		m_oController.calibrateCompass();
	}

	public void resetHeading() {
		m_oController.resetHeading();
	}

	public void turnDegrees(int angle) {
		m_oController.turnDegrees(angle);
	}


//	@Override
//	public void shootGuns() {
//		m_oController.shootGuns();
//	}

//	@Override
//	public void fireVolley() {
//		m_oController.fireVolley();
//	}

//	@Override
//	public void dock(boolean isDocking) {
//		m_oController.dock(isDocking);
//	}

//	@Override
//	public void onSensorData(String data) {
//		SensorMessageObj sensorData = SensorMessageObj.decodeJSON(getID(), data);
//		m_oSensorsSender.sendSensors(sensorData);
//	}

}
