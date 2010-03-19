package tcl.tm.torrent.communication.peer.listener.impl;

import tcl.tm.torrent.communication.peer.listener.handler.PeerEventHandler;
import tcl.tm.torrent.communication.peer.listener.handler.standard.*;
import tcl.tm.torrent.communication.peer.listener.PeerListener;
import tcl.tm.torrent.communication.peer.sender.PeerSender;
import tcl.tm.torrent.communication.peer.util.PeerEvent;
import tcl.tm.torrent.communication.peer.Peer;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.parseLength;

import java.util.Map;
import java.util.HashMap;

import java.io.InputStream;
import java.io.IOException;

public class PeerListenerImpl implements PeerListener {

	private Peer peer;
	private InputStream peerInput;
	protected Map<Integer, PeerEventHandler> handlers;

	public PeerListenerImpl(Peer peer, PeerSender sender, InputStream peerInput) {
		this.peer = peer;
		this.peerInput = peerInput;
		this.handlers = new HashMap<Integer, PeerEventHandler>();

		handlers.put(-2, new KeepAliveReceived(peer));
		handlers.put(0, new ChokeReceived(peer));
		handlers.put(1, new UnchokeReceived(peer));
		handlers.put(2, new InterestedReceived(peer));
		handlers.put(3, new DisinterestedReceived(peer));
		handlers.put(4, new HaveReceived(peer));
		handlers.put(5, new BitfieldReceived(peer));
		handlers.put(6, new RequestReceived(sender));
		handlers.put(7, new PieceReceived(peer));
		handlers.put(8, new CancelReceived(peer));

	}

	/**
	 * Reads messages as they are received and forwards them to the appropriate handlers
	 **/
	public void run() {
		while(peer.isRunning()) {
			try {
				byte[] value = new byte[4];
				int toRead = 4;
				while(toRead > 0) {
					int read = peerInput.read(value,4-toRead,toRead);
					if(read <= 0) { peer.close(); return;}
					toRead -= read;
				}
				int length = parseLength(value,0);
				byte[] content = new byte[(length-1 > 0) ? length-1 : 0];
				int code = -2;
				if(length > 0) {
					code = peerInput.read();
					if(code == -1) { peer.close(); return;}
				}
				toRead = length-1;
				while(toRead > 0) {
					int read = peerInput.read(content,(length-1)-toRead,toRead);
					if(read <= 0) { peer.close(); return;}
					toRead -= read;
				}
				PeerEvent event = new PeerEvent(content);
				handlers.get(code).handle(event);
			} catch(IOException e) {
				//e.printStackTrace();
				peer.close();
			} catch(NullPointerException e) {
				peer.close();
			}
		}
	}
}
