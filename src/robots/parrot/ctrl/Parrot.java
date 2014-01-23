package robots.parrot.ctrl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

import org.dobots.comm.Move;
import org.dobots.utilities.Utils;
import org.dobots.zmq.ZmqRemoteControlHelper;
import org.dobots.zmq.video.ZmqVideoSender;

import robots.ctrl.BaseRobot;
import robots.ctrl.control.ICameraControlListener;
import robots.ctrl.control.IMoveRepeaterListener;
import robots.ctrl.control.MoveRepeater;
import robots.ctrl.control.RobotDriveCommandListener;
import robots.gui.MessageTypes;
import robots.gui.comm.IConnectListener;
import android.os.SystemClock;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavData.CtrlState;
import com.codeminders.ardrone.NavData.FlyingState;
import com.codeminders.ardrone.NavDataListener;

public abstract class Parrot extends BaseRobot implements IParrot, DroneStatusChangeListener, NavDataListener, IConnectListener, IMoveRepeaterListener, ICameraControlListener {

	private static String TAG = Parrot.class.getSimpleName();

	protected ARDrone m_oController;

	protected String m_strAddress;
	private int m_nCommandPort = 5556;
	private int m_nNavDataPort = 5554;
	private int m_nMediaPort = 5555;
	
	private boolean m_bConnected = false;

	private double m_dblBaseSpeed = 40.0;

	private VideoChannel m_eVideoChannel = ARDrone.VideoChannel.HORIZONTAL_ONLY;
	
	private Parrot m_oInstance;
	
	private FlyingState flyingState;
	private CtrlState controlState;
	private Object state_mutex = new Object();
	
	private MoveRepeater m_oRepeater;
	
	private boolean m_bStreaming = true;

	private RobotDriveCommandListener m_oRemoteListener;

	private ZmqRemoteControlHelper m_oRemoteHelper;

	protected ZmqVideoSender m_oVideoSender;

	public Parrot() {
		m_oInstance = this;
		
		m_oRepeater = new MoveRepeater(this, 100);

		m_oRemoteListener = new ParrotDriveCommandListener(this);
		m_oRemoteHelper = new ZmqRemoteControlHelper(this);
		m_oRemoteHelper.setDriveControlListener(m_oRemoteListener);
		m_oRemoteHelper.setCameraControlListener(this);
		m_oRemoteHelper.startReceiver(getID());

		m_oVideoSender = new ZmqVideoSender(getID());
	}

	@Override
	public String getAddress() {
		return ParrotTypes.PARROT_IP;
	}

	@Override
	public void destroy() {
		m_oRepeater.destroy();
		if (isConnected()) {
			disconnect();
		}
	}
	
	private class DroneStarter implements Runnable {

		@Override
		public void run() {
			try {
				debug(TAG, "connecting...");
				m_oController = new ARDrone(InetAddress.getByName(m_strAddress), m_nCommandPort, m_nNavDataPort, m_nMediaPort, 10000, 60000);
//				m_oController = new ARDrone(InetAddress.getByName(m_strAddress), 10000, 60000);
				m_oController.connect();
//				m_oController.connectVideoArDrone1();
				m_oController.clearEmergencySignal();
				m_oController.waitForReady(ParrotTypes.CONNECTION_TIMEOUT);
				m_oController.playLED(1, 10, 4);
				m_oController.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
				m_oController.setCombinedYawMode(true);
				m_oController.addNavDataListener(m_oInstance);
				
				m_bConnected = true;
				Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
				
				if (m_bStreaming) {
					startVideo();
				}
				
				return;
			} catch (Exception e) {
				try {
					e.printStackTrace();
					error(TAG, "... failed");
					m_oController.clearEmergencySignal();
					m_oController.clearImageListeners();
					m_oController.clearNavDataListeners();
					m_oController.clearStatusChangeListeners();
					m_oController.disconnect();
				} catch (Exception e1) {
				}
	
			}
			m_bConnected = false;
			Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
		}
		
	}
	
	@Override
	public void connect() {
		new Thread(new DroneStarter()).start();
	}

	@Override
	public void setConnection(String address) {
		m_strAddress = address;
	}

	public void setVideoListener(DroneVideoListener i_oListener) {
		m_oController.addImageListener(i_oListener); 
	}

	public void removeVideoListener(DroneVideoListener i_oListener) {
		m_oController.removeImageListener(i_oListener);
	}

	public void setNavDataListener(NavDataListener i_oListener) {
		m_oController.addNavDataListener(i_oListener);
	}

	public void removeNavDataListener(NavDataListener i_oListener) {
		m_oController.removeNavDataListener(i_oListener);
	}

	@Override
	public void navDataReceived(NavData nd) {
		synchronized(state_mutex) {
			flyingState = nd.getFlyingState();
			controlState = nd.getControlState();
			state_mutex.notifyAll();
		}
	}
	
	public void waitForState(FlyingState i_oState, long i_lTimeout) throws TimeoutException {

        long since = System.currentTimeMillis();
        synchronized(state_mutex)
        {
            while(true)
            {
            	if (flyingState == i_oState) {
            		return; // OK, state reached
            	} else if ((System.currentTimeMillis() - since) >= i_lTimeout) {
            		// timeout
            		throw new TimeoutException();
            	}
            	
                long p = Math.min(i_lTimeout - (System.currentTimeMillis() - since), i_lTimeout);
                if(p > 0)
                {
                    try
                    {
                        state_mutex.wait(p);
                    } catch(InterruptedException e)
                    {
                        // Ignore
                    }
                }
            }
        }
	}
	

	@Override
	public void disconnect() {
		try {
			stopVideo();
			
			if (m_oController != null) {
				m_oController.disconnect();
			}
			m_oController = null;
			m_bConnected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConnected() {
		return m_bConnected;
	}

	public void sendEmergencySignal() {
		try {
			m_oController.sendEmergencySignal();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setVideoChannel(VideoChannel i_oChannel) {
		try {
			m_oController.selectVideoChannel(i_oChannel);
			m_eVideoChannel = i_oChannel;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void switchCamera() {
		debug(TAG, "switchCamera()");
		
		switch (m_eVideoChannel) {
		case HORIZONTAL_ONLY:
			setVideoChannel(VideoChannel.VERTICAL_ONLY);
			break;
		default:
			setVideoChannel(VideoChannel.HORIZONTAL_ONLY);
			break;
		}
	}

	public VideoChannel getVidoeChannel() {
		return m_eVideoChannel;
	}

	// Take Off -----------------------------------------------------------
	
	public void takeOff() {
		try {
			debug(TAG, "takeOff()");
			
			m_oRepeater.stopMove();
			
			synchronized (m_oRepeater.getMutex()) {
				
				m_oController.clearEmergencySignal();
				m_oController.trim();
				m_oController.takeOff();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	// Land --------------------------------------------------------------

	public void land() {
		try {
			debug(TAG, "land()");
			
			m_oRepeater.stopMove();
			
			synchronized (m_oRepeater.getMutex()) {
				
				m_oController.land();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	// Altitude Control ------------------------------------------------

	private AltitudeControl ctrl;

	public void stopAltitudeControl() {
		debug(TAG, "stopAltitudeControl()");
		
		ctrl.bRun = false;
		hover();
	}

	public void setAltitude(double i_dblSetpoint) {
		debug(TAG, "setAltitude(%3f)", i_dblSetpoint);
		
		m_oRepeater.stopMove();
		
		ctrl = new AltitudeControl(i_dblSetpoint);
		ctrl.start();
	}

	public double Kp = 0.1, Kd = 0, Ki = 0;

	class AltitudeControl extends Thread implements NavDataListener {

		private boolean bSetpointReached = false;

		boolean bNavDataReceived = false;
		private NavData oNavData;

		double dblSetpoint;
		// static final double Kp = 1, Kd = 0, Ki = 0;
		double dblLastError;
		long lLastTime;
		double dblIntegratedError;
		double dblSpeed;

		int count = 0;

		public boolean bRun = true;

		public AltitudeControl(double i_dblAltitudeSetpoint) {
			dblSetpoint = i_dblAltitudeSetpoint;
			initPIDControl();
		}

		@Override
		public void run() {
			m_oController.addNavDataListener(this);

			while (bRun && !bSetpointReached) {
				if (bNavDataReceived) {
					double dblError = dblSetpoint - oNavData.getAltitude();

					if (Math.abs(dblError) <= 0.01) {
						if (++count == 5) {
							debug(TAG, "Setpoint Reached");
							bSetpointReached = true;
							hover();
						}
					} else {
						count = 0;

						dblSpeed = pidControl(dblError);
						debug(TAG, String.format(
								"Altitude: %f, Error:%f, Speed: %f",
								oNavData.getAltitude(), dblError, dblSpeed));
						if ((dblSpeed > 0) && (dblSpeed <= 100)) {
							executeMoveUp(dblSpeed);
						} else if ((dblSpeed < 0) && (dblSpeed >= -100)) {
							executeMoveDown(-dblSpeed);
						} else {
							debug(TAG, "Fatal Error");
						}
					}

					Utils.waitSomeTime(100);
					bNavDataReceived = false;
				}
			}

			m_oController.removeNavDataListener(this);
		}

		@Override
		public void navDataReceived(NavData nd) {
			oNavData = nd;
			bNavDataReceived = true;
		}

		private void initPIDControl() {
			dblLastError = 0.0;
			lLastTime = -1;
			dblIntegratedError = 0.0;
		}

		private double pidControl(double i_dblError) {
			double dblTermI = 0.0, dblTermD = 0.0, dblTermP = 0.0;
			long lTimeNow = SystemClock.uptimeMillis();
			long dt = lTimeNow - lLastTime;
			if (dt == 0) {
				error(TAG, "Time Interval is 0!");
				return 0;
			}

			dblTermP = Kp + Math.abs(i_dblError);

			if (lLastTime != -1) {
				dblTermD = Math.abs(i_dblError - dblLastError) / dt * Kd;

				dblIntegratedError += Math.abs(i_dblError) * dt;
				dblTermI = dblIntegratedError * Ki;
			}

			double dblResult = dblTermI + dblTermD + dblTermP;

			dblLastError = i_dblError;
			lLastTime = lTimeNow;

			return Math.signum(i_dblError) * Math.min(dblResult * 100.0, 100.0);
		}
	}

	// Increase Altitude ------------------------------------------------------

	public void increaseAltitude() {
//		increaseAltitude(m_dblBaseSpeed);
		increaseAltitude(40);
	}

	public void increaseAltitude(double i_dblSpeed) {
		debug(TAG, "increaseAltitude(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.UP, i_dblSpeed, true);
	}

	private void executeMoveUp(double i_dblSpeed) {
		executeMove(0, 0, i_dblSpeed / 100.0, 0);
	}

	// Decrease Altitude ------------------------------------------------------

	public void decreaseAltitude() {
//		decreaseAltitude(m_dblBaseSpeed);
		decreaseAltitude(40);
	}

	public void decreaseAltitude(double i_dblSpeed) {
		debug(TAG, "decreaseAltitude(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.DOWN, i_dblSpeed, true);
	}

	public void executeMoveDown(double i_dblSpeed) {
		executeMove(0, 0, - i_dblSpeed / 100.0, 0);
	}

	// Hover ------------------------------------------------------

	public void hover() {
		debug(TAG, "hover()");
		try {
			m_oController.hover();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// nothing to do
	}

	private double capRadius(double i_dblRadius) {
		// io_nRadius = Math.min(io_nRadius, DottyTypes.MAX_RADIUS);
		// io_nRadius = Math.max(io_nRadius, -DottyTypes.MAX_RADIUS);

		return i_dblRadius;
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
		case LEFT:
			executeMoveLeft(i_dblSpeed);
			break;
		case RIGHT:
			executeMoveRight(i_dblSpeed);
			break;
		case UP:
			executeMoveUp(i_dblSpeed);
			break;
		case DOWN:
			executeMoveDown(i_dblSpeed);
			break;
		case ROTATE_LEFT:
			executeRotateCounterClockwise(i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			executeRotateClockwise(i_dblSpeed);
			break;
		default:
			error(TAG, "Move not available: %s", i_eMove.toString());
			return;
		}
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
//		case LEFT:
//			executeMoveLeft(i_dblSpeed, i_nRadius);
//			break;
//		case RIGHT:
//			executeMoveRight(i_dblSpeed, i_nRadius);
//			break;
		default:
			error(TAG, "Move not available: %s", i_eMove.toString());
			return;
		}
	}

	// Move Forward ------------------------------------------------------

	@Override
	public void moveForward() {
//		moveForward(m_dblBaseSpeed);
		moveForward(15);
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		debug(TAG, "moveForward(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.FORWARD, i_dblSpeed, true);
	}

	private void executeMoveForward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		executeMove(0, - i_dblSpeed / 100.0, 0, 0);
	}

	// radius for a holonomic robot doesn't make sense. why make an arc if you
	// can go in a direct line
	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_nRadius = capRadius(i_nRadius);
		
//		debug(TAG, "moveForward(%3f, %d)", i_dblSpeed, i_nRadius);
//		m_oRepeater.startMove(Move.FORWARD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		i_dblSpeed = capSpeed(i_dblSpeed);

		debug(TAG, "moveForward(%3f, %3f)", i_dblSpeed, i_dblAngle);
		// the angle is relative to the straight forward line, with a positive value
		// meaning left and a negative value for right.
		// we use angle instead of radius
		m_oRepeater.startMove(Move.FORWARD, i_dblSpeed, i_dblAngle, true);
	}
	
	private void executeMoveForward(double i_dblSpeed, double i_dblAngle) {
		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_dblRadius = capRadius(i_dblRadius);

		// calculate the speed settings for left / right and forward based
		// on the angle
		double i_dblFwdSpeed = Math.cos(i_dblAngle) * i_dblSpeed;
		double i_dblLeftRightSpeed = Math.sin(i_dblAngle) * i_dblSpeed;

		executeMove(- i_dblLeftRightSpeed / 100.0, - i_dblFwdSpeed / 100.0, 0, 0);
	}
	
	// Move Backward ------------------------------------------------------

	@Override
	public void moveBackward() {
		moveBackward(15);
//		moveBackward(m_dblBaseSpeed);
	}

	// radius for a holonomic robot doesn't make sense. why make an arc if you
	// can go in a direct line
	@Override
	public void moveBackward(double i_dblSpeed) {
		debug(TAG, "moveBackward(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.BACKWARD, i_dblSpeed, true);
	}

	public void executeMoveBackward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		executeMove(0, i_dblSpeed / 100.0, 0, 0);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_nRadius = capRadius(i_nRadius);

//		debug(TAG, "moveBackward(%3f, %d)", i_dblSpeed, i_nRadius);
//		m_oRepeater.startMove(Move.BACKWARD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {

		debug(TAG, "moveBackward(%3f, %3f)", i_dblSpeed, i_dblAngle);
		// the angle is relative to the straight backward line, with a positive value
		// meaning left and a negative value for right.
		// we use angle instead of radius
		m_oRepeater.startMove(Move.BACKWARD, i_dblSpeed, i_dblAngle, true);
	}

	private void executeMoveBackward(double i_dblSpeed, double i_dblAngle) {
		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_nRadius = capRadius(i_nRadius);

		double i_dblFwdSpeed = Math.cos(i_dblAngle) * i_dblSpeed;
		double i_dblLeftRightSpeed = Math.sin(i_dblAngle) * i_dblSpeed;

		executeMove(- i_dblLeftRightSpeed / 100.0, i_dblFwdSpeed / 100.0, 0, 0);
	}
	

	// Move Left ------------------------------------------------------

	public void moveLeft() {
//		moveLeft(m_dblBaseSpeed);
		moveLeft(15);
	}

	public void moveLeft(double i_dblSpeed) {
		debug(TAG, "moveLeft(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.LEFT, i_dblSpeed, true);
	}

	public void executeMoveLeft(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		executeMove(- i_dblSpeed / 100.0, 0, 0, 0);
	}
	
	// can be done with move fwd / move backward
//	public void moveLeft(double i_dblSpeed, int i_nRadius) {
//		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_nRadius = capRadius(i_nRadius);
//
//		debug(TAG, "moveLeft(%3f, %d)", i_dblSpeed, i_nRadius);
//		m_oRepeater.startMove(Move.LEFT, i_dblSpeed, i_nRadius, true);
//	}
//
//	private void executeMoveLeft(double i_dblSpeed, int i_nRadius) {
//		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_nRadius = capRadius(i_nRadius);
//
//		// TODO implement movement in two directions at the same time
//	}
	

	// Move Right ------------------------------------------------------

	public void moveRight() {
		moveRight(15);
//		moveRight(m_dblBaseSpeed);
	}

	public void moveRight(double i_dblSpeed) {
		debug(TAG, "moveRight(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.RIGHT, i_dblSpeed, true);
	}

	public void executeMoveRight(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		executeMove(i_dblSpeed / 100.0, 0, 0, 0);
	}

	// can be done with move fwd / move backward
//	public void moveRight(double i_dblSpeed, int i_nRadius) {
//		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_nRadius = capRadius(i_nRadius);
//
//		debug(TAG, "moveRight(%3f, %d)", i_dblSpeed, i_nRadius);
//		m_oRepeater.startMove(Move.RIGHT, i_dblSpeed, i_nRadius, true);
//	}
//
//	private void executeMoveRight(double i_dblSpeed, int i_nRadius) {
//		i_dblSpeed = capSpeed(i_dblSpeed);
//		i_nRadius = capRadius(i_nRadius);
//
//		// TODO implement movement in two directions at the same time
//	}
	
	
	// Rotate Right / Clockwise -----------------------------------------

	@Override
	public void rotateClockwise() {
		rotateClockwise(50);
//		rotateClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		debug(TAG, "rotateClockwise(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.ROTATE_RIGHT, i_dblSpeed, true);
	}

	public void executeRotateClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		executeMove(0, 0, 0, i_dblSpeed / 100.0);
	}

	// Rotate Left / Counterclockwise ------------------------------------

	@Override
	public void rotateCounterClockwise() {
//		rotateCounterClockwise(m_dblBaseSpeed);
		rotateCounterClockwise(50);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		debug(TAG, "rotateCounterClockwise(%3f)", i_dblSpeed);
		m_oRepeater.startMove(Move.ROTATE_LEFT, i_dblSpeed, true);
	}

	public void executeRotateCounterClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		executeMove(0, 0, 0, - i_dblSpeed / 100.0);
	}

	// Move Stop ------------------------------------------------------

	@Override
	public void moveStop() {
		debug(TAG, "moveStop()");
		m_oRepeater.stopMove();
		hover();
	}
	
	// Genearl Move ------------------------------------------------------ 
	
	// Note: the move is repeated until moveStop() is called!
	public void move(double i_dblLeftRightTilt, double i_dblFrontBackTilt,
			double i_dblVerticalSpeed, double i_dblAngularSpeed) {
		debug(TAG, "move(%3f, %3f, %3f, %3f)", i_dblLeftRightTilt, i_dblFrontBackTilt, i_dblVerticalSpeed, i_dblAngularSpeed);
		m_oRepeater.startMove(new GeneralMoveRunner(i_dblLeftRightTilt, i_dblFrontBackTilt, i_dblVerticalSpeed, i_dblAngularSpeed), true);
	}
	
	class GeneralMoveRunner implements Runnable {
		
		private double dblLeftRightTilt;
		private double dblFrontBackTilt;
		private double dblVerticalSpeed;
		private double dblAngularSpeed;
		
		public GeneralMoveRunner(double i_dblLeftRightTilt, double i_dblFrontBackTilt,
				double i_dblVerticalSpeed, double i_dblAngularSpeed) {
			dblAngularSpeed = i_dblAngularSpeed;
			dblFrontBackTilt = i_dblFrontBackTilt;
			dblLeftRightTilt = i_dblLeftRightTilt;
			dblVerticalSpeed = i_dblVerticalSpeed;
		}
		
		@Override
		public void run() {
			synchronized (m_oRepeater.getMutex()) {
				debug(TAG, "Move");
				executeMove(dblLeftRightTilt, dblFrontBackTilt, dblVerticalSpeed, dblAngularSpeed);
			}
		}
	}
	
	private void executeMove(double i_dblLeftRightTilt, double i_dblFrontBackTilt,
			double i_dblVerticalSpeed, double i_dblAngularSpeed) {

		try {
			m_oController.move((float) i_dblLeftRightTilt, (float) i_dblFrontBackTilt, 
					(float) i_dblVerticalSpeed, (float) i_dblAngularSpeed);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// Execute Circle ------------------------------------------------------

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSpeed() {
		return m_dblBaseSpeed;
	}

	@Override
	public void ready() {
		m_bConnected = true;
	}

	public boolean isARDrone1() {
		if (m_oController != null) {
			return m_oController.isARDrone1();
		} else {
			return false;
		}
	}

	@Override
	public void onConnect(boolean i_bConnected) {
		m_bConnected = i_bConnected;
	}

	@Override
	public boolean toggleInvertDrive() {
		// not applicable
		return false;
	}

	@Override
	public boolean isStreaming() {
		return m_bStreaming;
	}

	public abstract void startVideo();
	public abstract void stopVideo();

//	@Override
//	public void onFrame(byte[] rgb, int rotation) {
//		m_oVideoSender.onFrame(rgb, rotation);
//	}

	@Override
	public void toggleCamera() {
		switchCamera();
	}

	@Override
	public void cameraUp() {
		// NOT AVAILABLE
	}

	@Override
	public void cameraDown() {
		// NOT AVAILABLE		
	}

	@Override
	public void cameraStop() {
		// NOT AVAILABLE
	}

//	@Override
//    public void frameReceived(final int startX, final int startY, final int w, final int h, final
//            int[] rgbArray, final int offset, final int scansize) {
//		
//		(new VideoDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute();
//	}
//	
//	private class VideoDisplayer extends AsyncTask<Void, Integer, Void> {
//        
//        public Bitmap b;
//        public int[] rgbArray;
//        public int offset;
//        public int scansize;
//        public int w;
//        public int h;
//        public byte[] rgbJpeg;
//        
//        public VideoDisplayer(int x, int y, int width, int height, int[] arr, int off, int scan) {
//            // do stuff
//            rgbArray = arr;
//            offset = off;
//            scansize = scan;
//            w = width;
//            h = height;
//        }
//        
//        @Override
//        protected Void doInBackground(Void... params) {
//        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            b =  Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
//            b.compress(CompressFormat.JPEG, 90, bos);
//            rgbJpeg = bos.toByteArray();
//            return null;
//        }
//        
//        @Override
//        protected void onPostExecute(Void param) {
//        	m_oVideoSender.onFrame(rgbJpeg, 0);
//        }
//    }
}
