package robots.replicator.ctrl;

import java.nio.ByteBuffer;

public class ReplicatorMessage {

	public static final int MESSAGE_OVERHEAD = 9; 
	public static final byte EMSGSTARTVARETH = (byte) 0x77;
	public static final byte CRC8_INIT = (byte) 0x00;

	public enum MessageType {
		MSG_NONE,
		MSG_START,
		MSG_STOP,
		MSG_RESET,
		MSG_QUIT,
		MSG_ACKNOWLEDGE,
		MSG_INIT,
		MSG_SPEED,
		MSG_HINGE,
		MSG_POS,
		DAEMON_MSG_RECRUITING,
		DAEMON_MSG_SEEDING,
		DAEMON_MSG_DOCKING,
		DAEMON_MSG_NEIGHBOUR_IP_REQ,
		DAEMON_MSG_NEIGHBOUR_IP,
		DAEMON_MSG_SEED_IP_REQ,
		DAEMON_MSG_SEED_IP,
		DAEMON_MSG_ALLROBOTS_IP_REQ,
		DAEMON_MSG_ALLROBOTS_IP,
		DAEMON_MSG_PROGRESS_REQ,
		DAEMON_MSG_PROGRESS,
		DAEMON_MSG_DISASSEMBLY,
		DAEMON_MSG_STATE_REQ,
		DAEMON_MSG_STATE,
		MSG_CAM_VIDEOSTREAM_STOP,
		MSG_CAM_VIDEOSTREAM_START,
		MSG_CAM_DETECT_DOCKING,
		MSG_CAM_DETECT_MAPPING,
		MSG_CAM_DETECT_STAIR,
		MSG_CAM_DETECTED_BLOB,
		MSG_CAM_DETECTED_BLOB_ARRAY,
		MSG_CAM_DETECTED_STAIR,
		MSG_LASER_DETECT_STEP, // use the laser to detect the step
		MSG_MOTOR_CALIBRATION_RESULT,
		MSG_UBISENCE_POSITION,
		MSG_MAP_DATA,
		MSG_MAP_COVARIANCE,
		MSG_MAP_COMPLETE,
		MSG_CALIBRATE,
		MSG_NUMBER,
		MSG_ZIGBEE_MSG
	}

	private static final byte crc8_table[] = {
		(byte) 0x00, (byte) 0x07, (byte) 0x0E, (byte) 0x09, (byte) 0x1C, (byte) 0x1B, (byte) 0x12, (byte) 0x15,
		(byte) 0x38, (byte) 0x3F, (byte) 0x36, (byte) 0x31, (byte) 0x24, (byte) 0x23, (byte) 0x2A, (byte) 0x2D,
		(byte) 0x70, (byte) 0x77, (byte) 0x7E, (byte) 0x79, (byte) 0x6C, (byte) 0x6B, (byte) 0x62, (byte) 0x65,
		(byte) 0x48, (byte) 0x4F, (byte) 0x46, (byte) 0x41, (byte) 0x54, (byte) 0x53, (byte) 0x5A, (byte) 0x5D,
		(byte) 0xE0, (byte) 0xE7, (byte) 0xEE, (byte) 0xE9, (byte) 0xFC, (byte) 0xFB, (byte) 0xF2, (byte) 0xF5,
		(byte) 0xD8, (byte) 0xDF, (byte) 0xD6, (byte) 0xD1, (byte) 0xC4, (byte) 0xC3, (byte) 0xCA, (byte) 0xCD,
		(byte) 0x90, (byte) 0x97, (byte) 0x9E, (byte) 0x99, (byte) 0x8C, (byte) 0x8B, (byte) 0x82, (byte) 0x85,
		(byte) 0xA8, (byte) 0xAF, (byte) 0xA6, (byte) 0xA1, (byte) 0xB4, (byte) 0xB3, (byte) 0xBA, (byte) 0xBD,
		(byte) 0xC7, (byte) 0xC0, (byte) 0xC9, (byte) 0xCE, (byte) 0xDB, (byte) 0xDC, (byte) 0xD5, (byte) 0xD2,
		(byte) 0xFF, (byte) 0xF8, (byte) 0xF1, (byte) 0xF6, (byte) 0xE3, (byte) 0xE4, (byte) 0xED, (byte) 0xEA,
		(byte) 0xB7, (byte) 0xB0, (byte) 0xB9, (byte) 0xBE, (byte) 0xAB, (byte) 0xAC, (byte) 0xA5, (byte) 0xA2,
		(byte) 0x8F, (byte) 0x88, (byte) 0x81, (byte) 0x86, (byte) 0x93, (byte) 0x94, (byte) 0x9D, (byte) 0x9A,
		(byte) 0x27, (byte) 0x20, (byte) 0x29, (byte) 0x2E, (byte) 0x3B, (byte) 0x3C, (byte) 0x35, (byte) 0x32,
		(byte) 0x1F, (byte) 0x18, (byte) 0x11, (byte) 0x16, (byte) 0x03, (byte) 0x04, (byte) 0x0D, (byte) 0x0A,
		(byte) 0x57, (byte) 0x50, (byte) 0x59, (byte) 0x5E, (byte) 0x4B, (byte) 0x4C, (byte) 0x45, (byte) 0x42,
		(byte) 0x6F, (byte) 0x68, (byte) 0x61, (byte) 0x66, (byte) 0x73, (byte) 0x74, (byte) 0x7D, (byte) 0x7A,
		(byte) 0x89, (byte) 0x8E, (byte) 0x87, (byte) 0x80, (byte) 0x95, (byte) 0x92, (byte) 0x9B, (byte) 0x9C,
		(byte) 0xB1, (byte) 0xB6, (byte) 0xBF, (byte) 0xB8, (byte) 0xAD, (byte) 0xAA, (byte) 0xA3, (byte) 0xA4,
		(byte) 0xF9, (byte) 0xFE, (byte) 0xF7, (byte) 0xF0, (byte) 0xE5, (byte) 0xE2, (byte) 0xEB, (byte) 0xEC,
		(byte) 0xC1, (byte) 0xC6, (byte) 0xCF, (byte) 0xC8, (byte) 0xDD, (byte) 0xDA, (byte) 0xD3, (byte) 0xD4,
		(byte) 0x69, (byte) 0x6E, (byte) 0x67, (byte) 0x60, (byte) 0x75, (byte) 0x72, (byte) 0x7B, (byte) 0x7C,
		(byte) 0x51, (byte) 0x56, (byte) 0x5F, (byte) 0x58, (byte) 0x4D, (byte) 0x4A, (byte) 0x43, (byte) 0x44,
		(byte) 0x19, (byte) 0x1E, (byte) 0x17, (byte) 0x10, (byte) 0x05, (byte) 0x02, (byte) 0x0B, (byte) 0x0C,
		(byte) 0x21, (byte) 0x26, (byte) 0x2F, (byte) 0x28, (byte) 0x3D, (byte) 0x3A, (byte) 0x33, (byte) 0x34,
		(byte) 0x4E, (byte) 0x49, (byte) 0x40, (byte) 0x47, (byte) 0x52, (byte) 0x55, (byte) 0x5C, (byte) 0x5B,
		(byte) 0x76, (byte) 0x71, (byte) 0x78, (byte) 0x7F, (byte) 0x6A, (byte) 0x6D, (byte) 0x64, (byte) 0x63,
		(byte) 0x3E, (byte) 0x39, (byte) 0x30, (byte) 0x37, (byte) 0x22, (byte) 0x25, (byte) 0x2C, (byte) 0x2B,
		(byte) 0x06, (byte) 0x01, (byte) 0x08, (byte) 0x0F, (byte) 0x1A, (byte) 0x1D, (byte) 0x14, (byte) 0x13,
		(byte) 0xAE, (byte) 0xA9, (byte) 0xA0, (byte) 0xA7, (byte) 0xB2, (byte) 0xB5, (byte) 0xBC, (byte) 0xBB,
		(byte) 0x96, (byte) 0x91, (byte) 0x98, (byte) 0x9F, (byte) 0x8A, (byte) 0x8D, (byte) 0x84, (byte) 0x83,
		(byte) 0xDE, (byte) 0xD9, (byte) 0xD0, (byte) 0xD7, (byte) 0xC2, (byte) 0xC5, (byte) 0xCC, (byte) 0xCB,
		(byte) 0xE6, (byte) 0xE1, (byte) 0xE8, (byte) 0xEF, (byte) 0xFA, (byte) 0xFD, (byte) 0xF4, (byte) 0xF3
	};
	
	private byte command;
	private byte counter;
	private int length;
	private byte[] data;
	
	public ReplicatorMessage(byte command, byte[] data) {
		this.command = command;
		this.counter = 0;
		this.data = data;
		this.length = data.length;
	}
	
	public ReplicatorMessage(byte command) {
		this.command = command;
		this.counter = 0;
		this.data = null;
		this.length = 0;
	}
	
	public int size() {
		if (length >= 0) {
			return MESSAGE_OVERHEAD + length;
		} else {
			return MESSAGE_OVERHEAD - 1; // -1 because checksum of data missing
		}
	}
	
	public byte[] serialize() {
		byte checksum;
		ByteBuffer bb = ByteBuffer.allocate(size());
		
		byte b = EMSGSTARTVARETH;
		bb.put(b);
		checksum = crc8_byte(CRC8_INIT, b);
		
		b = this.counter;
		bb.put(b);
		checksum = crc8_byte(checksum, b);
		
		b = this.command;
		bb.put(b);
		checksum = crc8_byte(checksum, b);
		
		b = (byte)((this.length >> 24) & 0xFF);
		bb.put(b);
		checksum = crc8_byte(checksum, b);

		b = (byte)((this.length >> 16) & 0xFF);
		bb.put(b);
		checksum = crc8_byte(checksum, b);

		b = (byte)((this.length >> 8) & 0xFF);
		bb.put(b);
		checksum = crc8_byte(checksum, b);

		b = (byte)(this.length & 0xFF);
		bb.put(b);
		checksum = crc8_byte(checksum, b);
		
		bb.put(checksum);
		
		if (length >= 0) {
			
			checksum = CRC8_INIT;
			for (int i = 0; i < length; i++) {
				b = data[i];
				bb.put(b);
				checksum = crc8_byte(checksum, b);
			}
			
			bb.put(checksum);
		}
		
		return bb.array();
	}

	private byte crc8_byte(short crc, byte data) {
		int index = (crc ^ data) & 0xFF;
		return crc8_table[index];
	}

}
