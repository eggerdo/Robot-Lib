package org.dobots.zmq.video;


public interface IFpsListener {
	/**
	 * NOTE THAT CHANGING UI ELEMENTS HAVE TO BE DONE IN THE MAIN THREAD, HOWEVER
	 * THIS FUNCTION IS NOT CALLED IN THE MAIN THREAD.
	 * @param i_nFPS
	 */
	public void onFPS(int i_nFPS);
}

