package robots.gui.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Handler;

public interface IRobotConnection {

	/**
	 * Open the connection, establish I/O streams
	 * @return true if successful, false otherwise
	 */
	boolean open();
	/**
	 * Close the connection and all streams
	 * @throws IOException
	 */
	void close() throws IOException;
	/**
	 * Check if it is connected
	 * @return true if connected, false otherwise
	 */
	boolean isConnected();

	/**
	 * Get the output stream to write, if anything more has to be
	 * done except the send(byte[[] message)
	 * @return Output stream object
	 */
	OutputStream getOutputStream();
	/**
	 * Get the input stream to read, if anything else has to be done
	 * which is not provided by String readLine(), int read() or
	 * int read(byte[] reply, int i, int length)
	 * @return Input stream object
	 */
	InputStream getInputStream();
	/**
	 * Helper function which can be called if writing to the output
	 * stream fails. will send a report to the UI and set the connected
	 * flag to false
	 * @param e error thrown when writing to the output stream
	 */
	void onWriteError(IOException e);
	/**
	 * Helper function which can be called if reading from the input
	 * stream fails. will send a report to the UI and set the connected
	 * flag to false
	 * @param e error thrown when writing to the output stream
	 */
	void onReadError(IOException e);
	
	/**
	 * Get the address of the devices (MAC address) to which this
	 * connection is established
	 * @return MAC address
	 */
	String getAddress();

	/**
	 * Send a message. writes the message to the output stream
	 * @param message to be sent
	 */
	void send(byte[] message);

	/**
	 * Send a message. writes the message to the output stream
	 * @param message to be sent
	 */
	void send(String message);

	/** 
	 * Check if data is available to be read from the input stream
	 * @return true if data available, false otherwise
	 */
	boolean isDataAvailable();

	/**
	 * Read a string terminated with an newline '\n' from the input
	 * stream. reads (blocks) until the newline character is found.
	 * To avoid blocking, only read if isDataAvialable() returns true 
	 * @return message received
	 */
	String readLine();
	
	/**
	 * Read a byte from the input stream. blocks until data is available.
	 * To avoid blocking, only read if isDataAvialable() returns true 
	 * @return byte received
	 */
	int read();

	/**
	 * Read a maximum number of length bytes from the input stream and store 
	 * them in the buffer, starting at offset. blocks until data is available.
	 * To avoid blocking, only read if isDataAvialable() returns true 
	 * @return number of bytes actually read
	 */
	int read(byte[] buffer, int offset, int length);
	
	// can we skip this??? -> to avoid android dependency
	/**
	 * Set the UI handler which will receive error messages to display on screen
	 * @param handler the UI handler which will handle the messages
	 */
	void setReceiveHandler(Handler handler);
	
}
