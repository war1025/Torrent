package tcl.tm.torrent.communication.peer.listener.handler.standard;

import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;

public class HaveReceived implements PeerEventHandler {
	
	private Peer peer;
	
	public HaveReceived(Peer peer) {
		this.peer = peer;
	}
	
	public void handle(PeerEvent e) {
		peer.hasPiece(parseLength(e.getData(),0));
	}
}
