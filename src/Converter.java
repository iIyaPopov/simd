public class Converter {
	private static final int MASK = 0xff;
	private static final int INT_BYTE_COUNT = 4;
	
	public static int combine(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < INT_BYTE_COUNT; i++) {
			result += ((int) bytes[i] & MASK) << (Byte.SIZE * i);
		}
		return result;
	}
	
	public static int[] byte2int(byte[] x) {
		byte[] tmp = new byte[4];
		int[] res = new int[x.length / 4];
		int k = 0;
		for (int i = 0; i <= x.length - 4; i = i + 4) {
			tmp[0] = x[i];
			tmp[1] = x[i+1];
			tmp[2] = x[i+2];
			tmp[3] = x[i+3];
			res[k++] = combine(tmp);
		}
		return res;
	}
	
	public static byte[] int2byte(int[] x) {
		byte[] res = new byte[x.length * 4];
		int k = 0;
		for (int i = 0; i < x.length; i++) {
			res[k++] = (byte) (x[i] & 0xff);
			x[i] = x[i] >>> 8;
			res[k++] = (byte) (x[i] & 0xff);
			x[i] = x[i] >>> 8;
			res[k++] = (byte) (x[i] & 0xff);
			x[i] = x[i] >>> 8;
			res[k++] = (byte) (x[i] & 0xff);
		}
		return res;
	}
}