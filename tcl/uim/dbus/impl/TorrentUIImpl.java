package tcl.uim.dbus.impl;

import tcl.uim.UIManager;
import tcl.uim.dbus.TorrentUI;

import java.util.List;
import java.util.Map;

import java.io.IOException;

/**
 * This is the implementation of the TorrentUI, please see the interface to see what each 
 * method is supposed to do, also does most of the stuff that the UIManager does, 
 * pretty easy to follow.
 * 
 * 
 * @author Wayne Rowcliffe
 *
 */
public class TorrentUIImpl implements TorrentUI {
	
	UIManager uim;
	public TorrentUIImpl() {}
	
	
	//Need to have this here and not in the constructor because of bean stuff.
	public void setUIManager(UIManager uim) {
		this.uim = uim;
	}
	
	public String[] GetTorrentsAvailable() {
		return uim.getTorrentsAvailable();
	}

	public boolean IsComplete(String filePath) {
		return uim.isComplete(filePath);
	}

	public boolean Relocate(String filePath, String newFilePath) {
		return uim.relocate(filePath, newFilePath);
	}

	public boolean Remove(String filePath) {
		return uim.remove(filePath);
	}

	public boolean Start(String filePath) {
		return uim.start(filePath);
	}

	public boolean Stop(String filePath) {
		return uim.stop(filePath);
	}

	public List<Map<String,String>> GetTorrentStats() {
		return uim.getTorrentStats(0);
	}
	
	public void ShutDown() {
		try {
			uim.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRemote() {
		return false;
	}
	
}
