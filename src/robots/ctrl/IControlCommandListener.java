package robots.ctrl;

import org.dobots.lib.comm.msg.RoboCommands.ControlCommand;

public interface IControlCommandListener {
	
	public void onCommand(ControlCommand command);
	
}