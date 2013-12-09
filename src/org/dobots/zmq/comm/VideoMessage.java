package org.dobots.zmq.comm;

import java.io.ByteArrayInputStream;

import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class VideoMessage {

	public byte[] robotID;
	public int nRotation;
	private byte[] videoData;
	private Bitmap bmp = null;
	
	private VideoMessage() {
		
	}

	public VideoMessage(byte[] robotID, byte[] i_data, int i_nRotation) {
		this.robotID = robotID;
		this.nRotation = i_nRotation;
		this.videoData  = i_data;
	}

	public byte[] getVideoData() {
		return this.videoData;
	}

	public byte[] getRobotID() {
		return robotID;
	}
	
	public Bitmap getAsBmp() {
		if (this.bmp == null) {
			// decode the received frame from jpeg to a bitmap
			try {
				ByteArrayInputStream stream = new ByteArrayInputStream(getVideoData());
				this.bmp = BitmapFactory.decodeStream(stream);
			} catch (Exception e) {
				return null;
			}
//			this.bmp = BitmapFactory.decodeByteArray(getVideoData(), 0, getVideoData().length);;
		}
		return this.bmp;
	}

	public int getRotation() {
		return this.nRotation;
	}

	public static VideoMessage fromZMsg(ZMsg i_oMsg) {
		ZMsg clone = i_oMsg.duplicate();
		try {
			VideoMessage oMsg = new VideoMessage();
			ZFrame oRobot = i_oMsg.pop();
			ZFrame oRotation = i_oMsg.pop();
			ZFrame oData = i_oMsg.pop();
			
			oMsg.videoData = oData.getData();
			oMsg.nRotation = Integer.valueOf(oRotation.toString());
			oMsg.robotID = oRobot.getData();
			
			return oMsg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// create a zmsg from the chat message
	public ZMsg toZmsg() {
		ZMsg msg = new ZMsg();
		
		ZFrame channel = new ZFrame(this.robotID);
		ZFrame rotation = new ZFrame(Integer.toString(this.nRotation));
		ZFrame msgData = new ZFrame(this.videoData);
		
		msg.push(msgData);
		msg.push(rotation);
		msg.push(channel);
		
		return msg;
	}

}

