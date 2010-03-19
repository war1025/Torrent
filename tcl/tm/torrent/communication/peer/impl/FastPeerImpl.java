package tcl.tm.torrent.communication.peer.impl;

import tcl.tm.torrent.communication.peer.FastPeer;
import tcl.tm.torrent.communication.peer.listener.PeerListener;
import tcl.tm.torrent.communication.peer.listener.impl.FastPeerListener;
import tcl.tm.torrent.communication.peer.sender.PeerSender;
import tcl.tm.torrent.communication.peer.sender.impl.FastSenderImpl;
import tcl.tm.torrent.communication.peer.retriever.PeerRetriever;
import tcl.tm.torrent.communication.peer.retriever.impl.FastRetriever;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.util.PendingRequest;

import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.communication.CommunicationManager;
import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.file.FileAccessFuture;
import tcl.tm.torrent.info.TorrentInfo;
import tcl.tm.torrent.info.StatsInfo;

import tcl.tm.torrent.communication.util.Piece;

import java.net.Socket;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import java.util.Set;
import java.util.HashSet;

/**
 * This class deals with connections to a peer.
 * The connection is assumed to already have been made.
 * Handshake should have been taken care of by this point.
 * In short, this class should not need to do anything to establish the connection.
 * From that point on, however, it should be the sole interactor with the peer on this connection.
 *
 * @author Wayne Rowcliffe
 **/
public class FastPeerImpl implements FastPeer {

	private String name;

	private boolean peerInterested;
	private boolean peerChoking;
	private boolean amInterested;
	private boolean amChoking;

	private Socket peerConnection;
	private InputStream peerInput;
	private OutputStream peerOutput;

	private CommunicationManager cm;
	private FileAccessManager fam;
	private TorrentInfo ti;
	private StatsInfo si;

	private boolean[] peerBitfield;

	private Set<Integer> peerSuggested;
	private Set<Integer> peerAllowedFast;
	private Set<PendingRequest> pendingRequests;

	private boolean running;

	private Piece currentPiece;

	private Object chokeLock;
	private Object pieceLock;
	private Object runLock;
	private Object fastLock;

	private PeerListener peerListener;
	private PeerSender peerSender;
	private PeerRetriever pieceRetriever;

	/**
	 * Creates a StandardPeer for the given Torrent using the given Socket
	 *
	 * @param torrent The Torrent this Peer is sharing / downloading.
	 * @param peerConnection The Socket representing the connection to this Peer.
	 **/
	public FastPeerImpl(Torrent torrent, Socket peerConnection) {

		this.peerConnection = peerConnection;
		this.name = peerConnection.getInetAddress().toString();

		try{
			this.peerInput = peerConnection.getInputStream();
			this.peerOutput = peerConnection.getOutputStream();
		} catch(IOException e) {
			throw new IllegalStateException(e.getMessage());
		}

		this.cm = torrent.getCommunicationManager();
		this.fam = torrent.getFileAccessManager();
		this.ti = torrent.getInformationManager().getTorrentInfo();
		this.si = torrent.getInformationManager().getStatsInfo();

		this.peerBitfield = new boolean[ti.getPieceCount()];

		this.peerSuggested = new HashSet<Integer>();
		this.peerAllowedFast = new HashSet<Integer>();
		this.pendingRequests = new HashSet<PendingRequest>();

		this.chokeLock = new Object();
		this.pieceLock = new Object();
		this.runLock = new Object();
		this.fastLock = new Object();

		this.peerSender = new FastSenderImpl(this, fam, cm, peerOutput);
		this.peerListener = new FastPeerListener(this, peerSender, peerInput);
		this.pieceRetriever = new FastRetriever(this, peerSender, cm, si, chokeLock, pieceLock);

		this.running = true;

		this.amChoking = true;
		this.peerChoking = true;


		new Thread(peerListener,"Fast Peer Listener: " + name).start();
		new Thread(peerSender,"Fast Peer Sender: " + name).start();
		new Thread(pieceRetriever, "Fast Piece Retriever: " + name).start();

		peerSender.sendBitfield();
		peerSender.issueUnchoke();
	}

	/**
	 * Closes the connection with this Peer
	 **/
	public void close() {
		synchronized(runLock) {
			running = false;
		}
		try{ peerInput.close(); } catch(IOException e) {}
		try{ peerOutput.close(); } catch(IOException e) {}
		try{ peerConnection.close(); } catch(IOException e) {}
		cm.removePeer(name);

		peerSender.issueKeepAlive();

		setChoked(false);

		synchronized(pieceLock) {
			pieceLock.notifyAll();
		}
	}

	/**
	 * Whether or not this Peer connection is running
	 *
	 * @return True if this is an active connection, else false.
	 **/
	public boolean isRunning() {
		synchronized(runLock) {
			return running;
		}
	}

	public void setInterested(boolean interested) {
		peerInterested = interested;

	}

	public boolean getInterested() {
		return peerInterested;

	}

	public void setAmInterested(boolean interested) {
		amInterested = interested;
	}

	public boolean getAmInterested() {
		return amInterested;

	}

	public void setChoked(boolean choked) {
		synchronized(chokeLock) {
			peerChoking = choked;
			if(!choked) {
				chokeLock.notifyAll();
			}
		}
	}

	public boolean getChoked() {
		synchronized(chokeLock) {
			return peerChoking;
		}
	}

	public void setAmChoking(boolean choking) {
		amChoking = choking;
	}

	public boolean getAmChoking() {
		return amChoking;
	}

	public boolean[] getBitfield() {
		return peerBitfield;
	}

	public void bitfieldUpdated() {
		cm.peerBitfield(peerBitfield);
		if(!amInterested) {
			if(cm.peerInteresting(peerBitfield)) {
				peerSender.issueInterested();
			}
		}
	}

	public void hasPiece(int id) {
		if(id > 0 && id < peerBitfield.length) {
			peerBitfield[id] = true;
		}
	}

	public Piece getCurrentPiece() {
		return currentPiece;
	}

	public void setCurrentPiece(Piece piece) {
		currentPiece = piece;
	}

	public void notifyPieceComplete() {
		synchronized(pieceLock) {
			pieceLock.notifyAll();
		}
	}

	public void issueHave(int id) {
		peerSender.issueHave(id);
	}

	public Object getFastLock() {
		return fastLock;
	}

	public Set<PendingRequest> getPendingRequests() {
		return pendingRequests;
	}

	public Set<Integer> getSuggested() {
		return peerSuggested;
	}

	public Set<Integer> getAllowedFast() {
		return peerAllowedFast;
	}

}
