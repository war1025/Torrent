package tcl.tm.torrent.communication.peer.listener.handler.standard;

import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

public class CancelReceived implements PeerEventHandler {
	
	private Peer peer;
	
	public CancelReceived(Peer peer) {
		this.peer = peer;
	}
	
	public void handle(PeerEvent e) {
	}
}
