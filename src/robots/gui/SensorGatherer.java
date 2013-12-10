package robots.gui;

import org.dobots.utilities.BaseActivity;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public abstract class SensorGatherer extends Thread {

	private volatile boolean m_bStopped = false;
	private volatile boolean m_bPaused = true;
	
	protected Handler m_oUiHandler;
	protected BaseActivity m_oActivity;
	
//	public SensorGatherer() {
//		// why?
//	}
	
	public SensorGatherer(BaseActivity i_oActivity, String i_strThreadName) {
		super(i_strThreadName);
		
		m_oActivity = i_oActivity;

		m_oUiHandler = new Handler(Looper.getMainLooper());
	}
	
	public void stopThread() {
		m_bStopped = true;
		interrupt();
		shutDown();
	}
	
	public void pauseThread() {
		m_bPaused = true;
	}
	
	public void startThread() {
		m_bPaused = false;
	}

	@Override
	public void run() {
		
		while (!m_bStopped) {
			if (!m_bPaused) {
				execute();
			}
		
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public abstract void shutDown();
	
	protected void execute() {
		// needs to be defined by child class
	}
	

	protected void setText(TextView i_oView, String i_strValue) {
		i_oView.setText(i_strValue);
	}
	
	protected void setText(TextView i_oView, int i_nValue) {
		i_oView.setText(String.valueOf(i_nValue));
	}

	protected void setText(TextView i_oView, float i_fValue) {
		i_oView.setText(String.valueOf(i_fValue));
	}

	protected void setOnOffText(TextView i_oView, boolean i_bValue) {
		i_oView.setText(i_bValue ? "ON" : "OFF");
	}

}
