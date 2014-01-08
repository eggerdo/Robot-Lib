package robots.rover.ac13.ctrl;

// Uses the AC13Communication Library created by Uceta
// http://sourceforge.net/projects/ac13javalibrary/

import org.dobots.zmq.video.ZmqVideoSender;

import robots.RobotType;
import robots.rover.base.ctrl.RoverBase;

public class AC13Rover extends RoverBase implements IAC13Rover {
	
	private static final String TAG = "AC13Rover";
	private ZmqVideoSender m_oVideoSender;
	
	public AC13Rover() {
		super(AC13RoverTypes.AXLE_WIDTH, AC13RoverTypes.MIN_SPEED, AC13RoverTypes.MAX_SPEED, AC13RoverTypes.MIN_RADIUS, AC13RoverTypes.MAX_RADIUS);
		
		m_oController = new AC13Controller();
		
		m_oVideoSender = new ZmqVideoSender(getID());
		m_oController.setVideoListener(m_oVideoSender);
	}

	// Default Robot Device Functions =========================================

	@Override
	public RobotType getType() { 
		return RobotType.RBT_AC13ROVER;
	}

	@Override
	public String getAddress() {
		return m_oController.getAddress();
	}

	// Custom AC 13 Rover Functions ====================================================
	
	private AC13Controller getController() {
		return (AC13Controller) m_oController;
	}

	@Override
	public void enableInfrared() {
		getController().enableInfrared();
	}

	@Override
	public void disableInfrared() {
		getController().disableInfrared();
	}

	public void toggleInfrared() {
		getController().switchInfrared();
	}
	
	public boolean isInfraredEnabled() {
		return getController().isInfraredEnabled();
	}
	
	public void startVideo() {
		getController().startStreaming();
	}
	
	public void stopVideo() {
		getController().stopStreaming();
	}
	
	public boolean isStreaming() {
		return getController().isStreaming();
	}

}
