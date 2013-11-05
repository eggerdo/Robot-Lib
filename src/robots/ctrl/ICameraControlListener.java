package robots.ctrl;

public interface ICameraControlListener {
	
	public void toggleCamera();
	
	public void switchCameraOn();
	public void switchCameraOff();
	
	public void cameraUp();
	public void cameraDown();
	public void cameraStop();

	public void close();

}
