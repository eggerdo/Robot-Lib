package org.dobots.communication.control;

import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;


public interface ICommandReceiveListener {
	
	public void onCommandReceived(BaseCommand i_oCmd);
	
}