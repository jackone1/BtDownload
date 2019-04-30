package Connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
//UDP���ͽ������ݰ�����
public class UDPConnection {
	//����UDP���ӣ�����Ϊudp��ַ���Լ��˿ں�
	private DatagramSocket s;
	private int receivePort;
	//��ʼ�����ض˿�,�����׽���
	public UDPConnection(int receivePort) {
		this.receivePort = receivePort;
		try {
			this.s = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("�����׽���ʧ�ܣ�����˿��Ƿ�ռ��!");
			e.printStackTrace();
		}
	}
	//�����׽���
	public void setSocket(int receivePort) {
		try {
			this.s = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("�����׽���ʧ�ܣ�����˿��Ƿ�ռ��!");
			e.printStackTrace();
		}
	}
	//�ر��׽���
	public void close() {
		this.s.close();
	}
	//��ȡ�׽��ֶ˿�
	public int getReceivePort() {
		return this.receivePort;
	}
	//�������ݰ�
	public int send(byte[] data,String udp_url,Integer targetPort) {
		//�����ݱ����͵�Ŀ��
		try {
			//�������ݰ�
			DatagramPacket send_data = new DatagramPacket(data,data.length,InetAddress.getByName(udp_url),targetPort);
			//�������ݰ�
			this.s.send(send_data);
		} catch (IOException e) {
			System.out.println("�������ݰ�����!");
			return 4;
		}
		return 0;
	}
	//�������ݰ�
	public byte[] receive() {
		while(true){ 
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
				this.s.receive(packet);
			} catch (IOException e) {
				System.out.println("�������ݰ�����");
				e.printStackTrace();	
			}
            byte[] data = packet.getData();
            System.out.println("���ͷ���IP��ַ:"+packet.getAddress());
            System.out.println("���ͷ��Ķ˿ں�:"+packet.getPort());
            return data;
        }
	}
}
