package tcl.tests;

import tcl.tm.torrent.communication.util.ThrottledOutputStream;
import tcl.tm.torrent.communication.util.ThrottledInputStream;

import java.io.PipedOutputStream;
import java.io.PipedInputStream;

public class ThrottleTest {
	
	public static void main(String[] args) throws Exception{
		
		PipedOutputStream p = new PipedOutputStream();	
		
		final ThrottledOutputStream out = new ThrottledOutputStream(p,16332);
		
		final ThrottledInputStream in = new ThrottledInputStream(new PipedInputStream(p),45000);
		
		final Thread t1 = new Thread(new OutChecker(out));
		t1.start();
		Thread t2 = new Thread(new InChecker(in));
		t2.start();
		
		final byte[] bytes = new byte[500000];
		
		new Thread() {
			
			public void run() {
				long time = System.currentTimeMillis();
				int sum2 = 0;
				System.out.println("Out Start");
				for(int i = 0; i < 500000; i++) {
					bytes[i] = (byte) i;
				}
				try{
					out.write(bytes);
				} catch(Exception e) {}
				System.out.println("Out Done: " + ((System.currentTimeMillis() - time) / 1000.0));
				try{ 
					out.close();
					t1.interrupt();
					System.out.println("Closing out");
				} catch(Exception e) {}
				System.out.println("Out Thread Dead");
				
			}
		}.start();
		
		byte[] bytes2 = new byte[500000];
		in.read(bytes2);
		
		
		
		System.out.println("In Done: ");
		
		in.close();
		System.out.println("Closing in");
		t2.interrupt();
		int count = 0;
		for(int i = 0; i < bytes.length; i++) {
			if(bytes[i] != bytes2[i] && (count < 10 || (bytes2[i] != 0 && count < 20))) {
				System.out.println(i + " " + bytes[i] + " " + bytes2[i]);
				count++;
			}
		}
		System.out.println("Main Dead");

	} 
	
	private static class OutChecker implements Runnable {
		
		private ThrottledOutputStream out;
		private boolean go;
		
		public OutChecker(ThrottledOutputStream out) {
			this.out = out;
			this.go = true;
		}
		
		public void run() {
			int total = 0;
			while(go) {
				try{
					Thread.sleep(1000);
					int last = out.getConnectionSpeed();
					total += last;
					System.out.println("Out: " + last + " " + total);
				} catch(InterruptedException e) {
					go = false;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Out Checker Dead");
		}
	}
	
	
	private static class InChecker implements Runnable {
	
		private ThrottledInputStream in;
		private boolean go;
		
		public InChecker(ThrottledInputStream in) {
			this.in = in;
			this.go = true;
		}
		
		public void run() {
			int total = 0;
			while(go) {
				try{
					Thread.sleep(1000);
					int last = in.getConnectionSpeed();
					total += last;
					System.out.println("In: " + last + " " + total);
				} catch(InterruptedException e) {
					go = false;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("In Checker Dead");
		}
	}
}
