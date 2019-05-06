package TorrentDownload;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Connection.CheckCompleteThread;
import Connection.ConnectionThread;
import Connection.FindPeerThread;
import Connection.HTTPTrackerTransfor;
import Connection.UDPTrackerTransfor;
import function.MyFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.util.Random;

public class Demo {
	public static JSONObject downloadAndParse(String hash) {
		//byte[] getTorrent = GetTorrent.downTorrent(hash);
		//RWDTorrent.download(getTorrent, "E:/test2.torrent");
		char[] torrentChar = RWDTorrent.read("E:/test2.torrent");
//		//char[] torrentChar = RWDTorrent.readFromGet(getTorrent);
		if (torrentChar == null) {
			System.out.println("���ļ�������");
			return null;
		}
		String torrent = new String(torrentChar);
		// ��������
		String result1 = ParseTorrent.ParseBencode(torrent);
		if (result1 == null) {
			System.out.println("�ļ���������!");
		} else {
			System.out.println("�ļ������ɹ�!");
		}
		// �����������������ı�����txt�ļ�
		RWDTorrent.write(Regex.cleanData(result1), "E:/temp2.txt");
		JSONObject torrentJson = null;
		// ��ʾ���ӽṹ
		try {
			torrentJson = ParseTorrent.showTorrent(result1);
			return torrentJson;
		} catch (Exception e) {
			System.out.println("�����ļ��Ǳ�׼!");
			return null;
		}

	}

	public static void httpLink(String hash, int port) {
		HTTPTrackerTransfor httpTf = new HTTPTrackerTransfor();
		try {
			String data = httpTf.setUpLink("http://tracker.supertracker.net:1337/announce", hash, port);
			System.out.println(data);
		} catch (Exception e) {
			System.out.println("�������޷����ʣ�����");
		}
	}

	public static void udpLink(String hash) {
		UDPTrackerTransfor udpTf = new UDPTrackerTransfor(10000, hash);
		byte[] bytes = udpTf.setUpLink("udp://tracker.openbittorrent.com:80");
		System.out.println(bytes.length);
		for (byte b : bytes) {
			System.out.print(b);
			System.out.print(" ");
		}
		JSONObject ipAndPort = udpTf.startAnnounceRequest();
//		Iterator it = ipAndPort.keys();
//		while(it.hasNext()) {
//			String ip = (String)it.next();
//			int port = ipAndPort.getInt(ip);
//			System.out.println("ip:"+ip+" port:"+port);
//		}
	}

	public static JSONArray getAnnounce(JSONObject info) {
		JSONArray announce = null;
		announce = info.getJSONArray("announce-list");
		announce.add(0, info.getString("announce"));
		return announce;
	}
	private static byte[] createBitField(int pieceNum,int pieceSize) {
		int size = (int) Math.floor(pieceNum / 8) + 2;
		byte[] bitField = new byte[size + 5 - 1];
		// byte[] block = new byte[size*8];
		/* �Ӵ����ж�ȡ��������Ϣ����bitfield */
		bitField[4] = (byte) 5;
		bitField[0] = (byte) (size / Math.pow(256, 3));
		bitField[1] = (byte) (size / Math.pow(256, 2));
		bitField[2] = (byte) (size / Math.pow(256, 1));
		bitField[3] = (byte) (size / Math.pow(256, 0));
		bitField = DecodeChange.bytesToBinary(bitField);
		File file = new File("E:/torrent");	
		File[] fs = file.listFiles();	
		for(File f:fs){				
			if(!f.isDirectory()) {
				String fileName = f.getName();
				String regEx = "(\\d+).piece";
				Pattern pat = Pattern.compile(regEx);
				Matcher mat = pat.matcher(fileName);
				if (mat.find()&&f.length()==pieceSize) {
					String index = mat.group(1);
					System.out.println("index: " + index + "������ ");
					bitField[40+Integer.parseInt(index)] = 1;
				} else {
					System.out.println("δƥ�䵽��Ч����");
				}
			}
		}		
		return bitField;
	}
	public static void main(String[] args) {
		int port = 10086;
		String hash = "48e2590b74f73fd1bc148ee7551103a0a7f22c39";
		JSONObject torrentJson = downloadAndParse(hash);
		if (torrentJson == null) {
			System.out.println("����ʧ��!");
			return;
		}
		JSONObject info = (JSONObject) torrentJson.getJSONObject("info");
		List pieces = (List) info.getJSONArray("pieces");
//		//Ƭ����Ŀ
		int pieceNum = pieces.size();
//		System.out.println("pieceNum:"+pieceNum);
//		httpLink(hash,port);
//		udpLink(hash);

		// ��������
//		TCPPeerConnection peerConn = new TCPPeerConnection("49.85.56.246",26666,18888);
//		peerConn.peerHandshake(hash,4082);
		JSONObject use = new JSONObject();
		JSONObject unuse = new JSONObject();
		ArrayList<Socket> sockets = new ArrayList<Socket>();
		byte[] complete = new byte[1];
		
		byte[] bitField = createBitField(pieceNum,info.getInt("piece length"));
		Socket s = new Socket();
		try {
			// �󶨱������Ӷ˿�
			s.bind(new InetSocketAddress(InetAddress.getLocalHost(), 10000));
			System.out.println("�����׽��ֳɹ���");
		} catch (IOException e) {
			System.out.println("���ض˿��Ѿ���ռ��!");
		}
		try {
			// ����tcp����
			s.connect(new InetSocketAddress("203.187.186.67", 26983));
			System.out.println("�������ӳɹ���");
		} catch (IOException e) {
			System.out.println("����tcp����ʧ��!");
			return;
		}
		String path = "E:/torrent";
		new ConnectionThread(s, hash, bitField, info, path).start();
		// ��������ļ��Ƿ���������߳�
//		new CheckCompleteThread(bitField, complete).start();
//		new FindPeerThread(use, unuse, getAnnounce(info), hash, sockets).start();
//		while(sockets.size()==0)
//			for(Socket s:sockets)
//				new ConnectionThread(s, hash, bitField, info, hash).start();
	}

}
