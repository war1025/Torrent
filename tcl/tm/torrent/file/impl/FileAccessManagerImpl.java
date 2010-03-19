package tcl.tm.torrent.file.impl;

import tcl.tm.torrent.info.TorrentInfo;
import tcl.tm.torrent.file.util.FileCreator;
import tcl.tm.torrent.file.util.PieceVerifier;
import tcl.tm.torrent.file.util.StatusLoader;
import tcl.tm.torrent.file.FileAccessManager;
import tcl.tm.torrent.file.FileAccessFuture;
import tcl.tm.torrent.file.FileAccessFuture.Type;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.Closeable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * FileAccessMangerImpl is an implementation of the FileAccessManagerInterface
 *
 * The FileAccessManager aims to be a thread-safe
 * way to manage the reading/writing of pieces from file.
 *
 * The other main aim of the FileAccessManager is to abstract away
 * all of the actual file calculations, and reduce actions to:
 * 1. Retrieval of pieces, 2. Saving of pieces, 3. Checking whether a piece is available.
 *
 * FileAccessManager uses FileAccessFutures to return the result of the requested operation.
 *
 * A single thread should handle the actual file reading / writing.
 * This data should be saved into the FileAccessFuture which is returned from the method call.
 * FileAccessFutures should block until the FileAccessManager has processed them.
 *
 * @author Wayne Rowcliffe
 **/
public class FileAccessManagerImpl implements FileAccessManager, Runnable, Closeable {

	private TorrentInfo info;
	private StatusLoader status;
	private boolean[] have;
	private RandomAccessFile[] file;
	private BlockingQueue<FileAccessFutureImpl> requests;

	private boolean running;

	/**
	 * Creates a FileAccessManager for the given torrent, using files located in the given baseDirectory
	 *
	 * @param t The TorrentInfo for this torrent download
	 * @param baseDirectory The location on the filesystem to store files
	 *
	 * @throws IllegalArgumentException if the fileset for the given TorrentInfo could not be created in the baseDirectory.
	 **/
	public FileAccessManagerImpl(TorrentInfo t, StatusLoader s, String baseDirectory) {
		info = t;
		status = s;
		have = status.getStatus();
		requests = new LinkedBlockingQueue<FileAccessFutureImpl>();
		try{
			file = FileCreator.createFileSet(t,baseDirectory);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not create proper file set");
		}
		running = true;
	}

	/**
	 * Closes this FileAccessManager
	 **/
	public void close() {
		if(running) {
			running = false;
			requests.offer(new FileAccessFutureImpl(0,null,Type.GET_PIECE));
		}
	}

	/**
	 * The FileAccessManager does all backend work on the Thread running this method.
	 * Requests are evaluated in the order they are given to the FileAccessManager.
	 **/
	public void run() {
		while(running) {
			try {
				evaluate(requests.take());
			} catch(InterruptedException e) {e.printStackTrace();}
		}
		while(requests.size() > 0) {
			evaluate(requests.poll());
		}
		for(RandomAccessFile f : file) {
			try{
				f.close();
			} catch(IOException e) {e.printStackTrace();}
		}
	}

	/**
	 * Determines the proper course of action for the given FileAccessFuture
	 * The action taken is dependant on the type parameter of the FileAccessFuture
	 *
	 * @param faf The FileAccessFuture to evaluate
	 **/
	private void evaluate(FileAccessFutureImpl faf) {
		switch(faf.type) {
			case GET_PIECE : getPiece(faf); break;
			case SAVE_PIECE : savePiece(faf); break;
			case HAVE_PIECE : havePiece(faf); break;
			case GET_BITFIELD : getBitfield(faf); break;
		}
		faf.validate();
	}

	/**
	 * Attempts to retrieve the piece requested by the given FileAccessFuture.
	 * Upon success, faf.getSuccess() will return true, and faf.getData() will
	 * contain the byte[] for the requested piece.
	 *
	 * @param faf The FileAccessFuture to fulfill
	 **/
	private void getPiece(FileAccessFutureImpl faf) {
		// Have piece will set success to true if we have the piece.
		// We can then retrieve the piece
		havePiece(faf);
		try{
			if(faf.success) {
				// Get the start and end locations of the piece,
				// and a byte array of proper length
				long[] start = info.getPieceStartLocation(faf.id);
				long[] end = info.getPieceEndLocation(faf.id);

				int startFile = (int) start[0];
				int endFile = (int) end[0];

				byte[] b = (faf.id + 1 == info.getPieceCount()) ? new byte[info.getFinalPieceLength()]
																: new byte[info.getPieceLength()];

				// If the piece is entirely in one file, read straight through
				// Otherwise read from the start location in the first file, fully through any
				// intermediate files, and until the end location in the end file.
				if(startFile == endFile) {
					file[startFile].seek(start[1]);
					file[startFile].readFully(b);
				} else {
					file[startFile].seek(start[1]);
					file[startFile].readFully(b,0,info.getFileEndLocation(startFile)[1]+1);
					for(int i = startFile +1 ; i < endFile; i++) {
						file[i].seek(0);
						file[i].readFully(b,info.getFileStartLocation(i)[1],(int) info.getFileLength(i));
					}
					file[endFile].seek(0);
					file[endFile].readFully(b,info.getFileStartLocation(endFile)[1],(int) end[1]+1);
				}
				faf.setData(b);
			} else {
				// If we do not have the data or cannot retrieve it, set success to false.
				faf.setSuccess(false);
			}
		} catch(IOException io) {
			io.printStackTrace();
			faf.setSuccess(false);
		}
	}

	/**
	 * Attempts to save the piece requested by the given FileAccessFuture.
	 * Upon success, faf.getSuccess() will return true.
	 * Success means that the given data matched the SHA1 hash for that piece,
	 * and that the piece was successfully saved to file.
	 *
	 * @param faf The FileAccessFuture to fulfill
	 **/
	private void savePiece(FileAccessFutureImpl faf) {
		try {
			if(have[faf.id]) {
				faf.setSuccess(true);
			}
			// If the data matches the hash, save it
			else if(PieceVerifier.verify(faf.data,info.getPieceHash(faf.id))) {
				// Get the start and end locations, and the data.
				long[] start = info.getPieceStartLocation(faf.id);
				long[] end = info.getPieceEndLocation(faf.id);

				int startFile = (int) start[0];
				int endFile = (int) end[0];

				byte[] b = faf.data;

				// If the piece is all in one file, write it straight out
				// Otherwise, write from the start location to the end of that file
				// Write fully any intermediate files
				// Then write until the end location of the final file.
				if(startFile == endFile) {
					file[startFile].seek(start[1]);
					file[startFile].write(b);
				} else {
					file[startFile].seek(start[1]);
					file[startFile].write(b,0,info.getFileEndLocation(startFile)[1]+1);
					for(int i = startFile +1 ; i < endFile; i++) {
						file[i].seek(0);
						file[i].write(b,info.getFileStartLocation(i)[1],(int) info.getFileLength(i));
					}
					file[endFile].seek(0);
					file[endFile].write(b,info.getFileStartLocation(endFile)[1],(int) end[1]+1);
				}

				// Record that we have it and were successful.
				have[faf.id] = true;
				status.setStatus(faf.id,true);
				faf.setSuccess(true);
			}
		} catch(IOException io) {
			io.printStackTrace();
			// If anything goes wrong, set success to false, and record that we do not have the piece
			have[faf.id] = false;
			status.setStatus(faf.id,false);
			faf.setSuccess(false);
		}
		faf.setData(null);
	}

	/**
	 * Checks whether or not the requested piece is stored within this FileAccessManger's fileset
	 * faf.getSuccess() will return true if the fileset contains the piece.
	 *
	 * @param faf The FileAccessFuture to fulfill
	 **/
	private void havePiece(FileAccessFutureImpl faf) {
		faf.setSuccess(have[faf.id]);
	}

	/**
	 * Saves the bitfield for this FileAccessManager to the data variable of the given FileAccessFuture
	 *
	 * @param faf The FileAccessFuture to fulfill
	 **/
	private void getBitfield(FileAccessFutureImpl faf) {
		byte[] out = new byte[(int) Math.ceil(have.length / 8.0)];
		for(int i = 0; i < out.length; i++) {
			int aByte = 0;
			for(int j = 0; j < 8; j++) {
				if((i*8 + j) < have.length) {
					aByte |= (have[i*8 + j] ? 1 : 0) << (7-j);
				}
			}
			out[i] = (byte) aByte;
		}
		faf.setData(out);
		faf.setSuccess(true);
	}

	/**
	 * Request that the FileAccessManager retrieve the piece with the given id
	 *
	 * @param id The id of the piece to retrieve
	 *
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture getPiece(int id) {
		FileAccessFutureImpl f = new FileAccessFutureImpl(id,null,Type.GET_PIECE);
		try{
			if(running) {
				requests.put(f);
			} else {
				f.setSuccess(false);
				f.validate();
			}
		} catch(InterruptedException e) {e.printStackTrace();}
		return f;
	}

	/**
	 * Request that the FileAccessManager save the piece with the given id using the given data
	 *
	 * @param id The id of the piece to save
	 * @param data The data for the given piece
	 *
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture savePiece(int id, byte[] data) {
		FileAccessFutureImpl f = new FileAccessFutureImpl(id, data ,Type.SAVE_PIECE);
		try{
			if(running) {
				requests.put(f);
			} else {
				f.setSuccess(false);
				f.validate();
			}
		} catch(InterruptedException e) {e.printStackTrace();}
		return f;
	}

	/**
	 * Request that the FileAccessManager determine whether or not the piece with the given id is available.
	 *
	 * @param id The id of the piece to check for availability.
	 *
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture havePiece(int id) {
		FileAccessFutureImpl f = new FileAccessFutureImpl(id,null,Type.HAVE_PIECE);
		try{
			if(running) {
				requests.put(f);
			} else {
				f.setSuccess(false);
				f.validate();
			}
		} catch(InterruptedException e) {e.printStackTrace();}
		return f;
	}

	/**
	 * Request that the FileAccessManager return a bitfield corresponding to the pieces that are stored to file.
	 *
	 * @return A FileAccessFuture to obtain results from after the request has been processed.
	 **/
	public FileAccessFuture getBitfield() {
		FileAccessFutureImpl f = new FileAccessFutureImpl(0,null,Type.GET_BITFIELD);
		try {
			if(running) {
				requests.put(f);
			} else {
				f.setSuccess(false);
				f.validate();
			}
		} catch(InterruptedException e) {e.printStackTrace();}
		return f;
	}

	/**
	 * FileAccessFutureImpl is an implementation of the FileAccessFuture interface.
	 *
	 * A FileAccessFuture allows the result of an asynchronous
 	 * FileAccessManager request to be viewed once the request has been processed.
 	 *
 	 * There are three types of FileAccessFuture: GET_PIECE, SAVE_PIECE, and HAVE_PIECE
 	 * These correspond to the three different methods available in the FileAccessManger.
 	 *
 	 * FileAccessFutures should be immutable to everyone except the FileAccessManager.
 	 *
 	 * @author Wayne Rowcliffe
 	 **/
	private static class FileAccessFutureImpl implements FileAccessFuture {

		private int id;
		private boolean valid;
		private boolean success;
		private byte[] data;
		private Type type;

		/**
		 * Creates a FileAccessFutureImpl with the given id, data, and type
		 *
		 * @param id The piece id this FileAccessFuture corresponds to
		 * @param data The data to attempt to save, in the case of a savePiece() request
		 * @param type The type of request this FileAccessFuture is to fulfill.
		 **/
		private FileAccessFutureImpl(int id, byte[] data, Type type) {
			this.id = id;
			this.data = data;
			this.type = type;
		}

		/**
		 * Validates this FileAccessFuture, making its contents visible to
		 * the thread that requested it.
		 **/
		private synchronized void validate() {
			this.valid = true;
			notifyAll();
		}

		/**
		 * Sets the data in this FileAccessFuture
		 *
		 * @param data The data to place in this FileAccessFuture
		 **/
		private void setData(byte[] data) {
			this.data = data;
		}

		/**
		 * Whether or not this request was fulfilled successfully.
		 *
		 * @param success The success status of this request.
		 **/
		private void setSuccess(boolean success) {
			this.success = success;
		}

		/**
		 * Retrieves the success status of this request,
		 * blocking until this FileAccessFuture is validated.
		 *
		 * @return Whether or not the request was completed successfully.
		 **/
		public synchronized boolean getSuccess() {
			while(!valid) {
				try{
					wait();
				} catch(InterruptedException e) {}
			}
			return success;
		}

		/**
		 * The piece id this request pertained to.
		 *
		 * @return The piece id this request pertained to.
		 **/
		public int getPieceId() {
			return id;
		}

		/**
		 * The data for the given piece, or null in the case of a HAVE_PIECE request
		 * This method blocks until this FileAccessFuture has been validated.
		 *
		 * @return The data for the given piece, or null in the case of a HAVE_PIECE request.
		 **/
		public synchronized byte[] getData() {
			while(!valid) {
				try{
					wait();
				} catch(InterruptedException e) {}
			}
			return data;
		}

		/**
		 * The type of request that this FileAccessFuture was created for.
		 *
		 * @return The type of request that this FileAccessFuture was created for.
		 **/
		public Type getType() {
			return type;
		}
	}
}


