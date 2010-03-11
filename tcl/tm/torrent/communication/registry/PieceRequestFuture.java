package tcl.tm.torrent.communication.registry;



public interface PieceRequestFuture {

	public Piece getPiece();

	public boolean getSuccess();

}
