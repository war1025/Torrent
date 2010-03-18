package tcl.tm.torrent.communication.peer;

import tcl.tm.torrent.communication.util.Piece;

import java.io.Closeable;


public interface Peer extends Closeable {
	
	public void setInterested(boolean interested);
	
	public boolean getInterested();
	
	public void setAmInterested(boolean interested);
	
	public boolean getAmInterested();
	
	public void setChoked(boolean choked);
	
	public boolean getChoked();
	
	public void setAmChoking(boolean choking);
	
	public boolean getAmChoking();
	
	public boolean[] getBitfield();
	
	public void bitfieldUpdated();
	
	public void hasPiece(int id);
	
	public boolean isRunning();
	
	public void close();
	
	public Piece getCurrentPiece();
	
	public void setCurrentPiece(Piece piece);

	public void notifyPieceComplete();
	
	public void issueHave(int id);
	
}
