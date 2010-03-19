package tcl.tm.torrent.communication.registry.impl;

import tcl.tm.torrent.communication.registry.PieceRegistry;
import tcl.tm.torrent.communication.registry.PieceRequestFuture;

import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.file.FileAccessFuture;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PieceRegistryImpl implements PieceRegistry {

	private PriorityBlockingQueue<PieceRequest> pieceQueue;

	private BlockingQueue<Piece> piecePool;

	private boolean running;

	public PieceRegistryImpl(Piece template, int poolSize) {
		piecePool = new LinkedBlockingQueue<Piece>();
		for(int i = 0; i < poolSize; i++) {
			piecePool.push(template.newInstance());
		}

		pieceQueue = new PriorityBlockingQueue<PieceRequest>();

		running = false;
	}

	private class PieceRegistrar implements Runnable {

		public void run() {
			while(running) {
				PieceRequest request = pieceQueue.take();

				assignPiece(request.getBitfield());

				request.setPiece(piece);

				request.validate();
			}
			while(!pieceQueue.isEmpty()) {
				PieceRequest request = pieceQueue.poll();
				request.setPiece(null);
				request.validate();
			}
		}
	}

	public void start() {

	}

	public void close() {

	}

	public Piece requestPiece(boolean[] bitfield, int piecesCompleted) {
		if(running) {
			PieceRequest p = new PieceRequest(bitfield, piecesCompleted);
			pieceQueue.put(p);
			return p.getPiece();
		} else {
			return null;
		}
	}

	private Piece assignPiece(boolean[] bitfield) {
		Piece assigned = null;
		synchronized(lock) {
			int lowestId = -1;
			int lowestFrequency = Integer.MAX_VALUE;
			boolean foundAbandoned = false;
			for(int i : abandonedPieces.keySet()) {
				if(bitfield[i]) {
					lowestId = i;
					assigned = abandonedPieces.remove(lowestId);
					foundAbandoned = true;
					break;
				}
			}
			if(!foundAbandoned) {
				for(int i = 0; i < bitfield.length; i++) {
					if(bitfield[i] && (!inProgress[i]) && (multiBitField[i] > 0) && (multiBitField[i] < lowestFrequency)) {
						lowestId = i;
						lowestFrequency = multiBitField[i];
					}
				}
			}
			System.out.println("Abandoned Pieces Size : " + abandonedPieces.size());
			if(lowestId >= 0 && assigned == null) {
				int length = 0;
				if(lowestId == bitfield.length -1) {
					length = torrent.getInformationManager().getTorrentInfo().getFinalPieceLength();
				} else {
					length = torrent.getInformationManager().getTorrentInfo().getPieceLength();
				}
				assigned = new Piece(lowestId,16 * 1024,length, speed);
			}
			if(lowestId >= 0) {
				inProgress[lowestId] = true;
			} else if(!endgame) {
				checkEndgame();
			} else {
				assigned = attemptEndgame(bitfield);
			}
		}
		return assigned;
	}

	public boolean returnPiece(Piece p) {
		boolean success = false;
		synchronized(lock) {
			if(p.isComplete()) {
				success = torrent.getFileAccessManager().savePiece(p.getPieceId(),p.getData()).getSuccess();
				System.out.println("Verifying Returned Piece: " + p.getPieceId() + " " + success);
				if(success) {
					multiBitField[p.getPieceId()] = -1;
					for(Peer peer : peers.values()) {
						peer.issueHave(p.getPieceId());
					}
				}
			} else {
				abandonedPieces.put(p.getPieceId(),p);
			}
			inProgress[p.getPieceId()] = false;
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
		byte[] state = torrent.getFileAccessManager().getBitfield().getData();
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
	private Piece attemptEndgame(boolean[] bitfield) {
		int lowestId = -1;
		int lowestFrequency = Integer.MAX_VALUE;
		System.out.println("Attempting Endgame");
		for(int i = 0; i < bitfield.length; i++) {
			if(bitfield[i] &&  (multiBitField[i] > 0) && (multiBitField[i] < lowestFrequency)) {
				lowestId = i;
				lowestFrequency = multiBitField[i];
			}
		}
		if(lowestId >= 0) {
			int length = 0;
			if(lowestId == bitfield.length -1) {
				length = torrent.getInformationManager().getTorrentInfo().getFinalPieceLength();
			} else {
				length = torrent.getInformationManager().getTorrentInfo().getPieceLength();
			}
			return new Piece(lowestId,16 * 1024,length, speed);
		} else {
			return null;
		}
	}

}


