package robots.ctrl;

import java.util.UUID;

import org.dobots.utilities.log.Loggable;


public abstract class BaseRobot extends Loggable implements IRobotDevice {

	private String m_strID;
	
	public BaseRobot() {
		m_strID = getType().toString() + " " + UUID.randomUUID();
		m_strID = m_strID.replaceAll(" ", "_");
	}
	
	public String getID() {
		return m_strID;
	}
	
	protected double capSpeed(double io_dblSpeed) {
		// if a negative value was provided as speed
		// use the absolute value of it.
		io_dblSpeed = Math.abs(io_dblSpeed);
		io_dblSpeed = Math.min(io_dblSpeed, 100);
		io_dblSpeed = Math.max(io_dblSpeed, 0);
		
		return io_dblSpeed;
	}

}
