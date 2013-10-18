package my.util;

public class Util {

	public static <T> void print(T text) {
		System.out.println(text);
	}

	/**
	 * bit番目のbitをLSBにし他を0にして返す
	 * @param pattern
	 * @param bit
	 * @return
	 */
	public static int putByte(int pattern, int bit) {
		return (pattern >>> bit) & 0x00000001;
	}
	
	/**
	 * argbからそれぞれを抽出する
	 * @param argb
	 * @return
	 */
	public static int[] putARGB(int argb) {
		int a = (argb >>> 24) & 0xff;
		int r = (argb >>> 16) & 0xff;
		int g = (argb >>> 8) & 0xff;
		int b = argb & 0xff;
		
		return new int[]{a, r, g, b};
	}
}
