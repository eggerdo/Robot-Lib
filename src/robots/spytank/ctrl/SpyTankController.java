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

import org.dobots.communication.video.IRawVideoListener;

import robots.ctrl.BaseWifi;
import android.util.Log;

public class SpyTankController extends BaseWifi {
	
	public static final String TAG = "SpyTankController";
	
//	boolean connected;
	
	Socket m_oMediaSocket;
	DataInputStream m_oMediaIn;

	private IRawVideoListener oVideoListener = null;

	private boolean m_bRun = true;
	private boolean m_bStreaming = false;

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
		this.oVideoListener = listener;
	}
	
	public void removeVideoListener(IRawVideoListener listener) {
		if (this.oVideoListener == listener) {
			this.oVideoListener = null;
		}
	}
	
	public void setConnection(String address, int commandPort, int mediaPort) {
		m_strAddress = address;
		m_nPort = commandPort;
		m_nMediaPort = mediaPort;
	}

	@Override
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
		URL url = new URL(String.format("http://%s:%d", m_strAddress, m_nMediaPort));
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
						
						if (oVideoListener  != null) {
							oVideoListener.onFrame(data, 0);
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
		try {
			disconnect();
			disconnectMedia();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_oKeepAliveTimer.cancel();
		m_bRun = false;
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
		if (!m_bStreaming && m_bConnected) {
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
	
	private void send(byte[] buffer) throws IOException {
		if (m_bConnected) {
			m_oDataOut.write(buffer);
			m_oDataOut.flush();
		}
	}
	
	public void keepAlive() {
		try {
			if (m_bConnected) {
				byte[] request = CommandEncoder.getKeepAlive();
				send(request);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean cameraDown() throws IOException {
		return motor(SpyTankTypes.CAMERA, SpyTankTypes.DOWN);
	}

	public boolean cameraStop() throws IOException {
		return motor(SpyTankTypes.CAMERA, SpyTankTypes.STOP);
	}

	public boolean cameraUp() throws IOException {
		return motor(SpyTankTypes.CAMERA, SpyTankTypes.UP);
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
	
	private boolean motor(int id, int direction) {
		if (m_bConnected) {
			try {
				byte[] cmd = CommandEncoder.getMotorCommand(id, direction);
				send(cmd);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
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

}
