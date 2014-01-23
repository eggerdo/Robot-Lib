package robots.parrot.ardrone2.ctrl;

import org.dobots.zmq.video.IRawVideoListener;
import org.dobots.zmq.video.ZmqVideoSender;

import robots.RobotType;
import robots.parrot.ctrl.Parrot;
import android.widget.ImageView;

public class ArDrone2 extends Parrot implements IArDrone2 {

	private static final String TAG	= "ArDrone2";

	private ArDrone2VideoRenderer m_oVideoRenderer;

	@Override
	public RobotType getType() {
		return RobotType.RBT_ARDRONE2;
	}

	@Override
	public void startVideo() {
		debug(TAG, "startVideo()");
		m_oVideoRenderer = new ArDrone2VideoRenderer();
		m_oVideoRenderer.setConnection(m_strAddress);
		m_oVideoRenderer.setVideoListener(m_oVideoSender);
		m_oVideoRenderer.connect();
	}

	@Override
	public void stopVideo() {
		debug(TAG, "stopVideo()");
		if (m_oVideoRenderer != null) {
			m_oVideoRenderer.close();
			m_oVideoRenderer = null;
		}
	}

	@Override
	public void setVideoView(ImageView view) {
		if (m_oVideoRenderer != null) {
			m_oVideoRenderer.setVideoView(view);
		}
	}

}
