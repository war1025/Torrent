package tcl.tm.torrent.communication.peer.listener.handler.fast;

import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

public class HaveNoneReceived implements PeerEventHandler {

	private Peer peer;

	public HaveNoneReceived(Peer peer) {
		this.peer = peer;
	}

	public void handle(PeerEvent e) {
		peer.bitfieldUpdated();
	}
}
