package robots.replicator.ctrl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import org.dobots.communication.video.IRawVideoListener;

import android.util.Log;

import robots.ctrl.BaseWifi;
import robots.replicator.ctrl.ReplicatorTypes;

public class ReplicatorController extends BaseWifi {

	private static final String TAG = "ReplicatorCtrl";

	private int m_nVideoPort;

	Socket m_oVideoSocket;
	DataInputStream m_oVideoIn;

	private IRawVideoListener oVideoListener = null;

	private boolean m_bRun = true;
	private boolean m_bStreaming = false;

	public ReplicatorController() {
		super(ReplicatorTypes.ADDRESS, ReplicatorTypes.COMMAND_PORT);
		Log.w(TAG, "Construct controller as BaseWifi(" + ReplicatorTypes.ADDRESS + ":" + ReplicatorTypes.COMMAND_PORT + ")");
		m_nVideoPort = ReplicatorTypes.VIDEO_PORT;
	}

	@Override
	public boolean connect() throws IOException {
		if (super.connect()) {
			if (m_bStreaming) {
				connectVideo();
			}
			return true;
		}
		return false;
	}

	public void setVideoListener(IRawVideoListener listener) {
		this.oVideoListener = listener;
	}
	
	public void removeVideoListener(IRawVideoListener listener) {
		if (this.oVideoListener == listener) {
			this.oVideoListener = null;
		}
	}
	
	public void setConnection(String address, int commandPort, int videoPort) {
		Log.w(TAG, "Set connection info as " + address + ":" + videoPort);
		m_strAddress = address;
		m_nPort = commandPort;
		m_nVideoPort = videoPort;
	}
	
	private void connectVideo() throws IOException {
		URL url = new URL(String.format("http://%s:%d", m_strAddress, m_nVideoPort));
		Log.w(TAG, "Connect video on " + url.toString());
		HttpURLConnection videoCon = (HttpURLConnection) url.openConnection();
		m_oVideoIn = new DataInputStream(new BufferedInputStream(videoCon.getInputStream()));

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
		}, "Replicator Video Thread");
		localThread.start();
	}

	private byte[] readFrame() throws IOException {
		try {
			m_oVideoIn.mark(ReplicatorTypes.FRAME_SIZE);
			int header_size = ReplicatorTypes.HEADER_SIZE;
			m_oVideoIn.reset();
			byte[] header = new byte[header_size];
			m_oVideoIn.readFully(header);
			
			// todo: obtain this from the header
			//int contentLength = parseContentLength(header);
			int contentLength = ReplicatorTypes.IMAGE_SIZE;
			
			m_oVideoIn.reset();
			byte[] content = new byte[contentLength];
			m_oVideoIn.skipBytes(header_size);
			m_oVideoIn.readFully(content);
			return content;
		} catch (Exception e) {
			return null;
		}
	}
	
	private void disconnectVideo() {
		m_bRun = false;
		try {
			if (m_oVideoIn != null) {
				m_oVideoIn.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_oVideoIn = null;
	}
	
	public void destroy() {
		try {
			disconnect();
			disconnectVideo();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		m_oKeepAliveTimer.cancel();
		m_bRun = false;
	}

	public void startVideo() {
		if (!m_bStreaming && connected) {
			Log.d(TAG, "startVideo");
			
			try {
				connectVideo();
			} catch (IOException e) {
				e.printStackTrace();
			}

			m_bStreaming = true;
		}
	}

	public void stopVideo() {
		if (m_bStreaming) {
			Log.d(TAG, "stopVideo");
			disconnectVideo();
			m_bStreaming = false;
		}
	}
	
}