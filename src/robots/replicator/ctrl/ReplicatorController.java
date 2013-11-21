package robots.replicator.ctrl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.dobots.communication.video.IRawVideoListener;
import org.dobots.utilities.Utils;

import robots.ctrl.BaseWifi;
import robots.replicator.ctrl.ReplicatorMessage.MessageType;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class ReplicatorController extends BaseWifi {

	private static final String TAG = "ReplicatorCtrl";

	private int m_nVideoPort;

	Socket m_oVideoSocket;
	DataInputStream m_oVideoIn;

	private IRawVideoListener oVideoListener = null;

	private boolean m_bRun = true;
	private boolean m_bStreaming = false;
	private boolean m_bInitialized = false;

	private DataOutputStream m_oVideoOut;

	public ReplicatorController() {
		super(ReplicatorTypes.ADDRESS, ReplicatorTypes.COMMAND_PORT);
		Log.w(TAG, "Construct controller as BaseWifi(" + ReplicatorTypes.ADDRESS + ":" + ReplicatorTypes.COMMAND_PORT + ")");
		m_nVideoPort = ReplicatorTypes.VIDEO_PORT;
	}
	
	private void initialize() {
		if (m_bConnected) {
			sendData(MessageType.MSG_INIT);
			Utils.waitSomeTime(1000);

			sendData(MessageType.MSG_CAM_VIDEOSTREAM_START);
			Utils.waitSomeTime(1000);
			
			sendData(MessageType.MSG_START);
			Utils.waitSomeTime(3000);
			
			Log.d(TAG, "initialize successful");
			m_bInitialized = true;
		}
	}
	
	private void sendData(MessageType command) {
		ReplicatorMessage message = new ReplicatorMessage((byte)command.ordinal());
		sendData(message);
	}
	
	private void sendData(MessageType command, byte[] data) {
		ReplicatorMessage message = new ReplicatorMessage((byte)command.ordinal(), data);
		sendData(message);
	}
	
	private void sendData(ReplicatorMessage message) {
		byte[] buffer = message.serialize();
		
		try {
			if (m_oDataOut != null) {
				m_oDataOut.write(buffer);
			}
		} catch (IOException e) {
			onConnectError();
		}
	}
	
	@Override
	public boolean connect() throws IOException {
		if (super.connect()) {
			initialize();
			
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
		if (!m_bInitialized) {
			return;
		}
		
		Log.w(TAG, "Connect video");

//		sendData(MessageType.MSG_CAM_VIDEOSTREAM_START);
//		Utils.waitSomeTime(3000);
		
		m_oVideoSocket = new Socket();
		m_oVideoSocket.connect(new InetSocketAddress(m_strAddress, m_nVideoPort), CONNECT_TIMEOUT);
		m_oVideoIn = new DataInputStream(new BufferedInputStream(m_oVideoSocket.getInputStream()));
		m_oVideoOut = new DataOutputStream(m_oVideoSocket.getOutputStream());

		Utils.waitSomeTime(1000);

		Log.w(TAG, "Running: " + m_bRun + " and streaming: " + m_bStreaming);
		Thread localThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (m_bRun) {
					try {
						if (!m_bStreaming) {
							return;
						}
						m_oVideoOut.write(0);

						final byte[] data = readFrame();

						if (data == null) {
							Log.w(TAG, "Byte array is null!");
							continue;
						}
						if (data.length == 0) {
							Log.w(TAG, "Byte array is empty!");
							continue;
						}
						if (data.length != ReplicatorTypes.IMAGE_SIZE) {
							Log.w(TAG, "Byte array too small!");
							continue;
						}

						Log.i(TAG, "Try to decode array to bitmap with size " + data.length);			
										
						if (oVideoListener != null) {
							// the decoding shouldn't be dependent on the registered listener. but why
							// should we do the work of decoding if nobody is listening anyway.
							
							// the received format is of RGB888, but android only knows ARGB8888 and RGB565
							// so we first convert it to ARGB8888, then create a bitmap, and compress it as
							// JPEG to send it to the listener.
							// ideally, the robot would already send the image as compressed JPEG so we can
							// skip this whole step here.
							int argb8888[] = new int[ReplicatorTypes.IMAGE_WIDTH * ReplicatorTypes.IMAGE_HEIGHT];
							for (int i = 0, j = 0; i < ReplicatorTypes.IMAGE_SIZE; i+=3, j++) {
								int r = data[i+0]; int g = data[i+1]; int b = data[i+2];
								argb8888[j] = 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
							}
							Bitmap bmp = Bitmap.createBitmap(argb8888, ReplicatorTypes.IMAGE_WIDTH, ReplicatorTypes.IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);

							if (bmp == null) {
								Log.w(TAG, "bitmap decoding failed!");
								return;
							}
							
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							bmp.compress(CompressFormat.JPEG, 100, bos);
							
							Log.i(TAG, "Bitmap decoded and ready to send");
							
							oVideoListener.onFrame(bos.toByteArray(), 0);
							bmp.recycle();
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
					int len = m_oVideoIn.read(buffer, len_read, ReplicatorTypes.FRAME_SIZE - len_read);
					len_read += len;
					if (len > 0) {
						//String str = new String(buffer, 0, len);
						//Log.i(TAG, "Receive message of size " + len + ", now total size is " + len_read ); // String.format("Read i=%d, str=%s", len, str));
					}
				} while (len_read < ReplicatorTypes.FRAME_SIZE);
				return buffer;
			} catch (Exception eg) {
				Log.e(TAG, "General: input stream error");
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
	
	@Override
	public void disconnect() throws IOException {
		disconnectVideo();
		super.disconnect();
	}

	private void disconnectVideo() {
		m_bRun = false;
		try {
			
			if (m_oVideoSocket != null) {
//				sendData(MessageType.MSG_CAM_VIDEOSTREAM_STOP);
				
				m_oVideoSocket.close();
				m_oVideoSocket = null;
			}

			m_oVideoIn = null;
			m_oVideoOut = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroy() {
		m_bRun = false;
		try {
			disconnectVideo();
			disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		m_oKeepAliveTimer.cancel();
	}

	public void startVideo() {
		if (!m_bStreaming && m_bConnected) {
			Log.d(TAG, "startVideo");

			m_bRun = true;
			m_bStreaming = true;
			
			try {
				connectVideo();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stopVideo() {
		if (m_bStreaming) {
			Log.d(TAG, "stopVideo");
			m_bStreaming = false;
			disconnectVideo();
		}
	}
	
	public boolean isStreaming() {
		return m_bStreaming;
	}

	public void drive(int i_dblSpeed, int i_nRadius) {
		MotorCommand cmd = new MotorCommand(i_dblSpeed, i_nRadius);
		sendData(MessageType.MSG_SPEED, cmd.serialize());
	}

}