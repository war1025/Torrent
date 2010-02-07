package tcl.tests;

import tcl.tm.torrent.file.impl.FileAccessManagerImpl;
import tcl.tm.torrent.file.util.impl.StatusLoaderImpl;
import tcl.tm.torrent.file.FileAccessFuture;
import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.info.impl.TorrentInfoImpl;
import tcl.tm.torrent.info.TorrentInfo;

public class ManagerTest {
	
	private static int count = 0;
	private static Object lock = new Object();

	public static void main(String[] args) {

		TorrentInfo t = new TorrentInfoImpl(args[0]);

		FileAccessManagerImpl f = new FileAccessManagerImpl(t,new StatusLoaderImpl(t,"/home/war1025/Desktop/"),"/home/war1025/Desktop/");
		FileAccessManagerImpl f2 = new FileAccessManagerImpl(t,new StatusLoaderImpl(t,"/home/war1025/"),"/home/war1025/");

		Thread t1 = new Thread(f,"Secretary");
		Thread t2 = new Thread(f2,"Secretary2");

		t1.start();
		t2.start();

		int pieces = t.getPieceCount();
		for(int i = 0; i < pieces; i++ ) {
			new Thread(new Requester(i,f,f2)).start();
		}
		
		while(count < pieces) {
			try{
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		
		f.close();
		f2.close();
		
	}

	private static class Requester implements Runnable {
		
		FileAccessManager fam;
		FileAccessManager fam2;
		int id;

		public Requester(int id, FileAccessManager fam, FileAccessManager fam2) {
			this.fam = fam;
			this.fam2 = fam2;
			this.id = id;
		}

		public void run() {
			FileAccessFuture f = fam.getPiece(id);
			System.out.println("Retrieved: " + f.getSuccess() + " " + f.getPieceId());
			f = fam2.savePiece(id,f.getData());
			System.out.println("Saved: " + f.getSuccess() + " " + f.getPieceId());
			synchronized(lock) {
				count++;
			}
		}
	}

}
