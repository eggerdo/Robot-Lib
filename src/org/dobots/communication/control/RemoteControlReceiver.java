package org.dobots.communication.control;

import robots.ctrl.ICameraControlListener;
import robots.ctrl.IDriveControlListener;

public abstract class RemoteControlReceiver {

	protected IDriveControlListener m_oRemoteControlListener = null;
	protected ICameraControlListener m_oCameraListener = null;

	public void setDriveControlListener(IDriveControlListener i_oListener) {
		m_oRemoteControlListener = i_oListener;
	}
	
	public void removeDriveControlListener(IDriveControlListener i_oListener) {
		if (m_oRemoteControlListener == i_oListener) {
			m_oRemoteControlListener = null;
		}
	}

	/**
	 * assign a camera control listener which will handle camera on/off, toggle, and up/down commands
	 * @param i_oCameraListener object implementing the ICameraControlListener interface
	 */
	public void setCameraControlListener(ICameraControlListener i_oCameraListener) {
		m_oCameraListener = i_oCameraListener;
	}

	public void removeCameraControlListener(ICameraControlListener i_oCameraListener) {
		if (m_oCameraListener == i_oCameraListener) {
			m_oCameraListener = null;
		}
	}

	public abstract void start();
	public abstract void close();
	
}
