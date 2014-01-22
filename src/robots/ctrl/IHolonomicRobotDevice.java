package robots.ctrl;

public interface IHolonomicRobotDevice extends IRobotDevice {

	public void moveLeft(double i_dblSpeed);
	public void moveRight(double i_dblSpeed);

	public void moveLeft();
	public void moveRight();
	
}
