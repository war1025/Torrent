package tcl.tm;

import tcl.tm.torrent.Torrent;

import java.io.Closeable;


/**
 * The TorrentManager is responsible for managing the starting and stopping of Torrents.
 * It is also responsible for keeping track of which Torrents are running and 
 * for making information about the Torrents available to the UI.
 * 
 * @author Anthony Milosch 
 * @author Wayne Rowcliffe
 **/
public interface TorrentManager extends Closeable {
	
	/**
	 * This method will start the torrent downlaod, this will resume a download if it has 
	 * ready been downloaded before
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return ture if it has started downloading false otherwise
	 */
	public boolean start(String filePath);
	
	/**
	 * This method will stop the torrent downlaod.
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return true if the file has stopped downloading, false otherwise
	 */
	public boolean stop(String filePath);
	
	/**
	 * This method will relocate the downloaded torrent file, after the relocate is complete
	 * the backend will lose track of it, and completly disregard it.
	 * 
	 * @param filePath the path to the .torrent file that corresponds to the file set to be relocated
	 * @param newFilePath the new filepath where the file will be located
	 * 
	 * @return true if the relocate was sucessful, false if the file you are trying 
	 * to download cannot be relocated
	 */
	public boolean relocate(String filePath, String newFilePath);
	
	/**
	 * This method will remove a file from the queue/ stopdownloading and 
	 * delete, or delete if allready downloaded
	 * 
	 * @param filePath the filePath to the origional filepath in question
	 * 
	 * @return true if sucessful false otherwise
	 */
	public boolean remove(String filePath);
	

	/**
	 * This method will tell the user if the torrent is competly downloaded and ready 
	 * for upload.
	 * 
	 * @param filePath the filePath to the file is question
	 * 
	 * @return true if it is complete false otherwise
	 */
	public boolean isComplete(String filePath);
	

	/**
	 * This method will return a list of all torrents available for data lookup on the server
	 * 
	 * @return the string array of torrents available
	 */
	public String[] getTorrentsAvailable();
	
	/**
	 * This method will return the torrent object for a given filepath
	 * 
	 * @param filePath The path on the system to the .torrent file
	 * 
	 * @return The torrent object for that filePath if it exists, else null.
	 **/
	public Torrent getTorrent(String filePath);
	
	/**
	 * This method will return the torrent object for a given escaped info hash
	 * 
	 * @param escapedHash The escaped info_hash for the .torrent file
	 * 
	 * @return The torrent object for that escapedInfoHash if it exists, else null.
	 **/
	public Torrent getTorrentByHash(String escapedHash);
}
