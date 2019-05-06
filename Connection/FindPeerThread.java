package Connection;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

//Ѱ��Peer���߳�
public class FindPeerThread extends Thread {
	// ��ǰ����ip
	private JSONObject use = null;
	// �������ӹ������õ�ip
	private JSONObject unuse = null;
	// tracker��ַ�б�
	private JSONArray announce = null;
	// hash������
	private String hash = null;
	// ��ǰʹ�õ�����
	private ArrayList<Socket> sockets = null;

	public FindPeerThread(JSONObject use, JSONObject unuse, JSONArray announce, String hash,
			ArrayList<Socket> sockets) {
		this.use = use;
		this.unuse = unuse;
		this.hash = hash;
		// ��ȡannounce
		this.announce = announce;
		this.sockets = sockets;
	}

	// �ж��Ƿ������
	private Boolean canConnect(String ip, int port) {
		try {
			// ��������ӿ����ӣ��򽫸����Ӽ���list��
			Socket socket = new Socket(ip, port);
			this.sockets.add(socket);
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void udpfind() {
		UDPTrackerTransfor udpTf = new UDPTrackerTransfor(10000, this.hash);
		for (int i = 0; i < this.announce.size(); i++) {
			String url = this.announce.getString(i);
			if (url.matches("udp")) {
				byte[] bytes = udpTf.setUpLink(url);
				JSONObject ipAndPort = udpTf.startAnnounceRequest();
				Iterator it = ipAndPort.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					int port = ipAndPort.getInt(key);
					if (!canConnect(key, port)) {
						this.unuse.put(key, port);
					} else {
						this.use.put(key, port);
					}
				}
			}
		}
	}

	public void run() {
		while (true) {
			if (sockets != null) {
				// ��������ӶϿ��� �����use���Ƴ�����unuse�м���
				for (int i = 0; i < sockets.size(); i++) {
					if (!sockets.get(i).isConnected()) {
						sockets.remove(i);
					}
				}
			}
			udpfind();
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
