package tcl.tm.torrent.file;

/**
 * A FileAccessFuture allows the result of an asynchronous
 * FileAccessManager request to be viewed once the request has been processed.
 * 
 * There are four types of FileAccessFuture: GET_PIECE, SAVE_PIECE, HAVE_PIECE, and GET_BITFIELD
 * These correspond to the three different methods available in the FileAccessManger.
 * 
 * FileAccessFutures should be immutable to everyone except the FileAccessManager.
 * 
 * @author Wayne Rowcliffe
 **/
public interface FileAccessFuture {
	
	/**
	 * Was this request was completed successfully.
	 * 
	 * @return Whether or not the request was completed successfully.
	 **/
	public boolean getSuccess();

	/**
	 * The piece id this request pertained to.
	 * 
	 * @return The piece id this request pertained to.
	 **/
	public int getPieceId();

	/**
	 * The data for the given piece, or null in the case of a HAVE_PIECE request
	 * 
	 * @return The data for the given piece, or null in the case of a HAVE_PIECE request.
	 **/
	public byte[] getData();

	/**
	 * The type of request that this FileAccessFuture was created for.
	 * 
	 * @return The type of request that this FileAccessFuture was created for.
	 **/
	public FileAccessFuture.Type getType();

	public enum Type {
		GET_PIECE,
		SAVE_PIECE,
		HAVE_PIECE,
		GET_BITFIELD
	}

}
