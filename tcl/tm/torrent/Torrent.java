package tcl.tm.torrent;

import java.io.Closeable;
import java.net.Socket;

import tcl.tm.torrent.communication.CommunicationManager;
import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.info.InformationManager;

/**
 * A Torrent represents the complete process involved in downloading the files
 * specified in a .torrent file. This involves the ability to start and stop downloads,
 * as well as the ability to remove files related to a Torrent or relocate them to another location.
 * Starting and stopping will be accomplished by simply creating or disposing of the relevent Torrent object.
 * Relocate and Remove will be functionality provided in the Torrent implementation. These methods should either
 * close the Torrent when called, or strongly suggest that close() be called directly afterwards.
 * 
 * Torrents also need the ability to add new Peers which are connected via the TorrentManager's ConnectionManager.
 * 
 * @author Wayne Rowcliffe
 **/
public interface Torrent extends Closeable {

	/**
	 * Returns the CommunicationManager for this Torrent
	 * 
	 * @return The CommunicationManager for this Torrent
	 **/
	public CommunicationManager getCommunicationManager();

	/**
	 * Returns the FileAccessManager for this Torrent
	 * 
	 * @return The FileAccessManager for this Torrent
	 **/
	public FileAccessManager getFileAccessManager();

	/**
	 * Returns the InformationManager for this Torrent
	 * 
	 * @return The InformationManager for this Torrent
	 **/
	public InformationManager getInformationManager();

	/**
	 * This method will relocate the downloaded torrent file, after the relocate is complete
	 * the backend will lose track of it, and completly disregard it.
	 * 
	 * @param newFilePath the new filepath where the file will be located
	 * 
	 * @return true if the relocate was sucessful, false if the file you are trying 
	 * to download cannot be relocated
	 **/
	public boolean relocate(String newFilePath);
	
	/**
	 * This method will remove a file from the queue/ stop downloading and 
	 * delete, or delete if already downloaded
	 * 
	 * @return true if sucessful false otherwise
	 **/
	public boolean remove();
	
	/**
	 * Adds a Peer to this Torrent. Meaning that the peer on the other side 
	 * of the Socket is interested in downloading / sharing this Torrent.
	 * 
	 * @param peer The Socket representing the connection to an interested peer.
	 **/ 
	public void addPeer(Socket peer, byte[] reserved);
}
