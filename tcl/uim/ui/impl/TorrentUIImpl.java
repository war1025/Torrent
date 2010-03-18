package tcl.uim.ui.impl;

import tcl.uim.UIManager;
import tcl.uim.ui.TorrentUI;
import tcl.uim.util.TorrentStats;

import java.io.IOException;

/**
 * This is the implementation of the TorrentUI, please see the interface to see what each 
 * method is supposed to do, also does most of the stuff that the UIManager does, 
 * pretty easy to follow.
 * 
 * 
 * @author Anthony Milosch
 *
 */
public class TorrentUIImpl implements TorrentUI {
	
	UIManager uim;
	public TorrentUIImpl() {}
	
	
	//Need to have this here and not in the constructor because of bean stuff.
	public void setUIManager(UIManager uim) {
		this.uim = uim;
	}
	public String[] get(String property, String filePath) {
		return uim.get(property, filePath);
	}

	public String[] getTorrentsAvailable() {
		return uim.getTorrentsAvailable();
	}

	public boolean isComplete(String filePath) {
		return uim.isComplete(filePath);
	}

	public boolean relocate(String filePath, String newFilePath) {
		return uim.relocate(filePath, newFilePath);
	}

	public boolean remove(String filePath) {
		return uim.remove(filePath);
	}

	public boolean start(String filePath) {
		return uim.start(filePath);
	}

	public boolean stop(String filePath) {
		return uim.stop(filePath);
	}

	public String[] getPropertiesAvailable() {
		return uim.getPropertiesAvailable();
	}

	public String[][] getAll() {
		return uim.getAll();
	}
	
	public TorrentStats[] getTorrentStats() {
		return uim.getTorrentStats();
	}
	
	public void shutDown() {
		try {
			uim.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
