package my.util;

public class Util {

	private static final int MASK = 0x0000000000000000000000000000000000000000000000000000000000000001;
	/**
	 * System.out.print
	 * @param text
	 */
	public static <T> void print(T text) {
		System.out.print(text);
	}
	
	/**
	 * System.out.println
	 * @param text
	 */
	public static <T> void println(T text) {
		System.out.println(text);
	}
	
	/**
	 * System.out.println
	 * @param text
	 */
	public static void print(String format, Object...args) {
		System.out.print(String.format(format, args));
	}
	
	/**
	 * System.out.println
	 * @param text
	 */
	public static void println(String format, Object...args) {
		System.out.println(String.format(format, args));
	}
	
	/**
	 * エラーパターンをメッセージに変換する
	 * @param n
	 * @param code
	 * @return
	 */
	public static int error2Message(int n, int[] code) {
		// ハミングウェイトを計算
		int weight = hamingWeight(code[0]) + hamingWeight(code[1])
				+ hamingWeight(code[2]) + hamingWeight(code[3])
				+ hamingWeight(code[4]) + hamingWeight(code[5])
				+ hamingWeight(code[6]) + hamingWeight(code[7]),
			offset = calcOffset(n, weight);
		
		return antiCountableCode(n, weight, code) + offset;
	}
	
	
	/**
	 * メッセージをエラーパターンに変換する
	 * @param msg
	 * @param n
	 * @return
	 */
	public static int[] message2Error(int msg, int n) {
		int[] info = calcErrorWeightAndNum(msg, n);
		return countableCode(n, info[0], info[1]);
	}
	
	
	public static int[] calcErrorWeightAndNum(int num, int n) {
		int i, combNum;
		for(i=0; i<=n; i++) {
			combNum = (int) Calc.combination(n, i);
			if( num < combNum ) break;
			num -= Calc.combination(n, i);
		}
		return new int[]{i, num};
	}
	
	/**
	 * nC0 〜 nCk-1までの合計値（数え上げ符号のoffset）を計算する
	 * @param n
	 * @param k
	 * @return
	 */
	public static int calcOffset(int n, int k) {
		int offset = 0;
		for(int i=0; i<k; i++) {
			offset += Calc.combination(n, i);
		}
		return offset;
	}
	
	
	
	/**
	 * nビットの誤りパターンepから各ビットを取り出して配列として返す
	 * @param code
	 * @param n
	 * @return
	 */
	public static byte[] extractErrorPutternPerPix(int[] code, int n) {
		byte[] eppArray = new byte[n];
		for(int i=0; i<n; i++) {
			eppArray[i] = extractByte(code, i);
		}
		return eppArray;
	}
	
	
	/**
	 * n番目のbitをLSBにし他を0にして返す
	 * @param epp
	 * @param n
	 * @return
	 */
	public static byte extractByte(int code, int n) {
		return (byte) ((code >>> n) & MASK);
	}
	
	
	/**
	 * n番目のbitをLSBにし他を0にして返す
	 * @param epp
	 * @param n
	 * @return
	 */
	public static byte extractByte(int[] code, int n) {
		int index = n/32;
		int shiftBit = n%32;
		return (byte) ((code[index] >>> shiftBit) & MASK);
	}
	
	
	/**
	 * ステゴデータから埋め込みデータを抽出して返す
	 * @param stego
	 * @param cover
	 * @param start
	 * @param n
	 * @return
	 */
	public static int[] extractErrorPattern(byte[] stego, byte[] cover, int start, int n) { 
		byte eBit;
		int[] code = new int[8];
		for(int i=start; i<start+n; i++) {
			// 誤りビットを抽出する
			eBit = (byte) ((stego[i] ^ cover[i]) & 0x01);
			// とりだしたビットが1だったときに対応するビットを立てる
			if( eBit == 0x01) Util.raiseBit(code, n - 1 - (i - start));
		}
		return code;
	}
	
	
	/**
	 * パスカルの三角形を逆算して、番号numに対応する長さn, ハミングウェイトkの２進数を返す(byte形式)
	 * @param n
	 * @param k
	 * @param num
	 * @return
	 */
	public static int[] countableCode(int n, int k, long num) {
		int[] code = new int[8];
		long pascalNum;
		
		while( !(n < 0) ) {
			// 右斜め上の値を計算
			pascalNum = Calc.combination(n, k);
			
			if(pascalNum <= num) {
				k--;
				num -= pascalNum;
				raiseBit(code, n);
			}
			
			n--;
		}
		return code;
	}
	
	
	/**
	 * パスカルの三角形を利用して数え上げ符号を対応する値に変換する
	 * @param n
	 * @param k
	 * @param code
	 * @return
	 */
	public static int antiCountableCode(int n, int k, int[] code) {
		int num = 0;
		
		byte bit;
		for(int j=n-1; j>=0; j--) {
			// i番目のビットを取り出す
			bit = Util.extractByte(code, j);
			// もしLSBが１なら左斜め上に移動
			if( bit == 0x01 ) {
				// 右斜め上の数値を記録する
				num += Calc.combination(j, k);
				k--;
			} 
		}
		return num;
	}
	
	/**
	 * codeのn番目のビットを1にする
	 * @param code
	 * @param n
	 */
	
	// codeのnビット目を１にする
	public static void raiseBit(int[] code, int n) {
		// 配列のインデックス番号を計算
		int index = n/32;
		// code[index]中で何ビットずらすのかを計算
		int shiftNum = n%32;
		int mask = (MASK << shiftNum);
		code[index] = code[index] | mask;
	}
	
	
	/**
	 * numのハミングウェイトを計算する
	 * @param num
	 * @return
	 */
	public static int hamingWeight(int num) {
		int weight = 0;
		for(int i=0; i<32; i++) {
			if( Util.extractByte(num, i) == 0x01 ) weight++;
		}
		return weight;
	}
}
