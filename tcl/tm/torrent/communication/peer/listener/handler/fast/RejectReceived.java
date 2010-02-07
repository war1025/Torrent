package tcl.tm.torrent.communication.peer.listener.handler.fast;

import tcl.tm.torrent.communication.peer.FastPeer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.util.PendingRequest;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;


import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;


public class RejectReceived implements PeerEventHandler {

	private FastPeer peer;

	public RejectReceived(FastPeer peer) {
		this.peer = peer;
	}

	public void handle(PeerEvent e) {
		int piece = parseLength(e.getData(),0);
		int offset = parseLength(e.getData(),4);
		int length = parseLength(e.getData(),8);
		
		System.out.println("Got Piece Reject: " + piece + " " + offset + " " + length);
		
		synchronized(peer.getFastLock()) {
			peer.getPendingRequests().remove(new PendingRequest(piece,offset,length));
			if(peer.getPendingRequests().size() == 0) {
				peer.notifyPieceComplete();
			}
		}
	}
}
