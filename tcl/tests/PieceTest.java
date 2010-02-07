package tcl.tests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import tcl.tm.torrent.communication.util.Piece;
import tcl.tm.torrent.communication.util.CompletePiece;

public class PieceTest {
	
	private Piece p;
	private CompletePiece p2;
	
	@Before
	public void setup() {
		p = new Piece(1,10,100);
		p2 = new CompletePiece(2);
	}
	
	@Test
	public void blockSize() {
		assertEquals(p.getBlockSize(),10);
	}
	
	@Test
	public void blockCount() {
		assertEquals(p.getBlockCount(),10);
	}
	
	@Test
	public void pieceId() {
		assertEquals(p.getPieceId(),1);
	}
	
	@Test
	public void save() {
		p.saveBlock(0,new byte[] {1,2,3,4,5,6,7,8,9,10},0,10);
		assertEquals(p.getNeededBlocks()[0],1);
	}
	
	@Test
	public void complete() {
		for(int i = 0; i < 10; i++) {
			p.saveBlock(i,new byte[] {1,2,3,4,5,6,7,8,9,10},0,10);
		}
		assertTrue(p.isComplete());
	}
	
	@Test
	public void blockSizeP2() {
		assertEquals(p2.getBlockSize(),1);
	}
	
	@Test
	public void blockCountP2() {
		assertEquals(p2.getBlockCount(),1);
	}
	
	@Test
	public void pieceIdP2() {
		assertEquals(p2.getPieceId(),2);
	}
	
	@Test
	public void completeP2() {
		assertTrue(p2.isComplete());
	}
}
