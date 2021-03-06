package robots.ctrl.control;

import org.dobots.comm.Move;
import org.dobots.utilities.DoBotsThread;
import org.dobots.utilities.Utils;

import android.util.Log;

public class MoveRepeater extends DoBotsThread {
	
	private static final String TAG = "MoveRepeater";

//	private boolean m_bRun = true;
    private Runnable m_oCurrentMove = null;

    private IMoveRepeaterListener m_oRobot;

	private Object m_oMoveMutex = new Object();
	
	private int m_nInterval;
	private boolean m_bRepeat;

	//for consistent rendering
	private long m_lSleepTime;
	// last update
	private long m_lUpdateTime;
	//amount of time to sleep for (in milliseconds)
//	private long m_lDelay=70;
	
	public MoveRepeater(IMoveRepeaterListener i_oRobot, int i_nInterval) {
		super("MoveRepeater");
		m_oRobot = i_oRobot;
		m_nInterval = i_nInterval;
		start();
	}
	
	public Object getMutex() {
		return m_oMoveMutex;
	}
	
	public boolean isRepeating() {
		return m_bRepeat;
	}
	
	public void startMove(Move i_eMove, double i_dblSpeed, int i_nRadius, boolean i_bRepeat) {
		startMove(new MoveRunner(i_eMove, i_dblSpeed, i_nRadius), i_bRepeat);
	}

	public void startMove(Move i_eMove, double i_dblSpeed, boolean i_bRepeat) {
		startMove(new MoveRunner(i_eMove, i_dblSpeed, 0), i_bRepeat);
	}
	
	public void startMove(Runnable i_oMoveRunnable, boolean i_bRepeat) {
		stopMove();
		
		synchronized (m_oMoveMutex) {
			m_oCurrentMove = i_oMoveRunnable;
			m_bRepeat = i_bRepeat;
		}
	}

	public void stopMove() {
//		synchronized (m_oMoveMutex) {
			Log.d(TAG, "done");
			m_bRepeat = false;
			m_oCurrentMove = null;
//		}
	}
	
	class MoveRunner implements Runnable {
		
		private Move eMove;
		private double dblSpeed;
		private int nRadius;
		
		public MoveRunner(Move i_eMove, double i_dblSpeed, int i_nRadius) {
			super();
			
			eMove = i_eMove;
			dblSpeed = i_dblSpeed;
			nRadius = i_nRadius;
		}
		
		@Override
		public void run() {
			synchronized (m_oMoveMutex) {
				Log.d(TAG, eMove.toString());
				if (m_oRobot != null) {
					if (nRadius == 0) {
						m_oRobot.onDoMove(eMove, dblSpeed);
					} else {
						m_oRobot.onDoMove(eMove, dblSpeed, nRadius);
					}
				} else {
					Log.e(TAG, "fatal, no move listener assigned!");
				}
			}
		}
		
	}
	
	@Override
	public void execute() {
		
		m_lUpdateTime = System.nanoTime();
		
		if (m_oCurrentMove == null) {
			Utils.waitSomeTime(10);
		} else {

			synchronized (m_oMoveMutex) {
				m_oCurrentMove.run();
				
				if (!m_bRepeat) {
					m_oCurrentMove = null;
				}
			}

			//SLEEP
			//Sleep time. Time required to sleep to keep game consistent
			//This starts with the specified delay time (in milliseconds) then subtracts from that the
			//actual time it took to update and render the game. This allows the joystick to render smoothly.
			this.m_lSleepTime = m_nInterval-((System.nanoTime()-m_lUpdateTime)/1000000L);

			try {
				//actual sleep code
				if(m_lSleepTime>0){
					Thread.sleep(m_lSleepTime);
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
