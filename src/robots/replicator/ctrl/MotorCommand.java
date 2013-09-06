package robots.replicator.ctrl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MotorCommand {
	short forward;
	short radius;
	
	public MotorCommand(int forward, int radius) {
		this.forward = (short)(forward & 0xFFFF);
		this.radius = (short)(radius & 0xFFFF);
	}
	
	public byte[] serialize() {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putShort(forward);
		bb.putShort(radius);
		return bb.array();
	}
}