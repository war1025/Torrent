package tcl.tm.torrent.communication.registry;

import java.net.Socket;

/**
 * The PeerRegistry tracks all currently connected Peers.
 **/
public interface PeerRegistry {

	/**
	 * Adds a Peer to this CommunicationManager. Meaning that the peer on the other side
	 * of the Socket is interested in downloading / sharing this Torrent.
	 *
	 * @param peer The Socket representing the connection to an interested peer.
	 * @param reserved The reserved bytes from the handshake, used to determine what sort of Peer we have.
	 **/
	public void addPeer(Socket peer, byte[] reserved);

	/**
	 * Attempts to remove the given peer from the set of peers being managed by this CommunicationManager
	 *
	 * @param peer The peer to remove from this CommunicationManager
	 **/
	public void removePeer(String peer);

	/**
	 * Closes the PeerRegistry
	 **/
	public void close();

	/**
	 * The number of Peers we are currently connected to
	 **/
	public int getNumPeers();

	/**
	 * Notifies all current peers that we now have the given piece
	 *
	 * @param pieceId The id of the piece we now have
	 **/
	public void notifyHave(int pieceId);

}
