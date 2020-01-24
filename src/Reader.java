import java.io.*;

public class Reader {
	private FileInputStream fis;
	
	public Reader(String filename) throws FileNotFoundException {
		this.fis = new FileInputStream(new File(filename));
	}
	
	public byte[] getBytes(int byteCount) throws IOException {
		byte[] bytes = new byte[byteCount];
		int count = this.fis.read(bytes);
		if (count < byteCount) {
			for (int i = count; i < byteCount; i++) {
				bytes[i] = 0;
			}
		}
		return bytes;
	}
	
	public long getSize() throws IOException {
		return this.fis.available();
	}
}
// CourierNew
// TimesNewRoman