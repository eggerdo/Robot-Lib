package robots.rover.rover2.ctrl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.SocketFactory;

import org.apache.http.util.ByteArrayBuffer;
import org.dobots.utilities.Utils;

import robots.rover.ac13.ctrl.AC13RoverTypes;
import robots.rover.ctrl.RoverBaseController;
import android.util.Log;

public class Rover2Controller extends RoverBaseController {

	private static final String TAG = "Rover2Ctrl";
	
	private static final int BUFFER_SIZE = 1048576; // 1 MB

	private Socket m_oCommandSocket;
	private Socket m_oMediaSocket;

	private DataInputStream m_oDataIn;
	private DataOutputStream m_oDataOut;
	private DataInputStream m_oMediaIn;
	private DataOutputStream m_oMediaOut;

	private static Rover2Controller INSTANCE;

	private boolean m_bRun = true;

	private String m_strCameraId;
	private String m_strDeviceId;
	private int[] challenge = { 0, 0, 0, 0 };
	private int[] reverse_challenge = { 0, 0, 0, 0 };

//	private Timer keepAliveTimer;
//	private TimerTask keepAliveTask;
	
	private Timer m_oBatteryTimer;
	private TimerTask m_oBatteryTask;
	private int m_nBatteryPower;

	public Rover2Controller() {

		INSTANCE = this;

		m_strTargetHost = Rover2Types.ADDRESS;
		m_strTargetPort = Rover2Types.PORT;
		targetId = Rover2Types.ID;
		targetPassword = Rover2Types.PWD;

		parameters = new AC13RoverTypes().new RoverParameters();

//		keepAliveTimer = new Timer("keep alive");
//		keepAliveTask = new TimerTask() {
//			public void run() {
//				keepAlive();
//			}
//		};

		m_oBatteryTimer = new Timer("battery timer");
		m_oBatteryTask = new TimerTask() {
			public void run() {
				try {
					if (m_bConnected) {
						byte[] arrayOfByte = CommandEncoder.cmdBatteryPowerReq();
						send(arrayOfByte);
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public void keepAlive() {
		try {
			if (m_bConnected) {
				byte[] arrayOfByte = CommandEncoder.cmdKeepAlive();
				send(arrayOfByte);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void send(byte[] buffer) throws IOException {
		if (m_bConnected) {
			m_oDataOut.write(buffer);
			m_oDataOut.flush();
		}
	}
//
//	public void startKeepAliveTask() {
//		Log.d(TAG, "startKeepAliveTask");
//		keepAliveTimer.schedule(keepAliveTask, 1000, 30000);
//	}

	public void startBatteryTask() {
		Log.d(TAG, "startBatteryTask");
		m_oBatteryTimer.schedule(m_oBatteryTask, 1000, 5000);
	}

	private Socket createSocket(String host, int port) throws IOException {
		Socket localSocket = SocketFactory.getDefault().createSocket();
		InetSocketAddress localInetSocketAddress = new InetSocketAddress(host,
				port);
		localSocket.connect(localInetSocketAddress, 5000);
		return localSocket;
	}

	public boolean cameraDown() throws IOException {
		if (!m_bConnected)
			return false;

		byte[] request = CommandEncoder.cmdDecoderControlReq(2);
		send(request);
		return true;
	}

	public boolean cameraStop() throws IOException {
		if (!m_bConnected)
			return false;

		byte[] request = CommandEncoder.cmdDecoderControlReq(1);
		send(request);
		return true;
	}

	public boolean cameraUp() throws IOException {
		if (!m_bConnected)
			return false;

		byte[] request = CommandEncoder.cmdDecoderControlReq(0);
		send(request);
		return true;
	}

	private int[] createChallenge() {
		int[] newChallenge = new int[4];
		Random r = new Random(System.currentTimeMillis());
		newChallenge[0] = r.nextInt();
		newChallenge[1] = r.nextInt();
		newChallenge[2] = r.nextInt();
		newChallenge[3] = r.nextInt();
//		newChallenge[0] = -402456576;
//		newChallenge[1] = -804847616;
//		newChallenge[2] = 3000;
//		newChallenge[3] = 4000;
		return newChallenge;
	}

	private void connectCommand() throws IOException {
		m_oCommandSocket = createSocket(m_strTargetHost, m_strTargetPort);
		m_oDataOut = new DataOutputStream(m_oCommandSocket.getOutputStream());
		m_oDataIn = new DataInputStream(m_oCommandSocket.getInputStream());

		challenge = createChallenge();
		byte[] request = CommandEncoder.cmdLoginReq(challenge);
		send(request);
		

		Thread localThread = new Thread(new Runnable() {

			@Override
			public void run() {
				ByteArrayBuffer arrayBuffer = new ByteArrayBuffer(BUFFER_SIZE);
				arrayBuffer.clear();

				while (m_bRun) {
					try {
						int nCount;
						if (!m_bConnected) {
							return;
						} else {
							nCount = m_oDataIn.available();
						}

						if (nCount > 0) {
							byte[] buffer = new byte[nCount];
							arrayBuffer.append(buffer, 0, m_oDataIn.read(buffer, 0, nCount));
							arrayBuffer = CommandEncoder.parseCommand(INSTANCE, arrayBuffer);
						}
					} catch (IOException e) {
						e.printStackTrace();

						if (!m_bConnected)
							return;

						// try reconnecting
						try {
							connectCommand();
							return;
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});

		localThread.setName("Rover 2 Command Thread");
		localThread.start();
	}

	public void connectMediaReceiver(int nID) throws IOException {
		m_oMediaSocket = createSocket(m_strTargetHost, m_strTargetPort);
		m_oMediaOut = new DataOutputStream(m_oMediaSocket.getOutputStream());
		m_oMediaIn = new DataInputStream(m_oMediaSocket.getInputStream());

		byte[] request = CommandEncoder.cmdMediaLoginReq(nID);
		m_oMediaOut.write(request);
		m_oMediaOut.flush();

		Thread localThread = new Thread(new Runnable() {
			@Override
			public void run() {
				ByteArrayBuffer arrayBuffer = new ByteArrayBuffer(BUFFER_SIZE);
				arrayBuffer.clear();

				while (m_bRun) {
					try {
						if (!m_bStreaming) {
							return;
						}

						byte[] buffer = new byte[8192];
						int nCount = m_oMediaIn.read(buffer, 0, 8192);
						if (nCount > 0) {
							// Log.d("limit", "read:" + nCount + "\tavailable:"
							// + mediaIn.available());
							arrayBuffer.append(buffer, 0, nCount);
							arrayBuffer = CommandEncoder.parseMediaCommand(INSTANCE, arrayBuffer);
						}
						Utils.waitSomeTime(5);
					} catch (IOException e) {
						Log.i(TAG, "media Thread is stopping");
						e.printStackTrace();
					}
				}
			}
		});
		localThread.setName("Rover 2 Media Thread");
		localThread.start();
		return;
	}

	public void verifyCommand() throws IOException {
		byte[] request = CommandEncoder.cmdVerifyReq(this, getKey(), reverse_challenge);
		send(request);

		startBatteryTask();
//		startKeepAliveTask();
		requestAllParameters();
		return;
	}

	public void receiveVideo(byte[] data) {
		if (oVideoListener != null) {
			oVideoListener.frameReceived(data);
		}
	}

	public boolean enableInfrared() throws IOException {
		if (!m_bConnected)
			return false;

		Log.d(TAG, "enableInfrared");
		byte[] request = CommandEncoder.cmdDecoderControlReq(94);
		send(request);
		return true;
	}

	public boolean disableInfrared() throws IOException {
		if (!m_bConnected)
			return false;

		Log.d(TAG, "disableInfrared");
		byte[] request = CommandEncoder.cmdDecoderControlReq(95);
		send(request);
		return true;
	}

	public boolean enableVideo() throws IOException {
		if (!m_bConnected)
			return false;

		Log.d(TAG, "enableVideo");
//		Log.w(TAG, "streaming=true");
		m_bStreaming = true;
		byte[] request = CommandEncoder.cmdVideoStartReq();
		send(request);
		return true;
	}

	public boolean disableVideo() throws IOException {
		if (!m_bConnected)
			return false;

		Log.d(TAG, "disableVideo");
		m_bStreaming = false;
		byte[] request = CommandEncoder.cmdVideoEnd();
		send(request);
		return true;
	}

	public boolean isStreaming() {
		return m_bStreaming;
	}

	// public boolean g_move(int paramInt1, int paramInt2) throws IOException {
	// if (!bConnected)
	// return false;
	//
	// byte[] request = null;
	// if (paramInt2 > 0)
	// {
	// request = CommandEncoder.cmdDeviceControlReq(11, 255);
	// }
	// if (paramInt2 < 0)
	// {
	// request = CommandEncoder.cmdDeviceControlReq(12, 255);
	// }
	// send(request);
	// return true;
	// }

	public boolean isConnected() {
		try {
			if (m_oCommandSocket != null) {
				return m_oCommandSocket.isConnected();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean move(int id, int speed) {
		try {
			byte[] request = CommandEncoder.cmdDeviceControlReq(id, speed);
			send(request);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void ledON() throws IOException {
		byte[] reequest = CommandEncoder.cmdDeviceControlReq(8, 0);
		send(reequest);
	}

	public void ledOFF() throws IOException {
		byte[] request = CommandEncoder.cmdDeviceControlReq(9, 0);
		send(request);
	}

	public boolean connect() {
		try {
			m_bConnected = true;
			connectCommand();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_bConnected = false;
		return false;
	}

	public boolean disconnect() {
		try {
			if (m_bConnected) {
				m_oBatteryTimer.cancel();
//				keepAliveTimer.cancel();

				m_bConnected = false;
				m_bStreaming = false;
				
				Log.d(TAG, "disconnect");
				
				if (m_oCommandSocket != null) {
					m_oCommandSocket.close();
				}
				
				if (m_oMediaSocket != null) {
					m_oMediaSocket.close();
				}

				m_oDataOut = null;
				m_oDataIn = null;
				m_oMediaIn = null;
				m_oMediaOut = null;

				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setBatteryPower(int value) {
		m_nBatteryPower = value;
	}

	public int getBatteryPower() {
		return m_nBatteryPower;
	}

	public void setDeviceId(String id) {
		m_strDeviceId = id;
	}

	public void setCameraId(String id) {
		m_strCameraId = id;
	}

	public String getKey() {
		return targetId + ":" + m_strCameraId + "-save-private:" + targetPassword;
	}

	public int[] getChallenge() {
		return challenge;
	}

	public void setReverseChallenge(int[] challenge) {
		reverse_challenge = challenge;
	}

	// FORWARD

	protected void moveRightForward(int i_nVelocity) {
		move(1, i_nVelocity);
	}

	protected void moveLeftForward(int i_nVelocity) {
		move(4, i_nVelocity);
	}

	// BACKWARD

	protected void moveRightBackward(int i_nVelocity) {
		move(2, i_nVelocity);
	}

	protected void moveLeftBackward(int i_nVelocity) {
		move(5, i_nVelocity);
	}

	// STOP

	protected void moveRightStop() {
		move(0, 0);
	}

	protected void moveLeftStop() {
		move(3, 0);
	}

}
