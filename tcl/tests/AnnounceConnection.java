package tcl.tests;
//not working quite yet

import java.util.Map;
import java.io.*;
import java.net.URL;
import java.net.URI;
import java.net.Socket;
import java.net.ServerSocket;

import tcl.tm.torrent.info.util.Bencode;
import tcl.tm.torrent.info.impl.TorrentInfoImpl;
import tcl.tm.torrent.info.TorrentInfo;


public class AnnounceConnection {

	public static void main(String[] args) throws Exception {
		
		TorrentInfo ti = new TorrentInfoImpl(args[0]);
		
		Map<String,Object> torrent = Bencode.getTorrentInfo(args[0]);

		String s = ti.getAnnounceURL();
		
		//add hash
		s = s + "?info_hash=" + ti.getEscapedInfoHash();
		//add id
		s = s + "&peer_id=" + "-AZ2060-123456789012";
		//add port
		s = s + "&port=" + "1630";
		//add uploaded
		s = s + "&uploaded=" + "0";
		//add download
		s = s + "&downloaded=" + "0";
		//add bytes left
		s = s + "&left=" + "10";
		//specify compact or not
		s = s + "&compact=" + "1";
		//add event(what we are doing)
		s = s + "&event=" + "started";
		//add the number of peers we want (recomended that we use 30)
		s = s + "&numwant=" + "30";

		//Now we need to split up that string and get it into the approprite places....bencodeing
		
		//place to put all the stuff we find!

		
		System.out.println(s);
		System.out.println("");
		System.out.println("");
		
		
		URL url = new URL(s);

		Map<String,Object> info = Bencode.getTrackerInfo(url.openStream());
		String[] peers = (String[]) info.get("peers");
		for(String s2 : peers) {
			System.out.println(s2);
			int colon = s2.indexOf(":");
			new Thread(new Connection(s2.substring(0,colon),Integer.parseInt(s2.substring(colon+1,s2.length())),ti)).start();
		}
		//new Thread(new ListenerServer()).start();
		
		System.out.println(info);
		
		byte[] b = new byte[68];
		b[0] = (byte) 19;
		byte[] b2 = "BitTorrent protocol".getBytes("UTF-8");
		for(int i = 0; i < b2.length; i++) {
			b[i+1] = b2[i];
		}
		byte[] b3 = ti.getInfoHash();
		for(int i = 0; i < b3.length; i++) {
			b[i + 28] = b3[i];
		}
		byte[] b4 = "-AZ2060-123456789012".getBytes("UTF-8");
		for(int i = 0; i < 20; i++) {
			b[i + 48] = b4[i];
		}
		StringBuilder out = new StringBuilder();
		for(int i = 0 ; i < b.length; i++) {
			out.append(b[i] + " ");
		}
		System.out.println(out.toString());
				
	}

	private static class Connection implements Runnable {

		String host;
		int port;
		TorrentInfo ti;

		public Connection(String host, int port, TorrentInfo ti) {
			this.host = host;
			this.port = port;
			this.ti = ti;
		}

		public void run() {
			Socket s = null;
			try {
				s = new Socket(host,port);
				System.out.println("Connected: " + host + ":" + port + " " + s.isConnected());
				OutputStream out = s.getOutputStream();
				BufferedInputStream in = new BufferedInputStream(s.getInputStream());
				byte[] b = new byte[68];
				b[0] = (byte) 19;
				byte[] b2 = "BitTorrent protocol".getBytes("UTF-8");
				for(int i = 0; i < b2.length; i++) {
					b[i+1] = b2[i];
				}
				byte[] b3 = ti.getInfoHash();
				for(int i = 0; i < b3.length; i++) {
					b[i + 28] = b3[i];
				}
				byte[] b4 = "-AZ2060-123456789012".getBytes("UTF-8");
				for(int i = 0; i < 20; i++) {
					b[i + 48] = b4[i];
				}
				out.write(b);
				
				for(int header = 0; header < b.length; header++) {
					in.read();
				}
				while(true) {
					byte[] value = new byte[4];
					int read = in.read(value,0,4);
					if(read == -1) { break;}
					if(read == 4) {
						int length = parseLength(value);
						if(length > 0) {
							int code =  in.read();
							//System.out.println(length);
							//System.out.println(code);
							switch(code) {
								//case 0 : System.out.println(host + ":" + port + " Got Keep alive"); break;
								case 0 : System.out.println(host + ":" + port + " Got Choke"); break;
								case 1 : System.out.println(host + ":" + port + " Got Unchoke"); break;
								case 2 : System.out.println(host + ":" + port + " Got Interested"); break;
								case 3 : System.out.println(host + ":" + port + " Got Uninterested"); break;
								case 4 : System.out.println(host + ":" + port + " Got Have"); break;
								case 5 : System.out.println(host + ":" + port + " Got Bitfield"); break;
								case 6 : System.out.println(host + ":" + port + " Got Request"); break;
								case 7 : System.out.println(host + ":" + port + " Got Piece"); break;
								case 8 : System.out.println(host + ":" + port + " Got Cancel"); break;
							}
							int count = 0;
							StringBuilder content = new StringBuilder();
							while(count < length-1) {
								count++;
								content.append(in.read() + " ");
							}
							System.out.println(content.toString());
						} else {
							System.out.println(host + ":" + port + " Got Keep alive");
						} 
					}
				}
				
				
			} catch(Exception e) {
				//e.printStackTrace();
			} finally {
				if(s != null) {
					try{ s.close(); } catch(Exception e) {}
				}
			}
		}
		
		private int parseLength(byte[] value) {
			return (value[0] & (0xff)) << 24| (value[1] & (0xff)) << 16 | (value[2] & (0xff)) << 8 | (value[3] & (0xff));
		}
		
		private byte[] decomposeLength(int value) {
			byte[] bytes = new byte[4];
			bytes[3] = (byte) (value & (0xff));
			bytes[2] = (byte) (value >> 8 & (0xff));
			bytes[1] = (byte) (value >> 16 & (0xff));
			bytes[0] = (byte) (value >> 24 & (0xff));
			return bytes;
		}
	}
	
	private static class ListenerServer implements Runnable {
		
		public void run() {
			try {
				ServerSocket server = new ServerSocket(1630);
				
				while(true) {
					listen(server.accept());
				}
			} catch(Exception e) {e.printStackTrace();}
		}
		
		private void listen(Socket s) {
			new Thread(new Listener(s)).start();
		}
		
	}
	
	private static class Listener implements Runnable {
		
		private Socket s;
		
		private Listener(Socket s) {
			this.s = s;
		}
		
		public void run() {
			try {
				InputStream in = s.getInputStream();
				int read;
				while((read = in.read()) != -1) {
					System.out.print(read + " ");
				}
			} catch(Exception e) {e.printStackTrace();}
		}
	}
			
}
