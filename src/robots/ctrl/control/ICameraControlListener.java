package robots.ctrl.control;

public interface ICameraControlListener {
	
	public void toggleCamera();
	
	public void switchCameraOn();
	public void switchCameraOff();
	
	public void cameraUp();
	public void cameraDown();
	public void cameraStop();

}
