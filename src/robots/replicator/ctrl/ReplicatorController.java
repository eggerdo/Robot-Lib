package robots.replicator.ctrl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
		Log.w(TAG, "Connect video");
		//m_oVideoIn = new DataInputStream(new BufferedInputStream(m_oSocket.getInputStream()));

		//		URL url = new URL(String.format("http://%s:%d", m_strAddress, m_nVideoPort));
		//		Log.w(TAG, "Connect video on " + url.toString());
		//		HttpURLConnection videoCon = (HttpURLConnection) url.openConnection();
		//		m_oVideoIn = new DataInputStream(new BufferedInputStream(videoCon.getInputStream()));

		Log.w(TAG, "Running: " + m_bRun + " and streaming: " + m_bStreaming);
		Thread localThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (m_bRun) {
					try {
						if (!m_bStreaming) {
							return;
						}
						m_oDataOut.write(0);

						byte[] data = readFrame();

						if (oVideoListener != null) {
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
			Log.i(TAG, "Read frame of size: " + ReplicatorTypes.FRAME_SIZE);

			byte[] buffer = new byte[ReplicatorTypes.FRAME_SIZE];
			int len_read = 0;
			try {
				do {
					//int len = m_oVideoIn.read(buffer, 0, ReplicatorTypes.FRAME_SIZE);
					int len = m_oDataIn.read(buffer, len_read, ReplicatorTypes.FRAME_SIZE - len_read);
					len_read += len;
					if (len > 0) {
						//String str = new String(buffer, 0, len);
						//Log.i(TAG, "Receive message of size " + len + ", now total size is " + len_read ); // String.format("Read i=%d, str=%s", len, str));
					}
				} while (len_read < ReplicatorTypes.FRAME_SIZE);
				return buffer;
			} catch (Exception eg) {
				Log.e(TAG, "General: input stream error", eg);
				eg.printStackTrace();
				return null;
			}

			//			m_oVideoIn.mark(ReplicatorTypes.FRAME_SIZE);
			//			int header_size = ReplicatorTypes.HEADER_SIZE;
			//			m_oVideoIn.reset();
			//			byte[] header = new byte[header_size];
			//			m_oVideoIn.readFully(header);
			//			
			//			// todo: obtain this from the header
			//			//int contentLength = parseContentLength(header);
			//			int contentLength = ReplicatorTypes.IMAGE_SIZE;
			//			
			//			m_oVideoIn.reset();
			//			byte[] content = new byte[contentLength];
			//			m_oVideoIn.skipBytes(header_size);
			//			m_oVideoIn.readFully(content);
			//			return content;
		} catch (Exception e) {
			return null;
		}
	}
	//	
	//	private void receiveImage() {
	//		Log.w(TAG, "Get image");
	//		int len = 0;
	//		int newPtr = tcpPtr;
	//		int imageLength = 0;
	//		try {
	//			boolean fnew = false;
	//			while (!fnew && newPtr < maxImageBuffer - maxTCPBuffer) {
	//				len = vSock.getInputStream().read(imageBuffer, newPtr,
	//						maxTCPBuffer);
	//				// todo: check if this happens too often and exit
	//				if (len <= 0) {
	//					m_bConnected = false;
	//				}
	//
	//				byte[] f4 = new byte[4];
	//				for (int i = 0; i < 4; i++)
	//					f4[i] = imageBuffer[newPtr + i];
	//				if (imgStart(f4) && (imageLength > 0))
	//					fnew = true;
	//				if (!fnew) {
	//					newPtr += len;
	//					imageLength = newPtr - imagePtr;
	//				} else {
	//					debug(TAG, "Total image size is "
	//							+ (imageLength - 36));
	//
	//					byte[] rgb = new byte[imageLength - 36];
	////					Log.w(TAG, String.format("imagelen: %d", rgb.length));
	//					System.arraycopy(imageBuffer, imagePtr + 36, rgb, 0, rgb.length);
	//					if (rgb.length > 0) {
	//						if (oVideoListener != null) {
	//							oVideoListener.onFrame(rgb, 0);
	//						}
	//					}
	//					if (newPtr > maxImageBuffer / 2) {
	//						// copy first chunk of new arrived image to start of
	//						// array
	//						for (int i = 0; i < len; i++)
	//							imageBuffer[i] = imageBuffer[newPtr + i];
	//						imagePtr = 0;
	//						tcpPtr = len;
	//					} else {
	//						imagePtr = newPtr;
	//						tcpPtr = newPtr + len;
	//					}
	//					debug("Var", "imagePtr =" + imagePtr);
	//					debug("Var", "tcpPtr =" + tcpPtr);
	//					debug("Var", "imageLength =" + imageLength);
	//					debug("Var", "newPtr =" + newPtr);
	//					debug("Var", "len =" + len);
	//				}
	//			}
	//			// reset if ptr runs out of boundaries
	//			if (newPtr >= maxImageBuffer - maxTCPBuffer) {
	//				warn(TAG, "Out of index, should not happen!");
	//				imagePtr = 0;
	//				tcpPtr = 0;
	//			}
	//		} catch (Exception eg) {
	//			error(TAG, "General input stream error", eg);
	//			eg.printStackTrace();
	//		}
	//	}

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