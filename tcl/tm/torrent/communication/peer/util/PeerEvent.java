package tcl.tm.torrent.communication.peer.util;

/**
 * A PeerEvent contains the data portion of a message sent to us by a Peer.
 * The standard BitTorrent message format is <length><code><data>
 * The creator of the event should parse out the length and code portions
 * to determine what method the event should be forwarded to and how long the
 * included data should be.
 **/
public class PeerEvent {
	
	private byte[] data;

	/**
	 * Constructs a PeerEvent with the given data.
	 * 
	 * @param data The data for this PeerEvent.
	 **/
	public PeerEvent(byte[] data) {
		this.data = data;
	}

	/**
	 * Returns the data relevant to this event.
	 * 
	 * @return The data relevant to this event.
	 **/
	public byte[] getData() {
		return data;
	}
}
