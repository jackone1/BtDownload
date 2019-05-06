//package Connection;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketException;
//import java.util.ArrayList;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import TorrentDownload.DecodeChange;
//import function.MyFunction;
//
//public class TCPPeerConnection {
//	private  Socket sendSocket = null;
//	private  ServerSocket receiveSocket = null;
//	private ServerSocket serverSocket = null;
//	private int sendPort,receivePort;
//	private byte[] bitField = null;
//	public TCPPeerConnection(String ipAddress,int sendPort,int receivePort) {
//		//����tcp����
//		this.sendPort = sendPort;
//		this.receivePort = receivePort;
//		this.sendSocket = new Socket();
//		try {
//			//�󶨱������Ӷ˿�
//			sendSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(),receivePort));
//			System.out.println("�����׽��ֳɹ���");
//		} catch (IOException e) {
//			System.out.println("���ض˿��Ѿ���ռ��!");
//		}
//		try {
//			//����tcp����
//			sendSocket.connect(new InetSocketAddress(ipAddress, sendPort));
//			System.out.println("�������ӳɹ���");
//		} catch (Exception e) {
//			System.out.println("����tcp����ʧ��!");
//			try {
//				this.sendSocket.close();
//			} catch (IOException e1) {
//				System.out.println("�ر��׽���ʧ�ܣ�");
//			}
//		}		
//	}
//	
//	public void reset(String ipAddress,int sendPort) {
//		try {
//			//�ر�ԭ�׽�������
//			this.sendSocket.close();
//		} catch (IOException e1) {
//			System.out.println("�׽��ֹر�ʧ��!");
//		}
//		try {
//			//�󶨱������Ӷ˿�
//			sendSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(),receivePort));
//			System.out.println("�����׽��ֳɹ���");
//		} catch (IOException e) {
//			System.out.println("���ض˿��Ѿ���ռ��!");
//		}
//		try {
//			//����tcp����
//			sendSocket.connect(new InetSocketAddress(ipAddress, sendPort));
//			System.out.println("�������ӳɹ���");
//		} catch (Exception e) {
//			System.out.println("����tcp����ʧ��!");
//		}finally {
//			try {
//				this.sendSocket.close();
//			} catch (IOException e) {
//				System.out.println("�ر��׽���ʧ�ܣ�");
//			}
//		}
//	}
//	
//	
//	//������������
//	public void peerHandshake(String hash,int pieceNum) {
//		//����տ�ʼδ����Ƭ�Σ���ʼ��bitField
//		if(bitField==null) {
//			this.bitField = new byte[pieceNum+5];
//			bitField[4] = (byte)5;
//			bitField[0] = (byte) (pieceNum/Math.pow(256, 3));
//			bitField[1] = (byte) (pieceNum/Math.pow(256, 2));
//			bitField[2] = (byte) (pieceNum/Math.pow(256, 1));
//			bitField[3] = (byte) (pieceNum/Math.pow(256, 0));
//		}
//		if (sendSocket != null && sendSocket.isConnected()) {
//            new HandshakeThread(this.sendSocket,hash,bitField).start();
//            
////            if(sendSocket.isConnected()) {
////            	new SendThread(this.sendSocket,bitField).start();
////            	new RecvThread(this.sendSocket,bitField).start();
////            	new KeepAliveThread(this.sendSocket).start();
////            }
//        }
//	}
//	
//}
//
