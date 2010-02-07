package tcl.tm.connection.impl;

import tcl.tm.connection.ConnectionManager;
import tcl.tm.torrent.info.util.Bencode;
import tcl.tm.TorrentManager;
import tcl.tm.torrent.Torrent;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * The ConnectionManager is responsible for listening for Peer connections.
 * It is also responsible for performing the initial handshake between the peers,
 * dropping any connections that are not Peers looking for a Torrent that is currently being
 * served by this TorrentManager, and forwarding connections to the proper
 * Torrent object via the TorrentManager.
 * 
 * @author Wayne Rowcliffe
 **/
public class ConnectionManagerImpl implements ConnectionManager {
	
	private TorrentManager tm;
	private int port;
	private boolean running;
	private ServerSocket socket;
	
	private static final byte[] HANDSHAKE_TEMPLATE = {
		19, 66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114, 111, 116, 111, 99, 111, 108,
		0, 0, 0, 0, 0, 0, 0, 4,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		45, 65, 90, 50, 48, 54, 48, 45, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50}; 
	
	/**
	 * Constructs a ConnectionManager for the given TorrentManager, which listens on the given port.
	 * This ConnectionManager will handle incoming connections and perform handshakes with new peers.
	 * It will then forward them to the TorrentManager which will give connections to the appropriate Torrent.
	 *
	 * @param tm The TorrentManager this ConnectionManager is managing connections for.
	 * @param port The port this ConnectionManager should listen on.
	 **/
	public ConnectionManagerImpl(TorrentManager tm, int port) {
		this.tm = tm;
		this.port = port;
		this.running = true;
		try {
			this.socket = new ServerSocket(port);
		} catch(IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
	
	/**
	 * A synchronized method to help multiple threads know whether or not
	 * connections should still be handled.
	 * 
	 * @return Whether this ConnectionManager is still running.
	 **/
	private synchronized boolean isRunning() {
		return running;
	}
	
	/**
	 * Listens for incoming connections, and creates ConnectionHandlers to handle them.
	 **/
	public void run() {
		while(isRunning()) {
			try {
				new Thread(new ConnectionHandler(socket.accept()),"Connection Handler").start();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops this ConnectionManager, meaning it will no longer listen for new connections.
	 **/
	public void close() {
		running = false;
		try {
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * A ConnectionHandler handles a connection with one Peer.
	 * It will attempt to handshake with the Peer and forward
	 * the Peer onto the TorrentManager to be passed to the proper Torrent.
	 **/
	private class ConnectionHandler implements Runnable {
		
		private Socket socket;
		private InputStream in;
		private OutputStream out;
		private byte[] reserved;
		
		/**
		 * Constructs a ConnectionHandler for the given socket
		 * 
		 * @param socket The socket which represents our connection to a potential peer.
		 **/
		private ConnectionHandler(Socket socket) {
			this.socket = socket;
			try{
				this.socket.setSoTimeout(18000);
				this.in = socket.getInputStream();
				this.out = socket.getOutputStream();
			} catch(IOException e) {
				e.printStackTrace();
			}
			reserved = new byte[8];
		}
		
		/**
		 * Initiates the connection, and attempts to perform the handshake 
		 * and add the Peer to the relevant Torrent
		 **/
		public void run() {
			String hash = handShake();
			Torrent t = tm.getTorrentByHash(hash);
			if(t != null) {
				t.addPeer(socket, reserved);
				System.out.println("Adding Peer: " + hash);
			} else {
				try{
					socket.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Constructs and sends the handshake for the relevant Torrent.
		 * 
		 * @return The infohash for the torrent indicated in the handshake.
		 **/
		private String handShake() {
			try {
				byte[] handShake = HANDSHAKE_TEMPLATE.clone();
				byte[] peerHandShake = new byte[68];
				int toRead = 68;
				while(toRead > 0) {
					int read = in.read(peerHandShake,68-toRead,toRead);
					if(read <= 0) {
						return "";
					}
					toRead -= read;
				}
				for(int i = 0; i < 20; i++) {
					if(handShake[i] != peerHandShake[i]) {
						return "";
					}
				}
				System.arraycopy(peerHandShake,20,reserved,0,8);
				byte[] hash = new byte[20];
				System.arraycopy(peerHandShake,28,hash,0,20);
				String escapedHash = Bencode.escapeBytes(hash);
				if(tm.getTorrentByHash(escapedHash) != null) {
					System.arraycopy(hash,0,handShake,28,20);
					out.write(handShake);
					return escapedHash;
				} 
			} catch(IOException e) {
				e.printStackTrace();
			}
			return "";
		}
			
	}
}
