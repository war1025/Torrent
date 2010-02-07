package tcl.tm.torrent.communication.util;

/**
 * This is the interface for the Throtterable.  This lets your assign a specific 
 * speed and it will throttle it.  Pretty cool huh?
 * 
 * @author Wayne Rowcliffe
 *
 */
public interface Throttleable 
{
	/**
	 * This will set the throttle speed.
	 * 
	 * @param throttle The speed that you want to be limited too.
	 */
	public void setThrottleSpeed(int throttle);
	
	/**
	 * This will return the ThrottleSpeed.
	 * 
	 * @return The Throttle speed that you set earlier.
	 */
	public int getThrottleSpeed();
	
	/**
	 * This will return the ConnectionSpeed.
	 * 
	 * @return The speed of the connection.
	 */
	public int getConnectionSpeed();
	
}
