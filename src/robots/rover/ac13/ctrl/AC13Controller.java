package robots.rover.ac13.ctrl;

// This Code is based on Anne van Rossum's code (RoverOpen) with some additions
// taken from Ucetra's library (which can be found at 
// https://sourceforge.net/projects/ac13javalibrary/)

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import robots.rover.base.ctrl.RoverBaseController;
import robots.rover.base.ctrl.RoverBaseTypes;

public class AC13Controller extends RoverBaseController {
	
	public static final String TAG = "AC13Ctrl";

	// defines the start of an image
	private static final byte[] VIDEO_START = new byte[] { 'M', 'O', '_', 'V' };
	
	// TCP/IP sockets
	private Socket cSock;
	private Socket vSock;
	
	private DataInputStream mVideoIn;

	// Infrared on/off
	boolean infrared;

	// The maximal image buffer will be sufficient for hi-res images, notice
	// that jpeg images do not have a default image size. The tcp buffer is
	// large enough for the packages send by the Rover, but the latter tends
	// to chop images across multiple TCP chunks so it is not large enough
	// for one image.
	int maxTCPBuffer = 2048;
	int maxImageBuffer = 131072;
	byte[] imageBuffer = new byte[maxImageBuffer];
	int imagePtr = -1;
	int tcpPtr = 0;
	
	public AC13Controller() {
		m_strTargetHost = AC13RoverTypes.ADDRESS;
		m_nTargetPort = AC13RoverTypes.PORT;
		targetId = AC13RoverTypes.ID;
		targetPassword = AC13RoverTypes.PWD;

		parameters = new RoverBaseTypes().new RoverParameters();
	}
	
	public void close() {
		// nothing to do
	}

	public boolean isConnected() {
		return m_bConnected;
	}

	public boolean startStreaming() {
		if (!m_bStreaming) {
			m_bStreaming = true;
			Thread vThread = new Thread(new VideoThread(), "AC13Video");
			vThread.start();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean connect() {

		try {
			debug(TAG, "connecting...");
			
			if (m_strTargetHost == null) {
				error(TAG, "address not defined");
				return false;
			}
			
			//Initializing command socket
			SocketAddress sockaddr = new InetSocketAddress(m_strTargetHost, m_nTargetPort);
			cSock = new Socket();
			cSock.connect(sockaddr, CONNECT_TIMEOUT);
				
			//Setting the connection
			writeStart();
			receiveAnswer(0);
			
			cSock.close();
			
			//Reinitializing the command socket
			cSock = new Socket();
			cSock.connect(sockaddr, CONNECT_TIMEOUT);
			
			byte[] buffer = new byte[2048];
			
			for (int i = 1; i < 4; i++) {
				
				writeCmd(i, null);
				buffer = receiveAnswer(i);
			}
			
			byte[] imgid = new byte[4];
			
			for (int i = 0; i < 4; i++)
				imgid[i] = buffer[i + 25];

			vSock = new Socket();
			vSock.connect(sockaddr, CONNECT_TIMEOUT);
			writeCmd(4, imgid);
			
			mVideoIn = new DataInputStream(new BufferedInputStream(vSock.getInputStream()));

			requestAllParameters();
			
			startStreaming();
			
			m_bConnected = true;
			
		} catch (Exception e) {
			 return false;	
		}
		
		return true;
	}

	public boolean disconnect(){
		
		try {
		    m_bStreaming = false;
		    
		    if(infrared)
		    	switchInfrared();
		    
			cSock.close();
			vSock.close();
			
			mVideoIn = null;
			
			m_bConnected = false;
			
		} catch (Exception e) {
			return false;
		}
		
		return true;	
	}

	public void keepAlive() {
		debug(TAG, "keepAlive");
		writeCmd(1, null);
	}

	public void switchInfrared() {
		
		if (infrared)
			disableInfrared();
		else
			enableInfrared();
	}
	
	public void enableInfrared() {
		infrared = true;
		writeCmd(10,null);
	}

	public void disableInfrared() {
		infrared = false;
		writeCmd(11,null);
	}

	public void stopStreaming() {
		m_bStreaming = false;
	}

	public boolean isInfraredEnabled() {
		return infrared;
	}
	
	public boolean isStreaming(){
		return m_bStreaming;	
	}
	
	// STOP

	protected void moveLeftStop() {
		moveLeftForward((byte)0);
	}
	
	protected void moveRightStop() {
		moveRightForward((byte)0);
	}
	
	// FORWARD
	
	protected void moveLeftForward(int velocity){
		writeCmd(5, new byte[] {(byte) velocity});	
	}
	
	protected void moveRightForward(int velocity){
		writeCmd(7, new byte[] {(byte) velocity});
	}
	
	// BACKWARD

	protected void moveLeftBackward(int velocity){
		writeCmd(6, new byte[] {(byte) velocity});
	}
	
	protected void moveRightBackward(int velocity){
		writeCmd(8, new byte[] {(byte) velocity});	
	}
	
	/**
	 * Separate thread to handle TCP/IP data stream. The result is stored in
	 * image1 and image2. It is communicated to the original activity via the
	 * handler.
	 */
	private class VideoThread implements Runnable {
		public void run() {
			try {
				while (m_bConnected && m_bStreaming) {
					receiveImage();
				}

			} catch (Exception e) {
				error(TAG, "Socket read error", e);
			}
		}

	}
	
	private void writeStart() {
		try {
			debug(TAG, "HTTP GET cmd (authentication)");
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(cSock.getOutputStream())), true);
			out.println("GET /check_user.cgi?user=AC13&pwd=AC13 HTTP/1.1\r\nHost: 192.168.1.100:80\r\nUser-Agent: WifiCar/1.0 CFNetwork/485.12.7 Darwin/10.4.0\r\nAccept: */*\r\nAccept-Language: en-us\r\nAccept-Encoding: gzip, deflate\r\nConnection: keep-alive\r\n\r\n!");
		} catch (Exception e) {
			error(TAG, "S: Error", e);
			e.printStackTrace();
		}
	}

	private byte[] receiveAnswer(int i) {
		byte[] buffer = new byte[2048];
		try {
			int len = cSock.getInputStream().read(buffer, 0, 2048);
			if (len > 0) {
				String str = new String(buffer, 0, len);
				debug(TAG, String.format("Read i=%d, str=%s", i, str));
			}
		} catch (Exception eg) {
			error(TAG, "General: input stream error", eg);
			eg.printStackTrace();
		}
		return buffer;
	}

	private void receiveImage() {
		if (!m_bConnected) {
			return;
		}

		int len = 0;
		int newPtr = tcpPtr;
		int imageLength = 0;
		int newImagePtr = -1;
		
		try {
			boolean fnew = false;
			while (!fnew && newPtr < maxImageBuffer - maxTCPBuffer) {
				// read a chunk of the image
				len = mVideoIn.read(imageBuffer, newPtr, maxTCPBuffer);
				
				// TODO: check if this happens too often and exit
				if (len <= 0) {
					m_bConnected = false;
					return;
				}
				
				// an image can be split into several messages, so
				// look for the beginning of an image, defined by VIDEO_START
				newImagePtr = findArray(imageBuffer, VIDEO_START, newPtr, len);
				
				// if found ...
				if (newImagePtr != -1) {
					// ... check if it is the first image received so far
					if (imagePtr == -1) {
						// ... assign the pointer to imagePtr and read
						// the next chunck
						imagePtr = newImagePtr;
						continue;
					} else {
						// a new image start was found
						fnew = true;
						// image length is the length of the date between 
						// the newImagePtr and the imagePtr 
						imageLength = newImagePtr - imagePtr;
					}
				}
				
				// if we are still reading the current image
				if (!fnew) {
					// increase the pointer by the number of bytes read this round
					newPtr += len;
				} else {
					// otherwise copy the image from the buffer
					
					byte[] rgb = new byte[imageLength - 36];
					System.arraycopy(imageBuffer, imagePtr + 36, rgb, 0, rgb.length);
					
					if (rgb.length > 0) {
						// and send it to the listener
						if (oVideoListener != null) {
							oVideoListener.onFrame(rgb, 0);
						}
					}
					
					// check for buffer overflow
					if (newPtr > maxImageBuffer / 2) {
						// copy everything since the last image to the
						// beginning of the buffer
						int length = newPtr + len - imagePtr;
//						for (int i = 0; i < length; i++)
//							imageBuffer[i] = imageBuffer[imagePtr + i];
						System.arraycopy(imageBuffer, imagePtr, imageBuffer, 0, length);
						
						// and reset the pointers
						imagePtr = 0;
						tcpPtr = length;
					} else {
						// otherwise update the pointers
						imagePtr = newImagePtr;
						tcpPtr = newPtr + len;
					}
				}
			}
			
			// reset if ptr runs out of boundaries
			if (newPtr >= maxImageBuffer - maxTCPBuffer) {
				warn(TAG, "Out of index, should not happen!");
				imagePtr = 0;
				tcpPtr = 0;
			}
		} catch (Exception eg) {
			error(TAG, "General input stream error", eg);
			eg.printStackTrace();
		}
	}

//	private boolean imgStart(byte[] start) {
//		return (start[0] == 'M' && start[1] == 'O' && start[2] == '_' && start[3] == 'V');
//	}
//
//	private void receiveImage() {
//		debug("ReceiveImage", "Get image");
//		int len = 0;
//		int newPtr = tcpPtr;
//		int imageLength = 0;
//		try {
//			boolean fnew = false;
//			while (!fnew && newPtr < maxImageBuffer - maxTCPBuffer) {
//				len = vSock.getInputStream().read(imageBuffer, newPtr,
//						maxTCPBuffer);
//				// todo: check if this happens too often and exit
//				if (len <= 0) {
//					m_bConnected = false;
//				}
//
//				byte[] f4 = new byte[4];
//				for (int i = 0; i < 4; i++)
//					f4[i] = imageBuffer[newPtr + i];
//				if (imgStart(f4) && (imageLength > 0))
//					fnew = true;
//				if (!fnew) {
//					newPtr += len;
//					imageLength = newPtr - imagePtr;
//				} else {
//					debug(TAG, "Total image size is "
//							+ (imageLength - 36));
//
//					byte[] rgb = new byte[imageLength - 36];
//					//                                    Log.w(TAG, String.format("imagelen: %d", rgb.length));
//					System.arraycopy(imageBuffer, imagePtr + 36, rgb, 0, rgb.length);
//					if (rgb.length > 0) {
//						if (oVideoListener != null) {
//							oVideoListener.onFrame(rgb, 0);
//						}
//					}
//					if (newPtr > maxImageBuffer / 2) {
//						// copy first chunk of new arrived image to start of
//						// array
//						for (int i = 0; i < len; i++)
//							imageBuffer[i] = imageBuffer[newPtr + i];
//						imagePtr = 0;
//						tcpPtr = len;
//					} else {
//						imagePtr = newPtr;
//						tcpPtr = newPtr + len;
//					}
////					debug("Var", "imagePtr =" + imagePtr);
////					debug("Var", "tcpPtr =" + tcpPtr);
////					debug("Var", "imageLength =" + imageLength);
////					debug("Var", "newPtr =" + newPtr);
////					debug("Var", "len =" + len);
//				}
//			}
//			// reset if ptr runs out of boundaries
//			if (newPtr >= maxImageBuffer - maxTCPBuffer) {
//				warn(TAG, "Out of index, should not happen!");
//				imagePtr = 0;
//				tcpPtr = 0;
//			}
//		} catch (Exception eg) {
//			error(TAG, "General input stream error", eg);
//			eg.printStackTrace();
//		}
//	}
	
//	private int[] findAll(byte[] array, byte[] subArray) {
//		ArrayList<Integer> list = new ArrayList<Integer>();
//		int index = -1;
//		do {
//			index = findArray(array, subArray, index + 1);
//			if (index != -1) {
//				list.add(index);
//			}
//		} while (index != -1);
//		// needs commons-lang3 -> http://commons.apache.org/proper/commons-lang/
//		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
//	}
//	
//	private int findArray(byte[] array, byte[] subArray)
//    {
//    	return findArray(array, subArray, 0);
//    }
//    
//	private int findArray(byte[] array, byte[] subArray, int start)
//    {
//        if (array == null || subArray == null || subArray.length > array.length || start > array.length - 1)
//        {
//            return -1;
//        }
//         
//        int index = -1; // assume not found
//         
//        for (int i = start; i < array.length; i++)
//        {
//            // Check if the next element of array is same as the first element of subarray
//            if (array[i] == subArray[0])
//            {
//                //check subsequent elements of subarray against the subsequent elements of array
//                for (int j = 0; j < subArray.length; j++)
//                {
//                    //if found, set the index
//                    if (i + j < array.length && subArray[j] == array[i + j])
//                    {
//                        index = i;
//                    }
//                    else
//                    {
//                        index = -1;
//                        break;
//                    }
//                }
//                
//                if (index != -1) {
//                	return index;
//                }
//            }
//        }
//         
//        return index;
//         
//    }
    
	/**
	 * searches for the subArray in the array. the search starts at index start, and only checks for
	 * length bytes in the array, even if the array is longer. returns the index where the subArray
	 * starts.
	 * @param array		array to search in
	 * @param subArray  subarray to be searched
	 * @param start		start index for search
	 * @param length	maximum number of bytes to check in the array
	 * @return			index where the subArray was found
	 */
	private int findArray(byte[] array, byte[] subArray, int start, int length)
    {
        if (array == null || subArray == null || subArray.length > array.length || start > array.length - 1)
        {
            return -1;
        }
         
        int index = -1; // assume not found
        int end = Math.min(start + length, array.length); 
        
        for (int i = start; i < end; i++)
        {
            // Check if the next element of array is same as the first element of subarray
            if (array[i] == subArray[0])
            {
                //check subsequent elements of subarray against the subsequent elements of array
                for (int j = 0; j < subArray.length; j++)
                {
                    //if found, set the index
                    if (i + j < array.length && subArray[j] == array[i + j])
                    {
                        index = i;
                    }
                    else
                    {
                        index = -1;
                        break;
                    }
                }
                
                if (index != -1) {
                	return index;
                }
            }
        }
         
        return index;
         
    }
	
//    public int findArray(byte[] array, byte[] subArray)
//    {
//        if (array == null || subArray == null || subArray.length > array.length)
//        {
//            return -1;
//        }
//         
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < array.length; i++)
//        {
//            sb.append(array[i]);
//        }
//         
//        StringBuilder sbSub = new StringBuilder();
//        for (int i = 0; i < subArray.length; i++)
//        {
//            sbSub.append(subArray[i]);
//        }
//         
//        int index = sb.toString().indexOf(sbSub.toString());
//        if (index >= 0)
//        {
//            return index;
//        }
//        else
//        {
//            return -1;
//        }
//    }
    
	private void writeCmd(int index, byte[] extra_input) {
		int len = 0;
		switch (index) {
		case 1:
			len = 23;
			break;
		case 2:
			len = 49;
			break;
		case 3:
			len = 24;
			break;
		case 4:
			len = 27;
			break;
		case 5: // forward, right
			len = 25;
			break;
		case 6: // backward, right
			len = 25;
			break;
		case 7: // forward, left
			len = 25;
			break;
		case 8: // backward, left
			len = 25;
			break;
		case 9:
			len = 23;
			break;
		case 10: // infrared ON
			len = 24;
			break;
		case 11: // infrared OFF
			len = 24;
			break;
		}
		byte[] buffer = new byte[len];
		for (int i = 4; i < len; i++)
			buffer[i] = '\0';
		buffer[0] = 'M';
		buffer[1] = 'O';
		buffer[2] = '_';
		buffer[3] = 'O';
		if (index == 4) {
			buffer[3] = 'V';
		}

		switch (index) {
		case 1:
			break;
		case 2:
			buffer[4] = 0x02;
			buffer[15] = 0x1a;
			buffer[23] = 'A';
			buffer[24] = 'C';
			buffer[25] = '1';
			buffer[26] = '3';
			buffer[36] = 'A';
			buffer[37] = 'C';
			buffer[38] = '1';
			buffer[39] = '3';
			break;
		case 3:
			buffer[4] = 0x04;
			buffer[15] = 0x01;
			buffer[19] = 0x01;
			buffer[23] = 0x02;
			break;
		case 4:
			buffer[15] = 0x04;
			buffer[19] = 0x04;
			for (int i = 0; i < 4; i++)
				buffer[i + 23] = extra_input[i];
			break;
		case 5: // forward, left
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x04;
			buffer[24] = extra_input[0];
			break;
		case 6: // backward, left
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x05;
			buffer[24] = extra_input[0];
			break;
		case 7: // forward, right
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x01;
			buffer[24] = extra_input[0];
			break;
		case 8: // backward, right
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x02;
			buffer[24] = extra_input[0];
			break;
		case 9: // IR off(?)
			buffer[4] = (byte) 0xff;
			break;
		case 10: // infrared ON
			buffer[4] = (byte) 0x0e;
			buffer[15] = 0x01;
			buffer[19] = 0x01;
			buffer[23] = (byte)0x5e;
			break;
		case 11: // infrared OFF
			buffer[4] = (byte) 0x0e;
			buffer[15] = 0x01;
			buffer[19] = 0x01;
			buffer[23] = (byte)0x5f;
			break;
		}

//		String str = new String(buffer, 0, len);
//		debug(TAG, String.format("Write i=%d, str=%s", index, str));
		if (index != 4) {
			try {
				cSock.getOutputStream().write(buffer, 0, len);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				vSock.getOutputStream().write(buffer, 0, len);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
