package tcl.tm.connection;

import java.io.Closeable;

/**
 * The ConnectionManager is responsible for listening for Peer connections.
 * It is also responsible for performing the initial handshake between the peers,
 * dropping any connections that are not Peers looking for a Torrent that is currently being
 * served by this TorrentManager, and forwarding connections to the proper
 * Torrent object via the TorrentManager.
 * 
 * @author Wayne Rowcliffe
 **/
public interface ConnectionManager extends Closeable, Runnable {
	
	/**
	 * Nothing is required in here as all connection management will
	 * be handled in the run() method provided by Runnable.
	 * Likewise, close() for terminating this ConnectionManager
	 * is provided by the Closeable interface, how cool is that?
	 **/
	
}
	
