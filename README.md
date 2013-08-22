# Robot-Lib 

Robot-Lib contains a library of robots that can be used in the SwarmControl app (which you can find in the Google Play Store). The current supported robots are:

* Parrot
* Replicator
* Romo
* Rover AC13
* Rover 2
* Spytank

The SwarmControl app supports more robots, but they are not yet ported to the Robot-Lib library.

## How to add your own robot?

For a Wifi robot, let us show how this is done for the Replicator robot.

* copy the files in robots.template.wifirobot.ctrl and robots.template.wifirobot.ctrl and replace NAME accordingly.
* add your robot type to robots.RobotType as e.g. ````RBT_REPLICATOR````
* add your robot to robots.RobotDeviceFactory: ````oRobot = new Replicator();````
* add the GUI for your robot to robots.gui.RobotViewFactory
* or no, add to org.dobots.swarmcontrol.robots.RobotViewFactory
* add to manifest in SwarmControl

````
<activity android:name="robots.replicator.gui.ReplicatorRobot"
	android:screenOrientation="portrait" ></activity>
````

* add functionality to the Robot class (which functions as entry points for a graphical user interface), the Controller class, which encapsulates all the primitives that are valid for this robot, and the SensorGatherer which establishes the way the sensor data is read out, for example the camera.


## Copyrights
The copyrights (2013) belong to:

- Author: Dominik Egger
- Author: Anne van Rossum
- Date: 21 Aug. 2013
- License: LGPL v3
- Almende B.V., http://www.almende.com and DO bots B.V., http://www.dobots.nl
- Rotterdam, The Netherlands
