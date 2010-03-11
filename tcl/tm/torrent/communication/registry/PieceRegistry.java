package tcl.tm.torrent.communication.registry;

import tcl.tm.torrent.communication.util.Piece;


public interface PieceRegistry {

	public void start();

	public void close();

	public PieceRequestFuture requestPiece(boolean[] bitfield, int piecesCompleted);

	public boolean returnPiece(Piece p);

	public void peerHave(int pieceId);

	public void peerBitfield(boolean[] bitfield);

	public boolean peerInteresting(boolean[] bitfield);

	public void removeBitfield(boolean[] bitfield);

}
