package robots.ispytank.ctrl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dobots.communication.video.VideoDisplayThread.VideoListener;

import robots.ctrl.BaseWifi;
import android.util.Log;

public class SpyTankController extends BaseWifi {
	
	public static final String TAG = "SpyTankController";
	
//	boolean connected;
	
	Socket m_oMediaSocket;
	DataInputStream m_oMediaIn;

	private VideoListener oVideoListener = null;

	private boolean m_bRun = true;
	private boolean m_bStreaming = false;

	protected Timer m_oKeepAliveTimer;

	public SpyTankController() {
		super(SpyTankTypes.ADDRESS, SpyTankTypes.COMMAND_PORT);

		m_oKeepAliveTimer = new Timer("KeepAliveTimer");
		m_oKeepAliveTimer.schedule(m_oKeepAliveTask, 10000, 10000);

	}

	private final TimerTask m_oKeepAliveTask = new TimerTask() {
		
		@Override
		public void run() {
			keepAlive();
		}
		
	};

	public void setVideoListener(VideoListener listener) {
		this.oVideoListener = listener;
	}
	
	public void removeVideoListener(VideoListener listener) {
		if (this.oVideoListener == listener) {
			this.oVideoListener = null;
		}
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

	// debug frame counters
    int m_nFpsCounter = 0;
    long m_lLastTime = System.currentTimeMillis();

	private void connectMedia() throws IOException {
		DefaultHttpClient mediaClient = new DefaultHttpClient();
		URI address = URI.create(String.format("http://%s:%d", SpyTankTypes.ADDRESS, SpyTankTypes.MEDIA_PORT));
		InputStream is = mediaClient.execute(new HttpGet(address)).getEntity().getContent();
		m_oMediaIn = new DataInputStream(new BufferedInputStream(is, SpyTankTypes.FRAME_MAX_LENGTH));
		m_bRun = true;
		
		Thread localThread = new Thread(new Runnable() {

			@Override
			public void run() {

				while (m_bRun) {
					try {
						if (!m_bStreaming) {
							return;
						}
						
						byte[] data = readFrame();
						onVideoReceived(data);
						
			            ++m_nFpsCounter;
			            long now = System.currentTimeMillis();
			            if ((now - m_lLastTime) >= 1000) {
			            	Log.i(TAG, String.format("FPS incoming: %d", m_nFpsCounter));

			                m_lLastTime = now;
			                m_nFpsCounter = 0;
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

	public void onVideoReceived(byte[] data) {
		if (oVideoListener  != null) {
			oVideoListener.onFrame(data, 0);
		}
	}

	private byte[] readFrame() throws IOException {
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
			if ((byte)is.read() == sequence[index]) {
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
		if (!m_bStreaming && connected) {
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
		if (connected) {
			m_oDataOut.write(buffer);
			m_oDataOut.flush();
		}
	}
	
	public void keepAlive() {
		try {
			if (connected) {
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
		moveLeftForward();
		moveRightForward();
	}

	public void moveForward(int i_nLeftVelocity, int i_nRightVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftForward();
		moveRightForward();
	}
	
	public void moveBackward(int i_nVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftBackward();
		moveRightBackward();
	}

	public void moveBackward(int i_nLeftVelocity, int i_nRightVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftBackward();
		moveRightBackward();
	}

	public void rotateLeft(int i_nVelocity) {
		// there is only one velocity, either 0 or 100%
		moveLeftBackward();
		moveRightForward();
	}

	public void rotateRight(int i_nVelocity) {
		// there is only one velocity, either 0 or 100%
		moveRightBackward();
		moveLeftForward();
	}

	public void moveStop() {
		moveLeftStop();
		moveRightStop();
	}
	
	private boolean motor(int id, int direction) {
		if (connected) {
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
	
	private void moveLeftForward() {
		motor(SpyTankTypes.LEFT, SpyTankTypes.FWD);
	}
	
	private void moveRightForward() {
		motor(SpyTankTypes.RIGHT, SpyTankTypes.FWD);
	}
	
	private void moveLeftBackward() {
		motor(SpyTankTypes.LEFT, SpyTankTypes.BWD);	
	}
	
	private void moveRightBackward() {
		motor(SpyTankTypes.RIGHT, SpyTankTypes.BWD);
	}
	
	private void moveLeftStop() {
		motor(SpyTankTypes.LEFT, SpyTankTypes.STOP);
	}
	
	private void moveRightStop() {
		motor(SpyTankTypes.RIGHT, SpyTankTypes.STOP);
	}

}
