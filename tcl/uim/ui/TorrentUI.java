package tcl.uim.ui;

import javax.management.MXBean;

import tcl.uim.util.TorrentStats;

//make this an MXBean so this will be able to communicate all the information that we need.
@MXBean
/**
 * This interface is the MXBean for the communication with the frontend, it has  simple
 * method that get the data requested, or start, stop, delete, or relocate the torrent.
 * 
 * @author Tony Milosch
 * @author Wayne Rowcliffe
 */
public interface TorrentUI {
	
	/**
	 * Method that will return the information that you request, all of this information is mapped
	 * Right now you will input the file name and the property you are requesting for that file
	 * Property Available
	 * speed, ETA, seeders, leechers, filename, fileSize, numPieces, numPiecesUploaded, 
	 * numPiecesDownloaded, numBytesUp, numBytesDown
	 * 
	 * In the case of filename and filesize, if there are more than one file, the information will
	 * be returned in more array
	 * 
	 * @param property one of the properties available from above
	 * @param filePath the filePath of the file you are requesting information about
	 * @return the string array of the information being returned
	 */
	public String[] get(String property, String filePath);
	
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
	 * This method will return a list of all torrents avalable for data lookup on the server
	 * 
	 * @return the string array of torrents avalable
	 */
	public String[] getTorrentsAvailable();
	
	/**
	 * This method will return a list of al the properties avalable
	 * 
	 * @return The String array of Properties Available
	 */
	public String[] getPropertiesAvailable();
	
	/**
	 * This method will return all the data about everything
	 * 
	 * @return The string returning everything
	 */
	public String[][] getAll();
	
	public TorrentStats[] getTorrentStats();
	
	public void shutDown();
	
}

