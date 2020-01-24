import java.io.*;
import java.util.*;
import java.math.*;
/*for (int k = 0; k < 16; k++) {
				System.out.println(Integer.toHexString(s[k]));
			}
			System.out.println();*/

public class Simd {
		
	public void getHash(String filename, String output) throws IOException, FileNotFoundException {
		Reader reader = new Reader(filename);
		long fileSize = reader.getSize();
		int msgBlockCount = (int) fileSize / Constants.MSG_BLOCK_SIZE;
		if (fileSize % Constants.MSG_BLOCK_SIZE != 0) {
			msgBlockCount++;
		}
		byte[] iv = getIV();
		int[] s = Converter.byte2int(iv);
		for (int i = 0; i < msgBlockCount; i++) {
			byte[] msg = reader.getBytes(Constants.MSG_BLOCK_SIZE);
			s = compression(s, msg, 0);
		}
		long capacity = fileSize * Byte.SIZE;
		byte[] capacityBytes = HelpFunctions.getCapacityBytes(capacity);
		s = compression(s, capacityBytes, 1);
		HelpFunctions.writeHashIntoFile(output, s);
	}
	
	private byte[] getIV() throws IOException, FileNotFoundException {
		byte[] iv = new byte[Constants.MSG_BLOCK_SIZE];
		FileInputStream fis = new FileInputStream(new File(Constants.INIT_VECTOR_FILENAME));
		fis.read(iv);
		fis.close();
		return iv;
	}
	
	private int[] compression(int[] initVector, byte[] m, int f) {
		int[][] w = messageExpansion(m, f);		
		int[] msg = Converter.byte2int(m);
		int[] s = HelpFunctions.xor(initVector, msg);
		
		//steps: 0...31
		int[] subPi = new int[4];
		for (int i = 0; i < 4; i++) {
			subPi = Constants.PI[i];
			s = round(s, w, i, subPi);
		}
		
		//steps: 32...35
		int[] inputVector = new int[4];
		int nStep = Constants.STEPS_COUNT;
		subPi = Constants.PI[3];
		for (int i = 0; i < 4; i++) {
			inputVector[0] = initVector[4*i];
			inputVector[1] = initVector[4*i+1];
			inputVector[2] = initVector[4*i+2];
			inputVector[3] = initVector[4*i+3];
			s = step(s, inputVector, Constants.IF, subPi[i], subPi[(i+1) % 4], nStep);
			nStep++;
		}

		return s;
	}
	
	private int[] round(int[] s, int[][] w, int i, int[] pi) {
		s = step(s, w[8*i],   Constants.IF,  pi[0], pi[1], 8*i);
		s = step(s, w[8*i+1], Constants.IF,  pi[1], pi[2], 8*i+1);
		s = step(s, w[8*i+2], Constants.IF,  pi[2], pi[3], 8*i+2);
		s = step(s, w[8*i+3], Constants.IF,  pi[3], pi[0], 8*i+3);
		s = step(s, w[8*i+4], Constants.MAJ, pi[0], pi[1], 8*i+4);
		s = step(s, w[8*i+5], Constants.MAJ, pi[1], pi[2], 8*i+5);
		s = step(s, w[8*i+6], Constants.MAJ, pi[2], pi[3], 8*i+6);
		s = step(s, w[8*i+7], Constants.MAJ, pi[3], pi[0], 8*i+7);
		return s;
	}
	
	private int[] step(int[] s, int[] w, int mode, int pi1, int pi2, int nStep) {
		int[] res = new int[16];
		long mod = 0x100000000L;
		for (int i = 0; i < 4; i++) {
			if (mode == Constants.IF) {
				long tmp = s[12+i] + w[i] + IF(s[i], s[4+i], s[8+i]);
				res[i] = (int) (tmp % mod);
			} else if (mode == Constants.MAJ) {
				long tmp = s[12+i] + w[i] + MAJ(s[i], s[4+i], s[8+i]);
				res[i] = (int) (tmp % mod);
			}
			res[i] = Integer.rotateLeft(res[i], pi2);
			int index = -1;
			switch (nStep % 3) {
				case 0:
					index = i ^ 1;
					break;
				case 1:
					index = i ^ 2;
					break;
				case 2:
					index = i ^ 3;
					break;
			}
			res[i] = (int) ((res[i] + Integer.rotateLeft(s[index], pi1)) % mod);
		}
		for (int i = 4; i < 8; i++) {
			res[i] = Integer.rotateLeft(s[i-4], pi1);
		}
		for (int i = 8; i < 16; i++) {
			res[i] = s[i-4];
		}
		return res;		
	}
	
	private int IF(int a, int b, int c) {
		return (a & b) | (~a & c);
	}
	
	private int MAJ (int a, int b, int c) {
		return (a & b) | (a & c) | (b & c);
	}
	
	private int[][] messageExpansion(byte[] m, int f) {
		int[] y;
		if (f == 0) {
			y = ntt(m, 0, 1);
		} else {
			y = ntt(m, 1, 1);
		}
		
		int[][] z = new int[32][4];
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 4; j++) {
				z[i][j] = zCalculation(i, j, y);
			}
		}
		int[][] w = new int[32][4];
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 4; j++) {
				w[i][j] = z[Constants.Z_PERMUTATION[i]][j];
			}
		}
		return w;
	}
	
	private int[] ntt(byte[] x, int x125, int x127) {
		int alpha = 139;	//it's a 128th root of unity in F(257)
		int resultByteCount = 128;
		int inputByteCount = 64;
		int modul = 257;
		int[] y = new int[resultByteCount];
		for (int i = 0; i < resultByteCount; i++) {
			int tmp = 0;
			for (int j = 0; j < inputByteCount; j++) {
				int xEl = ((int) x[j]) & 0xff;
				int a = HelpFunctions.modulPow(alpha, i * j, modul);
				tmp += (xEl * a) % modul;
			}
			if (x125 == 1) {
				tmp = (tmp + HelpFunctions.modulPow(alpha, 125 * i, modul)) % modul;
			}
			if (x127 == 1) {
				tmp = (tmp + HelpFunctions.modulPow(alpha, 127 * i, modul)) % modul;
			}
			if (tmp < 0) {
				tmp += modul;
			}
			y[i] = tmp;
		}
		return y;
	}
	
	private int zCalculation(int i, int j, int[] y) {
		int index1;
		int index2;
		int coef = 8*i + 2*j;
		if (i >= 0 && i <= 15) {
			index1 = coef;
			index2 = coef + 1;
			return I_coef(y[index1], y[index2], Constants.I_185);
		}
		if (i >= 16 && i <= 23) {
			index1 = coef - 128;
			index2 = coef - 64;
			return I_coef(y[index1], y[index2], Constants.I_233);
		}
		if (i >= 24 && i <= 31){
			index1 = coef - 191;
			index2 = coef - 127;
			return I_coef(y[index1], y[index2], Constants.I_233);
		}
		return -1;
	}
	
	private int I_coef(int x, int y, int coef) {
		return (I_coef(x, coef) & 0xffff) + ((I_coef(y, coef) & 0xffff) << 16);
	}
	
	private int I_coef(int x, int coef) {
		int result;
		int modul = 257;
		int tmp = x % modul;
		if (tmp > 128) {
			tmp -= modul;
		} else if (tmp < -128) {
			tmp += modul;
		}
		int multiModul = 65536;
		return (coef * tmp) % multiModul;
	}	
	
}