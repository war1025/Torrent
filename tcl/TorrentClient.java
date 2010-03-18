package tcl;

import tcl.tm.TorrentManager;
import tcl.tm.impl.TorrentManagerImpl;

import tcl.uim.UIManager;
import tcl.uim.impl.UIManagerImpl;


/**
 * This is the main method, all it does is start (create the methods that need to be running for 
 * the backend to work.  This is a fairly simple class, but makes everything work.
 * 
 * @author Wayne Rowcliffe
 *
 **/
public class TorrentClient {
	//main method
	public static void main(String[] args) {
		
		//start the TorrentManager
		TorrentManager t = new TorrentManagerImpl(System.getProperty("user.home") + "/Desktop/");
		
		//Start the UI Manager
		UIManager u = new UIManagerImpl(t);
		
		//Start the UIManager Thread
		new Thread(u,"UI Manager").start();
	}
	
}
