package tcl.tm.torrent.file;

import java.io.Closeable;

/**
 * The FileAccessManager aims to be a thread-safe
 * way to manage the reading/writing of pieces from file.
 * 
 * The other main aim of the FileAccessManager is to abstract away
 * all of the actual file calculations, and reduce actions to: 
 * 1. Retrieval of pieces, 2. Saving of pieces, 3. Checking whether a piece is available.
 * 
 * FileAccessManager uses FileAccessFutures to return the result of the requested operation.
 * 
 * A single thread should handle the actual file reading / writing.
 * This data should be saved into the FileAccessFuture which is returned from the method call.
 * FileAccessFuture's should block until the FileAccessManager has processed them.
 * 
 * @author Wayne Rowcliffe
 **/
public interface FileAccessManager extends Closeable, Runnable {

	/**
	 * Request that the FileAccessManager retrieve the piece with the given id
	 * 
	 * @param id The id of the piece to retrieve
	 * 
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture getPiece(int id);
	
	/**
	 * Request that the FileAccessManager save the piece with the given id using the given data
	 * 
	 * @param id The id of the piece to save
	 * @param data The data for the given piece
	 * 
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture savePiece(int id, byte[] data);
	
	/**
	 * Request that the FileAccessManager determine whether or not the piece with the given id is available.
	 * 
	 * @param id The id of the piece to check for availability.
	 * 
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture havePiece(int id);
	
	/**
	 * Request that the FileAccessManager return a bitfield corresponding to the pieces that are stored to file.
	 * 
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture getBitfield();

}
