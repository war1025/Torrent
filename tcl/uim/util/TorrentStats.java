package tcl.uim.util;

import java.beans.ConstructorProperties;

public class TorrentStats {
	
	private final String name;
	private final String eta;
	private final String speed;
	private final String progress;
	private final String bytesLeft;
	private final String bytesDownloaded;
	private final String piecesLeft;
	private final String peers;
	private final String filepath;
	
	@ConstructorProperties({"name", "eta", "speed", "progress", "bytesLeft", "bytesDownloaded", "piecesLeft", "peers", "filepath"})
	public TorrentStats(String n, String e, String s, String p, String bl, String bd, String pl, String prs, String fp) {
		this.name = n;
		this.eta = e;
		this.speed = s;
		this.progress = p;
		this.bytesLeft = bl;
		this.bytesDownloaded = bd;
		this.piecesLeft = pl;
		this.peers = prs;
		this.filepath = fp;
	}
	
	public String getName() {
		return name;
	}
	
	public String getEta() {
		return eta;
	}
	
	public String getSpeed() {
		return speed;
	}
	
	public String getProgress() {
		return progress;
	}
	
	public String getBytesLeft() {
		return bytesLeft;
	}
	
	public String getBytesDownloaded() {
		return bytesDownloaded;
	}
	
	public String getPiecesLeft() {
		return piecesLeft;
	}
	
	public String getPeers() {
		return peers;
	}
	
	public String getFilepath() {
		return filepath;
	}
}
