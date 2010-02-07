package tcl.tm.torrent.file.util;

import java.io.Closeable;

/**
 * StatusLoader saves the status of the torrent download to the filesystem
 * This allows for a torrent to be resumed at a later time should the download be
 * stopped for whatever reason.
 **/
public interface StatusLoader extends Closeable {
	
	/**
	 * Retrieves the status for this torrent, meaning a boolean array indicating
	 * which pieces have been successfully dowloaded.
	 * 
	 * @return The status of this torrent.
	 **/
	public boolean[] getStatus();
	
	/**
	 * Sets the status of the given piece in this torrent.
	 * 
	 * @param piece The piece to set the status for.
	 * @param status The status to set this piece to.
	 * 
	 * @return Whether or not the status was successfully set.
	 **/
	public boolean setStatus(int piece, boolean status);
}
		
			
		
