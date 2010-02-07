package tcl.tests;

import java.util.Map;
import java.io.*;
import java.net.URL;
import java.net.URI;

import tcl.tm.torrent.info.util.Bencode;

public class HTTPGetTest {

	public static void main(String[] args) throws Exception {
		
		Map<String,Object> torrent = Bencode.getTorrentInfo(args[0]);

		String s = (String) torrent.get("announce");

		s = s.replace("announce","scrape");

		URL url = new URL(s + "?info_hash=" + torrent.get("escaped_info_hash"));

		System.out.println(Bencode.getTrackerInfo(url.openStream()));

	}
}
