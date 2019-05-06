package Connection;

//�ж������Ƿ���ɵ��߳�
public class CheckCompleteThread extends Thread {
	private byte[] bitField;
	private byte[] complete;

	// ����complete�����������ʾ��ǰ����������
	public CheckCompleteThread(byte[] bitField, byte[] complete) {
		this.bitField = bitField;
		this.complete = complete;
	}

	public void run() {
		while (true) {
			int flag = 0;
			int num = 0;
			for (int i = 40; i < this.bitField.length; i++) {
				if (this.bitField[i] != 1)
					flag = 1;
				else
					num++;
			}
			if (flag == 0) {
				System.out.println("�������!");
				complete[0] = 1;
				break;
			} else {
				System.out.println("���ؽ��� " + 100 * (num * 1.0) / (bitField.length - 40));
			}
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
