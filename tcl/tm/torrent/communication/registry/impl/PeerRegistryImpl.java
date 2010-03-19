package tcl.tm.torrent.communication.registry.impl;

import tcl.tm.torrent.Torrent;

import tcl.tm.torrent.communication.registry.PeerRegistry;

import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.impl.StandardPeer;
import tcl.tm.torrent.communication.peer.impl.FastPeerImpl;

import java.util.Map;
import java.util.HashMap;

import java.net.Socket;

/**
 * The PeerRegistry tracks all currently connected Peers.
 **/
public class PeerRegistryImpl implements PeerRegistry {

	private Map<String,Peer> peers;

	private Object peerLock;

	private Torrent torrent;

	private boolean running;

	public PeerRegistryImpl(Torrent torrent) {
		this.torrent = torrent;
		this.peerLock = new Object();
		this.peers = new HashMap<String,Peer>();
		this.running = true;
	}

	/**
	 * Adds a Peer to this CommunicationManager. Meaning that the peer on the other side
	 * of the Socket is interested in downloading / sharing this Torrent.
	 *
	 * @param peer The Socket representing the connection to an interested peer.
	 * @param reserved The reserved bytes from the handshake, used to determine what sort of Peer we have.
	 **/
	public void addPeer(Socket peer, byte[] reserved) {
		System.out.println("Might add Peer");
		synchronized(peerLock) {
			if(running && !peers.containsKey(peer.getInetAddress().toString()) && !(peer.getInetAddress().equals(peer.getLocalAddress()))) {
				System.out.println("Going to add Peer");
				if((reserved[7] & (0x04)) == 4) {
					peers.put(peer.getInetAddress().toString(),new FastPeerImpl(torrent,peer));
				} else {
					peers.put(peer.getInetAddress().toString(),new StandardPeer(torrent,peer));
				}
			}
		}
	}

	/**
	 * Attempts to remove the given peer from the set of peers being managed by this CommunicationManager
	 *
	 * @param peer The peer to remove from this CommunicationManager
	 **/
	public void removePeer(String peer) {
		System.out.println("Removing peer");
		synchronized(peerLock) {
			peers.remove(peer);
		}
	}

	/**
	 * Closes the PeerRegistry
	 **/
	public void close() {
		Map<String,Peer> temp = null;
		synchronized(peerLock) {
			running = false;
			temp = new HashMap<String,Peer>(peers);
		}
		for(Peer p : peers.values()) {
			p.close();
		}
	}

	/**
	 * The number of peers this Torrent is connected to.
	 *
	 * @return The number of peers this Torrent is connected to.
	 **/
	public int getNumPeers() {
		return peers.size();
	}

	/**
	 * Notifies all current peers that we now have the given piece
	 *
	 * @param pieceId The id of the piece we now have
	 **/
	public void notifyHave(int pieceId) {
		synchronized(peerLock) {
			for(Peer p : peers.values()) {
				p.issueHave(pieceId);
			}
		}
	}

}
