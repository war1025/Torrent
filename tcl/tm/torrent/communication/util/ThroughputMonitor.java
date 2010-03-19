package tcl.tm.torrent.communication.util;



public class ThroughputMonitor {

	private Object runLock;
	private int count;
	private boolean running;
	private int[] vals;
	private int curVal;

	public ThroughputMonitor() {
		runLock = new Object();
		count = 0;
		running = true;
		vals = new int[5];
		curVal = 0;
	}

	public void start() {
		running = true;
		new Thread(new Updater(), "Throughput Monitor").start();
	}

	public void close() {
		synchronized(runLock) {
			running = false;
			runLock.notifyAll();
		}
	}

	public void dataReceived(int length) {
		synchronized(runLock) {
			count += length;
		}
	}

	public int getSpeed() {
		synchronized(runLock) {
			int speed = 0;
			for(int i : vals) {
				speed += i;
			}
			return speed / vals.length;
		}
	}

	private class Updater implements Runnable {

		public void run() {
			synchronized(runLock) {
				while(running) {
					try {
						runLock.wait(1000);
					} catch(InterruptedException e) {
					}
					vals[curVal] = count;
					count = 0;
					curVal++;
					if(curVal == 5) {
						curVal = 0;
					}
				}
			}
		}
	}
}

