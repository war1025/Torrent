package tcl.tm.torrent.communication.peer.listener.handler;

import tcl.tm.torrent.communication.peer.util.PeerEvent;

public interface PeerEventHandler {
	
	public void handle(PeerEvent e);
}
