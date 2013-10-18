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
		System.out.println(String.format(format, args));
	}

	/**
	 * 逆テーブルを生成する
	 * @param table
	 * @return
	 */
	public static HashMap<Integer, Integer> aitiTable(int[] table) {
		HashMap<Integer, Integer> antiTable = new HashMap<Integer, Integer>();
		int index = 0;
		
		for(int item : table) {
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
	public static byte[] extractErrorPutternPerPix(int ep, int n) {
		byte[] eppArray = new byte[n];
		for(int i=0; i<n; i++) {
			eppArray[i] = extractByte(ep, i);
		}
		return eppArray;
	}
	
	/**
	 * bit番目のbitをLSBにし他を0にして返す
	 * @param epp
	 * @param bit
	 * @return
	 */
	public static byte extractByte(int epp, int bit) {
		return (byte) ((epp >>> bit) & 0x00000001);
	}
	
	/**
	 * argbからそれぞれを抽出する
	 * @param argb
	 * @return
	 */
	public static int[] extractARGB(int argb) {
		int a = (argb >>> 24) & 0xff;
		int r = (argb >>> 16) & 0xff;
		int g = (argb >>> 8) & 0xff;
		int b = argb & 0xff;
		
		return new int[]{a, r, g, b};
	}
	
	/**
	 * 誤りパターン埋め込み後のピクセル値を返す
	 * @param rgb
	 * @param epp
	 */
	public static int embededPix(int rgb, int epp) {
		int[] argb = extractARGB(rgb);
		argb[1] = argb[1] ^ epp;
		argb[2] = argb[2] ^ epp;
		argb[3] = argb[3] ^ epp;
		return argb[0] << 24 | argb[1] << 16 | argb[2] << 8 | argb[3];
	}
}
