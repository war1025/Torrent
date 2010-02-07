package tcl.tm.torrent.communication.peer.listener.handler.fast;

import tcl.tm.torrent.communication.peer.FastPeer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;

public class SuggestReceived implements PeerEventHandler {

	private FastPeer peer;

	public SuggestReceived(FastPeer peer) {
		this.peer = peer;
	}

	public void handle(PeerEvent e) {
		System.out.println("Got Piece Suggest: " + parseLength(e.getData(),0));
		synchronized(peer.getFastLock()) {
			peer.getSuggested().add(parseLength(e.getData(),0));
		}
	}
}
