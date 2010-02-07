package tcl.tm.torrent.communication.peer.sender;

public interface PeerSender extends Runnable {
	
	/**
	 * Indicate to this Peer that we have the given piece.
	 * 
	 * @param pieceID The piece which we want to inform this Peer that we now have.
	 **/
	public void issueHave(int pieceId);
	
	/**
	 * Indicate to this Peer that we wish to keep the connection alive.
	 **/
	public void issueKeepAlive();
	
	/**
	 * Indicate to this Peer that we are not currently interested in them.
	 **/
	public void issueDisinterested();
	
	/**
	 * Indicate to this Peer that we are currently interested in them.
	 **/
	public void issueInterested();
	
	/**
	 * Indicate to this Peer that we are currently choking them.
	 **/
	public void issueChoke();
	
	/**
	 * Indicate to this Peer that we are now unchoking them.
	 **/
	public void issueUnchoke();
	
	/**
	 * Request that this Peer send us the indicated piece data
	 * 
	 * @param pieceId The piece the data comes from
	 * @param byteOffset The offset within the piece to start sending data.
	 * @param length The length of data to send
	 **/
	public void issueRequest(int pieceId, int byteOffset, int length);
	
	/**
	 * Send a piece to this Peer
	 * 
	 * @param pieceId The piece the data comes from
	 * @param byteOffset The offset within the piece the data starts at.
	 * @param length The length of the data that will be sent
	 **/
	public void sendPiece(int pieceId, int byteOffset, int length);
	
	/**
	 * Sends our Bitfield to this peer
	 **/
	public void sendBitfield();
	
	/**
	 * Cancel a request for a piece that we previously made to this peer.
	 * 
	 * @param pieceId The piece the data comes from
	 * @param byteOffset The offset within the piece to start sending data.
	 * @param length The length of data to send
	 **/
	public void cancelRequest(int pieceId, int byteOffset, int length);

}

	
