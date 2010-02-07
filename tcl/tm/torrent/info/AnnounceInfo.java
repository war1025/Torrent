package tcl.tm.torrent.info;

import java.io.Closeable;

/**
 * AnnounceInfo is a collection of methods that allows its user to retrieve all of the 
 * required information from the announce tracker.  In order for this to work, the user must 
 * specify some information once.  There is some other information that must be updated constantly.
 * 
 * @author Anthony Milosch
 *
 */
public interface AnnounceInfo extends Closeable {
	/**
	 * Use this method to set the peer id, this should be a unique number for the current machine 
	 * you are working on, should only be set once.
	 * 
	 * @param id This is the id that you are setting.
	 */
	public void setPeerId(String id);
	
	
	/**
	 * Use this to set the port number that the machine is lisning on, should only need to be set once.
	 * 
	 * @param port The port number
	 */
	public void setPortNumber(int port);
	
	/**
	 * Use this to set the number of peers that you wish to have returned from the tracker the next time information is retrieved.
	 * This should be set to a maximum of 30, see the bittorrent spec for more information on that.
	 * 
	 * @param peers The number of peers that you wish to have
	 */
	public void setNumberPeersWanted(int peers);
	
	/**
	 * Use this method to get the number of leachers that are currently downloading the torrent.
	 * Leachers are peers that are downloding the file, but do not have the entire file yet.
	 * 
	 * @return The number of leachers downloading the file
	 */
	public int getLeecherCount();
	
	/**
	 * Use this method to get the number of seeders that are currently uploading the torrent.
	 * Seeders are peers that are uploading the file, they allready have the entire thing.
	 * 
	 * @return The number of seeders that are uploading the file
	 */
	public int getSeederCount();
	
	/**
	 * Use the method to retrieve the list of peers that you have avaliable.
	 * 
	 * @return This is a string arrayList, the strings are formatted as follows xxx.xxx.xxx.xxxx:xxxx
	 */
	public String[] getPeers();
	
	/**
	 * This will return the number of peers that you have available to you, note that this number is not
	 * always the same of the number requested.
	 * 
	 * @return The number of peers that you have avalable to you.
	 */
	public int getPeerCount();
	
	public void startTracker();
	
	public boolean hasStarted();
	
}
