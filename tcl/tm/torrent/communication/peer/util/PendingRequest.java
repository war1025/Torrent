package tcl.tm.torrent.communication.peer.util;

public class PendingRequest {
	
	private int index;
	private int offset;
	private int length;
	
	public PendingRequest(int index, int offset, int length) {
		this.index = index;
		this.offset = offset;
		this.length = length;
	}
	
	public boolean equals(Object o) {
		if( o instanceof PendingRequest) {
			PendingRequest p = (PendingRequest) o;
			return (p.index == this.index) && (p.offset == this.offset) && (p.length == this.length);
		}
		return false;
	}
	
	public int hashCode() {
		int hash = 31;
		hash *= index;
		hash += 31;
		hash *= offset;
		hash += 31;
		hash *= length;
		
		return hash;
	}
}
