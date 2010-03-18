package tcl.uim.util;

import tcl.tm.TorrentManager;
import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.info.StatsInfo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class StatCreator {
	
	public static List<Map<String,String>> createDBusStats(TorrentManager tm) {
		String[] torrents = tm.getTorrentsAvailable();
		List<Map<String,String>> stats = new ArrayList<Map<String,String>>();
		if(torrents.length > 0) {
			for(int i = 0; i < torrents.length; i++) {
				Torrent t = tm.getTorrent(torrents[i]);
				Map<String,String> stat = new HashMap<String,String>();
				if(t != null) {
					stat.put("name",getName(t));
					stat.put("eta", getEta(t));
					stat.put("speed", getSpeed(t));
					stat.put("progress", getProgress(t));
					stat.put("bytes_left", getBytesLeft(t));
					stat.put("bytes_downloaded", getBytesDownloaded(t));
					stat.put("pieces_left", getPiecesLeft(t));
					stat.put("peers", getPeers(t));
					stat.put("file_path", torrents[i]);
				} else {
					stat.put("name", "");
					stat.put("eta", "");
					stat.put("speed", "");
					stat.put("progress", "");
					stat.put("bytes_left", "");
					stat.put("bytes_downloaded", "");
					stat.put("pieces_left", "");
					stat.put("peers", "");
					stat.put("file_path", "");
				}
				stats.add(stat);
			}
		} else {
			stats.add(new HashMap<String,String>());
		}
		return stats;
	}
				
	
	private static String getName(Torrent t) {
		return t.getInformationManager().getTorrentInfo().getTorrentName();
	}
	
	private static String getEta(Torrent t) {
		int speed = t.getInformationManager().getStatsInfo().getSpeed();
		long bytesLeft = t.getInformationManager().getStatsInfo().getNumBytesLeft();
		String eta = "Unknown";
		if(speed > 0) {
			long time = bytesLeft/speed;
			long hours = time / 3600;
			time %= 3600;
			int minutes = (int) time / 60;
			time %= 60;
			int seconds = (int) time;
			if(hours < 100) {
				eta = String.format("%02d:%02d:%02d", hours, minutes, seconds);
			}
		}
		return eta;
	}
	
	private static String getSpeed(Torrent t) {
		long total = t.getInformationManager().getStatsInfo().getSpeed();
		return humanBytes(total) + "/s";
	}
	
	private static String getProgress(Torrent t) {
		StatsInfo si = t.getInformationManager().getStatsInfo();
		double progress = (100.0 * si.getNumPiecesDownloaded()) / si.getNumPieces();
		return String.format("%.2f%%",progress);
	}
	
	private static String getBytesLeft(Torrent t) {
		 long total = t.getInformationManager().getStatsInfo().getNumBytesLeft();
		 return humanBytes(total);
	}
	
	private static String getBytesDownloaded(Torrent t) {
		long total = t.getInformationManager().getStatsInfo().getNumBytesDownloaded();
		return humanBytes(total);
	}
	
	private static String getPiecesLeft(Torrent t) {
		return t.getInformationManager().getStatsInfo().getNumPiecesLeft() + "";
	}
	
	private static String getPeers(Torrent t) {
		int known = t.getInformationManager().getAnnounceInfo().getPeerCount();
		int connected = t.getInformationManager().getStatsInfo().getNumPeers();
		return "(" + connected + ") " + known;
	}
	
	private static String humanBytes(long bytes) {
		String[] suffix = {"B","KB","MB","GB"};
		int pos = 0;
		while(bytes > 1024) {
			bytes /= 1024;
			pos ++;
		}
		return bytes + suffix[pos];
	}
}
