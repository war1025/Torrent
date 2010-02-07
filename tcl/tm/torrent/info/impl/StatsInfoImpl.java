package tcl.tm.torrent.info.impl;

import java.io.IOException;
import java.net.MalformedURLException;

import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.info.StatsInfo;
import tcl.tm.torrent.info.InformationManager;

/**
 * This is the implementation of the statsinfo interface
 * 
 * More information is in the interface.
 * 
 * @author Anthony Milosch
 *
 */
public class StatsInfoImpl implements StatsInfo {

	private InformationManager im;
	private Torrent torrent;
	
	/**
	 * Constructor for the StatsInforImplementation
	 * 
	 * @param infoManager The Information Manager that controls this class.
	 */
	public StatsInfoImpl(InformationManager im, Torrent torrent) {
		this.torrent = torrent;
		this.im = im;
	}

	public int getSpeed() {
		return torrent.getCommunicationManager().getConnectionSpeed();
	}

	public long getETA() {
		long retVal;
		if(this.getSpeed() == 0) {
			return -1;
		}
		retVal = this.getNumBytesLeft()/(this.getSpeed());
		
		return retVal;
	}

	public String[] getFileNames() {
		return im.getTorrentInfo().getFileNames();
	}

	public long[] getFileSizes() {
		long[] retVal = new long[this.getFileNames().length];
		for(int i = 0; i < retVal.length; i ++)	{
			retVal[i] = im.getTorrentInfo().getFileLength(i);
		}
		return retVal;
	}

	public long getNumBytesDownloaded() {
		return ((long) this.getNumPiecesDownloaded()) * im.getTorrentInfo().getPieceLength();
	}

	public long getNumBytesLeft() {
		return this.getTorrentSize() - this.getNumBytesDownloaded();
	}

	
	public long getNumBytesUploaded() {
		return ((long) getNumPiecesUploaded()) * im.getTorrentInfo().getPieceLength();
	}

	public int getNumLeechers() {
		return im.getAnnounceInfo().getLeecherCount();
	}

	public int getNumPeers() {
		return torrent.getCommunicationManager().getNumPeers();
	}

	public int getNumPieces() {
		return im.getTorrentInfo().getPieceCount();
	}

	public int getNumPiecesDownloaded() {
		int count = 0;
		byte[] state = torrent.getFileAccessManager().getBitfield().getData();
		if(state != null) {
			for(int i = 0; i < state.length; i++) {
				for(int j = 0; j < 8; j++) {
					if(((state[i] >> (7-j)) & 1) == 1) {
						count++;
					}
				}
			}
		} 
		return count;
	}

	public int getNumPiecesLeft() {
		return this.getNumPieces() - this.getNumPiecesDownloaded();
	}

	public int getNumPiecesUploaded() {
		return getNumPiecesDownloaded()/2;
	}

	public int getNumSeeders() {
		return im.getAnnounceInfo().getSeederCount();
	}

	public long getTorrentSize() {
		long[] fileSizes = this.getFileSizes();
		long retVal = 0;
		for(int i = 0; i < fileSizes.length; i ++) {
			retVal = retVal + fileSizes[i];
		}
		return retVal;
	}
	
}
