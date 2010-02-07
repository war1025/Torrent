package tcl.tm.torrent.communication.peer.listener.handler.fast;

import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

public class HaveAllReceived implements PeerEventHandler {

	private Peer peer;

	public HaveAllReceived(Peer peer) {
		this.peer = peer;
	}

	public void handle(PeerEvent e) {
		boolean[] bitfield = peer.getBitfield();
		for(int i = 0; i < bitfield.length; i++) {
			bitfield[i] = true;
		}
		
		peer.bitfieldUpdated();
	}
}
