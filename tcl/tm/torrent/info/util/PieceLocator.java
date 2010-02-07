package tcl.tm.torrent.info.util;

import tcl.tm.torrent.info.TorrentInfo;

/**
 * A utility class for determining where a piece is located in terms of the files in a torrent
 **/
public class PieceLocator {
	
	/**
	 * The pair (file,offset) which indicates where this piece starts
	 *
	 * @param torrent The TorrentInfo for this torrent
	 * @param piece The piece to locate.
	 *
	 * @return A pair of integers of the form (file,offset) where the piece starts
	 **/
	public static long[] getPieceStartLocation(TorrentInfo torrent, int piece) {

		int pieceLength = torrent.getPieceLength();

		long start = (long) piece * pieceLength;
		long end = start + pieceLength -1;

		long startFile = 0;
		
		long startFileByte = 0;
		

		for(int i = 0; i < torrent.getFileCount(); i++) {
			
			long startByte = torrent.getFileStartByte(i);
			long nextStart = startByte + torrent.getFileLength(i);
			if(nextStart > start) {
				startFile = i;
				startFileByte = startByte;
				break;
			}
		}
		
		long offset = start - startFileByte;
		
		return new long[] {startFile, offset};
	}
	
	/**
	 * The pair (file,offset) which indicates where this piece ends
	 *
	 * @param torrent The TorrentInfo for this torrent
	 * @param piece The piece to locate.
	 *
	 * @return A pair of integers of the form (file,offset) where the piece ends
	 **/	
	public static long[] getPieceEndLocation(TorrentInfo torrent, int piece) {
		
		int pieceLength = torrent.getPieceLength();

		long start = (long) piece * pieceLength;
		long end = 0;
		if(piece +1 < torrent.getPieceCount()) {
			end = start + pieceLength -1;
		} else {
			end = start + torrent.getFinalPieceLength() -1;
		}
		
		long endFile = 0;
		long endFileByte = 0;
		
		for(int i = 0; i < torrent.getFileCount(); i++) {
			
			long endByte = torrent.getFileStartByte(i) + torrent.getFileLength(i);

			if(endByte > end) {
				endFile = i;
				endFileByte = torrent.getFileStartByte(i);
				break;
			}
		}

		
		long endLength = end - endFileByte;
		
		return new long[] {endFile,endLength};
	}
}
		
