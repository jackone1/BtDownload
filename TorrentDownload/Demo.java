package TorrentDownload;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import Connection.HTTPTrackerTransfor;
import Connection.UDPTrackerTransfor;
import net.sf.json.JSONObject;
import java.util.Random;
public class Demo {
//	public static byte[] reverseByte(byte[] array) {
//		for(int i=0;i<=array.length/2;i++) {
//			byte temp;
//			temp = array[i];
//			array[i] = array[array.length-i-1];
//			array[array.length-i-1] = temp;
//		}
//		return array;
//	}
	
	
	public static void main(String[] args) {
		int port = 6881;
		String hash = "052ef38011e34ef27e58391da13a327eb88323a3";
//		byte[] getTorrent = GetTorrent.downTorrent(hash);
//		RWDTorrent.download(getTorrent, "E:/test.torrent");
//		char[] torrentChar = RWDTorrent.read("E:/test.torrent");
//		//char[] torrentChar = RWDTorrent.readFromGet(getTorrent);
//		if(torrentChar==null) {
//			System.out.println("���ļ�������");
//			return;
//		}
//		String torrent = new String(torrentChar);
//		System.out.println(torrent);
//		//��������
//		String result1 = ParseTorrent.ParseBencode(torrent);
//		if(result1==null) {
//			System.out.println("�ļ���������!");
//		}else {
//			System.out.println("�ļ������ɹ�!");
//		}	
//		//�����������������ı�����txt�ļ�
//		RWDTorrent.write(Regex.cleanData(result1),"E:/temp2.txt");
//		//��ʾ���ӽṹ
//		try {
//			JSONObject torrentJson = ParseTorrent.showTorrent(result1);
//		}catch(Exception e) {
//			System.out.println("�����ļ��Ǳ�׼!");
//		}
//		HTTPTrackerTransfor httpTf = new HTTPTrackerTransfor();
//		String data = httpTf.setUpLink("http://tracker.supertracker.net:1337/announce", hash, port);
//		System.out.println(data);
		UDPTrackerTransfor udpTf = new UDPTrackerTransfor(10086,"052ef38011e34ef27e58391da13a327eb88323a3");
		byte[] bytes = udpTf.setUpLink("udp://tracker.openbittorrent.com:80");
		System.out.println(bytes.length);
		for(byte b:bytes) {
			System.out.print(b);
			System.out.print(" ");
		}
		byte[] bytes2 = udpTf.startAnnounceRequest();
		for(byte b:bytes2) {
			System.out.print(b);
			System.out.print(" ");
		}
		
	}
	
}
