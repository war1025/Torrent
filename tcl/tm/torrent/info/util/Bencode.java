package tcl.tm.torrent.info.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility for deciphering bencoded data
 **/
public class Bencode {

	/**
	 * Returns the data stored in the given .torrent file as a series of Maps, Lists, Strings, and Integers.
	 * These can be interlayed recursively in any manner possible. This is why all maps are Map<String,Object> and all
	 * lists are List<Object>
	 *
	 * Extra data is also added to the torrent Map, and minor processing is done on the data from the .torrent file.
	 *
	 * If there is an error, this method will return null.
	 *
	 * @param The path to the .torrent file to decode.
	 *
	 * @return A decoded form of the .torrent file, where the top level is a Map<String,Object>
	 **/
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getTorrentInfo(String location) {

		try{
			InputStream in = new FileInputStream(location);

			Map<String,Object> m = null;
			while(in.available() > 0) {

				int first = in.read();
				if(first == 'd') {
					m = doDictionary(in,null);
				}
			}
			String piece = (String)((Map<String,Object>)m.get("info")).get("pieces");

			String[] pieces = new String[piece.length()/20];

			for(int i = 0; i < pieces.length; i++) {
				pieces[i] = piece.substring(i*20,(i+1)*20);
			}
			((Map<String,Object>)m.get("info")).put("pieces",pieces);

			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update((byte[])m.get("info_hash"));
			m.put("info_hash",digest.digest());
			m.put("escaped_info_hash",escapeBytes((byte[])m.get("info_hash")));
			Map<String,Object> info = (Map<String,Object>)m.get("info");
			if(info.get("files") == null) {
				List<Object> file = new ArrayList<Object>();
				Map<String,Object> one = new HashMap<String,Object>();
				one.put("length",info.get("length"));
				List<Object> path = new ArrayList<Object>();
				path.add(info.get("name"));
				one.put("path",path);
				file.add(one);
				info.put("files",file);
			}
			addPieceInfo(m);
			return m;
		} catch(Exception e) {e.printStackTrace();}

		return null;


	}

	/**
	 * Decodes the bencoded data returned by the tracker.
	 *
	 * @param in The stream containing the tracker's response
	 *
	 * @return The decoded data.
	 **/
	public static Map<String,Object> getTrackerInfo(InputStream in) {

		try {
			Map<String,Object> m = null;
			while(in.available() > 0) {

				int first = in.read();
				if(first == 'd') {
					m = doDictionary(in,null);
				}
			}

			if(m.get("peers") instanceof String) {
				String peers = (String) m.get("peers");
				if(peers != null) {
					String[] peerList = new String[peers.length()/6];
					for(int i = 0; i < peerList.length; i++) {
						String s2 = peers.substring(i*6,(i+1)*6);
						StringBuilder out = new StringBuilder();
						out.append((s2.charAt(0) & 0xFF) + ".");
						out.append((s2.charAt(1) & 0xFF) + ".");
						out.append((s2.charAt(2) & 0xFF) + ".");
						out.append((s2.charAt(3) & 0xFF) + ":");
						out.append((s2.charAt(4) & 0xFF) << 8 | (s2.charAt(5) & 0xFF));
						peerList[i] = out.toString();
					}
					m.put("peers",peerList);
				}
			} else {
				m.put("peers",new String[0]);
			}
			return m;
		} catch(Exception e) {e.printStackTrace();}
		return null;
	}

	/**
	 * Reads from the given input stream and creates a Map out of a Bencoded dictionary.
	 * If the given output stream is not null, all data read from in is echoed to out.
	 *
	 * @param in The stream containing the bencoded data
	 * @param out A stream to echo all data from in to.
	 *
	 * @return A Map<String,Object> corresponding to the bencoded dictionary.
	 **/
	@SuppressWarnings("unchecked")
	private static Map<String,Object> doDictionary(InputStream in, OutputStream out) throws Exception {
		Map<String,Object> map = new HashMap<String,Object>();
		int next = in.read();
		if(out != null) { out.write(next); }
		while((char)next != 'e') {
			String s = doString(next,in,out);
			if(s.equals("info")) {
				out = new ByteArrayOutputStream();
			}
			next = in.read();
			if(out != null) { out.write(next); }
			if(s.equals("")) {
				continue;
			}
			Object o = doSwitch(next,in,out);
			if(s.equals("info")) {
				map.put("info_hash",((ByteArrayOutputStream)out).toByteArray());
				out = null;
			}
			next = in.read();
			if(out != null) { out.write(next); }
			map.put(s,o);
		}
		return map;
	}

	/**
	 * Since bencoded data can be stacked recursively, we need to do a switch to see
	 * what type of data structure is next.
	 *
	 * @param next The next byte from the bencoded data, which determines what sort of datastructure will follow.
	 * @param in The input stream which contains the bencoded data.
	 * @param out The output stream to echo data read from in to
	 *
	 * @return An Object corresponding to whichever datastructure was next in the bencoded data.
	 **/
	private static Object doSwitch(int next, InputStream in, OutputStream out) throws Exception {

		switch((char)next) {
			case('d') : return doDictionary(in,out);
			case('l') : return doList(in,out);
			case('i') : return doInteger(in,out);
			default : return doString(next,in,out);
		}
	}

	/**
	 * Decodes a bencoded String.
	 *
	 * @param i The first byte of the bencoded string data
	 * @param in The stream containing the bencoded data.
	 * @param out The stream to echo data read from in to.
	 *
	 * @return The String corresponding to the bencoded String.
	 **/
	private static String doString(int i, InputStream in, OutputStream out) throws Exception {
		char[] count = new char[30];
		count[0] = (char) i;
		int pos = 1;
		int cur = in.read();
		if(out != null) { out.write(cur); }
		while((char)cur != ':') {
			count[pos] = (char) cur;
			pos++;
			cur = in.read();
			if(out != null) { out.write(cur); }
		}
		char[] word = new char[Integer.parseInt(new String(count,0,pos))];
		for(int j = 0; j < word.length ; j++) {
			int next = in.read();
			word[j] = (char) next;
			if(out != null) { out.write(next); }
		}
		return new String(word);
	}

	/**
	 * Decodes a bencoded Integer.
	 *
	 * @param in The stream containing the bencoded data.
	 * @param out The stream to echo data read from in to.
	 *
	 * @return The Integer corresponding to the bencoded Integer.
	 **/
	private static Long doInteger(InputStream in, OutputStream out) throws Exception {
		char[] num = new char[30];
		int i = 0;
		int next = in.read();
		if(out != null) { out.write(next); }
		while((char)next != 'e') {
			num[i] = (char) next;
			i++;
			next = in.read();
			if(out != null) { out.write(next); }
		}
		Long e = Long.parseLong(new String(num,0,i));
		return e;
	}

	/**
	 * Decodes a bencoded List.
	 *
	 * @param in The stream containing the bencoded data.
	 * @param out The stream to echo data read from in to.
	 *
	 * @return The List corresponding to the bencoded List.
	 **/
	private static List<Object> doList(InputStream in, OutputStream out) throws Exception {
		List<Object> list = new ArrayList<Object>();
		int next = in.read();
		if(out != null) { out.write(next); }
		while((char)next != 'e') {
			list.add(doSwitch(next,in,out));
			next = in.read();
			if(out != null) { out.write(next); }
		}
		return list;
	}

	/**
	 * Escapes an array of bytes and forms a String according the bittorrent spec.
	 *
	 * @param bytes The bytes to escape.
	 *
	 * @return The escaped String corresponding to the given bytes.
	 **/
	public static String escapeBytes(byte[] bytes) {
		int[] ints = new int[bytes.length];
		for(int i = 0; i < bytes.length; i++) {
			ints[i] = bytes[i] & 0xFF;
		}
	/*	For Testing against the BitTorrent Spec Example
		ints[0] = Integer.valueOf("12",16);
		ints[1] = Integer.valueOf("34",16);
		ints[2] = Integer.valueOf("56",16);
		ints[3] = Integer.valueOf("78",16);
		ints[4] = Integer.valueOf("9a",16);
		ints[5] = Integer.valueOf("bc",16);
		ints[6] = Integer.valueOf("de",16);
		ints[7] = Integer.valueOf("f1",16);
		ints[8] = Integer.valueOf("23",16);
		ints[9] = Integer.valueOf("45",16);
		ints[10] = Integer.valueOf("67",16);
		ints[11] = Integer.valueOf("89",16);
		ints[12] = Integer.valueOf("ab",16);
		ints[13] = Integer.valueOf("cd",16);
		ints[14] = Integer.valueOf("ef",16);
		ints[15] = Integer.valueOf("12",16);
		ints[16] = Integer.valueOf("34",16);
		ints[17] = Integer.valueOf("56",16);
		ints[18] = Integer.valueOf("78",16);
		ints[19] = Integer.valueOf("9a",16);
	*/
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < ints.length; i++) {
			int letter = ints[i];
			if(letter == 45 || letter == 46 || letter == 95 || letter == 126) {
				out.append((char)letter);
			} else if((letter <= 57 && letter >= 48) || (letter <= 122 && letter >= 97) || (letter <= 90 && letter >= 65)) {
				out.append((char)letter);
			} else {
				String s = Integer.toHexString(letter).toUpperCase();
				if(s.length() ==1) {
					s = "0"+s;
				}
				out.append("%"+ s.substring(s.length()-2,s.length()));
			}
		}
		return out.toString();
	}

	/**
	 * Adds information about where files start and stop in relation to
	 * pieces into the torrent Map.
	 *
	 * @param torrent The torrent Map to add piece info to.
	 **/
	@SuppressWarnings("unchecked")
	private static void addPieceInfo(Map<String,Object> torrent) {
		Map<String,Object> info = (Map<String,Object>)torrent.get("info");
		long length = (Long) info.get("piece length");
		List<Object> files = (List<Object>) info.get("files");
		long curBytes = 0;
		for(Object o : files) {
			Map<String,Object> f = (Map<String,Object>) o;
			f.put("start_byte",curBytes);
			f.put("start_offset",curBytes % length);
			f.put("start_piece",curBytes/length);
			long fileLength = (Long) f.get("length");
			curBytes += fileLength;
			f.put("end_offset",(curBytes-1)%length);
			f.put("end_piece",(curBytes -1)/length);
		}
	}

}
