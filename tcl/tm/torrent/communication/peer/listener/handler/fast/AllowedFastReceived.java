package tcl.tm.torrent.communication.peer.listener.handler.fast;

import tcl.tm.torrent.communication.peer.FastPeer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;

public class AllowedFastReceived implements PeerEventHandler {

	private FastPeer peer;

	public AllowedFastReceived(FastPeer peer) {
		this.peer = peer;
	}

	public void handle(PeerEvent e) {
		synchronized(peer.getFastLock()) {
			peer.getAllowedFast().add(parseLength(e.getData(),0));
		}
	}
}
