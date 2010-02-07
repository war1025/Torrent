package tcl.tests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;

import java.util.Date;

import tcl.tm.torrent.info.TorrentInfo;
import tcl.tm.torrent.info.impl.TorrentInfoImpl;

/**
 * Tests the functionality of the TorrentInfo interface.
 * 
 * All tests pertain to the commented out method most directly above them.
 **/
public class TorrentInfoTest {
	
	private static TorrentInfo single;
	private static TorrentInfo multi;
	
	@BeforeClass
	public static void before() {
		single = new TorrentInfoImpl("/home/war1025/.local/share/Trash/files/debian-testing-amd64-CD-1.iso.torrent");
		multi = new TorrentInfoImpl("/home/war1025/.local/share/Trash/files/Death_Cab_For_Cutie_Narrow_Stairs_2008_DEATHCABFORCUTiE.torrent");
	}
	
	//public String getTorrentName()
	
	@Test
	public void nameSingle() {
		assertEquals(single.getTorrentName(),"debian-testing-amd64-CD-1.iso");
	}
	
	@Test
	public void nameMulti() {
		assertEquals(multi.getTorrentName(),"Death_Cab_For_Cutie-Narrow_Stairs-2008-DEATHCABFORCUTiE");
	}
	
	//public String getAnnounceURL()
	
	@Test
	public void announceSingle() {
		assertEquals(single.getAnnounceURL(),"http://bttracker.acc.umu.se:6969/announce");
	}
	
	@Test
	public void announceMulti() {
		assertEquals(multi.getAnnounceURL(),"http://vip.tracker.thepiratebay.org/announce");
	}
	
	//public String getEscapedInfoHash()
	
	@Test
	public void escapedInfoHashSingle() {
		assertEquals(single.getEscapedInfoHash(),"%9E%8A%C0%9B%8A%8B%B0X%9DNi_%3D%15z%F8%3F%BBnu");
	}
	
	@Test
	public void escaptedInfoHashMulti() {
		assertEquals(multi.getEscapedInfoHash(),"%B2%97%8E%3E%F3%85e%EB%AA%AC%DE%07%24%9E%E1%60%90S%8B%EE");
	}

	//public byte[] getInfoHash()

	@Test
	public void infoHashSingle() {
		assertEquals(single.getInfoHash().length,20);
	}
	
	@Test
	public void infoHashMulti() {
		assertEquals(multi.getInfoHash().length,20);
	}
	
	//public String[] getAnnounceList();
	
	@Test
	public void announceListSingle() {
		assertArrayEquals(single.getAnnounceList(),new String[] {""});
	}
	
	@Test
	public void announceListMulti() {
		assertArrayEquals(multi.getAnnounceList(), new String[] {""});
	}

	//public String getCreatedBy();
	
	@Test
	public void createdSingle() {
		assertEquals(single.getCreatedBy(),"");
	}
	
	@Test
	public void createdMulti() {
		assertEquals(multi.getCreatedBy(),"Azureus/3.0.5.2");
	}

	//public String getComment();
	
	@Test
	public void commentSingle() {
		assertEquals(single.getComment(),"\"Debian CD from cdimage.debian.org\"");
	}
	
	@Test
	public void commentMulti() {
		assertEquals(multi.getComment(),"http://www.mp3nova.org");
	}

	//public Date getCreationDate();
	
	@Test
	public void creationDateSingle() {
		assertTrue(single.getCreationDate() instanceof Date);
	}
	
	@Test
	public void creationDateMulti() {
		assertTrue(multi.getCreationDate() instanceof Date);
	}
	
	//public String[] getPieceHashes();

	//public String getPieceHash(int id);

	@Test
	public void hashTestSingle() {
		assertEquals(single.getPieceHashes()[1000],single.getPieceHash(1000));
	}
	
	@Test
	public void hashTestMulti() {
		assertEquals(multi.getPieceHashes()[1000],multi.getPieceHash(1000));
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceHashNegativeSingle() {
		single.getPieceHash(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceHashNegativeMulti() {
		multi.getPieceHash(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceHashBoundrySingle() {
		single.getPieceHash(1294);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceHashBoundryMulti() {
		multi.getPieceHash(1946);
	}
	
	//public String[] getFileNames();
	
	@Test
	public void filesSingle() {
		assertArrayEquals(single.getFileNames(),new String[] {"debian-testing-amd64-CD-1.iso"});
	}
	
	@Test
	public void filesMulti() {
		String[] files = {	"00-death_cab_for_cutie-narrow_stairs-2008.m3u",
							"00-death_cab_for_cutie-narrow_stairs-2008.nfo",
							"00-death_cab_for_cutie-narrow_stairs-2008.sfv",
							"01-death_cab_for_cutie-bixby_canyon_bridge.mp3",
							"02-death_cab_for_cutie-i_will_possess_your_heart.mp3",
							"03-death_cab_for_cutie-no_sunlight.mp3",
							"04-death_cab_for_cutie-cath.mp3",
							"05-death_cab_for_cutie-talking_bird.mp3",
							"06-death_cab_for_cutie-you_can_do_better_than_me.mp3",
							"07-death_cab_for_cutie-grapevine_fires.mp3",
							"08-death_cab_for_cutie-your_new_twin_sized_bed.mp3",
							"09-death_cab_for_cutie-long_division.mp3",
							"10-death_cab_for_cutie-pity_and_fear.mp3",
							"11-death_cab_for_cutie-the_ice_is_getting_thinner.mp3",
							"www.mp3nova.org.url" };
		assertArrayEquals(multi.getFileNames(),files);
	}
	
	//public int getFileCount();
	
	@Test
	public void fileCountSingle() {
		assertEquals(single.getFileCount(),1);
	}
	
	@Test
	public void fileCountMulti() {
		assertEquals(multi.getFileCount(),15);
	}
	
	/**
	 * The pair (piece,offset) which indicates where this file starts
	 *
	 * @param id The id for the file, which is its location in the getFileNames() array.
	 *
	 * @return A pair of integers of the form (piece,offset) where the file starts
	 **/
	//public int[] getFileStartLocation(int id);
	
	@Test
	public void fileStartSingle() {
		assertArrayEquals(single.getFileStartLocation(0), new int[] {0,0});
	}
	
	@Test
	public void fileStartMulti() {
		assertArrayEquals(multi.getFileStartLocation(3), new int[] {0,1979});
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartNegativeSingle() {
		single.getFileStartLocation(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartNegativeMulti() {
		multi.getFileStartLocation(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartBoundrySingle() {
		single.getFileStartLocation(1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartBoundryMulti() {
		multi.getFileStartLocation(15);
	}
	
	//public int[] getFileEndLocation(int id);

	@Test
	public void fileEndSingle() {
		assertArrayEquals(single.getFileEndLocation(0), new int[] {1293,368639});
	}
	
	@Test
	public void fileEndMulti() {
		assertArrayEquals(multi.getFileEndLocation(12), new int[] {1807,27319});
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileEndNegativeSingle() {
		single.getFileStartLocation(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileEndNegativeMulti() {
		multi.getFileStartLocation(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileEndBoundrySingle() {
		single.getFileStartLocation(1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileEndBoundryMulti() {
		multi.getFileStartLocation(15);
	}
	
	//public int getFileLength(int id);
	
	@Test
	public void fileLengthSingle() {
		assertEquals(single.getFileLength(0),678273024);
	}
	
	@Test
	public void fileLengthMulti() {
		assertEquals(multi.getFileLength(9),6034493);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileLengthNegativeSingle() {
		single.getFileLength(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileLengthNegativeMulti() {
		multi.getFileLength(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileLengthBoundrySingle() {
		single.getFileLength(1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileLengthBoundryMulti() {
		multi.getFileLength(15);
	}
	
	//public int getFileStartByte(int id);

	@Test
	public void fileStartByteSingle() {
		assertEquals(single.getFileStartByte(0),0);
	}
	
	@Test
	public void fileStartByteMulti() {
		assertEquals(multi.getFileStartByte(6),24447845);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartByteNegativeSingle() {
		single.getFileStartByte(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartByteNegativeMulti() {
		multi.getFileStartByte(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartByteBoundrySingle() {
		single.getFileStartByte(1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void fileStartByteBoundryMulti() {
		multi.getFileStartByte(15);
	}
	
	//public String[] getFilePath(int id);
	
	@Test
	public void filePathSingle() {
		assertArrayEquals(single.getFilePath(0),new String[] {"debian-testing-amd64-CD-1.iso"});
	}
	
	@Test
	public void filePathMulti() {
		assertArrayEquals(multi.getFilePath(3),new String[] {"01-death_cab_for_cutie-bixby_canyon_bridge.mp3"});
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void filePathNegativeSingle() {
		single.getFilePath(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void filePathNegativeMulti() {
		multi.getFilePath(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void filePathBoundrySingle() {
		single.getFilePath(1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void filePathBoundryMulti() {
		multi.getFilePath(15);
	}
	
	//public int getPieceLength();
	
	@Test
	public void pieceLengthSingle() {
		assertEquals(single.getPieceLength(),524288);
	}
	
	@Test
	public void pieceLengthMulti() {
		assertEquals(multi.getPieceLength(),32768);
	}
	
	//public int getFinalPieceLength();

	@Test
	public void finalPieceLengthSingle() {
		assertEquals(single.getFinalPieceLength(),368640);
	}
	
	@Test
	public void finalPieceLengthMulti() {
		assertEquals(multi.getFinalPieceLength(),5013);
	}

	//public int getPieceCount();

	@Test
	public void pieceCountSingle() {
		assertEquals(single.getPieceCount(),1294);
	}
	
	@Test
	public void pieceCountMulti() {
		assertEquals(multi.getPieceCount(),1946);
	}

	//public String getEncoding();
	
	@Test
	public void encodingSingle() {
		assertEquals(single.getEncoding(),"");
	}
	
	@Test
	public void encodingMulti() {
		assertEquals(multi.getEncoding(),"UTF-8");
	}
	
	/**
	 * The pair (file,offset) which indicates where this piece starts
	 *
	 * @param id The id of this piece, which is its location in the getPieceHashes array
	 *
	 * @return A pair of integers of the form (file,offset) where the piece starts
	 **/
	//public int[] getPieceStartLocation(int id);
	
	@Test
	public void pieceStartSingle() {
		assertArrayEquals(single.getPieceStartLocation(0), new long[] {0,0});
	}
	
	@Test
	public void pieceStartMulti() {
		assertArrayEquals(multi.getPieceStartLocation(0), new long[] {0,0});
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceStartNegativeSingle() {
		single.getPieceStartLocation(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceStartNegativeMulti() {
		multi.getPieceStartLocation(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceStartBoundrySingle() {
		single.getPieceStartLocation(1294);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceStartBoundryMulti() {
		multi.getPieceStartLocation(1946);
	}
			
	//public int[] getPieceEndLocation(int id);
	
	@Test
	public void pieceEndSingle() {
		assertArrayEquals(single.getPieceEndLocation(0), new long[] {0,524287});
	}
	
	@Test
	public void pieceEndMulti() {
		assertArrayEquals(multi.getPieceEndLocation(0), new long[] {3,30788});
	}
	
	@Test
	public void finalPieceEndSingle() {
		assertArrayEquals(single.getPieceEndLocation(1293), new long[] {0,678273023});
	}
	
	@Test
	public void finalPieceEndMulti() {
		assertArrayEquals(multi.getPieceEndLocation(1945), new long[] {14,121});
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceEndNegativeSingle() {
		single.getPieceEndLocation(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceEndNegativeMulti() {
		multi.getPieceEndLocation(-1);
	}
		
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceEndBoundrySingle() {
		single.getPieceEndLocation(1294);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void pieceEndBoundryMulti() {
		multi.getPieceEndLocation(1946);
	}
}
