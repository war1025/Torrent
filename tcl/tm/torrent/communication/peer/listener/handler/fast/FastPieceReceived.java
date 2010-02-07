package tcl.tm.torrent.communication.peer.listener.handler.fast;

import tcl.tm.torrent.communication.util.Piece;
import tcl.tm.torrent.communication.peer.FastPeer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.util.PendingRequest;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;

public class FastPieceReceived implements PeerEventHandler {
	
	private FastPeer peer;
	
	public FastPieceReceived(FastPeer peer) {
		this.peer = peer;
	}
	
	public void handle(PeerEvent e) {
		Piece currentPiece = peer.getCurrentPiece();
		int piece = parseLength(e.getData(),0);
		int offset = parseLength(e.getData(),4);
		//System.out.println(name + "-> Received: " + piece + " " + offset + " " + e.getData().length);
		if(currentPiece != null) {
			if((piece == currentPiece.getPieceId())) {
				currentPiece.saveBlock(offset/currentPiece.getBlockSize(),e.getData(),8,e.getData().length - 8);
			}
		}
		synchronized(peer.getFastLock()) {
			peer.getPendingRequests().remove(new PendingRequest(piece, offset, e.getData().length -8));
			if(peer.getPendingRequests().size() == 0) {
				peer.notifyPieceComplete();
			}
		}
	}
}
