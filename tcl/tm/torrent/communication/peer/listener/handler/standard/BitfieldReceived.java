package tcl.tm.torrent.communication.peer.listener.handler.standard;

import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

public class BitfieldReceived implements PeerEventHandler {
	
	private Peer peer;
	
	public BitfieldReceived(Peer peer) {
		this.peer = peer;
	}
	
	public void handle(PeerEvent e) {
		boolean[] peerBitfield = peer.getBitfield();
		for(int i = 0; i < e.getData().length; i++) {
			int bit = e.getData()[i] & (0xff);
			for(int j = 0; j < 8; j++) {
				if((i*8 + (7 - j)) < peerBitfield.length) {
					peerBitfield[i*8 + (7 - j)] = ((bit >> j) & 1) == 1;
				}
			}
		}
		peer.bitfieldUpdated();
	}
}
