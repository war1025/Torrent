package tcl.tm.torrent.communication.peer;

import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.util.PendingRequest;

import java.util.Set;

public interface FastPeer extends Peer {
	
	public Set<Integer> getAllowedFast();
	
	public Set<Integer> getSuggested();
	
	public Set<PendingRequest> getPendingRequests();
	
	public Object getFastLock();
	
}
