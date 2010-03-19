package tcl.tm.torrent.impl;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.communication.CommunicationManager;
import tcl.tm.torrent.communication.impl.CommunicationManagerImpl;
import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.file.impl.FileAccessManagerImpl;
import tcl.tm.torrent.file.util.StatusLoader;
import tcl.tm.torrent.file.util.impl.StatusLoaderImpl;
import tcl.tm.torrent.info.InformationManager;
import tcl.tm.torrent.info.impl.InformationManagerImpl;

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
 * @author Anthony Milosch
 **/
public class TorrentImpl implements Torrent {

	private InformationManager im;
	private FileAccessManager fam;
	private StatusLoader sl;
	private CommunicationManager cm;
	private String directoryFolder;
	private String statusLocation;

	/**
	 * This is the constructor, you need to give it the filepath and the
	 * directory where you want the file to be storred.
	 *
	 * @param filePath The filepath to the .torrent file
	 * @param baseDirectory The base Directory of where the torrent information will
	 * be held.
	 */
	public TorrentImpl(String filePath, String baseDirectory) {
		im = new InformationManagerImpl(filePath, this);
		sl = new StatusLoaderImpl(im.getTorrentInfo(),baseDirectory);
		fam = new FileAccessManagerImpl(im.getTorrentInfo(),sl,baseDirectory);
		new Thread((FileAccessManagerImpl) fam,"File Access Manager - " + filePath).start();
		im.getAnnounceInfo().startTracker();
		cm = new CommunicationManagerImpl(this);
		directoryFolder = baseDirectory + im.getTorrentInfo().getTorrentName();
		statusLocation = baseDirectory + "." + im.getTorrentInfo().getEscapedInfoHash();

	}

	/**
	 * Closes all subsections of the Torrent, cleaning up any loose ends, closing any connections, and killing additional threads.
	 **/
	public void close() {
		try {
			System.out.println("Closing Communication Manager");
			cm.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			System.out.println("Closing File Access Manager");
			fam.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			System.out.println("Closing Status Loader");
			sl.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			System.out.println("Closing Information Manager");
			im.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a Peer to this Torrent. Meaning that the peer on the other side
	 * of the Socket is interested in downloading / sharing this Torrent.
	 *
	 * @param peer The Socket representing the connection to an interested peer.
	 **/
	public void addPeer(Socket peer, byte[] reserved) {
		cm.addPeer(peer, reserved);
	}

	/**
	 * Returns the CommunicationManager for this Torrent
	 *
	 * @return The CommunicationManager for this Torrent
	 **/
	public CommunicationManager getCommunicationManager() {
		return cm;
	}

	/**
	 * Returns the FileAccessManager for this Torrent
	 *
	 * @return The FileAccessManager for this Torrent
	 **/
	public FileAccessManager getFileAccessManager() {
		return fam;
	}

	/**
	 * Returns the InformationManager for this Torrent
	 *
	 * @return The InformationManager for this Torrent
	 **/
	public InformationManager getInformationManager() {
		return im;
	}

	/**
	 * This method will relocate the downloaded torrent file, after the relocate is complete
	 * the backend will lose track of it, and completly disregard it.
	 *
	 * @param newFilePath the new filepath where the file will be located
	 *
	 * @return true if the relocate was sucessful, false if the file you are trying
	 * to download cannot be relocated
	 **/
	public boolean relocate(String newFilePath) {
		boolean success = false;
		close();
		try {
			Thread.sleep(1000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		File inFile = new File(directoryFolder);
		File outFile = new File(newFilePath + im.getTorrentInfo().getTorrentName());
		success = inFile.renameTo(outFile);

		File status = new File(statusLocation);
		success &= status.delete();
		return success;
	}

	/**
	 * This method will remove a file from the queue/ stop downloading and
	 * delete, or delete if already downloaded
	 *
	 * @return true if sucessful false otherwise
	 **/
	public boolean remove() {
		boolean success = false;
		close();
		try {
			Thread.sleep(1000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		File base = new File(directoryFolder);
		if(base.exists()) {
			success = recursiveDelete(base);
		}
		File status = new File(statusLocation);
		success &= status.delete();
		return success;
	}

	/**
	 * This function will go through and delete an entire directory recursively.
	 *
	 * @param base The base directory.
	 *
	 * @return true if it was sucessful
	 */
	private boolean recursiveDelete(File base) {
		boolean success = true;
		if(base.isDirectory()) {
			File[] files = base.listFiles();
			if(files != null) {
				for(File f : files) {
					success &= recursiveDelete(f);
				}
			}
		}
		success &= base.delete();
		return success;
	}


}
