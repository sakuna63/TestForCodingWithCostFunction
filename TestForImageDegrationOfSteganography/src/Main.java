import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * US-ASCIIを用いることを前提にコードを書く
 * @author sakuna63
 *
 */
public class Main {
	public static void main(String[] args) {
		String text = getRandomText(256);
		System.out.println( text );
    }

	private static HashMap<Byte, Byte> getCodeTable() {
		HashMap<Byte, Byte> table = new HashMap<Byte, Byte>();
		
		
		return table;
	}
	
	/**
	 * 数え上げ符号を返す
	 * @param code_lenght
	 * @param code_weight
	 * @param num
	 * @return
	 */
	private static byte countableCode(int code_lenght, int code_weight, int num) {
		byte code = 0;
		// Step1
		// ハミングウェイトを計算
		int weight = hamingWeight(num);
		int step1_length = p
		return code;
	}
	
	/**
	 * 底が２のlogの値を返す
	 * @param num
	 * @return
	 */
	private static double log2(double num) {
		return Math.log(num) / Math.log(2);
	}
	
	private static int factorial(int n) {
		return n <= 1 ? 1 : n * factorial(n-1);
	}
	
	private static int combination(int n, int k) {
		return factorial(n) / (factorial(k) * factorial(n-k));
	}
	
	private static int hamingWeight(int num) {
		int weight = 0;
		while(num != 0) {
			if(num%2 == 1) {
				weight++;
			}
			num /=2;
		}
		return weight;
	}

	private static String getRandomText(int textNum) {
		byte[] randByteArray = getRandomTextByte(textNum);
		String text = null;
		try {
			text = new String(randByteArray, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return text;
    }

	private static byte[] getRandomTextByte(int textNum) {
		Sfmt s = new Sfmt(1);
		Integer num;
		byte byte_num;
		byte[] randByteArray = new byte[textNum];
	
		for( int i=0; i<textNum; i++) {
		    // null文字などを避ける
		    num = s.NextInt(95) + 32;
		    byte_num = num.byteValue();
		    randByteArray[i] = byte_num;
		}
		return randByteArray;
    }
	
}
