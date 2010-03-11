package tcl.tm.torrent.communication.registry.impl;

import tcl.tm.torrent.communication.registry.PieceRegistry;
import tcl.tm.torrent.communication.registry.PieceRequestFuture;

import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.file.FileAccessFuture;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PieceRegistryImpl implements PieceRegistry {

	private PriorityBlockingQueue<PieceRequestFutureImpl> pieceQueue;

	private BlockingQueue<Piece> piecePool;

	private boolean running;

	public PieceRegistryImpl(Piece template, int poolSize) {
		piecePool = new LinkedBlockingQueue<Piece>();
		for(int i = 0; i < poolSize; i++) {
			piecePool.push(template.newInstance());
		}

		pieceQueue = new PriorityBlockingQueue<PieceRequestFutureImpl>();

		running = false;
	}




