package robots.ctrl.control;

public interface ICameraControlListener {
	
	public void toggleCamera();
	
	public void startVideo();
	public void stopVideo();
	
	public void cameraUp();
	public void cameraDown();
	public void cameraStop();

}
