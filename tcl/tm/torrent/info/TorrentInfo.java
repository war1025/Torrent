package tcl.tm.torrent.info;

import java.util.Date;

/**
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
 * 
 * @author Wayne Rowcliffe
 * @author Anthony Milosch
 **/
public interface TorrentInfo {

	/**
	 * The announce URL for this torrent
	 * 
	 * @return The announce URL for this torrent
	 **/
	public String getAnnounceURL();

	/**
	 * The announce list for this torrent
	 * 
	 * @return The announce list for this torrent
	 **/
	public String[] getAnnounceList();
	
	/**
	 * The name of this torrent
	 * 
	 * @return The name of this torrent
	 **/
	public String getTorrentName();

	/**
	 * Who created this torrent
	 * 
	 * @return Who created this torrent
	 **/
	public String getCreatedBy();

	/**
	 * The info hash of this torrent as a byte[]
	 * 
	 * @return The info hash of this torrent as a byte[]
	 **/
	public byte[] getInfoHash();

	/**
	 * The escaped info hash of this torrent as a String
	 * 
	 * @return The escaped info hash of this torrent as a String
	 **/
	public String getEscapedInfoHash();

	/**
	 * This torrent's comment
	 * 
	 * @return This torrent's comment
	 **/
	public String getComment();

	/**
	 * This torrent's creation date
	 * 
	 * @return This torrent's creation date
	 **/
	public Date getCreationDate();

	/**
	 * All piece hashes for this torrent, as Strings
	 * 
	 * @return All piece hashes for this torrent as Strings
	 **/
	public String[] getPieceHashes();

	/**
	 * The "id"th piece hash for this torrent
	 * 
	 * @param id The piece whose info hash to retrieve
	 * 
	 * @return The "id"th piece hash for this torrent
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public String getPieceHash(int id);

	/**
	 * The names of all files in this torrent
	 * 
	 * @return The names of all files in this torrent
	 **/
	public String[] getFileNames();
	
	/**
	 * The number of files in this torrent
	 * 
	 * @return The number of files in this torrent
	 **/
	public int getFileCount();
	
	/**
	 * The pair (piece,offset) which indicates where this file starts
	 *
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 *
	 * @return A pair of integers of the form (piece,offset) where the file starts
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public int[] getFileStartLocation(int id);
	
	/**
	 * The pair (piece,offset) which indicates where this file ends
	 *
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 *
	 * @return A pair of integers of the form (piece,offset) where the file ends
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public int[] getFileEndLocation(int id);

	/**
	 * The length of the file with id "id"
	 * 
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 * 
	 * @return The length of the file in bytes
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public long getFileLength(int id);
	
	/**
	 * The byte where, if all pieces were strung together, this file would start
	 * 
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 * 
	 * @return The byte where the file starts overall in the torrent
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public long getFileStartByte(int id);

	/**
	 * A String[] where each entry is a directory, and the final entry is the filename for the file with the given id
	 * 
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 * 
	 * @return The byte where the file starts overall in the torrent
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public String[] getFilePath(int id);

	/**
	 * The number of bytes in a standard piece for this torrent
	 * 
	 * @return The number of bytes in a standard piece for this torrent
	 **/
	public int getPieceLength();
	
	/**
	 * The number of bytes in the final piece of this torrent
	 * 
	 * @return The number of bytes in the final piece of this torrent
	 **/
	public int getFinalPieceLength();

	/**
	 * The number of pieces in this torrent
	 * 
	 * @return The number of pieces in this torrent
	 **/
	public int getPieceCount();

	/**
	 * The String encoding used in this torrent
	 * 
	 * @return The String encoding used in this torrent.
	 **/
	public String getEncoding();
	
	/**
	 * The pair (file,offset) which indicates where this piece starts
	 *
	 * @param id The id of this piece, which is its location in the getPieceHashes array
	 *
	 * @return A pair of integers of the form (file,offset) where the piece starts
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public long[] getPieceStartLocation(int id);

	/**
	 * The pair (file,offset) which indicates where this piece ends
	 *
	 * @param id The id of this piece, which is its location in the getPieceHashes array
	 *
	 * @return A pair of integers of the form (file,offset) where the piece ends
	 * 
	 * @throws IndexOutOfBoundsException if the id parameter is out of bounds
	 **/
	public long[] getPieceEndLocation(int id);

}
