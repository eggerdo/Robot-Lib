package org.dobots.robotalk.msg;

import java.io.ByteArrayOutputStream;

import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import android.graphics.Bitmap;

public class VideoMessage {
	
	// A ZMQ video message consists of four ZFrames:
	//
	// -------------------
	// | nick | rgb data |
	// -------------------
	//
	// where nick		is the nick name of the sender which is used for
	//					channel subscription / filtering
	//		 rgb data	is the video frame as a compressed JPEG rgb array. 

	// the nick name of the sender
	public String strRobotName = "";
	// the video data as an rgb array
	public byte[] videoData = null;
	
	private VideoMessage() {
		// nothing to do
	}
	
	public VideoMessage(String i_strRobotName, Bitmap i_oBmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		i_oBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

		strRobotName = i_strRobotName;
		videoData  = stream.toByteArray();
	}
	
	public VideoMessage(String i_strRobotName, byte[] i_data) {
		strRobotName = i_strRobotName;
		videoData  = i_data;
	}
	
	// create a video message out of the zmq message
	public static VideoMessage fromZMsg(ZMsg i_oMsg) {
		VideoMessage oVideoMsg = new VideoMessage();
		
		ZFrame target = i_oMsg.pop();
		ZFrame data = i_oMsg.pop();
		
		oVideoMsg.strRobotName = target.toString();
		oVideoMsg.videoData = data.getData();

		return oVideoMsg;
	}
	
	// create a zmq message
	public ZMsg toZmsg() {
		ZMsg msg = new ZMsg();
		
		ZFrame target = new ZFrame(strRobotName);
		ZFrame oVideoData = new ZFrame(videoData);
		
		msg.push(oVideoData);
		msg.push(target);
		
		return msg;
	}
}

