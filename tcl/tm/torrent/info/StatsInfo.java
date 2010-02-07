package tcl.tm.torrent.info;

/**
 * This is a collection of methods to get the statictics for a torrent.   This will be a 
 * central place to store, and have access to all of the Statisticts for a given torrent. 
 * This class will be called by different methods including the UIManger to the frontend will 
 * have access to the data.
 * 
 * 
 * @author Anthony Milosch
 *
 */
public interface StatsInfo 
{
	/**
	 * This method will return the speed that the torrent is currently downloading at. 
	 * Will be returned in KB/s
	 * 
	 * @return The speed of download in KB/s
	 */
	public int getSpeed();
	
	/**
	 * This method will return the number of bytes that are in the torrent total, like all files added
	 * together.
	 * 
	 * @return The Size of the Torrent.
	 */
	public long getTorrentSize();
	
	/**
	 * This will return the number of peers that we are currently downloading from
	 * 
	 * @return The number of peers that we are downloading from
	 */
	public int getNumPeers();
	
	/**
	 * This will return the estimated time of arival for the torrent, in seconds.
	 * 
	 * @return The number of seconds until the torrent will be downloaded.
	 */
	public long getETA();
	
	/**
	 * The will return the number of Pieces that still need to be downloaded.
	 * 
	 * @return The number of Pieces that need to be downloaded.
	 */
	public int getNumPiecesLeft();
	
	/**
	 * This will return the number of bytes that have been downloaded for the given torrent.
	 * 
	 * @return The number of bytes that have beed downloaded for the given torrent.
	 */
	public long getNumBytesDownloaded();
	
	/**
	 * This will return the number of bytes that have been uploaded for the given torrent
	 * 
	 * @return The number of bytes that have been uploaded for thr given torrent.
	 */
	public long getNumBytesUploaded();
	
	/**
	 * This will return the number of seeders as given by the tracker for the given torrent.
	 * 
	 * @return The number of seeders for the given torrent.
	 */
	public int getNumSeeders();
	
	/**
	 * This will return the number of Leechers as given by the tracker for the given torrent.
	 * 
	 * @return The number of Leechers for the given torrent.
	 */
	public int getNumLeechers();
	
	/**
	 * This will return the string array of all the files that are associated with the given torrent.
	 * 
	 * @return The string array of the given files in the torrent.
	 */
	public String[] getFileNames();
	
	/**
	 * This will return the integer array of all the files that are associated, this will be given in 
	 * the same order that the getFileNames() method returns.
	 * 
	 * @return The int array of file sizes.
	 */
	public long[] getFileSizes();
	
	/**
	 * This will return the in of the number of Pieces that have been uploaded allready.
	 * 
	 * @return The int that represents the number of pieces uploaded.
	 */
	public int getNumPiecesUploaded();
	
	/**
	 * This will return the number of pieces in the file (total)
	 * 
	 * @return The number of pieces in the file
	 */
	public int getNumPieces(); 
	
	/**
	 * This will return the number of pieces that have been downloaded
	 * 
	 * @return The number of pieces that we downloaded
	 */
	public int getNumPiecesDownloaded();
	
	/**
	 * This will return the number of bytes that need to still need to be downloaded.
	 * 
	 * @return The number of bytes that still need to be downlaoded.
	 */
	public long getNumBytesLeft();
	
}
