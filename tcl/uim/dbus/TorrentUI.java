package tcl.uim.dbus;

import org.freedesktop.dbus.DBusInterface;

import java.util.List;
import java.util.Map;


/**
 * This is the DBus interface for the communication with the frontend, it has  simple
 * method that get the data requested, or start, stop, delete, or relocate the torrent.
 * 
 * @author Wayne Rowcliffe
 **/
public interface TorrentUI extends DBusInterface {
	
	/**
	 * This method will start the torrent downlaod, this will resume a download if it has 
	 * ready been downloaded before
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return ture if it has started downloading false otherwise
	 */
	public boolean Start(String filePath);
	
	/**
	 * This method will stop the torrent downlaod.
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return true if the file has stopped downloading, false otherwise
	 */
	public boolean Stop(String filePath);
	
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
	public boolean Relocate(String filePath, String newFilePath);
	
	/**
	 * This method will remove a file from the queue/ stopdownloading and 
	 * delete, or delete if allready downloaded
	 * 
	 * @param filePath the filePath to the origional filepath in question
	 * 
	 * @return true if sucessful false otherwise
	 */
	public boolean Remove(String filePath);
	
	/**
	 * This method will tell the user if the torrent is competly downloaded and ready 
	 * for upload.
	 * 
	 * @param filePath the filePath to the file is question
	 * 
	 * @return true if it is complete false otherwise
	 */
	public boolean IsComplete(String filePath);
	
	
	/**
	 * This method will return a list of all torrents avalable for data lookup on the server
	 * 
	 * @return the string array of torrents avalable
	 */
	public String[] GetTorrentsAvailable();
	
	public List<Map<String,String>> GetTorrentStats();
	
	@org.freedesktop.DBus.Method.NoReply
	public void ShutDown();
	
}

