package robots.ctrl;

public interface ICameraControlListener {
	
	public void toggleCamera();
	
	public void startVideo();
	public void stopVideo();
	
	public void cameraUp();
	public void cameraDown();
	public void cameraStop();

}
