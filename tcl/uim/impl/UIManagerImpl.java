package tcl.uim.impl;

import tcl.tm.TorrentManager;
import tcl.uim.UIManager;
import tcl.uim.dbus.TorrentUI;
import tcl.uim.dbus.impl.TorrentUIImpl;

import tcl.uim.util.StatCreator;

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import java.io.IOException;

/**
 * This class implemets the UIManagers
 * 
 * Please see the interface for more information about it, it should all be there.
 * 
 * @author Wayne Rowcliffe
 *
 */
public class UIManagerImpl implements UIManager 
{
	
	private TorrentManager tm;
	private TorrentUI tui;
	private boolean running;
	private Object lock;
	private DBusConnection dbc;
	
	public UIManagerImpl(TorrentManager tm) {
		this.tm = tm;
		TorrentUIImpl tuii = new TorrentUIImpl();
		tuii.setUIManager(this);
		tui = tuii;
	
		try {
			dbc = DBusConnection.getConnection(DBusConnection.SESSION);
			dbc.requestBusName("tcl.uim.dbus.TorrentUI");
			dbc.exportObject("/tcl/uim/dbus/TorrentUI", tui);
		} catch(DBusException e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		}

		
		running = true;
		lock = new Object();
	}
	
	public synchronized boolean isRunning() {
		return running;
	}
	
	public void run()
	{
		while(isRunning()) {
			synchronized(lock) {
				try {
					lock.wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		synchronized(lock) {
			try {
				lock.wait(1000);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		dbc.disconnect();
	}
	
	public String[] getTorrentsAvailable() {
		return tm.getTorrentsAvailable();
	}

	public boolean isComplete(String filePath) {
		if(!this.filePathCheck(filePath)) {
			return false;
		}
		return tm.isComplete(filePath);
	}

	public boolean relocate(String filePath, String newFilePath) {
		System.out.println("Moving: " + filePath + " To: " + newFilePath);
		if(!this.filePathCheck(filePath)) {
			return false;
		}
		return tm.relocate(filePath,newFilePath);
	}

	public boolean remove(String filePath) {
		if(!this.filePathCheck(filePath)) {
			return false;
		}
		return tm.remove(filePath);
	}

	public boolean start(String filePath) {
		return tm.start(filePath);
	}

	public boolean stop(String filePath) {
		if(!this.filePathCheck(filePath)) {
			return false;
		}
		return tm.stop(filePath);
	}

	private boolean filePathCheck(String filePath) {
		for(int i = 0; i < this.getTorrentsAvailable().length; i ++)
		{
			if(filePath.equals(this.getTorrentsAvailable()[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	public List<Map<String,String>> getTorrentStats() {
		return StatCreator.createDBusStats(tm);
	}
	
	public void close() throws IOException {
		running = false;
		synchronized(lock) {
			lock.notifyAll();
		}
		tm.close();
	}
}
