package tcl.tm.torrent.communication.peer.sender;

import tcl.tm.torrent.communication.peer.sender.PeerSender;

public interface FastSender extends PeerSender {
	
	public void issueReject(int pieceId, int byteOffset, int length);
	
}
