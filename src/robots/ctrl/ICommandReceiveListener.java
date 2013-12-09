package robots.ctrl;

import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;

public interface ICommandReceiveListener {
	
	public void onCommandReceived(BaseCommand i_oCmd);
	
}