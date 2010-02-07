package tcl.tm.torrent.communication.peer.util;

public class PeerUtils {
	
	/**
	 * A helper method to parse a 4 byte set into an int.
	 * 
	 * @param value The byte[] containing the int.
	 * @param offset The offset within value at which the int begins.
	 * 
	 * @return The parsed int value.
	 **/
	public static int parseLength(byte[] value, int offset) {
		return (value[0+offset] & (0xff)) << 24 |
				 (value[1+offset] & (0xff)) << 16 |
				 (value[2+offset] & (0xff)) << 8 | 
				 (value[3+offset] & (0xff));
	}

	/**
	 * A helper method to decompose an int into a 4 byte array.
	 * 
	 * @param value The int to decompose
	 * 
	 * @return The byte[] containing the decomposed int.
	 **/
	public static byte[] decomposeLength(int value) {
		byte[] bytes = new byte[4];
		bytes[3] = (byte) (value & (0xff));
		bytes[2] = (byte) (value >> 8 & (0xff));
		bytes[1] = (byte) (value >> 16 & (0xff));
		bytes[0] = (byte) (value >> 24 & (0xff));
		return bytes;
	}
}
