import java.io.*;

public class HelpFunctions {
	
	public static byte[] getCapacityBytes(long x) {
		byte[] bytes = new byte[Constants.MSG_BLOCK_SIZE];
		int mask = 0xff;
		for (int i = 0; i < 8; i++) {
			byte tmp = (byte) (x & mask);
			bytes[i] = tmp;
			x = x >>> Byte.SIZE;
		}
		return bytes;
	}	
	
	public static int[] xor(int[] a, int[] b) {
		int[] res = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] ^ b[i];
		}
		return res;
	}
	
	public static void writeHashIntoFile(String filename, int[] s) throws IOException, FileNotFoundException {
		FileOutputStream fos = new FileOutputStream(new File(filename));
		int size = s.length / 2;
		int[] half = new int[size];
		for (int i = 0; i < size; i++) {
			half[i] = s[i];
		}
		byte[] hash = Converter.int2byte(half);
		fos.write(hash);
		fos.close();
	}
	
	public static int modulPow(int a, int b, int modul) {
		int res = 1;
		for (int i = 0; i < b; i++) {
			res = (res * a) % modul;
		}
		return res;
	}
}