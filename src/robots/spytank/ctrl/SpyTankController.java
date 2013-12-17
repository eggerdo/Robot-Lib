package robots.spytank.ctrl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.dobots.zmq.video.IRawVideoListener;

import robots.ctrl.WifiRobotController;
import robots.gui.comm.wifi.WifiConnection;
import android.os.Handler;
import android.util.Log;

public class SpyTankController extends WifiRobotController {
	
	public static final String TAG = "SpyTankController";
	
	private DataInputStream m_oMediaIn;

	private IRawVideoListener m_oVideoListener = null;

	private boolean m_bRun = true;
	private boolean m_bStreaming = true;

	protected Timer m_oKeepAliveTimer;
	
	private int m_nMediaPort;
	
	public SpyTankController() {
		super(SpyTankTypes.ADDRESS, SpyTankTypes.COMMAND_PORT);
		m_nMediaPort = SpyTankTypes.MEDIA_PORT;

		m_oKeepAliveTimer = new Timer("KeepAliveTimer");
		m_oKeepAliveTimer.schedule(m_oKeepAliveTask, 10000, 10000);
	}

	private final TimerTask m_oKeepAliveTask = new TimerTask() {
		
		@Override
		public void run() {
			keepAlive();
		}
		
	};

	public void setVideoListener(IRawVideoListener listener) {
		this.m_oVideoListener = listener;
	}
	
	public void removeVideoListener(IRawVideoListener listener) {
		if (this.m_oVideoListener == listener) {
			this.m_oVideoListener = null;
		}
	}
	
	public void setConnection(String address, int commandPort, int mediaPort) {
		super.setConnection(address, commandPort);
		m_nMediaPort = mediaPort;
	}

	public boolean connect() throws IOException {
		if (super.connect()) {
			if (m_bStreaming) {
				connectMedia();
			}
			return true;
		}
		return false;
	}

	private void connectMedia() throws IOException {
		URL url = new URL(String.format("http://%s:%d", getAddress(), m_nMediaPort));
		HttpURLConnection mediaCon = (HttpURLConnection) url.openConnection();
		m_oMediaIn = new DataInputStream(new BufferedInputStream(mediaCon.getInputStream()));
		
		Thread localThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (m_bRun) {
					try {
						if (!m_bStreaming) {
							return;
						}

						byte[] data = readFrame();
						
						if (m_oVideoListener  != null && data != null) {
							m_oVideoListener.onFrame(data, 0);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, "SpyTank Media Thread");
		localThread.start();
	}
	
	private void disconnectMedia() {
		m_bRun = false;
		try {
			if (m_oMediaIn != null) {
				m_oMediaIn.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_oMediaIn = null;
	}
	
	public void destroy() {
		m_oKeepAliveTimer.cancel();
		super.destroy();
	}

	private byte[] readFrame() throws IOException {
		try {
			m_oMediaIn.mark(SpyTankTypes.FRAME_MAX_LENGTH);
			int index = getStartOfSequence(m_oMediaIn, SpyTankTypes.SOI_MARKER);
			if (index == -1) {
				throw new IOException("read Error");
			}
			
			m_oMediaIn.reset();
			byte[] header = new byte[index];
			m_oMediaIn.readFully(header);
			int contentLength = parseContentLength(header);
			m_oMediaIn.reset();
			byte[] content = new byte[contentLength];
			m_oMediaIn.skipBytes(index);
			m_oMediaIn.readFully(content);
			return content;
		} catch (Exception e) {
			return null;
		}
	}
	
	private int parseContentLength(byte[] header) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(header);
		Properties properties = new Properties();
		properties.load(is);
		return Integer.valueOf(properties.getProperty(SpyTankTypes.CONTENT_LENGTH));
	}
	
	private int getStartOfSequence(InputStream is, byte[] sequence) throws IOException {
		int index = getEndOfSequence(is, sequence);
		if (index > 0) {
			index = index - sequence.length;
		}
		return index;
	}
	
	private int getEndOfSequence(InputStream is, byte[] sequence) throws IOException {
		int index = 0;
		for (int i = 0; i < SpyTankTypes.FRAME_MAX_LENGTH; ++i) {
			byte in = (byte)is.read();
			if (in == sequence[index]) {
				index++;
				if (index == sequence.length) {
					return i + 1;
				}
			} else {
				index = 0;
			}
		}
		return -1;
	}
	
	public void startVideo() {
		if (!m_bStreaming && isConnected()) {
			Log.d(TAG, "startVideo");
			
			try {
				connectMedia();
			} catch (IOException e) {
				e.printStackTrace();
			}

			m_bStreaming = true;
		}
	}

	public void stopVideo() {
		if (m_bStreaming) {
			Log.d(TAG, "stopVideo");
			disconnectMedia();
			m_bStreaming = false;
		}
	}
	
	public void keepAlive() {
		byte[] request = SpyTankTypes.getKeepAlive();
		send(request);
	}
	
	public void cameraDown() throws IOException {
		motor(SpyTankTypes.CAMERA, SpyTankTypes.DOWN);
	}

	public void cameraStop() throws IOException {
		motor(SpyTankTypes.CAMERA, SpyTankTypes.STOP);
	}

	public void cameraUp() throws IOException {
		motor(SpyTankTypes.CAMERA, SpyTankTypes.UP);
	}
	
	public void moveForward(int i_nVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftForward(i_nVelocity);
		moveRightForward(i_nVelocity);
	}

	public void moveForward(int i_nLeftVelocity, int i_nRightVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftForward(i_nLeftVelocity);
		moveRightForward(i_nRightVelocity);
	}
	
	public void moveBackward(int i_nVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftBackward(i_nVelocity);
		moveRightBackward(i_nVelocity);
	}

	public void moveBackward(int i_nLeftVelocity, int i_nRightVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftBackward(i_nLeftVelocity);
		moveRightBackward(i_nRightVelocity);
	}

	public void rotateLeft(int i_nVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftBackward(i_nVelocity);
		moveRightForward(i_nVelocity);
	}

	public void rotateRight(int i_nVelocity) {
		// there is only one velocity, either 0 or 100%
		moveRightBackward(i_nVelocity);
		moveLeftForward(i_nVelocity);
	}

	public void moveStop() {
		moveLeftStop();
		moveRightStop();
	}
	
	private void motor(int id, int direction) {
		byte[] cmd = SpyTankTypes.getMotorCommand(id, direction);
		send(cmd);
	}
	
	private void moveLeftForward(int i_nVelocity) {
		if (i_nVelocity > 0) {
			motor(SpyTankTypes.LEFT, SpyTankTypes.FWD);
		} else {
			motor(SpyTankTypes.LEFT, SpyTankTypes.STOP);
		}
	}
	
	private void moveRightForward(int i_nVelocity) {
		if (i_nVelocity > 0) {
			motor(SpyTankTypes.RIGHT, SpyTankTypes.FWD);
		} else {
			motor(SpyTankTypes.RIGHT, SpyTankTypes.STOP);
		}
	}
	
	private void moveLeftBackward(int i_nVelocity) {
		if (i_nVelocity > 0) {
			motor(SpyTankTypes.LEFT, SpyTankTypes.BWD);	
		} else {
			motor(SpyTankTypes.LEFT, SpyTankTypes.STOP);
		}
	}
	
	private void moveRightBackward(int i_nVelocity) {
		if (i_nVelocity > 0) {
			motor(SpyTankTypes.RIGHT, SpyTankTypes.BWD);
		} else {
			motor(SpyTankTypes.RIGHT, SpyTankTypes.STOP);
		}
	}
	
	private void moveLeftStop() {
		motor(SpyTankTypes.LEFT, SpyTankTypes.STOP);
	}
	
	private void moveRightStop() {
		motor(SpyTankTypes.RIGHT, SpyTankTypes.STOP);
	}

	@Override
	public void disconnect() throws IOException {
		if (m_bStreaming) {
			disconnectMedia();
		}
		super.disconnect();
	}

}
