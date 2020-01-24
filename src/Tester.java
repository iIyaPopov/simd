import java.io.FileNotFoundException;
import java.io.IOException;

public class Tester {
	
	public static void main(String[] args) {
		try {
			Simd simd = new Simd();
			simd.getHash("../test2.txt", "../hash.txt");
		} catch (FileNotFoundException err) {
			System.out.println(err);
		} catch (IOException err) {
			System.out.println(err);
		}
	}
}