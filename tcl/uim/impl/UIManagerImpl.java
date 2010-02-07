package tcl.uim.impl;

import tcl.tm.TorrentManager;
import tcl.uim.UIManager;
import tcl.uim.dbus.TorrentUI;
import tcl.uim.dbus.impl.TorrentUIImpl;

import tcl.uim.util.TorrentStats;
import tcl.uim.util.StatCreator;

import java.util.List;
import java.util.Map;

/*import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import java.lang.management.ManagementFactory;
*/

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import java.io.IOException;

/**
 * This class implemets the UIManagers
 * 
 * Please see the interface for more information about it, it should all be there.
 * 
 * @author Anthony Milosch
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
		/*MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		try {
			// Construct the ObjectName for the TorrentUI MBean we will register
			ObjectName mbeanName = new ObjectName("tcl.uim.ui:type=TorrentUI");

			// Register the TorrentUI MXBean
    	    mbs.registerMBean(tui, mbeanName);
		} catch(MalformedObjectNameException e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		} catch(InstanceAlreadyExistsException e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		} catch(MBeanRegistrationException e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		} catch(NotCompliantMBeanException e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		}
		*/ 
		
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
	
	/**
	 * This is the big get method, please see the information from the interface
	 * so you know how it works.
	 */
	public String[] get(String property, String filePath) 
	{
		if(!this.filePathCheck(filePath))
		{
			return new String[] {"Error: The Torrent Client is not keeping track of (" + filePath + ")"};
		}
		
		if(property.equals("numSeeders")) 
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumSeeders()).toString();
			return new String[] {item, filePath};
		} 
		else if(property.equals("numLeachers")) 
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumLeechers()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("fileSize"))
		{
			String item = new Long(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getTorrentSize()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numPieces"))
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumPieces()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numPiecesUploaded"))
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumPiecesUploaded()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numPiecesDownloaded"))
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumPiecesDownloaded()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numBytesUploaded"))
		{
			String item = new Long(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumBytesUploaded()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numBytesDownloaded"))
		{
			String item = new Long(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumBytesDownloaded()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numBytesLeft"))
		{
			String item = new Long(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumBytesLeft()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numPiecesLeft"))
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumPiecesLeft()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("ETA"))
		{
			String item = new Long(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getETA()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("numPeers"))
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getNumPeers()).toString();
			return new String[] {item, filePath};
		}
		else if(property.equals("name"))
		{
			String item = tm.getTorrent(filePath).getInformationManager().getTorrentInfo().getTorrentName();
			return new String[] {item, filePath};
		}
		else if(property.equals("speed"))
		{
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getStatsInfo().getSpeed()).toString();
			if(item == "-1")
			{
				item = "Infinite";
			}
			return new String[] {item, filePath};
		} else if(property.equals("peersAvailable")) {
			String item = new Integer(tm.getTorrent(filePath).getInformationManager().getAnnounceInfo().getPeerCount()).toString();
			return new String[] {item, filePath};
		}
		return new String[] {"Oh No! You used a Property that doesn't Exist!, Please try again"};
	}

	public String[] getPropertiesAvailable() 
	{
		return new String[] {"numSeeders", "numLeachers", "fileSize", "numPieces", "numPiecesUploaded", "numPiecesDownloaded", 
			"numPiecesLeft", "numBytesUploaded", "numBytesDownloaded", "numBytesLeft", "ETA", "numPeers", "speed", "name"};
	}
	public String[] getTorrentsAvailable() 
	{
		return tm.getTorrentsAvailable();
	}

	public boolean isComplete(String filePath) 
	{
		if(!this.filePathCheck(filePath))
		{
			return false;
		}
		return tm.isComplete(filePath);
	}

	public boolean relocate(String filePath, String newFilePath)
	{
		System.out.println("Moving: " + filePath + " To: " + newFilePath);
		if(!this.filePathCheck(filePath))
		{
			return false;
		}
		return tm.relocate(filePath,newFilePath);
	}

	public boolean remove(String filePath) 
	{
		if(!this.filePathCheck(filePath))
		{
			return false;
		}
		return tm.remove(filePath);
	}

	public boolean start(String filePath)
	{
		return tm.start(filePath);
	}

	public boolean stop(String filePath) 
	{
		if(!this.filePathCheck(filePath))
		{
			return false;
		}
		return tm.stop(filePath);
	}

	public String[][] getAll() 
	{		
		String[][] retVal = new String[this.getTorrentsAvailable().length][this.getPropertiesAvailable().length+1];
		String[] torrents = this.getTorrentsAvailable();
		for(int i = 0; i < torrents.length; i ++)
		{
			retVal[i][0] = torrents[i];
			
			for(int j = 0; j < this.getPropertiesAvailable().length; j ++)
			{
				retVal[i][j+1] = this.get(this.getPropertiesAvailable()[j], retVal[i][0])[0];
			}
		}
		
		return retVal;
	}
	
	private boolean filePathCheck(String filePath)
	{
		for(int i = 0; i < this.getTorrentsAvailable().length; i ++)
		{
			if(filePath.equals(this.getTorrentsAvailable()[i]));
			{
				return true;
			}
		}
		return false;
	}
	
	public TorrentStats[] getTorrentStats() {
		return StatCreator.createStats(tm);
	}
	
	public List<Map<String,String>> getTorrentStats(int i) {
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
