package Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import TorrentDownload.DecodeChange;
import function.MyFunction;

public class TCPPeerConnection {
	private  Socket sendSocket = null;
	private  ServerSocket receiveSocket = null;
	private ServerSocket serverSocket = null;
	private int sendPort,receivePort;
	public TCPPeerConnection(String ipAddress,int sendPort,int receivePort) {
		//����tcp����
		this.sendPort = sendPort;
		this.receivePort = receivePort;
		this.sendSocket = new Socket();
		try {
			//�󶨱������Ӷ˿�
			sendSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(),receivePort));
			System.out.println("�����׽��ֳɹ���");
		} catch (IOException e) {
			System.out.println("���ض˿��Ѿ���ռ��!");
		}
		try {
			//����tcp����
			sendSocket.connect(new InetSocketAddress(ipAddress, sendPort));
			System.out.println("�������ӳɹ���");
		} catch (IOException e) {
			System.out.println("����tcp����ʧ��!");
		}
		
		//�������ؼ����˿�
//		try {
//			this.receiveSocket =new ServerSocket(receivePort);
//			System.out.println("�������ؼ����˿� "+receivePort+" �ɹ���");
//		} catch (IOException e) {
//			System.out.println("�������ؼ����˿� "+receivePort+" ʧ�ܣ�");
//		}
	}
	//�����������װ��
	class HandshakePacket{
		private byte pstrlen = (byte)19;
		private byte[] pstr = "BitTorrent protocol".getBytes();
		private byte[] reserved = new byte[8];
		private byte[] infoHash = new byte[20];
		private byte[] peer_id = new byte[20];
		private byte[] packet = new byte[68];
		HandshakePacket(String Hash) {
			peer_id = MyFunction.createPeerId().getBytes();
			infoHash = DecodeChange.HexToByte20(Hash);
			packet[0] = pstrlen;
			MyFunction.byteCopy(pstr, 0, packet, 1, 20);
			MyFunction.byteCopy(reserved, 0, packet, 20, 28);
			MyFunction.byteCopy(infoHash, 0, packet, 28, 48);
			MyFunction.byteCopy(peer_id, 0, packet, 48, 68);
		}
		HandshakePacket(){
			
		}
		public byte[] getPacket() {
			System.out.println("�������ְ�");
			for(byte b:packet) {
				System.out.print(b);
				System.out.print(" ");
			}
			return packet;
		}
		public void setPacket(byte[] packet) {
			this.packet = packet;
		}
	}
	//�������ݰ�
	class SendThread extends Thread{
		private Socket sk;
		private ArrayList<Byte> bitField = new ArrayList<Byte>();
		private ArrayList<Byte> commends = new ArrayList<Byte>();
		private HandshakePacket handShakePacketS;//���͵����ְ�
		private HandshakePacket handShakePacketR;//���ص����ְ�
	    SendThread(Socket sk,String hash)
	    {
	        this.sk=sk;
	        //��ʼ�����������
	        this.handShakePacketS = new HandshakePacket(hash);
	        this.handShakePacketR = new HandshakePacket();
	    }
	    
		public void run() {
            try {
                OutputStream os = this.sk.getOutputStream();
                os.write(this.handShakePacketS.getPacket());
                os.flush();
                int ret;
                int index=0;
                int length = 0;
                this.sk.setSoTimeout(3000);
        		InputStream is = this.sk.getInputStream();
        		System.out.println("������Ϣ");
        		byte[] handshakeTemp = new byte[68];
        		
                try {
        			while((ret=is.read())!=-1) {
        				if(index < 68) {
        					handshakeTemp[index] = (byte)ret;
        				}else if(index >=68 && index <=71){
        					length += Math.pow(256,71-index)*ret;
        				}else if(index>=72 && index <72+length){
        					bitField.add((byte)ret);
        				}else {
        					commends.add((byte)ret);
        				}
        				System.out.print(ret);
    	                System.out.print(" ");
    	                index++;
        			   }
        			this.handShakePacketR.setPacket(handshakeTemp);
        		} catch (Exception e) {
        			System.out.println("��ȡ����!");
        		}
                finally {
        			is.close();
        		}  
                System.out.print("\nhandshake:");
	            for(byte b:handshakeTemp) {
	                System.out.print(b);
	                System.out.print(" ");
	            }
                System.out.print("\nbitField:");
	            for(byte b:bitField) {
	                System.out.print(b);
	                System.out.print(" ");
	              }
	            System.out.print("\nunkonwndata:");
	            for(byte b:commends) {
	                System.out.print(b);
	                System.out.print(" ");
	              }
	            System.out.println("�����ܳ�:"+index);
	            System.out.println("�����ܳ�:"+handshakeTemp.length +" "+bitField.size()+" "+commends.size());
            } catch (IOException e) {
            	System.out.println("��Ϣ����ʧ��!");
                e.printStackTrace();
            }
        }
	}
	
	//���������Ϣ��
	class ServerThread extends Thread
	{
	    private Socket sk;
	    ServerThread(Socket sk)
	    {
	        this.sk=sk;
	    }
	    //�߳�����ʵ��
	    public void run() {
	    	byte[] data;
	    	InputStream is = null;
	    	OutputStream os = null;
	        try {
	        	//socket������ӿ�
	            is = sk.getInputStream();
	            //socket������ӿ�
	            os = sk.getOutputStream();
	            //������Ϣ
	            while(true)
	            {
	                int ret;
	                int index = 0;
	                data = new byte[2048];
	                while((ret=is.read())!=-1) {
	                	data[index] = (byte)ret;
	                	index++;
	                }
	                for(byte b:data) {
	                	System.out.print(b);
	                	System.out.print(" ");
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        finally{
	            try {
	                if (is!=null) {
	                    is.close();
	                }
	                if (os!=null) {
	                    os.close();
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
	
	//��Ϊ���շ�ʱ
	public void peerReceive() {
		//���ն˿���Ŀ
		int count = 0; 
        //����socket����
        Socket socket = null;
        while (this.receiveSocket!=null) {
            try {
				socket = receiveSocket.accept();
			} catch (IOException e) {
				System.out.println("���������󣬴����µ�socket�˿ڣ�");
			}
            //�ֳ�һ���̴߳�����������
            ServerThread serverThread = new ServerThread(socket);
            System.out.println("client host address is: " + socket.getInetAddress().getHostAddress());
            serverThread.start();
            count++;
            System.out.println("now client count is: " + count);
        }
	}
	//������������
	public void peerHandshake(String hash) {
		//�׽��ֽ���
		if (sendSocket != null && sendSocket.isConnected()) {
            new SendThread(this.sendSocket,hash).start();
        }
	}
	
}

