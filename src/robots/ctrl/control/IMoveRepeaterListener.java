package robots.ctrl.control;

import org.dobots.lib.comm.Move;

public interface IMoveRepeaterListener {
	
	void onDoMove(Move i_eMove, double i_dblSpeed);
	void onDoMove(Move i_eMove, double i_dblSpeed, int i_nRadius);

}
