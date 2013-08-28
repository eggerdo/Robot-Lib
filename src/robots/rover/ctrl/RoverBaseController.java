package robots.rover.ctrl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.utilities.log.Loggable;

import android.util.Log;

import robots.rover.ctrl.RoverBaseTypes.RoverParameters;

public abstract class RoverBaseController extends Loggable {

	protected static final String TAG = "RoverBaseController";
	
	protected static final int CONNECT_TIMEOUT = 10000; // 10 seconds
	
	// Flags to store state of the connection
	protected boolean m_bConnected;

	// Flags to store state of the video
	protected boolean m_bStreaming = false;
	
	// receives new frame events
	protected IRawVideoListener oVideoListener = null;

	protected RoverParameters parameters;

	protected String m_strTargetHost = null;
	protected int m_nTargetPort;
	protected String targetId;
	protected String targetPassword;


	public abstract void keepAlive();

	public abstract boolean connect();

	public abstract boolean disconnect();

	public abstract boolean isConnected();
	
	public String getAddress() {
		return m_strTargetHost;
	}

	public void setVideoListener(IRawVideoListener listener) {
		this.oVideoListener = listener;
	}
	
	public void removeVideoListener(IRawVideoListener listener) {
		if (this.oVideoListener == listener) {
			this.oVideoListener = null;
		}
	}

	public void setConnection(String address, int port) {
		m_strTargetHost = address;
		m_nTargetPort = port;
	}

	public void moveForward(int i_nVelocity) {
		Log.d(TAG, String.format("move fwd (%d, %d)", i_nVelocity, i_nVelocity));
		moveLeftForward(i_nVelocity);
		moveRightForward(i_nVelocity);
	}

	public void moveForward(int i_nLeftVelocity, int i_nRightVelocity) {
		Log.d(TAG, String.format("move fwd (%d, %d)", i_nLeftVelocity, i_nRightVelocity));
		moveLeftForward(i_nLeftVelocity);
		moveRightForward(i_nRightVelocity);
	}
	
	public void moveBackward(int i_nVelocity) {
		Log.d(TAG, String.format("move bwd (%d, %d)", i_nVelocity, i_nVelocity));
		moveLeftBackward(i_nVelocity);
		moveRightBackward(i_nVelocity);
	}

	public void moveBackward(int i_nLeftVelocity, int i_nRightVelocity) {
		Log.d(TAG, String.format("move bwd (%d, %d)", i_nLeftVelocity, i_nRightVelocity));
		moveLeftBackward(i_nLeftVelocity);
		moveRightBackward(i_nRightVelocity);
	}

	public void rotateLeft(int i_nVelocity) {
		Log.d(TAG, String.format("rotate left (%d)", i_nVelocity));
		moveLeftBackward(i_nVelocity);
		moveRightForward(i_nVelocity);
	}

	public void rotateRight(int i_nVelocity) {
		Log.d(TAG, String.format("rotate right (%d)", i_nVelocity));
		moveRightBackward(i_nVelocity);
		moveLeftForward(i_nVelocity);
	}

	public void moveStop() {
		Log.d(TAG, "stop");
		moveLeftStop();
		moveRightStop();
	}

	protected abstract void moveLeftForward(int i_nVelocity);
	protected abstract void moveRightForward(int i_nVelocity);
	
	protected abstract void moveLeftBackward(int i_nVelocity);
	protected abstract void moveRightBackward(int i_nVelocity);

	protected abstract void moveLeftStop();
	protected abstract void moveRightStop();
	
	

	// call is nonblocking. returns directly and doesn't wait for an answer
	public void switchTo640X480Resolution(){
		new Thread(new ResolutionCommandRunnable(32)).start();	
	}

	// call is made blocking until answer received
	public boolean setResolution640x480() {
		ResolutionCommandRunnable oRunner = new ResolutionCommandRunnable(32);
		oRunner.run();
		return oRunner.success;
	}
	
	// call is made blocking until answer received
	public void switchTo320X240Resolution(){
		new Thread(new ResolutionCommandRunnable(8)).start();
	}
	
	public boolean setResolution320x240() {
		ResolutionCommandRunnable oRunner = new ResolutionCommandRunnable(8);
		oRunner.run();
		return oRunner.success;
	}

	private class ResolutionCommandRunnable implements Runnable {

		int command;
		public boolean success = false;

		public ResolutionCommandRunnable(int command) {
			this.command = command;
		}

		public void run() {
			try {
				HttpClient mClient= new DefaultHttpClient();
				String strCommand = String.format("http://%s:%d/set_params.cgi?resolution=%s", m_strTargetHost, m_nTargetPort, command);
				HttpGet get = new HttpGet(strCommand);
				get.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(targetId, targetPassword), "UTF-8", false));

				mClient.execute(get);
				HttpResponse response = mClient.execute(get);

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					info(TAG, "RESOLUTION COMMAND: " + line + " " + command);

					if (line.startsWith("ok")) {
						success = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				error(TAG, "Resolution Command Error "+  e.toString());
			}
		}
	}

	public RoverParameters getParameters() {
		return parameters;
	}

	public void requestAllParameters()
	{
		//Getting Parameters
		new Thread(new GetParametersRunnable()).start();
	}

	private class GetParametersRunnable implements Runnable {

		@Override
		public void run() {
			try {	

				ArrayList<String> params = new ArrayList<String>();

				HttpClient mClient= new DefaultHttpClient();
				HttpGet get = new HttpGet(String.format("http://%s:%d/get_params.cgi", m_strTargetHost, m_nTargetPort));
				get.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(targetId, targetPassword),"UTF-8", false));

				mClient.execute(get);
				HttpResponse response = mClient.execute(get);

				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";

				while ((line = rd.readLine()) != null) 
					params.add((line.substring(line.indexOf("=")+1,line.indexOf(";"))).replace("'", ""));

				parameters.fillParameters(params);

			} catch (Exception e) {
				e.printStackTrace();
				error(TAG, "GET PARAMETERS ERROR: " +  e.toString());
			}
		}
	}
	
}
