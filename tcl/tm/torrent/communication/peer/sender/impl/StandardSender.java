package tcl.tm.torrent.communication.peer.sender.impl;

import tcl.tm.torrent.file.FileAccessManager;

import tcl.tm.torrent.communication.CommunicationManager;
import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.sender.PeerSender;
import tcl.tm.torrent.communication.peer.util.PeerEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.io.OutputStream;
import java.io.IOException;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.decomposeLength;


public class StandardSender implements PeerSender {
	
	protected Peer peer;
	protected FileAccessManager fam;
	protected CommunicationManager cm;
	private BlockingQueue<PeerEvent> outboundData;
	private OutputStream peerOutput;
	
	private int currentUploadPiece;
	private byte[] currentUploadData;
	
	public StandardSender(Peer peer, FileAccessManager fam, CommunicationManager cm, OutputStream peerOutput) {
		this.peer = peer;
		this.fam = fam;
		this.cm = cm;
		this.peerOutput = peerOutput;
		this.outboundData = new LinkedBlockingQueue<PeerEvent>();
	}
	
	/**
	 * Continously cycles through a queue of PeerEvents which represent 
	 * outbound data, writing it to the output stream.
	 **/
	public void run() {
		while(peer.isRunning()) {
			try {
				peerOutput.write(outboundData.take().getData());
			} catch(IOException e) {
				peer.close();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Indicate to this Peer that we have the given piece.
	 * 
	 * @param pieceID The piece which we want to inform this Peer that we now have.
	 **/
	public void issueHave(int pieceId) {
		if(!peer.getBitfield()[pieceId]) {
			byte[] out = new byte[9];
		
			byte[] mLength = decomposeLength(4);
			System.arraycopy(mLength,0,out,0,4);
			
			out[4] = 4;
			
			byte[] mPieceId = decomposeLength(pieceId);
			System.arraycopy(mPieceId,0,out,5,4);
			
			write(out);
					
		}
		if(peer.getAmInterested()) {
			if(!cm.peerInteresting(peer.getBitfield())) {
				issueDisinterested();
			}
		}
	}
	
	/**
	 * Indicate to this Peer that we wish to keep the connection alive.
	 **/
	public void issueKeepAlive() {
		write(new byte[] {0,0,0,0});
	}
	
	/**
	 * Indicate to this Peer that we are not currently interested in them.
	 **/
	public void issueDisinterested() {
		if(peer.getAmInterested()) {
			peer.setAmInterested(false);
			write(new byte[] {0,0,0,1,3});
		}

	}
	
	/**
	 * Indicate to this Peer that we are currently interested in them.
	 **/
	public void issueInterested() {
		if(!peer.getAmInterested()) {
			peer.setAmInterested(true);
			write(new byte[] {0,0,0,1,2});
		}
	}
	
	/**
	 * Indicate to this Peer that we are currently choking them.
	 **/
	public void issueChoke() {
		if(!peer.getAmChoking()) {
			peer.setAmChoking(true);
			write(new byte[] {0,0,0,1,0});
		}
	}
	
	/**
	 * Indicate to this Peer that we are now unchoking them.
	 **/
	public void issueUnchoke() {
		if(peer.getAmChoking()) {
			peer.setAmChoking(false);
			write(new byte[] {0,0,0,1,1});
		}
	}
	
	/**
	 * Request that this Peer send us the indicated piece data
	 * 
	 * @param pieceId The piece the data comes from
	 * @param byteOffset The offset within the piece to start sending data.
	 * @param length The length of data to send
	 **/
	public void issueRequest(int pieceId, int byteOffset, int length) {
		byte[] out = new byte[17];
		
		byte[] mLength = decomposeLength(13);
		System.arraycopy(mLength,0,out,0,4);
		
		out[4] = 6;
		
		byte[] mPieceId = decomposeLength(pieceId);
		System.arraycopy(mPieceId,0,out,5,4);
		
		byte[] mByteOffset = decomposeLength(byteOffset);
		System.arraycopy(mByteOffset,0,out,9,4);
		
		byte[] mLength2 = decomposeLength(length);
		System.arraycopy(mLength2,0,out,13,4);
		
		write(out);
	}
	
	/**
	 * Send a piece to this Peer
	 * 
	 * @param pieceId The piece the data comes from
	 * @param byteOffset The offset within the piece the data starts at.
	 * @param length The length of the data that will be sent
	 **/
	public void sendPiece(int pieceId, int byteOffset, int length) {
		/*if(pieceId != currentUploadPiece) {
			currentUploadData = fam.getPiece(pieceId).getData();
		}
		byte[] out = new byte[13 + length];
		
		byte[] mLength = decomposeLength(length + 9);
		System.arraycopy(mLength,0,out,0,4);
		
		out[4] = 7;
		
		byte[] mPieceId = decomposeLength(pieceId);
		System.arraycopy(mPieceId,0,out,5,4);
		
		byte[] mByteOffset = decomposeLength(byteOffset);
		System.arraycopy(mByteOffset,0,out,9,4);
		
		System.arraycopy(currentUploadData,byteOffset,out,13,length);
		
		write(out);
		*/
	}
	
	/**
	 * Sends our Bitfield to this peer
	 **/
	public void sendBitfield() {
		byte[] out = new byte[5 + (int) Math.ceil(peer.getBitfield().length / 8.0)];
		
		byte[] mLength = decomposeLength(out.length - 4);
		System.arraycopy(mLength,0,out,0,4);
		
		out[4] = 5;
		
		byte[] bitfield = fam.getBitfield().getData();
		System.arraycopy(bitfield,0,out,5,bitfield.length);
		
		write(out);
	}
	
	/**
	 * Cancel a request for a piece that we previously made to this peer.
	 * 
	 * @param pieceId The piece the data comes from
	 * @param byteOffset The offset within the piece to start sending data.
	 * @param length The length of data to send
	 **/
	public void cancelRequest(int pieceId, int byteOffset, int length) {
		byte[] out = new byte[17];
		
		byte[] mLength = decomposeLength(13);
		System.arraycopy(mLength,0,out,0,4);
		
		out[4] = 8;
		
		byte[] mPieceId = decomposeLength(pieceId);
		System.arraycopy(mPieceId,0,out,5,4);
		
		byte[] mByteOffset = decomposeLength(byteOffset);
		System.arraycopy(mByteOffset,0,out,9,4);
		
		byte[] mLength2 = decomposeLength(length);
		System.arraycopy(mLength2,0,out,13,4);
		
		write(out);
	}

	protected void write(byte[] data) {
		try {
			outboundData.put(new PeerEvent(data));
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

}

