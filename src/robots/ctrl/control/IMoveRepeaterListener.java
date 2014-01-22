package robots.ctrl.control;

import org.dobots.comm.Move;

public interface IMoveRepeaterListener {
	
	void onDoMove(Move i_eMove, double i_dblSpeed);
	void onDoMove(Move i_eMove, double i_dblSpeed, double i_dblRadius);

}
