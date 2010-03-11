package tcl.tm.torrent.communication.registry;

import tcl.tm.torrent.communication.util.Piece;


public interface PieceRegistry {

	public void close();

	public void start();

	public PieceRequest requestPiece(boolean[] bitfield, int piecesCompleted);

	public boolean returnPiece(Piece p);

}
