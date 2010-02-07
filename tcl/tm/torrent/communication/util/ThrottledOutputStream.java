package tcl.tm.torrent.communication.util;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * An OutputStream which can be throttled to a given rate in bytes/second.
 * In addition the current speed of data transfer through this stream can be measured.
 * 
 * @author Wayne Rowcliffe
 **/
public class ThrottledOutputStream extends FilterOutputStream implements Throttleable {
	
	private int throttle;
	private int written;
	private int lastWritten;
	private Thread throttler;
	private Object lock = new Object();
	private boolean running;
	private int[] aveArr;
	
	/**
	 * Creates a ThrottledOutputStream from the given OutputStream.
	 * This constructor does not place a throttle cap on the stream,
	 * However, the connection speed over the stream will be measurable.
	 * 
	 * @param out The OutputStream to wrap this ThrottledOutputStream around.
	 **/
	public ThrottledOutputStream(OutputStream out) {
		this(out,-1);
	}
	
	/**
	 * Creates a ThrottledOutputStream from the given OutputStream.
	 * This constructor places the given throttle cap on the stream.
	 * In addition, connection speed can be measured through this stream.
	 * 
	 * @param out The OutputStream to wrap this ThrottledOutputStream around.
	 * @param throttle The number of bytes/second to throttle this stream at.
	 **/
	public ThrottledOutputStream(OutputStream out, int throttle) {
		super(out);
		setThrottleSpeed(throttle);
		this.written = 0;
		this.lastWritten = 0;
		this.aveArr = new int[5];
		this.throttler = new Thread(new Throttler(),"Output Throttler");
		this.running = true;
		throttler.start();
	}
	
	/**
	 * Returns the connection speed of this stream.
	 * Speed is provided as a rolling average of the past 5 seconds.
	 * 
	 * @return The average speed of this stream over the past 5 seconds.
	 **/
	public int getConnectionSpeed() {
		int sum = 0;
		for(int i : aveArr) {
			sum += i;
		}
		return sum/aveArr.length;
	}
	
	/**
	 * Returns the speed this stream is being capped at.
	 * 
	 * @return The throttle speed for this stream.
	 **/
	public int getThrottleSpeed() { 
		return throttle;
	}
	
	/**
	 * Sets the throttle cap for this stream to the given throttle.
	 * 
	 * @param throttle The speed to cap this stream at.
	 **/
	public void setThrottleSpeed(int throttle) {
		synchronized(lock) {
			this.throttle = throttle < 0 ? -1 : throttle;
		}
	}
	
	public void write(int i) throws IOException {
		synchronized(lock) {
			while(throttle >= 0 && written > throttle) {
				try {
					lock.wait();
				} catch(InterruptedException e) {}
			}
			written++;
		}
		out.write(i);	
	}
	
	public void write(byte[] b) throws IOException {
		write(b,0,b.length);
	}
	
	public void write(byte[] b, int offset, int length) throws IOException {
		while(length > 0) {
			int toWrite = 0;
			synchronized(lock) {
				while(throttle >= 0 && written >= throttle) {
					try {
						lock.wait();
					} catch(InterruptedException e) {}
				}
				toWrite = (length < (throttle - written) || throttle < 0 ) ? length : (throttle - written);
				length -= toWrite;
				written += toWrite;
			}
			out.write(b,offset,toWrite);
			offset += toWrite;
		}
	}
	
	/**
	 * Closes this ThrottledOutputStream.
	 * It is VERY important that a ThrottledStream is always closed.
	 * Programs may behave unexpectedly if this method is not called when the 
	 * ThrottledStream is no longer needed.
	 **/
	public void close() throws IOException {
		running = false;
		out.close();
	}
	
	/**
	 * Resets the byte count which keeps track of how many bytes have been
	 * Written to the stream in the past second.
	 * Also updates the rolling average for the connection speed.
	 **/
	private void resetCount() {
		synchronized(lock) {
			aveArr[lastWritten] = written;
			lastWritten += 1;
			if(lastWritten >= 5) {
				lastWritten = 0;
			}
			written = 0;
			lock.notifyAll();
		}
	}
	
	/**
	 * A synchronized boolean so all of the threads will know if we are running or not.
	 * 
	 * @return Whether or not this stream is still running.
	 **/
	private synchronized boolean isRunning() {
		return running;
	}
	
	/**
	 * The Throttler accesses the ThrottledStream periodically to enforce the throttle quota.
	 * Since it runs on a separate thread it is VERY IMPORTANT that close() is called on this 
	 * stream when it is done being used. Otherwise the Throttler thread will not die.
	 **/
	private class Throttler implements Runnable {
		
		/**
		 * Periodically enforces the throttle quota and updates the connection speed
		 **/
		public void run() {
			while(isRunning()) {
				resetCount();
				try{
					Thread.sleep(1000);
				} catch(InterruptedException e) {}
			}
			resetCount();
			resetCount();
		}
	}
}
