package tcl.tm.torrent.communication;

import java.io.Closeable;
import java.net.Socket;

import tcl.tm.torrent.communication.util.Piece;

/**
 * The CommunicationManager is responsible for all logic regarding
 * the downloading of the Torrent's files.
 *
 * The main need for outside interaction with the CommunicationManager
 * is to add new Peer connections to the Torrent.
 * These Peers should already be past the handshake stage, meaning
 * any incoming messages follow the standard <length><code><data> format.
 *
 * CommunicationManager should also take measures to contact new Peers on its own
 * and add them after the handshake has successfully occurred.
 *
 * @author Wayne Rowcliffe
 **/
public interface CommunicationManager extends Closeable {

	/**
	 * Adds a Peer to this CommunicationManager. Meaning that the peer on the other side
	 * of the Socket is interested in downloading / sharing this Torrent.
	 *
	 * @param peer The Socket representing the connection to an interested peer.
	 **/
	public void addPeer(Socket peer, byte[] reserved);

	/**
	 * Attempts to remove the given peer from the set of peers being managed by this CommunicationManager
	 *
	 * @param peer The peer to remove from this CommunicationManager
	 **/
	public void removePeer(String peer);

	/**
	 * Returns the speed this Torrent is downloading at
	 *
	 * @return The speed this Torrent is downloading at.
	 **/
	public int getConnectionSpeed();

	/**
	 * The number of peers this Torrent is connected to.
	 *
	 * @return The number of peers this Torrent is connected to.
	 **/
	public int getNumPeers();

	/**
	 * The number of bytes that we have uploaded for this torrent.
	 *
	 * @return The number of bytes that have been uploaded for this torrent.
	 **/
	public int getNumBytesUploaded();

	/**
	 * Given a bitfield, assigns a Piece which can be downloaded by this Peer.
	 * Preferably, the chosen piece should be the rarest piece that can be downloaded
	 * by a Peer with this bitfield.
	 *
	 * @param bitfield The list of pieces which the given Peer has available for downloading.
	 *
	 * @return A piece which can be downloaded by the Peer, given the bitfield they have provided.
	 **/
	public Piece assignPiece(boolean[] bitfield, int piecesCompleted);

	/**
	 * Called by a Peer to return a Piece it has been assigned.
	 * If the piece is complete, this CommunicationManager will attempt to
	 * save it to the file system. Upon success, this piece will be marked off
	 * of the list of pieces still needed.
	 *
	 * This method can also be used to return pieces assigned to unresponsive Peers.
	 * In this case, Pieces will be saved so that any data recieved previously will not be lost.
	 *
	 * @param p The Piece which is being returned.
	 **/
	public boolean returnPiece(Piece p);

	/**
	 * Called by a Peer to indicate to this CommunicationManager that it now has a new piece which can be downloaded from it.
	 *
	 * @param pieceId The piece which a Peer now has.
	 **/
	public void peerHave(int pieceId);

	/**
	 * Called by a Peer to indicate to this CommunicationManager the entire bitfield of pieces
	 * that are available for download from the peer.
	 *
	 * @param bitfield The bitfield of the calling peer.
	 **/
	public void peerBitfield(boolean[] bitfield);

	/**
	 * Called by a Peer to decide whether it has pieces which are still needed for this Torrent.
	 *
	 * @param bitfield The bitfield of the calling peer.
	 *
	 * @return Whether the calling peer has pieces which are still needed for this Torrent.
	 **/
	public boolean peerInteresting(boolean[] bitfield);

	/**
	 * Called by a Peer to indicate that it will no longer be sharing pieces. Probably because of
	 * disconnection. Its pieces will be removed from the list of available pieces for download.
	 *
	 * @param bitfield The bitfield of the calling peer.
	 **/
	public void removeBitfield(boolean[] bitfield);

}
