package tcl.tm.torrent.communication.peer.listener.handler.standard;

import tcl.tm.torrent.communication.peer.sender.PeerSender;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;

public class RequestReceived implements PeerEventHandler {
	
	private PeerSender sender;
	
	public RequestReceived(PeerSender peer) {
		this.sender = sender;
	}
	
	public void handle(PeerEvent e) {
		int piece = parseLength(e.getData(),0);
		int block = parseLength(e.getData(),4);
		int length = parseLength(e.getData(),8);
			
		//System.out.println(name + "-> Requested: " + piece + " " + block + " " + length);
		sender.sendPiece(piece,block,length);
	}
}
