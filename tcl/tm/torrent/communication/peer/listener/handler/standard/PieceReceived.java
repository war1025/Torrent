package tcl.tm.torrent.communication.peer.listener.handler.standard;

import tcl.tm.torrent.communication.util.Piece;
import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;

public class PieceReceived implements PeerEventHandler {
	
	private Peer peer;
	
	public PieceReceived(Peer peer) {
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
				if(currentPiece.isComplete()) {
					peer.notifyPieceComplete();
				}
			}
		}
	}
}
