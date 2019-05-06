package Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import TorrentDownload.DecodeChange;
import function.MyFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ConnectionThread extends Thread {
	private Socket sk = null;
	private InputStream is = null;
	private OutputStream os = null;
	private String hash = null;
	private byte[] bitField = null;
	private JSONObject tinfo = null;
	private String path = null;

	public ConnectionThread(Socket sk, String hash, byte[] bitField, JSONObject tinfo, String path) {
		this.sk = sk;
		this.hash = hash;
		this.bitField = bitField;
		this.tinfo = tinfo;
		this.path = path;
		if (sk != null && !sk.isClosed()) {
			try {
				this.sk.setSoTimeout(100000);
				this.is = this.sk.getInputStream();
				this.os = this.sk.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void send(byte[] data) {
		try {
			synchronized (os) {
				os.write(data);
				os.flush();
			}
		} catch (IOException e) {
			System.out.println("������Ϣ����!");
			e.printStackTrace();
		}
	}

	public void run() {
		new HandshakeThread(hash, bitField, tinfo, path).start();
		// �������߳�
		new KeepAliveThread().startHeartBeatThread();
	}

	// ������Ϣ�̣߳������������󣬽����������󲢷�������ȷ�ԣ�����ȷ������bitField��,����bitField����ƥ�䣬��飬����interested��uninterested��
	class HandshakeThread extends Thread {
		private ArrayList<Byte> commends = new ArrayList<Byte>();
		private HandshakePacket handShakePacketS;// ���͵����ְ�
		private HandshakePacket handShakePacketR;// ���ص����ְ�
		private ArrayList<Integer> requestPiece = new ArrayList<Integer>();// ����ȡ��Ƭ��
		private byte[] bitFieldR = null;
		private byte[] bitFieldbytesR = null;
		private byte[] bitFieldbytesS = null;
		private String infohash = null;
		private RequestsPacket rp = new RequestsPacket();

		private int pieceLen = 0;// Ƭ�γ���
		private JSONArray hashList = null;// ÿ��Ƭ�ε�hashУ��ֵ
		private String path = null;

		HandshakeThread(String hash, byte[] bitField, JSONObject tinfo, String path) {
			// ��ʼ�����������
			this.infohash = hash;
			this.path = path;
			this.bitFieldbytesS = bitField;
			this.bitFieldR = new byte[this.bitFieldbytesS.length / 8];
			this.handShakePacketS = new HandshakePacket(hash);
			this.handShakePacketR = new HandshakePacket();
			this.pieceLen = tinfo.getInt("piece length");// Ƭ�γ���
			this.hashList = tinfo.getJSONArray("pieces");
			// ���ö�ȡ��ʱʱ��
			initSocketIO();
		}

		// ����������Ϣ��
		private Boolean checkHandshake(byte[] hspacket) {
			// ���������Ϣ��Ϊ��
			if (hspacket == null) {
				System.out.println("������Ϣ����,�ر�socket");
				return false;
			}
			byte[] hash = new byte[20];
			MyFunction.byteCopy(hspacket, 28, hash, 0, 20);
			// ����hash�������Ƿ������Ҫ��һ��
			byte[] hashpacket = DecodeChange.HexToByte20(infohash);
			if (!Arrays.equals(hash, hashpacket)) {
				System.out.println("hash�����벻ƥ��!");
				return false;
			}
			return true;
		}

		// ���������ж�ȡ����
		public byte[] readRet(int length) {
			byte[] get = new byte[length];
			int ret = 0;
			try {
				int index = 0;
				synchronized (is) {
					while ((ret = is.read()) != -1) {
						get[index++] = (byte)ret;
						if(index==length) {
							break;
						}
					}
				}
				if(index==length)
					return get;
				else
					return null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		public void sendInfo(String type) throws IOException {
			synchronized (os) {
				if (type.equals("choke"))
					os.write(this.rp.getChoke());
				else if (type.equals("unchoke")) {
					os.write(this.rp.getUnchoke());
				} else if (type.equals("interested")) {
					os.write(this.rp.getInterested());
				} else if (type.equals("uninterested")) {
					os.write(this.rp.getUninterseted());
				} else if (type.equals("hava")) {
					os.write(this.rp.getHave());
				}
			}
		}

		// ��socket�������л�ȡ���ְ���Ϣ
		private byte[] getHandshake(InputStream is) {
			int ret;
			int index = 0;
			byte[] handshakeTemp = new byte[68];
			if ((handshakeTemp = readRet(68)) == null) {
				System.out.println("���ص�������Ϣ������!");
				return null;
			}
			return handshakeTemp;
		}

		// �Ƚ��Լ���bitField�ͶԷ�������,����Լ�û�У��Է��еİ�
		private Boolean compareSRBitField(byte[] bitFieldbytesS, byte[] bitFieldbytesR) {
			int count = 0;
			for (int i = 0; i < bitFieldbytesS.length; i++) {
				if (bitFieldbytesS[i] == 0 && bitFieldbytesR[i] == 1) {
					synchronized (requestPiece) {
						requestPiece.add(i);
					}
					count++;
				}
			}
			if (count == 0) {
				System.out.println("�Է�û������Ҫ��pieces");
				return false;
			}
			return true;

		}

		private int getPiece() {
			// ����һ��peer����һ��piece����ʱ,��Ҫ�����ص�bitfield��Ϊ2;�����������߳̾ͻ�����������
			this.bitFieldbytesS[this.requestPiece.get(0)] = 2;
			int remove = -1;
			synchronized (requestPiece) {
				if (requestPiece.size() > 0)
					remove = requestPiece.remove(0);
			}
			return remove;
		}

		private void initSocketIO() {
			try {
				synchronized (sk) {
					sk.setSoTimeout(100000);
					is = sk.getInputStream();
					os = sk.getOutputStream();
				}
			} catch (SocketException e) {
				System.out.println("�����׽��ֶ�ȡʱ�����!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("��ʼ���������������!");
			}

		}

		private void socketClose() {
			try {
				synchronized (is) {
					is.close();
				}
				synchronized (os) {
					os.close();
				}
				synchronized (sk) {
					sk.close();
				}
			} catch (IOException e) {
				System.out.println("�ر��׽��ֳ���!");
				e.printStackTrace();
			}
		}

		public void run() {
			// ��ʼ���׽��ֵĶ�ȡ�˿ں�����˿�
			if (sk != null && !sk.isClosed()) {
				
				// ������������
				send(handShakePacketS.getPacket());
				// ������������
				byte[] handshakeTemp = null;
				if ((handshakeTemp = getHandshake(is)) == null)
					return;
				// ���У��ͨ�����򱣴�������Ϣ�������ͱ��ص�bitfield��Ϣ���Է������У�鲻ͨ������ر�socket����
				if (checkHandshake(handshakeTemp)) {
					// ����Է������ְ�
					handShakePacketR.setPacket(handshakeTemp);
					/// ���͸��Է��Լ���bitField
					send(DecodeChange.binaryToBytes(bitFieldbytesS));
				} else {
					socketClose();
					return;
				}

				System.out.print("\nhandshake:");
				for (byte b : handshakeTemp) {
					System.out.print(b);
					System.out.print(" ");
				}
				System.out.println();
			} else {
				return;
			}
			// ����Ƿ���յ�bitfield����
			byte[] getBitField = new byte[1];
			// �Ƿ�����
			byte[] isUnchoke = new byte[1];
			// requests�����Ƿ��ѷ��ض�Ӧpiece
			byte[] getPiece = new byte[] { 0 };
			new RequestThread(getBitField, isUnchoke, getPiece).start();
			new RecvThread(getBitField, isUnchoke, getPiece).start();
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

		}

		// ����������Ϣ
		class RequestThread extends Thread {
			private byte[] getBitField = null;
			private byte[] isUnchoke = null;
			private byte[] getRequest = null;

			RequestThread(byte[] getBitField, byte[] isUnchoke, byte[] getRequest) {
				this.getBitField = getBitField;
				this.isUnchoke = isUnchoke;
				this.getRequest = getRequest;
			}

			public void run() {
				RequestsPacket rp = new RequestsPacket();
				// ����һ������ĳ���Ϊ32kB
				int length = 32768;
				// һ��Ƭ����Ҫ����Ĵ���
				int requestCount = pieceLen / length;
				// ������δ�Ͽ�
				while (!sk.isClosed()) {
					// �����ȡ���Է����͹�����bitField
					if (this.getBitField[0] == 1) {
						synchronized (this.getBitField) {
							this.getBitField[0] = 0;
						}
						// ����Է�û������Ҫ��piece���Ǿͷ���uninterested�źţ���֮������interested�ź�
						if (!compareSRBitField(bitFieldbytesS, bitFieldbytesR)) {
							try {
								if (!sk.isClosed())
									sendInfo("uninterested");
							} catch (IOException e) {
								System.out.println("����uninterested�ź�ʧ��!");
								socketClose();
								return;
							}
						} else {
							try {
								if (!sk.isClosed()) {
									sendInfo("interested");
								}
							} catch (IOException e) {
								System.out.println("����interested�ź�ʧ��!");
								socketClose();
								return;
							}
						}
					}
					int count = 5;// ��¼�����������Ŀ
					if (requestPiece != null && requestPiece.size() > 0 && this.isUnchoke[0] == 1
							&& this.getRequest[0] == 0) {
						int pieceIndex = requestPiece.get(0) - 40;
						int begin = 0;
						int i = 0;
						// ���ϴ�δ������ĵط���������
						File f = new File(path + "/" + pieceIndex + ".piece");
						if (f.exists()) {
							i = (int) Math.floor(f.length() / length);
						}
//						for (; i <= requestCount; i++) {
//							begin = length * i;
//							rp.setRequest(pieceIndex, begin, length);
//							send(rp.getRequest());
//							count++;
//							System.out.println("����requests��Ϣ,piece index:" + pieceIndex);
//						}
						for (int j = 0; j < count; j++) {
							if (i < requestCount) {
								begin = length * i;
								rp.setRequest(pieceIndex, begin, length);
								send(rp.getRequest());
								i++;
								synchronized (this.getRequest) {
									this.getRequest[0]++;
								}
								System.out.println("����requests��Ϣ,piece index:" + pieceIndex + " pieceƫ��" + begin);
							} else {
								System.out.println("pieces " + pieceIndex + "ȫ�������ѷ���");
							}
						}

					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("�������!");
			}
		}

		// ������Ϣ�߳�
		class RecvThread extends Thread {
			private byte[] getBitField;
			private byte[] isUnchoke;
			private byte[] getRequest;

			RecvThread(byte[] getBitField, byte[] isUnchoke, byte[] getRequest) {
				this.getBitField = getBitField;
				this.isUnchoke = isUnchoke;
				this.getRequest = getRequest;
			}

			private void chokeHandle() {
				System.out.println("�Է�choke���ҷ�����!");
				synchronized (this.isUnchoke) {
					this.isUnchoke[0] = 0;
				}
				// socketClose();
			}

			private void unchokeHandle() {
				synchronized (this.isUnchoke) {
					this.isUnchoke[0] = 1;
				}
				System.out.println("�Է�unchoke���ҷ�����!");
			}

			private void interestedHandle() {
				System.out.println("�Է�interest!");
			}

			private void uninterestedHandle() {
				System.out.println("�Է�uninterest!");
			}

			private void haveHandle() {
				System.out.println("�Է�������have����!");
				byte[] get;
				if ((get = readRet(4)) != null) {
					requestPiece.add(DecodeChange.byteArrayToInt(get));
				} else {
					System.out.println("����,have����δ�����±�");
					socketClose();
				}
			}

			private void bitFieldHandle(byte[] head) {
				System.out.println("�Է�������bitfield��");
				synchronized (bitFieldR) {
					int length = DecodeChange.byteArrayToInt(head);
					bitFieldR = new byte[length + 4];
					int ret = 0;
					int index = 0;
					MyFunction.byteCopy(head, 0, bitFieldR, 0, 4);
					bitField[4] = 5;
					byte[] block = new byte[length - 1];
					if ((block = readRet(length - 1)) == null) {
						return;
					}
					MyFunction.byteCopy(block, 0, bitFieldR, 5, length + 4);
					bitFieldbytesR = DecodeChange.bytesToBinary(bitFieldR);
				}
				System.out.print("\nbitFieldR:");
				for (byte b : bitFieldR) {
					System.out.print(b);
					System.out.print(" ");
				}
				System.out.println();
				synchronized (this.getBitField) {
					this.getBitField[0] = 1;
				}
			}

			private void requestHandle() {
				byte[] index = null;
				byte[] begin = null;
				byte[] length = null;
				if ((index = readRet(4)) == null) {
					System.out.println("request����δ�����±�!");
					socketClose();
					return;
				}
				if ((begin = readRet(4)) == null) {
					System.out.println("request����δ������ʼλ��!");
					socketClose();
					return;
				}
				if ((length = readRet(4)) == null) {
					System.out.println("request����δ���ֳ���!");
					socketClose();
					return;
				}
				int indexInt = DecodeChange.byteArrayToInt(index);
				int lengthInt = DecodeChange.byteArrayToInt(length);
				int beginInt = DecodeChange.byteArrayToInt(begin);
				byte[] block = new byte[lengthInt];
				byte[] sendPiecePack = new byte[13 + lengthInt];
				int size = 0;
				if ((size = ReadFromFile(block, beginInt, lengthInt, path + "/" + indexInt + ".piece")) == 0) {
					return;
				}

				MyFunction.byteCopy(DecodeChange.intToByteArray(9 + lengthInt), 0, sendPiecePack, 0, 4);
				sendPiecePack[4] = 7;
				MyFunction.byteCopy(index, 0, sendPiecePack, 5, 9);
				MyFunction.byteCopy(begin, 0, sendPiecePack, 9, 13);
				MyFunction.byteCopy(block, 0, sendPiecePack, 13, 13 + lengthInt);
				send(sendPiecePack);
			}

			private void pieceHandle(byte[] head) {
				byte[] index = null;
				byte[] begin = null;
				int length = DecodeChange.byteArrayToInt(head) - 9;
				if ((index = readRet(4)) == null) {
					System.out.println("piece����δ�����±�!");
					socketClose();
					return;
				}
				if ((begin = readRet(4)) == null) {
					System.out.println("piece����δ������ʼλ��!");
					socketClose();
					return;
				}
				int indexInt = DecodeChange.byteArrayToInt(index);
				int beginInt = DecodeChange.byteArrayToInt(begin);
				byte[] block = new byte[length];
				if ((block = readRet(length)) == null) {
					System.out.println("piece����δ����block");
					return;
				}
				long size = 0;
				if (bitFieldbytesS[indexInt] != 1) {
					size = writeToFile(block, path + "/" + indexInt + ".piece");
					if (size / pieceLen == 1) {
						System.out.println("����Ƭ��" + indexInt + "�ɹ�!");
						bitFieldbytesS[indexInt] = 1;
						getPiece();
					}
				}
				synchronized (this.getRequest) {
					this.getRequest[0]--;
				}
				// ����Ƭ�ε�bitField��Ϊ1
			}

			private void cancelHandle() {
				byte[] index = null;
				byte[] begin = null;
				byte[] length = null;
				if ((index = readRet(4)) == null) {
					System.out.println("request����δ�����±�!");
					socketClose();
					return;
				}
				if ((begin = readRet(4)) == null) {
					System.out.println("request����δ������ʼλ��!");
					socketClose();
					return;
				}
				if ((length = readRet(4)) == null) {
					System.out.println("request����δ���ֳ���!");
					socketClose();
					return;
				}
				System.out.println("�Է��������!");
			}

			private void extendedHandle(byte[] head) {
				int lengthInt = DecodeChange.byteArrayToInt(head);
				lengthInt = lengthInt > 0 ? lengthInt : 1;
				byte[] extend = new byte[lengthInt - 1];
				if ((extend = readRet(lengthInt - 1)) == null) {
					System.out.println("extended����δ����extend!");
				}
			}

			private void portHandle(byte[] head) {
				int lengthInt = DecodeChange.byteArrayToInt(head);
				lengthInt = lengthInt > 0 ? lengthInt : 1;
				byte[] port;
				try {
					port = new byte[lengthInt - 1];
					if ((port = readRet(lengthInt - 1)) == null) {
						System.out.println("port����δ����port!");
					}
				} catch (Exception e) {
					System.out.println(lengthInt + "===========================");
				}

			}

			private void unknownHandle(byte[] head) {
				int lengthInt = DecodeChange.byteArrayToInt(head);
				lengthInt = lengthInt > 0 ? lengthInt : 1;
				byte[] unknown;
				try {
					unknown = new byte[lengthInt - 1];
					if ((unknown = readRet(lengthInt - 1)) == null) {
						System.out.println("unkonwn����δ����playout!");
					}
				} catch (Exception e) {
					System.out.println(lengthInt + "========================");
				}

			}

			private int ReadFromFile(byte[] piece, int begin, int length, String path) {
				File file = new File(path);
				// ����ļ��������򴴽�һ��
				if (!file.exists()) {
					System.out.println("û�и�Ƭ��!");
					return 0;
				}
				int ret = 0;
				int count = 0;
				int index = 0;
				InputStream in;
				try {
					in = new FileInputStream(file);
					while ((ret = in.read()) != -1) {
						if (count >= begin && count < begin + length) {
							piece[index++] = (byte) ret;
						}
						count++;
					}
					return count;
				} catch (FileNotFoundException e) {
					System.out.println("�Ҳ����ļ���");
					e.printStackTrace();
					return 0;
				} catch (IOException e) {
					System.out.println("��ȡ�����ļ�����!");
					e.printStackTrace();
					return 0;
				}
			}

			private long writeToFile(byte[] piece, String path) {
				File file = new File(path);
				// ����ļ��������򴴽�һ��
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					FileOutputStream fileOutputStream1 = new FileOutputStream(file, true);
					fileOutputStream1.write(piece);
					fileOutputStream1.flush();
					fileOutputStream1.close();
					return file.length();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return 0;
				} catch (IOException e) {
					e.printStackTrace();
					return 0;
				}
			}

			private void receiveHandler(byte[] head) {
				int ret = 0;
				byte[] type = null;
				if ((type = readRet(1)) == null) {
					System.out.println("δ����type������,�ر��׽���");
					socketClose();
					return;
				}
				switch (type[0]) {
				case 0:
					chokeHandle();
					break;
				case 1:
					unchokeHandle();
					break;
				case 2:
					interestedHandle();
					break;
				case 3:
					uninterestedHandle();
					break;
				case 4:
					haveHandle();
					break;
				case 5:
					bitFieldHandle(head);
					break;
				case 6:
					requestHandle();
					break;
				case 7:
					pieceHandle(head);
					break;
				case 8:
					cancelHandle();
					break;
				case 9:
					portHandle(head);
					break;
				case 20:
					extendedHandle(head);
					break;
				default:
					unknownHandle(head);
					System.out.println("��������!");
				}
			}

			public void run() {
				while (!sk.isClosed()) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					int ret = 0;
					byte[] head = null;
					// ��ȡǰ4���ֽ�
					if ((head = readRet(4)) != null) {
						// ���ǰ4���ֽ�Ϊ0����ʾΪ���������ɺ���
						if (!Arrays.equals(head, new byte[] { 0, 0, 0, 0 })) {
							System.out.println("��ȡ����������!");
							receiveHandler(head);
						} else {
							System.out.println("��ȡ��������!");
						}
					} else {
						System.out.println("��ȡ�������ݳ���!��ʱδ�ظ���");
					}
				}
				System.out.println("���ս���!");

			}
		}

	}

	// �������߳�
	class KeepAliveThread {
		private long timeInterval = 100000;
		// private OutputStream os = null;
		// private Socket sk = null;

		KeepAliveThread() {
			if (os==null && sk != null && !sk.isClosed()) {
				try {
					os = sk.getOutputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void startHeartBeatThread() {
			// ���������߳�
			Timer heartBeatTimer = new Timer();
			TimerTask heartBeatTask = new TimerTask() {
				@Override
				public void run() {
					if (os != null && sk != null && !sk.isClosed()) {
						send(new byte[4]);
					}

				}
			};
			heartBeatTimer.schedule(heartBeatTask, 100000, 100000);
		}
	}
}
