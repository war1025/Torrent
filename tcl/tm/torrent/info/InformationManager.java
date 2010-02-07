package tcl.tm.torrent.info;

import java.io.Closeable;

/**
 * This is the interface to a simple class that will make supporting calls to all of the lower
 * classes.  Just holds things.
 *   
 * 
 * @author Anthony Milosch
 *
 */
public interface InformationManager extends Closeable {

	/**
	 * This method just returns the AnnounceInfo class for this torrent
	 * 
	 * @return The announceInfo for this torrent
	 */
	public AnnounceInfo getAnnounceInfo();
	
	
	/**
	 * This method just returns the TorrentInfo class for this torrent
	 * 
	 * @return The TorrentInfo for this torrent
	 */
	public TorrentInfo getTorrentInfo();
	
	/**
	 * This method just return the StatsInfo class for this torrent
	 * 
	 * @return The StatsInfo for this torrent
	 */
	public StatsInfo getStatsInfo();
	
}
