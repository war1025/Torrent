package tcl.tm.impl;

import tcl.tm.TorrentManager;
import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.impl.TorrentImpl;
import tcl.tm.connection.ConnectionManager;
import tcl.tm.connection.impl.ConnectionManagerImpl;

import java.util.Hashtable;
import java.io.IOException;

/**
 * This is the Implementation of the Torrent Manager class, this is basically just
 * a class that holds everything together as well as performing some of the
 * basic things such as starting and stopping the torrent, removing(delete) the torrent
 * and relocate the downloaded torrent file.  This also holds everything together
 * so that we can access anything that we want to from anywhere, yeah a little dirty
 * but overall a good thing.
 * 
 * @author Anthony Milosch
 * @author Wayne Rowcliffe
 *
 */
public class TorrentManagerImpl implements TorrentManager
{
	private String baseDirectory;
	private Hashtable<String,Torrent> fileMap;
	private Hashtable<String,Torrent> hashMap;
	private ConnectionManager cm;

	/**
	 * Constructor for this class, you need to include the baseDirectory
	 * for this to work, the base directory is the directory where all
	 * of the information for the download will be stored.
	 * 
	 * @param baseDirectory The Director that the download will be stored in.
	 */
	public TorrentManagerImpl(String baseDirectory) 
	{
		this.baseDirectory = baseDirectory;
		this.fileMap = new Hashtable<String,Torrent>();
		this.hashMap = new Hashtable<String,Torrent>();
		this.cm = new ConnectionManagerImpl(this,1630);
		new Thread(cm,"ConnectionManager").start();
	}
	
	/**
	 * This method will start the torrent downlaod, this will resume a download if it has 
	 * ready been downloaded before
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return ture if it has started downloading false otherwise
	 */
	public boolean start(String filePath) {
		boolean success = true;
		if(fileMap.get(filePath) == null) {
			Torrent t = null;
			try {
				t = new TorrentImpl(filePath,baseDirectory);
			} catch(IllegalArgumentException e) {
				success = false;
			}
			if(t != null) {
				fileMap.put(filePath,t);
				hashMap.put(t.getInformationManager().getTorrentInfo().getEscapedInfoHash(),t);
			}
		}
		return success;
	}
	
	/**
	 * This method will stop the torrent downlaod.
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return true if the file has stopped downloading, false otherwise
	 */
	public boolean stop(String filePath) {
		boolean success = true;
		Torrent t = fileMap.get(filePath);
		if(t != null) {
			fileMap.remove(filePath);
			hashMap.remove(t.getInformationManager().getTorrentInfo().getEscapedInfoHash());
			try {
				t.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}
	
	/**
	 * This method will relocate the downloaded torrent file, after the relocate is complete
	 * the backend will lose track of it, and completly disregard it.
	 * 
	 * @param filePath the path to the .torrent file that corresponds to the file set to be relocated
	 * @param newFilePath the new filepath where the file will be located
	 * 
	 * @return true if the relocate was sucessful, false if the file you are trying 
	 * to download cannot be relocated
	 */
	public boolean relocate(String filePath, String newFilePath)
	{
		boolean success = true;
		Torrent t = fileMap.get(filePath);
		if(t != null) {
			fileMap.remove(filePath);
			hashMap.remove(t.getInformationManager().getTorrentInfo().getEscapedInfoHash());
			t.relocate(newFilePath);
		}
		return success;
	}
	
	/**
	 * This method will remove a file from the queue/ stopdownloading and 
	 * delete, or delete if allready downloaded
	 * 
	 * @param filePath the filePath to the origional filepath in question
	 * 
	 * @return true if sucessful false otherwise
	 */
	public boolean remove(String filePath) {
		boolean success = true;
		Torrent t = fileMap.get(filePath);
		if(t != null) {
			fileMap.remove(filePath);
			hashMap.remove(t.getInformationManager().getTorrentInfo().getEscapedInfoHash());
			t.remove();
		}
		return success;
	}
	

	/**
	 * This method will tell the user if the torrent is competly downloaded and ready 
	 * for upload.
	 * 
	 * @param filePath the filePath to the file is question
	 * 
	 * @return true if it is complete false otherwise
	 */
	public boolean isComplete(String filePath) {
		boolean success = true;
		Torrent t = fileMap.get(filePath);
		if(t != null) {
			byte[] bitfield = t.getFileAccessManager().getBitfield().getData();
			int pieceCount = t.getInformationManager().getTorrentInfo().getPieceCount();
			int count = pieceCount / 8;
			int extra = pieceCount % 8;
			for(int i = 0; i < count; i++) {
				if((bitfield[i] & (0xff)) != 255) {
					success = false;
					break;
				}
			}
			if(success && extra > 0) {
				int aByte = 0;
				for(int i = 0; i < extra; i++) {
					aByte |= (1 << (7-i));
				}
				if((bitfield[count] & (0xff)) != aByte) {
					success = false;
				}
			}
		} else {
			success = false;
		}
		return success;
	}
	

	/**
	 * This method will return a list of all torrents available for data lookup on the server
	 * 
	 * @return the string array of torrents available
	 */
	public String[] getTorrentsAvailable() {
		return fileMap.keySet().toArray(new String[0]);
	}
	
	/**
	 * This method will return the torrent object for a given filepath
	 * 
	 * @param filePath The path on the system to the .torrent file
	 * 
	 * @return The torrent object for that filePath if it exists, else null.
	 **/
	public Torrent getTorrent(String filePath) {
		return fileMap.get(filePath);
	}
	
	/**
	 * This method will return the torrent object for a given escaped info hash
	 * 
	 * @param escapedHash The escaped info_hash for the .torrent file
	 * 
	 * @return The torrent object for that escapedInfoHash if it exists, else null.
	 **/
	public Torrent getTorrentByHash(String escapedHash) {
		return hashMap.get(escapedHash);
	}
	
	public void close() throws IOException {
		cm.close();
		for(String torrent : getTorrentsAvailable()) {
			stop(torrent);
		}
	}
}
