package tcl.tm.torrent.communication.peer.retriever.impl;

import tcl.tm.torrent.info.StatsInfo;

import tcl.tm.torrent.communication.CommunicationManager;
import tcl.tm.torrent.communication.util.Piece;
import tcl.tm.torrent.communication.peer.util.PendingRequest;
import tcl.tm.torrent.communication.peer.FastPeer;
import tcl.tm.torrent.communication.peer.sender.PeerSender;
import tcl.tm.torrent.communication.peer.retriever.PeerRetriever;

public class FastRetriever implements PeerRetriever {
	
	private FastPeer peer;
	private PeerSender sender;
	private CommunicationManager cm;
	private StatsInfo si;
	private Object chokeLock;
	private Object pieceLock;
	private boolean strikeOne;
		
	public FastRetriever(FastPeer peer, PeerSender sender, CommunicationManager cm, StatsInfo si, Object chokeLock, Object pieceLock) {
		this.peer = peer;
		this.sender = sender;
		this.cm = cm;
		this.si = si;
		this.chokeLock = chokeLock;
		this.pieceLock = pieceLock;
		this.strikeOne = false;
	}
	
	public void run() {
		while(peer.isRunning()) {
			chokeWait();
			if(peer.isRunning()) {
				updateCurrentPiece();
				if(peer.getCurrentPiece() == null && si.getNumPiecesLeft() == 0) {
					peer.close();
					return;
				}
				Piece currentPiece = peer.getCurrentPiece();
				if(currentPiece != null) {
					int[] needed = currentPiece.getNeededBlocks();
					for(int block : needed) {
						int id = currentPiece.getPieceId();
						int offset = currentPiece.getBlockSize() * block;
						int length = (block == currentPiece.getBlockCount()-1) ? currentPiece.getFinalBlockSize() 
																					: currentPiece.getBlockSize();
						sender.issueRequest(id,offset,length);
						synchronized(peer.getFastLock()) {
							peer.getPendingRequests().add(new PendingRequest(id, offset, length));
						}
					}
					pieceWait();
				} else {
					synchronized(pieceLock) {
						try {
							pieceLock.wait(15000);
						} catch(InterruptedException e) {}
					}
				}							
			}
		}
		if(peer.getCurrentPiece() != null) {
			cm.returnPiece(peer.getCurrentPiece());
			peer.setCurrentPiece(null);
		}
	}
	
	private void chokeWait() {
		synchronized(chokeLock) {
			while(peer.getChoked()) {
				try {
					if(peer.getCurrentPiece() != null) {
						cm.returnPiece(peer.getCurrentPiece());
						peer.setCurrentPiece(null);
					}
					chokeLock.wait(300000);
					if(peer.getChoked()) {
						peer.close();
					}
				} catch(InterruptedException e) {}
			}
		}
	}
	
	private void pieceWait() {
		try {
			synchronized(pieceLock) {
				pieceLock.wait(200000);
			}
			if(!peer.getCurrentPiece().isComplete()) {
				peer.close();
			}
		} catch(InterruptedException e) {}
	}
	
	private void updateCurrentPiece() {
		if(peer.getCurrentPiece() == null) {
			peer.setCurrentPiece(cm.assignPiece(peer.getBitfield()));
		} else if(peer.getCurrentPiece().isComplete()) {
			boolean valid = cm.returnPiece(peer.getCurrentPiece());
			if(!valid) {
				if(strikeOne) {
					peer.close();
				} else {
					strikeOne = true;
				}
			}
			peer.setCurrentPiece(cm.assignPiece(peer.getBitfield()));
		}
	} 
	
}
