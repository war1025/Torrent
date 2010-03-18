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
				Piece piece = piecePool.take();
				PieceRequest request = pieceQueue.take();

				assignPiece(piece, request.getBitfield());

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

	}

	public boolean returnPiece(Piece p) {

	}

	public void peerHave(int pieceId) {

	}

	public void peerBitfield(boolean[] bitfield) {

	}

	public boolean peerInteresting(boolean[] bitfield) {

	}

	public void removeBitfield(boolean[] bitfield) {

	}

}


