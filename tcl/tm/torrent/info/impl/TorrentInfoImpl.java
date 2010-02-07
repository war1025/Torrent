package tcl.tm.torrent.info.impl;

import tcl.tm.torrent.info.TorrentInfo;
import tcl.tm.torrent.info.util.PieceLocator;
import tcl.tm.torrent.info.util.Bencode;

import java.util.Date;
import java.util.Map;
import java.util.List;

/**
 * An implementation of the TorrentInfo interface.
 * 
 * TorrentInfo is a collection of methods that encapsulate all standard
 * data contained in a .torrent file. This allows users of the interface
 * to focus on what data they need from the torrent, rather than how 
 * they can retrieve it. 
 * 
 * Only getCreationDate() should ever return a null value.
 * All other methods should take measures to return a blank value
 * if the information is not available.
 * 
 * An IndexOutOfBoundsException should be thrown if an id parameter is out of bounds.
 * @author Wayne Rowcliffe
 * @author Tony Milosch
 **/
public class TorrentInfoImpl implements TorrentInfo {
	
	private Map<String,Object> torrent;
	private Map<String,Object> info;
	private List<Object> files;
	
	/**
	 * Creates a new TorrentInfoImpl using the .torrent file at the given filepath
	 * 
	 * @param filepath The path to the desired .torrent file
	 * 
	 * @throws IllegalArgumentException if the path does not lead to a readable .torrent file
	 **/
	@SuppressWarnings("unchecked")
	public TorrentInfoImpl(String filepath) {
		torrent = Bencode.getTorrentInfo(filepath);
		if(torrent == null) {
			throw new IllegalArgumentException("Couldn't read .torrent file");
		}
		info = (Map<String,Object>) torrent.get("info");
		files = (List<Object>) info.get("files");
	}
	
	/**
	 * The announce URL for this torrent
	 * 
	 * @return The announce URL for this torrent
	 **/
	@SuppressWarnings("unchecked")
	public String getAnnounceURL() {
		return (String) torrent.get("announce");	
	}

	/**
	 * The announce list for this torrent
	 * 
	 * @return The announce list for this torrent
	 **/
	@SuppressWarnings("unchecked")
	public String[] getAnnounceList() {
		List<Object> list = (List<Object>) torrent.get("announce-list");
		String[] s = { getAnnounceURL() };
		if(list != null) {
			s = new String[list.size()];
			for(int i = 0; i < list.size(); i++) {
				List<Object> l = (List<Object>) list.get(i);
				s[i] = (String) l.get(0);
			}
		}
		return s;	
	}
	
	/**
	 * The name of this torrent
	 * 
	 * @return The name of this torrent
	 **/
	@SuppressWarnings("unchecked")
	public String getTorrentName() {
		return (String) info.get("name");
	}

	/**
	 * Who created this torrent
	 * 
	 * @return Who created this torrent
	 **/
	@SuppressWarnings("unchecked")
	public String getCreatedBy() {
		String createdBy = (String) torrent.get("created by");	
		return createdBy == null ? "" : createdBy;
	}

	/**
	 * The info hash of this torrent as a byte[]
	 * 
	 * @return The info hash of this torrent as a byte[]
	 **/
	@SuppressWarnings("unchecked")
	public byte[] getInfoHash() {
		return ((byte[]) torrent.get("info_hash")).clone();	
	}

	/**
	 * The escaped info hash of this torrent as a String
	 * 
	 * @return The escaped info hash of this torrent as a String
	 **/
	@SuppressWarnings("unchecked")
	public String getEscapedInfoHash() {
		return (String) torrent.get("escaped_info_hash");	
	}

	/**
	 * This torrent's comment
	 * 
	 * @return This torrent's comment
	 **/
	@SuppressWarnings("unchecked")
	public String getComment() {
		String comment = (String) torrent.get("comment");
		return comment == null ? "" : comment;
	}

	/**
	 * This torrent's creation date
	 * 
	 * @return This torrent's creation date
	 **/
	@SuppressWarnings("unchecked")
	public Date getCreationDate() {
		Long created = (Long) torrent.get("creation date");
		return (created == null) ? null : new Date(created);
	}

	/**
	 * All piece hashes for this torrent, as Strings
	 * 
	 * @return All piece hashes for this torrent as Strings
	 **/
	@SuppressWarnings("unchecked")
	public String[] getPieceHashes() {
		return 	((String[]) info.get("pieces")).clone();
	}

	/**
	 * The "id"th piece hash for this torrent
	 * 
	 * @param id The piece whose info hash to retrieve
	 * 
	 * @return The "id"th piece hash for this torrent
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	@SuppressWarnings("unchecked")
	public String getPieceHash(int id) {
		if(id < 0 || id >= getPieceCount()) {
			throw new IndexOutOfBoundsException("The given piece id: " + id + " is out of range.");
		}
		return 	((String[]) info.get("pieces"))[id];
	}

	/**
	 * The names of all files in this torrent
	 * 
	 * @return The names of all files in this torrent
	 **/
	@SuppressWarnings("unchecked")
	public String[] getFileNames() {
		String[] names = new String[files.size()];
		for(int i = 0; i < names.length; i++) {
			Map<String,Object> file = (Map<String,Object>) files.get(i);
			List<Object> path = (List<Object>) file.get("path");
			names[i] = (String) path.get(path.size() -1);
		}
		return names;
	}
	
	/**
	 * The number of files in this torrent
	 * 
	 * @return The number of files in this torrent
	 **/
	public int getFileCount() {
		return files.size();	
	}
	
	/**
	 * The pair (piece,offset) which indicates where this file starts
	 *
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 *
	 * @return A pair of integers of the form (piece,offset) where the file starts
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	@SuppressWarnings("unchecked")
	public int[] getFileStartLocation(int id) {
		if(id < 0 || id >= getFileCount()) {
			throw new IndexOutOfBoundsException("The given file id: " + id + " is out of range.");
		}
		Map<String,Object> file = (Map<String,Object>) files.get(id);
		return new int[] {((Long) file.get("start_piece")).intValue(), ((Long) file.get("start_offset")).intValue()};	
	}
	
	/**
	 * The pair (piece,offset) which indicates where this file ends
	 *
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 *
	 * @return A pair of integers of the form (piece,offset) where the file ends
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	@SuppressWarnings("unchecked")
	public int[] getFileEndLocation(int id) {
		if(id < 0 || id >= getFileCount()) {
			throw new IndexOutOfBoundsException("The given file id: " + id + " is out of range.");
		}
		Map<String,Object> file = (Map<String,Object>) files.get(id);
		return new int[] {((Long) file.get("end_piece")).intValue(), ((Long) file.get("end_offset")).intValue()};	
	}

	/**
	 * The length of the file with id "id"
	 * 
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 * 
	 * @return The length of the file in bytes
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	@SuppressWarnings("unchecked")
	public long getFileLength(int id) {
		if(id < 0 || id >= getFileCount()) {
			throw new IndexOutOfBoundsException("The given file id: " + id + " is out of range.");
		}
		Map<String,Object> file = (Map<String,Object>) files.get(id);
		return (Long) file.get("length");
	}
	
	/**
	 * The byte where, if all pieces were strung together, this file would start
	 * 
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 * 
	 * @return The byte where the file starts overall in the torrent
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	@SuppressWarnings("unchecked")
	public long getFileStartByte(int id) {
		if(id < 0 || id >= getFileCount()) {
			throw new IndexOutOfBoundsException("The given file id: " + id + " is out of range.");
		}
		Map<String,Object> file = (Map<String,Object>) files.get(id);
		return (Long) file.get("start_byte");	
	}

	/**
	 * A String[] where each entry is a directory, and the final entry is the filename for the file with the given id
	 * 
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 * 
	 * @return The byte where the file starts overall in the torrent
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	@SuppressWarnings("unchecked")
	public String[] getFilePath(int id) {
		if(id < 0 || id >= getFileCount()) {
			throw new IndexOutOfBoundsException("The given file id: " + id + " is out of range.");
		}
		Map<String,Object> file = (Map<String,Object>) files.get(id);
		List<Object> path = (List<Object>) file.get("path");
		return path.toArray(new String[0]);	
	}

	/**
	 * The number of bytes in a standard piece for this torrent
	 * 
	 * @return The number of bytes in a standard piece for this torrent
	 **/
	@SuppressWarnings("unchecked")
	public int getPieceLength() {
		return ((Long) info.get("piece length")).intValue();	
	}
	
	/**
	 * The number of bytes in the final piece of this torrent
	 * 
	 * @return The number of bytes in the final piece of this torrent
	 **/
	public int getFinalPieceLength() {
		long size = getPieceLength() * (getPieceCount() -1);
		long total = getFileLength(files.size()-1) + getFileStartByte(files.size() -1);
		return (int) (total - size);	
	}

	/**
	 * The number of pieces in this torrent
	 * 
	 * @return The number of pieces in this torrent
	 **/
	@SuppressWarnings("unchecked")
	public int getPieceCount() {
		return 	((String[]) info.get("pieces")).length;	
	}

	/**
	 * The String encoding used in this torrent
	 * 
	 * @return The String encoding used in this torrent.
	 **/
	@SuppressWarnings("unchecked")
	public String getEncoding() {
		String encoding = (String) torrent.get("encoding");	
		return encoding == null ? "" : encoding;
	}
	
	/**
	 * The pair (file,offset) which indicates where this piece starts
	 *
	 * @param id The id of this piece, which is its location in the getPieceHashes array
	 *
	 * @return A pair of integers of the form (file,offset) where the piece starts
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public long[] getPieceStartLocation(int id) {
		if(id < 0 || id >= getPieceCount()) {
			throw new IndexOutOfBoundsException("The given piece id: " + id + " is out of range.");
		}
		return PieceLocator.getPieceStartLocation(this,id);
	}

	/**
	 * The pair (file,offset) which indicates where this piece ends
	 *
	 * @param id The id of this piece, which is its location in the getPieceHashes array
	 *
	 * @return A pair of integers of the form (file,offset) where the piece ends
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public long[] getPieceEndLocation(int id) {
		if(id < 0 || id >= getPieceCount()) {
			throw new IndexOutOfBoundsException("The given piece id: " + id + " is out of range.");
		}
		return PieceLocator.getPieceEndLocation(this,id);
	}
}
