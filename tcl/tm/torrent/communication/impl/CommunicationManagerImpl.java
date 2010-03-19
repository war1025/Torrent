package tcl.tm.torrent.communication.impl;

import java.net.Socket;
import java.io.IOException;
import java.util.Map;
import java.util.Hashtable;

import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.communication.CommunicationManager;
import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.impl.StandardPeer;
import tcl.tm.torrent.communication.peer.impl.FastPeerImpl;
import tcl.tm.torrent.communication.util.ConnectionSeeker;
import tcl.tm.torrent.communication.util.Piece;
import tcl.tm.torrent.communication.util.ThroughputMonitor;


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
 * As always see the interface for any other information that you might need
 * about this class.
 *
 * @author Wayne Rowcliffe
 **/
public class CommunicationManagerImpl implements CommunicationManager
{
	private Torrent torrent;
	private Map<String,Peer> peers;
	private boolean running;

	private ConnectionSeeker seeker;


	private boolean[] inProgress;
	private int[] multiBitField;
	private Object lock;
	private Object peerLock;
	private Object runLock;
	private Map<Integer,Piece> abandonedPieces;
	private boolean endgame;

	private ThroughputMonitor speed;

	public CommunicationManagerImpl(Torrent torrent) {
		this.torrent = torrent;
		this.peers = new Hashtable<String,Peer>();
		this.running = true;
		this.endgame = false;
		this.seeker = new ConnectionSeeker(torrent);
		this.speed = new ThroughputMonitor();
		this.speed.start();

		this.lock = new Object();
		this.peerLock = new Object();
		this.runLock = new Object();

		this.inProgress = new boolean[torrent.getInformationManager().getTorrentInfo().getPieceCount()];
		this.multiBitField = new int[inProgress.length];
		getPreviousState();
		this.abandonedPieces = new Hashtable<Integer,Piece>();

		new Thread(seeker,"Connection Seeker: " + torrent.getInformationManager().getTorrentInfo().getTorrentName()).start();

	}

	/**
	 * Adds a Peer to this CommunicationManager. Meaning that the peer on the other side
	 * of the Socket is interested in downloading / sharing this Torrent.
	 *
	 * @param peer The Socket representing the connection to an interested peer.
	 **/
	public void addPeer(Socket peer, byte[] reserved) {
		synchronized(peerLock) {
			if(isRunning() && !peers.containsKey(peer.getInetAddress().toString()) && !(peer.getInetAddress().equals(peer.getLocalAddress()))) {
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
		synchronized(peerLock) {
			peers.remove(peer);
		}
	}

	/**
	 * Returns the speed this Torrent is downloading at
	 *
	 * @return The speed this Torrent is downloading at.
	 **/
	public int getConnectionSpeed() {
		return speed.getSpeed();
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
	 * The number of bytes that we have uploaded for this torrent.
	 *
	 * @return The number of bytes that have been uploaded for this torrent.
	 **/
	public int getNumBytesUploaded() {
		return 0;
	}

	/**
	 * A synchronized check whether this CommunicationManager is still running.
	 * This allows multiple threads to know whether or not to continue operation.
	 *
	 * @return Whether this CommunicationManager is still running.
	 **/
	private boolean isRunning() {
		synchronized(runLock) {
			return running;
		}
	}

	/**
	 * Closes this CommunicationManager. This involves disconnecting from all Peers
	 * for this Torrent, as well as ceasing active ConnectionSeeking.
	 **/
	public void close() {
		synchronized(runLock) {
			running = false;
		}
		seeker.close();
		Map<String,Peer> temp = new Hashtable<String,Peer>(peers);
		for(Peer p: temp.values()) {
			p.close();
		}
		speed.close();
	}

	/**
	 * Given a bitfield, assigns a Piece which can be downloaded by this Peer.
	 * Preferably, the chosen piece should be the rarest piece that can be downloaded
	 * by a Peer with this bitfield.
	 *
	 * @param bitfield The list of pieces which the given Peer has available for downloading.
	 *
	 * @return A piece which can be downloaded by the Peer, given the bitfield they have provided.
	 **/
	public Piece assignPiece(boolean[] bitfield) {
		Piece assigned = null;
		synchronized(lock) {
			int lowestId = -1;
			int lowestFrequency = Integer.MAX_VALUE;
			boolean foundAbandoned = false;
			for(int i : abandonedPieces.keySet()) {
				if(bitfield[i]) {
					lowestId = i;
					assigned = abandonedPieces.remove(lowestId);
					foundAbandoned = true;
					break;
				}
			}
			if(!foundAbandoned) {
				for(int i = 0; i < bitfield.length; i++) {
					if(bitfield[i] && (!inProgress[i]) && (multiBitField[i] > 0) && (multiBitField[i] < lowestFrequency)) {
						lowestId = i;
						lowestFrequency = multiBitField[i];
					}
				}
			}
			System.out.println("Abandoned Pieces Size : " + abandonedPieces.size());
			if(lowestId >= 0 && assigned == null) {
				int length = 0;
				if(lowestId == bitfield.length -1) {
					length = torrent.getInformationManager().getTorrentInfo().getFinalPieceLength();
				} else {
					length = torrent.getInformationManager().getTorrentInfo().getPieceLength();
				}
				assigned = new Piece(lowestId,16 * 1024,length, speed);
			}
			if(lowestId >= 0) {
				inProgress[lowestId] = true;
			} else if(!endgame) {
				checkEndgame();
			} else {
				assigned = attemptEndgame(bitfield);
			}
		}
		return assigned;
	}

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
	public boolean returnPiece(Piece p) {
		boolean success = false;
		synchronized(lock) {
			if(p.isComplete()) {
				success = torrent.getFileAccessManager().savePiece(p.getPieceId(),p.getData()).getSuccess();
				System.out.println("Verifying Returned Piece: " + p.getPieceId() + " " + success);
				if(success) {
					multiBitField[p.getPieceId()] = -1;
					for(Peer peer : peers.values()) {
						peer.issueHave(p.getPieceId());
					}
				}
			} else {
				abandonedPieces.put(p.getPieceId(),p);
			}
			inProgress[p.getPieceId()] = false;
		}
		return success;
	}

	/**
	 * Called by a Peer to indicate to this CommunicationManager that it now has a new piece which can be downloaded from it.
	 *
	 * @param pieceId The piece which a Peer now has.
	 **/
	public void peerHave(int pieceId) {
		synchronized(lock) {
			if(multiBitField[pieceId] >= 0) {
				multiBitField[pieceId] += 1;
			}
		}
	}

	/**
	 * Called by a Peer to indicate to this CommunicationManager the entire bitfield of pieces
	 * that are available for download from the peer.
	 *
	 * @param bitfield The bitfield of the calling peer.
	 **/
	public void peerBitfield(boolean[] bitfield) {
		synchronized(lock) {
			for(int i = 0; i < bitfield.length; i++) {
				if(bitfield[i] && (multiBitField[i] >= 0)) {
					multiBitField[i] += 1;
				}
			}
		}
	}

	/**
	 * Called by a Peer to decide whether it has pieces which are still needed for this Torrent.
	 *
	 * @param bitfield The bitfield of the calling peer.
	 *
	 * @return Whether the calling peer has pieces which are still needed for this Torrent.
	 **/
	public boolean peerInteresting(boolean[] bitfield) {
		boolean interesting = false;
		synchronized(lock) {
			for(int i = 0; i < bitfield.length; i++) {
				if(bitfield[i] && !(inProgress[i]) && multiBitField[i] > 0) {
					interesting = true;
					break;
				}
			}
		}
		return interesting;
	}

	/**
	 * Called by a Peer to indicate that it will no longer be sharing pieces. Probably because of
	 * disconnection. Its pieces will be removed from the list of available pieces for download.
	 *
	 * @param bitfield The bitfield of the calling peer.
	 **/
	public void removeBitfield(boolean[] bitfield) {
		synchronized(lock) {
			for(int i = 0; i < bitfield.length; i++) {
				if(bitfield[i] && (multiBitField[i] > 0)) {
					multiBitField[i] -= 1;
				}
			}
		}
	}

	/**
	 * Contacts the FileAccessManager to establish which pieces have been completed previously.
	 **/
	private void getPreviousState() {
		byte[] state = torrent.getFileAccessManager().getBitfield().getData();
		for(int i = 0; i < state.length; i++) {
			for(int j = 0; j < 8; j++) {
				if((((state[i] >> (7-j)) & 1) == 1) && inProgress.length > (i*8 + j)) {
					System.out.println("Found Previously completed piece: " + (i*8+j));
					multiBitField[i*8 + j] = -1;
				}
			}
		}
	}

	/**
	 * Checks whether "Endgame" has occured.
	 * This means that all pieces in the Torrent are either completed
	 * or in process of downloading.
	 * Connection speeds at this time can drop drastically as the remaining pieces
	 * are usually assigned to unresponsive peers. During endgame, these pieces are assigned
	 * to multiple peers.
	 **/
	private void checkEndgame() {
		System.out.println("Checking Endgame");
		boolean end = true;
		for(int i = 0; i < multiBitField.length; i++) {
			if(multiBitField[i] >= 0 && !inProgress[i]) {
				end = false;
				break;
			}
		}
		endgame = end;
	}

	/**
	 * Attempts to assign an endgame piece given a Peer's bitfield.
	 * This is only done in the case that we are in "endgame" and no other pieces
	 * are available for download.
	 *
	 * @param bitfield The calling Peer's bitfield.
	 *
	 * @return An endgame Piece to assign to the calling Peer.
	 **/
	private Piece attemptEndgame(boolean[] bitfield) {
		int lowestId = -1;
		int lowestFrequency = Integer.MAX_VALUE;
		System.out.println("Attempting Endgame");
		for(int i = 0; i < bitfield.length; i++) {
			if(bitfield[i] &&  (multiBitField[i] > 0) && (multiBitField[i] < lowestFrequency)) {
				lowestId = i;
				lowestFrequency = multiBitField[i];
			}
		}
		if(lowestId >= 0) {
			int length = 0;
			if(lowestId == bitfield.length -1) {
				length = torrent.getInformationManager().getTorrentInfo().getFinalPieceLength();
			} else {
				length = torrent.getInformationManager().getTorrentInfo().getPieceLength();
			}
			return new Piece(lowestId,16 * 1024,length, speed);
		} else {
			return null;
		}
	}


}
