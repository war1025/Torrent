package tcl.tm.torrent.communication.util;

import tcl.tm.torrent.Torrent;

/**
 * Represents a Piece from a BitTorrent download.
 * A Piece encapsulates the data within the piece as well
 * as tracking which sections of the piece have been downloaded.
 * Pieces contain all relevant information needed for the downloading
 * of a piece of a BitTorrent file, as well as providing methods to
 * save and monitor them.
 *
 * @author Wayne Rowcliffe
 **/
public class Piece {

	private int pieceId;
	private boolean[] block;
	private byte[] data;

	private int blockSize;
	private int finalBlockSize;

	private int pieceCount;
	private int standardPieceSize;
	private int finalPieceSize;

	private long startTime;
	private long endTime;

	private Object lock;
	private ThroughputMonitor monitor;

	/**
	 * Creates a Piece with the given pieceId and pieceSize, where
	 * the piece is broken into blocks of the given blockSize.
	 * All blocks are uniform length except for the final block, which
	 * may be smaller in order to sync with the pieceSize.
	 *
	 * @param pieceId The id of this piece object.
	 * @param blockSize The size of block to use when downloading this piece.
	 * @param pieceSize The total size, in bytes, of this piece.
	 **/
	public Piece(int blockSize, Torrent torrent, ThroughputMonitor monitor) {
		this.blockSize = blockSize;
		this.data = null;
		this.lock = null;

		this.pieceCount = torrent.getInformationManager().getTorrentInfo().getPieceCount();
		this.standardPieceSize = torrent.getInformationManager().getTorrentInfo().getPieceLength();
		this.finalPieceSize = torrent.getInformationManager().getTorrentInfo().getFinalPieceLength();
		this.monitor = monitor;
	}

	private Piece() {

	}

	public Piece newInstance() {
		Piece piece = new Piece();
		piece.blockSize = this.blockSize;
		piece.lock = new Object();
		piece.pieceCount = this.pieceCount;
		piece.standardPieceSize = this.standardPieceSize;
		piece.finalPieceSize = this.finalPieceSize;
		piece.monitor = this.monitor;

		return piece;
	}

	public Piece reset(int pieceId) {
		if(pieceId < 0 || pieceId >= pieceCount) {
			throw new IndexOutOfBoundsException("PieceId is out of bounds: " + pieceId);
		}
		this.pieceId = pieceId;
		int pieceSize = (pieceId < pieceCount -1) ? standardPieceSize : finalPieceSize;
		if(pieceSize % blockSize == 0) {
			this.finalBlockSize = blockSize;
			this.block = new boolean[pieceSize/blockSize];
		} else {
			this.finalBlockSize = pieceSize % blockSize;
			this.block = new boolean[(pieceSize/blockSize) +1];
		}
		if(data == null || data.length != pieceSize) {
			System.out.println("Creating a new data array of length " + pieceSize);
			data = new byte[pieceSize];
		}

		startTime = System.currentTimeMillis();
		endTime = 0;

		return this;
	}

	/**
	 * Saves a block within the piece to its proper location and marks it as saved.
	 *
	 * @param number The block number of this piece
	 * @param data The data to place in this block
	 * @param offset The offset within data to start saving.
	 * @param length The number of bytes after the offset to save.
	 *
	 * @return If the block was saved successfully
	 **/
	public boolean saveBlock(int number, byte[] data, int offset, int length) {
		boolean success = true;
		monitor.dataReceived(length);
		synchronized(lock) {
			if(number < 0 || number >= block.length) {
				success = false;
			} else if(!block[number] && ((length == blockSize) || ((number == block.length -1) && (length == finalBlockSize)))) {
				System.arraycopy(data,offset,this.data,number * blockSize,length);
				block[number] = true;
				if(isComplete()) {
					endTime = System.currentTimeMillis();
				}
			}
		}
		return success;
	}

	/**
	 * Returns the pieceId for this piece.
	 *
	 * @return The id for this piece.
	 **/
	public int getPieceId() {
		return pieceId;
	}

	/**
	 * Returns the standard block size for this piece
	 *
	 * @return The standard block size in bytes.
	 **/
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * Returns the size of this final block within this piece
	 *
	 * @return The size of the final block in bytes.
	 **/
	public int getFinalBlockSize() {
		return finalBlockSize;
	}

	/**
	 * Returns the number of blocks this piece is broken into.
	 *
	 * @return The number of blocks in this piece
	 **/
	public int getBlockCount() {
		return block.length;
	}

	/**
	 * Whether or not all blocks for this piece have been saved.
	 *
	 * @return Whether or not all blocks for this piece have been saved.
	 **/
	public boolean isComplete() {
		boolean complete = true;
		synchronized(lock) {
			for(boolean b : block) {
				complete &= b;
			}
		}
		return complete;
	}

	/**
	 * The set of blocks which have not yet been saved in this piece
	 *
	 * @return The indices of the blocks which have not yet been saved.
	 **/
	public int[] getNeededBlocks() {
		int count = 0;
		int[] need = null;
		synchronized(lock) {
			for(boolean b : block) {
				if(!b) {count++;}
			}
			need = new int[count];
			count = 0;
			for(int i = 0; i < block.length; i++) {
				if(!block[i]) {
					need[count] = i;
					count++;
				}
			}
		}
		return need;
	}

	/**
	 * The data this piece encapsulates.
	 *
	 * @return The data this piece encapsulates.
	 **/
	public byte[] getData() {
		return data;
	}

	/**
	 * The time it took to complete this piece download.
	 *
	 * @return The time it took to complete this piece download, or a large number if not yet complete
	 **/
	public long completeTime() {
		synchronized(lock) {
			if(endTime > 0) {
				return endTime - startTime;
			}
			return startTime;
		}
	}
}

