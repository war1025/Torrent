package tcl.tm.torrent.communication.registry.impl;

import tcl.tm.torrent.communication.registry.PieceRegistry;
import tcl.tm.torrent.communication.registry.PeerRegistry;

import tcl.tm.torrent.communication.util.Piece;

import tcl.tm.torrent.Torrent;

import tcl.tm.torrent.info.TorrentInfo;

import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.file.FileAccessFuture;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The PieceRegistry tracks the pieces needed for the current torrent.
 * It assigns pieces to peers for download and also handles saving
 * completed pieces to file.
 **/
public class PieceRegistryImpl implements PieceRegistry {

	private PriorityBlockingQueue<PieceRequest> pieceQueue;

	private BlockingQueue<Piece> piecePool;

	private boolean running;
	private Object runLock;

	private Object lock;

	private boolean[] inProgress;
	private int[] multiBitField;

	private boolean endgame;

	private FileAccessManager fam;
	private TorrentInfo ti;
	private PeerRegistry peerRegistry;

	public PieceRegistryImpl(Torrent torrent, PeerRegistry peerRegistry, Piece template, int poolSize) {
		this.piecePool = new LinkedBlockingQueue<Piece>();
		try {
			for(int i = 0; i < poolSize; i++) {
				System.out.println("Added new Piece to pool");
				piecePool.put(template.newInstance());
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}

		this.pieceQueue = new PriorityBlockingQueue<PieceRequest>();
		this.fam = torrent.getFileAccessManager();
		this.ti = torrent.getInformationManager().getTorrentInfo();
		this.peerRegistry = peerRegistry;

		this.inProgress = new boolean[torrent.getInformationManager().getTorrentInfo().getPieceCount()];
		this.multiBitField = new int[inProgress.length];
		getPreviousState();

		this.endgame = false;

		this.running = true;
		this.runLock = new Object();

		this.lock = new Object();

		new Thread(new PieceRegistrar(),"PieceRegistry : " + torrent.getInformationManager().getTorrentInfo().getTorrentName()).start();

	}

	private class PieceRegistrar implements Runnable {

		public void run() {
			while(isRunning()) {
				PieceRequest request = null;
				try {
					request = pieceQueue.take();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				if(request.getBitfield() != null) {
					Piece piece = assignPiece(request.getBitfield());

					request.setPiece(piece);
				}

				request.validate();
			}
			while(!pieceQueue.isEmpty()) {
				PieceRequest request = pieceQueue.poll();
				request.setPiece(null);
				request.validate();
			}
		}
	}

	private class PieceRequest implements Comparable<PieceRequest> {

		private Piece piece;

		private int piecesCompleted;
		private boolean[] bitfield;

		private boolean valid;
		private Object validLock;

		public PieceRequest(boolean[] bitfield, int piecesCompleted) {
			this.bitfield = bitfield;
			this.piecesCompleted = piecesCompleted;
			this.validLock = new Object();
			this.valid = false;
		}

		public void setPiece(Piece piece) {
			this.piece = piece;
		}

		public boolean[] getBitfield() {
			return bitfield;
		}

		public Piece getPiece() {
			synchronized(validLock) {
				while(!valid) {
					try {
						validLock.wait();
					} catch(InterruptedException e) {}
				}
			}
			return piece;
		}

		public void validate() {
			synchronized(validLock) {
				valid = true;
				validLock.notifyAll();
			}
		}

		public int compareTo(PieceRequest p) {
			return (this.piecesCompleted > p.piecesCompleted) ? -1 :
					(this.piecesCompleted < p.piecesCompleted) ? 1 :
					0;
		}
	}

	private boolean isRunning() {
		synchronized(runLock) {
			return running;
		}
	}

	/**
	 * Closes this PieceRegistry
	 **/
	public void close() {
		synchronized(runLock) {
			running = false;
		}
		pieceQueue.put(new PieceRequest(null,0));
	}

	public Piece requestPiece(boolean[] bitfield, int piecesCompleted) {
		if(isRunning()) {
			PieceRequest p = new PieceRequest(bitfield, piecesCompleted);
			pieceQueue.put(p);
			return p.getPiece();
		} else {
			return null;
		}
	}

	private Piece assignPiece(boolean[] bitfield) {
		int lowestId = -1;
		synchronized(lock) {
			int lowestFrequency = Integer.MAX_VALUE;

			for(int i = 0; i < bitfield.length; i++) {
				if(bitfield[i] && (!inProgress[i]) && (multiBitField[i] > 0) && (multiBitField[i] < lowestFrequency)) {
					lowestId = i;
					lowestFrequency = multiBitField[i];
				}
			}

			if(lowestId >= 0) {
				inProgress[lowestId] = true;
			} else if(!endgame) {
				checkEndgame();
			} else {
				lowestId = attemptEndgame(bitfield);
			}
		}
		Piece ret = null;
		try {
			ret = (lowestId >= 0) ? piecePool.take().reset(lowestId) : null;
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public boolean returnPiece(Piece p) {
		boolean success = false;
		synchronized(lock) {
			if(p.isComplete()) {
				success = fam.savePiece(p.getPieceId(),p.getData()).getSuccess();
				System.out.println("Verifying Returned Piece: " + p.getPieceId() + " " + success);
				if(success) {
					peerRegistry.notifyHave(p.getPieceId());
				}
			}
			inProgress[p.getPieceId()] = false;
		}
		try {
			piecePool.put(p);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		return success;
	}

	public void peerHave(int pieceId) {
		synchronized(lock) {
			if(multiBitField[pieceId] >= 0) {
				multiBitField[pieceId] += 1;
			}
		}
	}

	public void peerBitfield(boolean[] bitfield) {
		synchronized(lock) {
			System.out.println("Piece Registry Updating Bitfield");
			for(int i = 0; i < bitfield.length; i++) {
				if(bitfield[i] && (multiBitField[i] >= 0)) {
					multiBitField[i] += 1;
				}
			}
		}
	}

	public boolean peerInteresting(boolean[] bitfield) {
		boolean interesting = false;
		synchronized(lock) {
			for(int i = 0; i < bitfield.length; i++) {
				if(bitfield[i] && !(inProgress[i]) && multiBitField[i] > 0) {
					interesting = true;
					break;
				}
			}
		}
		return interesting;
	}

	public void removeBitfield(boolean[] bitfield) {
		synchronized(lock) {
			for(int i = 0; i < bitfield.length; i++) {
				if(bitfield[i] && (multiBitField[i] > 0)) {
					multiBitField[i] -= 1;
				}
			}
		}
	}

	/**
	 * Contacts the FileAccessManager to establish which pieces have been completed previously.
	 **/
	private void getPreviousState() {
		byte[] state = fam.getBitfield().getData();
		for(int i = 0; i < state.length; i++) {
			for(int j = 0; j < 8; j++) {
				if((((state[i] >> (7-j)) & 1) == 1) && inProgress.length > (i*8 + j)) {
					System.out.println("Found Previously completed piece: " + (i*8+j));
					multiBitField[i*8 + j] = -1;
				}
			}
		}
	}

	/**
	 * Checks whether "Endgame" has occured.
	 * This means that all pieces in the Torrent are either completed
	 * or in process of downloading.
	 * Connection speeds at this time can drop drastically as the remaining pieces
	 * are usually assigned to unresponsive peers. During endgame, these pieces are assigned
	 * to multiple peers.
	 **/
	private void checkEndgame() {
		System.out.println("Checking Endgame");
		boolean end = true;
		for(int i = 0; i < multiBitField.length; i++) {
			if(multiBitField[i] >= 0 && !inProgress[i]) {
				end = false;
				break;
			}
		}
		endgame = end;
	}

	/**
	 * Attempts to assign an endgame piece given a Peer's bitfield.
	 * This is only done in the case that we are in "endgame" and no other pieces
	 * are available for download.
	 *
	 * @param bitfield The calling Peer's bitfield.
	 *
	 * @return An endgame Piece to assign to the calling Peer.
	 **/
	private int attemptEndgame(boolean[] bitfield) {
		int lowestId = -1;
		int lowestFrequency = Integer.MAX_VALUE;
		System.out.println("Attempting Endgame");
		for(int i = 0; i < bitfield.length; i++) {
			if(bitfield[i] &&  (multiBitField[i] > 0) && (multiBitField[i] < lowestFrequency)) {
				lowestId = i;
				lowestFrequency = multiBitField[i];
			}
		}
		return lowestId;
	}

}


