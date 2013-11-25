package robots.rover.rover2.ctrl.remote;

import java.util.concurrent.TimeoutException;

import robots.RobotType;
import robots.ctrl.RemoteWrapperUi;
import robots.gui.RobotView;
import robots.rover.base.ctrl.RoverBaseTypes.VideoResolution;
import robots.rover.rover2.ctrl.IRover2;

public class Rover2Remote extends RemoteWrapperUi implements IRover2 {
	
	public Rover2Remote(RobotView activity, RobotType type, Class serviceClass) {
		super(activity, type, serviceClass);
	}
	
	public void setConnection(String address, int port) {
		sendRPC("setConnection", address, port);
	}

	public void toggleLight() {
		sendRPC("toggleLight");
	}
	
	public void toggleInfrared() {
		sendRPC("toggleInfrared");
	}

	@Override
	public boolean isStreaming() {
		try {
			return (Boolean)sendRPCandWaitForReply("isStreaming");
		} catch (TimeoutException e) {
			onTimeout();
			return false;
		}
	}

	@Override
	public void startVideo() {
		sendRPC("startVideo");
	}

	@Override
	public void stopVideo() {
		sendRPC("stopVideo");
	}

	@Override
	public void enableInfrared() {
		sendRPC("enableInfrared");
	}

	@Override
	public void disableInfrared() {
		sendRPC("disableInfrared");
	}

	@Override
	public void enableLight() {
		sendRPC("enableLight");
	}

	@Override
	public void disableLight() {
		sendRPC("disableLight");
	}

	@Override
	public void cameraUp() {
		sendRPC("cameraUp");
	}

	@Override
	public void cameraStop() {
		sendRPC("cameraStop");
	}

	@Override
	public void cameraDown() {
		sendRPC("cameraDown");
	}

	@Override
	public double getBatteryPower() {
		// TODO: solve asynchronous
		try {
			return (Double)sendRPCandWaitForReply("getBatteryPower");
		} catch (TimeoutException e) {
			onTimeout();
			return -1;
		}
	}

	@Override
	public VideoResolution getResolution() {
		try {
			String name = (String)sendRPCandWaitForReply("getResolution");
			return VideoResolution.valueOf(name);
		} catch (TimeoutException e) {
			onTimeout();
			return VideoResolution.res_unknown;
		}
	}

	@Override
	public void setResolution(VideoResolution i_eResolution) {
		sendRPC("setResolution", i_eResolution);
	}
}
