package tcl.tm.torrent.communication.util;

import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;

/**
 * An OutputStream which can be throttled to a given rate in bytes/second.
 * In addition the current speed of data transfer through this stream can be measured.
 * 
 * @author Wayne Rowcliffe
 **/
public class ThrottledInputStream extends FilterInputStream implements Throttleable {
	
	private int throttle;
	private int read;
	private int lastRead;
	private Thread throttler;
	private Object lock = new Object();
	private boolean running;
	private int[] aveArr; 
	
	/**
	 * Creates a ThrottledInputStream from the given InputStream.
	 * This constructor does not place a throttle cap on the stream,
	 * However, the connection speed over the stream will be measurable.
	 * 
	 * @param in The InputStream to wrap this ThrottledInputStream around.
	 **/	
	public ThrottledInputStream(InputStream in) {
		this(in,-1);
	}
	
	/**
	 * Creates a ThrottledInputStream from the given InputStream.
	 * This constructor places the given throttle cap on the stream.
	 * In addition, connection speed can be measured through this stream.
	 * 
	 * @param in The InputStream to wrap this ThrottledInputStream around.
	 * @param throttle The number of bytes/second to throttle this stream at.
	 **/
	public ThrottledInputStream(InputStream in, int throttle) {
		super(in);
		setThrottleSpeed(throttle);
		this.read = 0;
		this.lastRead = 0;
		this.throttler = new Thread(new Throttler(), "Input Throttler");
		this.running = true;
		this.aveArr = new int[5];
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
	
	public int read() throws IOException{
		synchronized(lock) {
			while(throttle >= 0 && read > throttle) {
				try {
					lock.wait();
				} catch(InterruptedException e) {}
			}
			read++;
		}
		return in.read();	
	}
	
	public int read(byte[] b, int offset, int length) throws IOException {
		int total = 0;
		int actual = 0;
		while(length > 0 && actual >= 0) {
			int toRead = 0;
			synchronized(lock) {
				length -= actual;
				offset += actual;
				total += actual;
				read += actual;
				while(throttle >= 0 && read >= throttle) {
					try {
						lock.wait();
					} catch(InterruptedException e) {}
				}
				toRead = (length < (throttle - read) || throttle < 0 ) ? length : (throttle - read);
			}
			if(toRead > 0) {
				actual = in.read(b,offset,toRead);
			}
		}
		return total;
	}
	
	/**
	 * Closes this ThrottledInputStream.
	 * It is VERY important that a ThrottledStream is always closed.
	 * Programs may behave unexpectedly if this method is not called when the 
	 * ThrottledStream is no longer needed.
	 **/	
	public void close() throws IOException {
		running = false;
		in.close();
	}
	
	/**
	 * Resets the byte count which keeps track of how many bytes have been
	 * Read from the stream in the past second.
	 * Also updates the rolling average for the connection speed.
	 **/
	private void resetCount() {
		synchronized(lock) {
			aveArr[lastRead] = read;
			lastRead += 1;
			if(lastRead >= 5) {
				lastRead = 0;
			}
			read = 0;
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
