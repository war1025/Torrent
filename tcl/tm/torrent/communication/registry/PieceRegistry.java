package tcl.tm.torrent.communication.registry;

import tcl.tm.torrent.communication.util.Piece;

/**
 * The PieceRegistry tracks the pieces needed for the current torrent.
 * It assigns pieces to peers for download and also handles saving
 * completed pieces to file.
 **/
public interface PieceRegistry {

	/**
	 * Closes this PieceRegistry
	 **/
	public void close();

	/**
	 * Called by a Peer to request a piece for download.
	 *
	 * @param bitfield The bitfield of pieces the Peer has available.
	 * @param piecesCompleted The number of pieces the Peer has successfully downloaded.
	 *
	 * @return The next piece the Peer should attempt to download, or null if nothing acceptable was found.
	 **/
	public Piece requestPiece(boolean[] bitfield, int piecesCompleted);

	/**
	 * Called by a Peer to return a downloaded piece
	 *
	 * @param p The piece which the Peer wishes to return
	 *
	 * @return Whether or not the Piece could be saved to file
	 **/
	public boolean returnPiece(Piece p);

	/**
	 * Called by a Peer to indicate that they now have the given piece available
	 *
	 * @param pieceId The piece the Peer now has available
	 **/
	public void peerHave(int pieceId);

	/**
	 * Called by a Peer to indicate the entire bitfield of pieces they have available
	 *
	 * @param bitfield The bitfield of available pieces
	 **/
	public void peerBitfield(boolean[] bitfield);

	/**
	 * Called by a Peer to determine if they are interesting to us or not
	 *
	 * @param bitfield The bitfield of pieces the Peer has available.
	 *
	 * @return Whether or not the Peer has pieces we do not have.
	 **/
	public boolean peerInteresting(boolean[] bitfield);

	/**
	 * Called by a Peer when they are closing. Removes their pieces
	 * from the list of available pieces to download
	 *
	 * @param bitfield The pieces the Peer had available for download.
	 **/
	public void removeBitfield(boolean[] bitfield);

}
