package tcl.tests;

import tcl.tm.torrent.info.impl.TorrentInfoImpl;
import tcl.tm.torrent.file.util.impl.StatusLoaderImpl;

public class StatusFiller {
	
	public static void main(String[] args) {
		
		TorrentInfoImpl t = new TorrentInfoImpl(args[0]);
		StatusLoaderImpl l = new StatusLoaderImpl(t,args[1]);
		
		for(int i = 0; i < t.getPieceCount(); i++) {
			l.setStatus(i,true);
		}
	}
}
