package org.dobots.robotalk.control;

import org.dobots.robotalk.msg.RoboCommands.BaseCommand;

public interface ICommandReceiveListener {
	
	public void onCommandReceived(BaseCommand i_oCmd);
	
}