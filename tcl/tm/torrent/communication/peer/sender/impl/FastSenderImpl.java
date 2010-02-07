package tcl.tm.torrent.communication.peer.sender.impl;

import tcl.tm.torrent.file.FileAccessManager;

import tcl.tm.torrent.communication.CommunicationManager;
import tcl.tm.torrent.communication.peer.Peer;
import tcl.tm.torrent.communication.peer.sender.FastSender;
import tcl.tm.torrent.communication.peer.sender.impl.StandardSender;

import java.io.OutputStream;

import static tcl.tm.torrent.communication.peer.util.PeerUtils.decomposeLength;

public class FastSenderImpl extends StandardSender implements FastSender {
	
	public FastSenderImpl(Peer peer, FileAccessManager fam, CommunicationManager cm, OutputStream peerOutput) {
		super(peer,fam,cm,peerOutput);
	}
	
	public void sendPiece(int pieceId, int byteOffset, int length) {
		issueReject(pieceId, byteOffset, length);
	}
	
	public void issueReject(int pieceId, int byteOffset, int length) {
		byte[] out = new byte[17];
		
		byte[] mLength = decomposeLength(13);
		System.arraycopy(mLength,0,out,0,4);
		
		out[4] = 16;
		
		byte[] mPieceId = decomposeLength(pieceId);
		System.arraycopy(mPieceId,0,out,5,4);
		
		byte[] mByteOffset = decomposeLength(byteOffset);
		System.arraycopy(mByteOffset,0,out,9,4);
		
		byte[] mLength2 = decomposeLength(length);
		System.arraycopy(mLength2,0,out,13,4);
		
		write(out);
	}
}
