package tcl.tm.torrent.file.util.impl;

import tcl.tm.torrent.info.TorrentInfo;
import tcl.tm.torrent.file.util.StatusLoader;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;

/**
 * StatusLoader saves the status of the torrent download to the filesystem
 * This allows for a torrent to be resumed at a later time should the download be
 * stopped for whatever reason.
 **/
public class StatusLoaderImpl implements StatusLoader {
	
	private RandomAccessFile file;
	private int pieceCount;
	
	/**
	 * Creates a StatusLoader object, and if necessary, creates a file
	 * to track the progress of the torrent download.
	 * This file has a name of the form .escapedInfoHash
	 * 
	 * @param info The TorrentInfo for this torrent download
	 * @param baseDirectory The directory the torrent is being downloaded to, where the status will be saved.
	 * 
	 * @throws IllegalArgumentException if the StatusLoader is unable to create or load a status file for the torrent.
	 **/
	public StatusLoaderImpl(TorrentInfo info, String baseDirectory) {
		try{
			File f = new File(baseDirectory + "." + info.getEscapedInfoHash());
			f.createNewFile();
			
			file = new RandomAccessFile(f,"rwd");
			pieceCount = info.getPieceCount();
			
			file.seek(0);
			
			if(file.length() != pieceCount) {
				byte[] b = new byte[pieceCount];
				for(int i = 0; i < b.length; i++) {
					b[i] = (byte) 0;
				}
				file.write(b);
			}
		} catch(IOException io) {
			io.printStackTrace();
			throw new IllegalArgumentException("Unable to load status");
		}
	}
	
	/**
	 * Retrieves the status for this torrent, meaning a boolean array indicating
	 * which pieces have been successfully dowloaded.
	 * 
	 * @return The status of this torrent.
	 **/
	public boolean[] getStatus() {
		
		boolean[] pieces = new boolean[pieceCount];
		
		try{
			byte[] b = new byte[pieces.length];
			
			file.seek(0);
			
			if(file.length() == pieceCount) {
				file.readFully(b);
				for(int i = 0; i < pieces.length; i++) {
					pieces[i] = (b[i] == 1) ? true : false;
				}
			}
		} catch(IOException io) {
			io.printStackTrace();
		}
		
		return pieces;
	}
	
	/**
	 * Sets the status of the given piece in this torrent.
	 * 
	 * @param piece The piece to set the status for.
	 * @param status The status to set this piece to.
	 * 
	 * @return Whether or not the status was successfully set.
	 **/
	public boolean setStatus(int piece, boolean status) {
		
		try{
			file.seek(piece);
			
			file.write(status ? 1 : 0);
			
			return true;
		} catch(IOException io) {
			io.printStackTrace();
		}
		
		return false;
	}

	/**
	 * Closes this status loader
	 **/
	public void close() throws IOException {
		synchronized(file) {
			file.close();
		}
	}
}
		
			
		
