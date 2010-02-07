package tcl.tests;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

import tcl.tm.torrent.info.TorrentInfo;
import tcl.tm.torrent.info.impl.TorrentInfoImpl;

public class TorrentInfoDisplay {

	public static void main(String[] args) {

		TorrentInfo torrent = new TorrentInfoImpl(args[0]);

		System.out.println("Announce URL: " + torrent.getAnnounceURL());
		System.out.println("Announce List: " + Arrays.toString(torrent.getAnnounceList()));
		
		System.out.println("Name: " + torrent.getTorrentName());
		System.out.println("Comment: " + torrent.getComment());
		System.out.println("Created By: " + torrent.getCreatedBy());
		System.out.println("Piece Length: " + torrent.getPieceLength());
		System.out.println("Final Piece Length: " + torrent.getFinalPieceLength());
		System.out.println("Pieces: " + torrent.getPieceCount());
		System.out.println("Escaped Hash: " + torrent.getEscapedInfoHash());

		for(int i = 0; i < torrent.getFileCount(); i++) {
			System.out.println("Length: " + torrent.getFileLength(i) + 
							   " \tByte: " + torrent.getFileStartByte(i) + 
							   " \tStart: (" + torrent.getFileStartLocation(i)[0] + 
							   "," + torrent.getFileStartLocation(i)[1] + 
							   ")  \tEnd: (" + torrent.getFileEndLocation(i)[0]	+ 
							   "," + torrent.getFileEndLocation(i)[1] + 
							   ") \t" + Arrays.toString(torrent.getFilePath(i)));
		}
		
		 		
	}
}
