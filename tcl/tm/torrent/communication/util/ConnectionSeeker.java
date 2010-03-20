package tcl.tm.torrent.communication.util;

import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tcl.tm.torrent.Torrent;
import tcl.tm.torrent.info.util.Bencode;

/**
 * A ConnectionSeeker actively seeks out connections to peers for a given Torrent
 * every 10 minutes for the life of the Torrent object. It does this by contacting IP
 * addresses provided by the Tracker via AnnounceInfo.
 *
 * @author Wayne Rowcliffe
 **/
public class ConnectionSeeker implements Runnable, Closeable {

	private Torrent torrent;
	private boolean running;
	private Object waitLock;

	// This is the standard form of a BitTorrent handshake.
	private static final byte[] HANDSHAKE_TEMPLATE = {
		19, 66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114, 111, 116, 111, 99, 111, 108,
		0, 0, 0, 0, 0, 0, 0, 4,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		45, 65, 90, 50, 48, 54, 48, 45, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50};

	/**
	 * Creates a ConnectionSeeker for the given Torrent
	 *
	 * @param torrent The Torrent for which to seek connections.
	 **/
	public ConnectionSeeker(Torrent torrent) {
		this.torrent = torrent;
		this.running = true;
		this.waitLock = new Object();
	}

	/**
	 * A synchronized method to help multiple threads know whether or not
	 * connections should still be sought.
	 *
	 * @return Whether this ConnectionSeeker is still running.
	 **/
	private boolean isRunning() {
		synchronized(waitLock) {
			return running;
		}
	}

	private void timedWait(long interval) {
		try {
			synchronized(waitLock) {
				if(running) {
					waitLock.wait(interval);
				}
			}
		} catch(InterruptedException e) {}
	}

	/**
	 * Stops this ConnectionSeeker, meaning it will no longer seek connections.
	 * This causes any active threads for connection seeking to die peacefully.
	 **/
	public void close() {
		synchronized(waitLock) {
			running = false;
			waitLock.notifyAll();
		}
	}

	/**
	 * Every ten minutes the ConnectionSeeker gets a list of IP addresses, contacts them,
	 * and attempts to connect to each on a separate ConnectionHandler thread.
	 **/
	public void run() {
		while(isRunning()) {
			if(torrent.getInformationManager().getAnnounceInfo().hasStarted()) {
				for(String ip : torrent.getInformationManager().getAnnounceInfo().getPeers()) {
					int colon = ip.indexOf(":");
					String host = ip.substring(0,colon);
					int port = Integer.parseInt(ip.substring(colon+1,ip.length()));
					new Thread(new ConnectionHandler(host,port),"Connection Handler").start();

				}
				timedWait(450000);
			} else {
				timedWait(30000);
			}
		}
	}

	/**
	 * A ConnectionHandler deals with one specific peer.
	 * It will connect to that peer, initiate the handshake, and attempt
	 * to add the Peer to the Torrent it is seeking connections for.
	 **/
	private class ConnectionHandler implements Runnable {

		private String host;
		private int port;
		private Socket socket;
		private InputStream in;
		private OutputStream out;
		private byte[] reserved;
		/**
		 * Creates a ConnectionHandler to contact the given host and port
		 * and attempt to add it as a Peer to the relevant Torrent
		 *
		 * @param host The host's IP address
		 * @param port The port to contact the host on
		 **/
		private ConnectionHandler(String host, int port) {
			this.host = host;
			this.port = port;
			this.reserved = new byte[8];
		}

		/**
		 * Initiates the connection, and attempts to perform the handshake
		 * and add the Peer to the relevant Torrent
		 **/
		public void run() {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(host,port),10000);
				socket.setSoTimeout(18000);
				in = socket.getInputStream();
				out = socket.getOutputStream();
			} catch(IOException e) {
				try {
					socket.close();
				} catch(IOException io) {
				}
				return;
			}
			boolean correct = handShake();
			if(correct) {
				torrent.addPeer(socket, reserved);
				//System.out.println("Adding Peer: " + hash);
			} else {
				try{
					socket.close();
				} catch(IOException e) {
				}
			}
		}

		/**
		 * Constructs and sends the handshake for the relevant Torrent.
		 *
		 * @return The infohash for the torrent indicated in the handshake.
		 **/
		private boolean handShake() {
			try {
				byte[] handShake = HANDSHAKE_TEMPLATE.clone();
				byte[] hash = torrent.getInformationManager().getTorrentInfo().getInfoHash();
				System.arraycopy(hash,0,handShake,28,20);
				out.write(handShake);
				byte[] peerHandShake = new byte[68];
				int toRead = 68;
				while(toRead > 0) {
					int read = in.read(peerHandShake,68-toRead,toRead);
					if(read <= 0) {
						return false;
					}
					toRead -= read;
				}
				for(int i = 0; i < 20; i++) {
					if(handShake[i] != peerHandShake[i]) {
						return false;
					}
				}
				for(int i = 28; i < 48; i++) {
					if(handShake[i] != peerHandShake[i]) {
						return false;
					}
				}
				System.arraycopy(peerHandShake,20,reserved,0,8);
				return true;
			} catch(IOException e) {
			}
			return false;
		}

	}
}
