package tcl.tm.torrent.file.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A static class which deals with the verification of pieces of data
 **/
public class PieceVerifier {
	
	/**
	 * Verifies that the SHA 1 hash of the given data is equivalent to the hash String provided
	 * 
	 * @param data The data to verify
	 * @param hash The hash which the SHA 1 of data should return
	 * 
	 * @return Whether data has the same hash as was provided
	 **/
	public static boolean verify(byte[] data, String hash) {
		
		if(data == null) {return false;}
		
		MessageDigest md = null;
		
		try{
			md = MessageDigest.getInstance("SHA");
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
		byte[] hash1 = md.digest(data);
		
		// Bytes are signed, chars are unsigned.
		// We need to convert the bytes to chars to get values
		// in the same range as in the String hash.
		char[] hash2 = new char[hash1.length];
		
		for(int i = 0; i < hash1.length; i++) {
			hash2[i] = (char) (hash1[i] & 0xFF);
		}
		
		return hash.equals(new String(hash2));
	}
}
