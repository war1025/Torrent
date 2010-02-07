package tcl.tm.torrent.file.util;

import java.io.*;

import tcl.tm.torrent.info.TorrentInfo;

/**
 * A utility class to create files corresponding to a torrent file
 **/
public class FileCreator {
	
	/**
	 * Creates / loads the files of a torrent download and returns an array of RandomAccessFiles corresponding to them.
	 * 
	 * @param t The TorrentInfo for this torrent
	 * @param baseDirectory The directory to create the fileset for this torrent.
	 * 
	 * @return An array of RandomAccessFiles corresponding to the created / loaded fileset.
	 **/
	public static RandomAccessFile[] createFileSet(TorrentInfo t, String baseDirectory) throws IOException {

		String name = t.getTorrentName();
		
		int numFiles = t.getFileCount();
		
		RandomAccessFile[] files = new RandomAccessFile[numFiles];
		
		for(int i = 0; i < numFiles; i++) {
			String[] path = t.getFilePath(i);

			StringBuilder out = new StringBuilder();

			out.append(baseDirectory);
			out.append(name);
			out.append("/");
			
			for(int j = 0; j < path.length -1; j++) {
				out.append(path[j]);
				out.append("/");
			}

			File fi = new File(out.toString());
			fi.mkdirs();

			out.append(path[path.length -1]);
			File fl = new File(out.toString());	
			fl.createNewFile();
			files[i] = new RandomAccessFile(fl,"rws");
		}
		return files;

	}
}
