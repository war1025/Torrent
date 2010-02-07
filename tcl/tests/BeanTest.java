package tcl.tests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;

import tcl.uim.impl.UIManagerBeanTest;
import tcl.uim.UIManager;
import tcl.uim.ui.TorrentUI;

import javax.management.AttributeChangeNotification;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Sole purpose of these tests is to verify that the TorrentUI bean is accessable from the JMX server,
 * and that methods called on the bean affect the UIManager.
 * 
 * Note that in order to run this the following System properties need to be set at runtime:
 * -Dcom.sun.management.jmxremote.port=9999 
 * -Dcom.sun.management.jmxremote.authenticate=false 
 * -Dcom.sun.management.jmxremote.ssl=false
 **/ 
public class BeanTest {

	private static TorrentUI tui;
	private static UIManagerBeanTest uim;
	
	@BeforeClass
	public static void setUp() throws Exception{
		uim = new UIManagerBeanTest(null);
		
		Thread.sleep(2000);
		
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
		JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
		ObjectName mxbeanName = new ObjectName("tcl.uim.ui:type=TorrentUI");
		tui = JMX.newMXBeanProxy(mbsc, mxbeanName, TorrentUI.class);
	}
	
	/**
	 * Method that will return the information that you request, all of this information is mapped
	 * Right now you will input the file name and the property you are requesting for that file
	 * Property Available
	 * speed, ETA, seeders, leechers, filename, fileSize, numPieces, numPiecesUploaded, 
	 * numPiecesDownloaded, numBytesUp, numBytesDown
	 * 
	 * In the case of filename and filesize, if there are more than one file, the information will
	 * be returned in more array
	 * 
	 * @param property one of the properties available from above
	 * @param filePath the filePath of the file you are requesting information about
	 * @return the string array of the information being returned
	 **/
	//public String[] get(String property, String filePath);
	
	@Test
	public void getA() {
		assertEquals(tui.get("A","B"),new String[] {"I like you","B"});
	}
	
	@Test
	public void getB() {
		assertEquals(tui.get("B","A"),new String[] {"You smell funny","A"});
	}
	
	@Test
	public void getC() {
		assertEquals(tui.get("C","B"),new String[] {"MonkeyBucket"});
	}
	
	/**
	 * This method will start the torrent downlaod, this will resume a download if it has 
	 * ready been downloaded before
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return ture if it has started downloading false otherwise
	 **/
	//public boolean start(String filePath);
	
	@Test
	public void startTrue() {
		assertTrue(tui.start("A"));
	}
	
	@Test
	public void startFalse() {
		assertFalse(tui.start("B"));
	}
	
	/**
	 * This method will stop the torrent downlaod.
	 * 
	 * @param filePath the path to the .torrent file that you will be downloading
	 * @return true if the file has stopped downloading, false otherwise
	 **/
	//public boolean stop(String filePath);
	
	@Test
	public void stopTrue() {
		assertTrue(tui.remove("A"));
	}
	
	@Test
	public void stopFalse() {
		assertFalse(tui.remove("B"));
	}
	
	/**
	 * This method will relocate the downloaded torrent file, after the relocate is complete
	 * the backend will lose track of it, and completly disregard it.
	 * 
	 * @param filePath the origional filepath
	 * @param newFilePath the new filepath where the file will be located
	 * 
	 * @return true if the relocate was sucessful, false if the file you are trying 
	 * to download cannot be relocated
	 **/
	//public boolean relocate(String filePath, String newFilePath);
	
	@Test
	public void relocateTrue() {
		assertTrue(tui.relocate("A","B"));
	}
	
	@Test
	public void relocateFalse() {
		assertFalse(tui.relocate("B","A"));
	}
	
	/**
	 * This method will remove a file from the queue/ stopdownloading and 
	 * delete, or delete if allready downloaded
	 * 
	 * @param filePath the filePath to the origional filepath in question
	 * 
	 * @return true if sucessful false otherwise
	 **/
	//public boolean remove(String filePath);
	
	@Test
	public void removeTrue() {
		assertTrue(tui.remove("A"));
	}
	
	@Test
	public void removeFalse() {
		assertFalse(tui.remove("B"));
	}

	/**
	 * This method will tell the user if the torrent is competly downloaded and ready 
	 * for upload.
	 * 
	 * @param filePath the filePath to the file is question
	 * 
	 * @return true if it is complete false otherwise
	 **/
	//public boolean isComplete(String filePath);
	
	@Test
	public void completeTrue() {
		assertTrue(tui.isComplete("A"));
	}
	
	@Test
	public void completeFalse() {
		assertFalse(tui.isComplete("B"));
	}

	/**
	 * This method will return a list of all torrents available for data lookup on the server
	 * 
	 * @return the string array of torrents available
	 **/
	//public String[] getTorrentsAvaliable();
	
	@Test
	public void torrentAvaliable() {
		assertEquals(tui.getTorrentsAvailable(),new String[] {"a","B","3","IV"});
	}
}	
		
