package my.util;

import java.util.HashMap;

public class Util {

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
	 * @param ep
	 * @return
	 */
	public static int error2Message(int n, long[] ep) {
		// ハミングウェイトを計算
		int weight = hamingWeight(ep[0]) + hamingWeight(ep[1])
				+ hamingWeight(ep[2]) + hamingWeight(ep[3]);
		return antiCountableCode(n, weight, ep);
	}
	
	/**
	 * 0~maxまでの数値に対応したnビット誤りパターンテーブルを返す
	 * @param n
	 * @param max
	 * @return
	 */
	public static long[][] errorPatternTable(int n, int max) {
		long[][] table = new long[max][4];
		int index = 0, combNum;
		
		// ハミングウェイトを変化させる
		for(int i=0; i<=n; i++) {
			// nCiを計算	
			combNum = Calc.combination(n, i);
			// 0~nCiまでに対応するnビットでハミングウェイとiのパターンを決定する
			for(int j=0; j<combNum; j++) {
				table[index] = countableCode(n, i, j);
				index++;
				if(index > max) break;
			}
			if(index > max) break;
		}
		return table;
	}
	
	/**
	 * 逆テーブルを生成する
	 * @param table
	 * @return
	 */
	public static HashMap<long[], Integer> antiTable(long[][] table) {
		HashMap<long[], Integer> antiTable = new HashMap<long[], Integer>();
		int index = 0;
		
		for(long[] item : table) {
			antiTable.put(item, index);
			index++;
		}
		
		return antiTable;
	}
	
	/**
	 * nビットの誤りパターンepから各ビットを取り出して配列として返す
	 * @param ep
	 * @param n
	 * @return
	 */
	public static byte[] extractErrorPutternPerPix(long[] ep, int n) {
		byte[] eppArray = new byte[n];
		for(int i=0; i<n; i++) {
			eppArray[i] = extractByte(ep, i);
		}
		return eppArray;
	}
	
	/**
	 * n番目のbitをLSBにし他を0にして返す
	 * @param epp
	 * @param n
	 * @return
	 */
	public static byte extractByte(long epp, int n) {
		return (byte) ((epp >>> n) & 0x0000000000000000000000000000000000000000000000000000000000000001);
	}
	
	/**
	 * n番目のbitをLSBにし他を0にして返す
	 * @param epp
	 * @param n
	 * @return
	 */
	public static byte extractByte(long[] epp, int n) {
		int index = n/64;
		int shiftBit = n%64;
		return (byte) ((epp[index] >>> shiftBit) & 0x0000000000000000000000000000000000000000000000000000000000000001);
	}
	
	/**
	 * ステゴデータから埋め込みデータを抽出して返す
	 * @param stego
	 * @param cover
	 * @param start
	 * @param n
	 * @return
	 */
	public static long[] extractErrorPattern(byte[] stego, byte[] cover, int start, int n) { 
		byte eBit;
		long[] code = new long[4];
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
	public static long[] countableCode(int n, int k, int num) {
		long[] code = new long[4];
		int pascalNum;
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
	
	public static int antiCountableCode(int n, int k, long[] code) {
		int num = 0, offset = 0;
		
		for(int i=0; i<k; i++) {
			offset += Calc.combination(n, i);
		}
		
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
		return num + offset;
	}
	
	// codeのnビット目を１にする
	public static void raiseBit(long[] code, int n) {
		// 配列のインデックス番号を計算
		int index = n/64;
		if( index < 0 || index >= code.length )
			Util.print("???");
		// code[index]中で何ビットずらすのかを計算
		int shiftNum = n%64;
		long mask = 0x0000000000000000000000000000000000000000000000000000000000000001 << shiftNum;
		code[index] = code[index] | mask;
	}
	
	/**
	 * numのハミングウェイトを計算する
	 * @param num
	 * @return
	 */
	public static int hamingWeight(long num) {
		int weight = 0;
		for(int i=0; i<64; i++) {
			if( Util.extractByte(num, i) == 0x01 ) weight++;
		}
		return weight;
	}
}
