package tcl.uim;

import java.io.Closeable;

import java.util.List;
import java.util.Map;

/**
 * This class is the interface for the UIManager, it has all of the methods included in the TorrentUI
 * The implementation of this class will be what is used to get the data and send it along.
 * 
 * @author Anthony Milosch
 *
 */
public interface UIManager extends Runnable, Closeable {

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
	 * @param filePath the origional filepath
	 * @param newFilePath the new filepath where the file will be located
	 * 
	 * @return true if the relocate was sucessful, false if the file you are trying 
	 * to download cannot be relocated
	 */
	public boolean relocate(String filePath, String newFilePath);
	
	/**
	 * This method will remove a file from the queue/ stop downloading and 
	 * delete, or delete if already downloaded
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
	
	public List<Map<String,String>> getTorrentStats();

}
