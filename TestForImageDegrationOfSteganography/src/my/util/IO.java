package my.util;

public class IO {
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
	
}
