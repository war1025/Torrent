package tcl.tm.torrent.communication.peer.listener.impl;

import tcl.tm.torrent.communication.peer.FastPeer;
import tcl.tm.torrent.communication.peer.sender.PeerSender;
import tcl.tm.torrent.communication.peer.listener.impl.PeerListenerImpl;
import tcl.tm.torrent.communication.peer.listener.handler.fast.*;

import java.io.InputStream;

public class FastPeerListener extends PeerListenerImpl {
	
	public FastPeerListener(FastPeer peer, PeerSender sender, InputStream peerInput) {
		super(peer,sender,peerInput);
		
		handlers.put(7, new FastPieceReceived(peer));
		handlers.put(14, new HaveAllReceived(peer));
		handlers.put(15, new HaveNoneReceived(peer));
		handlers.put(13, new SuggestReceived(peer));
		handlers.put(16, new RejectReceived(peer));
		handlers.put(17, new AllowedFastReceived(peer));
		
	}
}
		
	
	
