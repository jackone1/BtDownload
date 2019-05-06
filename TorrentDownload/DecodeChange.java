package TorrentDownload;

import java.io.UnsupportedEncodingException;

public class DecodeChange {
	// ���ַ���ת��Ϊ16λ�ַ���,�ȼ���php��pack
	public static String toStringHexTest(String inHex) {
		byte[] hexBytes = new byte[inHex.length() / 2];
		int index = 0;
		char[] inHexChr = inHex.toCharArray();
		for (int i = 0; i < inHex.length(); i = i + 2) {
			int n = Integer.parseInt(inHex.substring(i, i + 2), 16);
			hexBytes[index] = (byte) (n & 0xff);
			index++;
		}
		String result = null;
		try {
			result = new String(hexBytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] HexToByte20(String inHex) {
		byte[] hexBytes = new byte[20];
		int index = 0;
		while (inHex.length() < 40) {
			inHex = "0" + inHex;
		}
		for (int i = 0; i < inHex.length(); i = i + 2) {
			int n = Integer.parseInt(inHex.substring(i, i + 2), 16);
			hexBytes[index] = (byte) (n & 0xff);
			index++;
		}
		return hexBytes;
	}

	public static byte[] HexToByte8(String inHex) {
		byte[] hexBytes = new byte[8];
		int index = 0;
		while (inHex.length() < 16) {
			inHex = "0" + inHex;
		}
		for (int i = 0; i < inHex.length(); i = i + 2) {
			int n = Integer.parseInt(inHex.substring(i, i + 2), 16);
			hexBytes[index] = (byte) (n & 0xff);
			index++;
		}
		return hexBytes;
	}

	// ��һ���ֽ�ת��Ϊ8λ0��1����
	public static byte[] bytesToBinary(byte[] bytes) {
		// byteתint
		byte[] binary = new byte[bytes.length * 8];
		int index = 0;
		for (byte b : bytes) {
			int temp = b & 0xff;
			for (int i = 0; i < 8; i++) {
				binary[index++] = (byte) (temp >> (7 - i) & 1);
			}
		}
		return binary;
	}

	public static byte[] binaryToBytes(byte[] bytes) {
		// byteתint
		byte[] binary = new byte[bytes.length / 8];
		int index = 0;
		int k = 0;
		for (int i = 0; i < bytes.length; i++) {

			binary[index] += bytes[i] * Math.pow(2, 7 - k);
			k++;
			if (k == 8) {
				k = 0;
				index++;
			}
		}
		return binary;
	}

	public static byte[] intToByteArray(int a) {
		return new byte[] { (byte) ((a >> 24) & 0xFF), (byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF),
				(byte) (a & 0xFF) };
	}

	public static int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
	}
}
