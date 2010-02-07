package tcl.tm.torrent.info.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

import tcl.tm.torrent.info.AnnounceInfo;
import tcl.tm.torrent.info.InformationManager;
import tcl.tm.torrent.info.util.Bencode;

/**
 * This is an implementation of AnnounceInfo. You must supply the constructor with some default information first,
 * then you can change it on subsequent calls.  This class will obey the specification and not communicate with the
 * tracker unless the specified time that the tracker has specified has elapsed.
 *
 * See the interfaces for more information about the methods in here.
 *
 * @author Wayne Rowcliffe
 *
 **/
public class AnnounceInfoImpl implements AnnounceInfo {
	private String peerId;

	private String eventState;

	private int portNumber;
	private int numPeersWanted;

	private InformationManager im;

	private boolean running;
	private boolean started;

	private Object waitLock;

	private Set<String> peers;

	public AnnounceInfoImpl(InformationManager im) {
		this.waitLock = new Object();
		this.numPeersWanted = 50;

		Random rand = new Random();
		String one = rand.nextInt(1000000) + "";
		String two = rand.nextInt(1000000) + "";
		while(one.length() < 6) {
			one += "0";
		}
		while(two.length() < 6) {
			two += "0";
		}
		this.peerId = "-AZ2060-" + one + two;
		this.portNumber = 1630;
		this.eventState = "started";

		this.im = im;

		this.peers = new HashSet<String>();

	}

	public void startTracker() {
		if(!running) {
			this.running = true;
			this.started = false;
			for( String url : im.getTorrentInfo().getAnnounceList()) {
				new Thread(new TrackerHound(url),"Tracker Hound: " + url + " " + im.getTorrentInfo().getTorrentName()).start();
			}
		}
	}

	public boolean hasStarted() {
		return started;
	}

	public int getLeecherCount() {
		int leechers = -1;
		//Object o = trackerResponse.get("incomplete");
		//if(o != null) {
		//	leechers = ((Long) trackerResponse.get("incomplete")).intValue();
		//}
		return leechers;
	}

	public int getSeederCount() {
		int seeders = -1;
		//Object o = trackerResponse.get("complete");
		//if(o != null) {
		//	seeders = ((Long) trackerResponse.get("complete")).intValue();
		//}
		return seeders;

	}

	public int getPeerCount() {
		return peers.size();
	}

	public String[] getPeers() {
		return peers.toArray(new String[0]);
	}

	public void setNumberPeersWanted(int peers) {
		numPeersWanted = peers;
	}

	public void setPeerId(String id) {
		peerId = id;
	}

	public void setPortNumber(int port) {
		portNumber = port;
	}

	public void close() throws IOException {
		synchronized(waitLock) {
			running = false;
			waitLock.notifyAll();
		}
	}

	private boolean isRunning() {
		synchronized(waitLock) {
			return running;
		}
	}

	private void timedWait(long millis) {
		try {
			synchronized(waitLock) {
				if(running) {
					waitLock.wait(millis);
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class TrackerHound implements Runnable {

		private String trackerURL;
		private String eventState;
		private long interval;
		private long lastConnectTime;
		private Map<String, Object> trackerResponse;

		private TrackerHound(String trackerURL) {
			this.trackerURL = trackerURL;
			this.eventState = "started";
			this.interval = 180 * 1000;
			this.lastConnectTime = 0;
			this.trackerResponse = new HashMap<String,Object>();
		}

		public void run() {
			while(isRunning()) {
				if(System.currentTimeMillis() - lastConnectTime > interval) {
					trackerConnect();
					if(started) {
						setCurrentState(null);
					}
					lastConnectTime = System.currentTimeMillis();
					if(trackerResponse.get("interval") != null) {
						interval = ((Long) trackerResponse.get("interval")) * 1000;
					}
				}
				if(im.getStatsInfo().getNumPiecesLeft() == 0) {
					setCurrentState("completed");
					break;
				}
				timedWait(interval);
			}
			// To Avoid null pointers...
			if(!"completed".equals(eventState)) {
				setCurrentState("stopped");
			}
			trackerConnect();
		}

		private void setCurrentState(String state) {
			eventState = state;
		}

		private void trackerConnect() {

			//First we need to piece together the String for the tracker
			StringBuilder s = new StringBuilder(trackerURL);

			//add hash
			s.append("?info_hash=" + im.getTorrentInfo().getEscapedInfoHash());
			//add id
			s.append("&peer_id=" + peerId);
			//add port
			s.append("&port=" + portNumber);
			//add uploaded
			s.append("&uploaded=" + im.getStatsInfo().getNumBytesUploaded());
			//add download
			s.append("&downloaded=" + im.getStatsInfo().getNumBytesDownloaded());
			//add bytes left
			s.append("&left=" + im.getStatsInfo().getNumBytesLeft());
			//specify compact or not
			//We will leave this at compact as we don't need the information that is provided otherwise, and we can't process it anyway
			s.append("&compact=" + "1");
			//add event(what we are doing)
			if(eventState != null) {
				s.append("&event=" + eventState);
			}
			//add the number of peers we want
			s.append("&numwant=" + numPeersWanted);

			try {
				URLConnection url = new URL(s.toString()).openConnection();
				url.setConnectTimeout(15000);
				url.setReadTimeout(15000);
				trackerResponse = Bencode.getTrackerInfo(url.getInputStream());
				if(trackerResponse.get("peers") != null) {
					for(String peerIp : ((String[]) trackerResponse.get("peers"))) {
						peers.add(peerIp);
					}
				}
				started = true;
			} catch (MalformedURLException e) {
				//throw new IllegalStateException("Tracker Connect Problem");
			} catch (IOException e) {
				//throw new IllegalStateException("Tracker Connect Problem");
			}
		}
	}

}
