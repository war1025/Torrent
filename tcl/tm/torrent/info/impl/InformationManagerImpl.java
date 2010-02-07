package tcl.tm.torrent.info.impl;

import java.io.IOException;
import java.net.MalformedURLException;

import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.info.AnnounceInfo;
import tcl.tm.torrent.info.InformationManager;
import tcl.tm.torrent.info.StatsInfo;
import tcl.tm.torrent.info.TorrentInfo;
import tcl.tm.torrent.info.impl.AnnounceInfoImpl;


/**
 * This will be a simple class that will make calls to all the information classes so that they are
 * all in one spot.  
 * 
 * More information is in the interface.
 * 
 * @author Anthony Milosch
 *
 */
public class InformationManagerImpl implements InformationManager {
	
	AnnounceInfo ai;
	TorrentInfo ti;
	StatsInfo si;
	
	
	
	/**
	 * Constructor, only variable to pass in is the filepath for .torrent file
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * 
	 */
	public InformationManagerImpl(String filepath, Torrent torrent)	{
		this.ti = new TorrentInfoImpl(filepath);
		this.si = new StatsInfoImpl(this, torrent); 
		this.ai = new AnnounceInfoImpl(this);
	}
	
	/**
	 * Method that returns the class
	 */
	public AnnounceInfo getAnnounceInfo() {
		return ai;
	}
	
	/**
	 * Method that returns the class
	 */
	public StatsInfo getStatsInfo() {
		return si;
	}

	/**
	 * Method that returns the class
	 */
	public TorrentInfo getTorrentInfo() {
		return ti;
	}
	
	public void close() throws IOException {
		ai.close();
	}
	
}
