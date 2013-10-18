package my.util;

public class Calc {
	
	/**
	 * パスカルの三角形を逆算して、番号numに対応する長さn, ハミングウェイトkの２進数を返す(byte形式)
	 * @param n
	 * @param k
	 * @param num
	 * @return
	 */
	public static int countableCode(int n, int k, int num) {
		int code = 0;
		int pascalNum;
		// パスカルの三角形上の座標
		while( !(n<0) ) {
			// 右斜め上に移動
			pascalNum = combination(n-1, k);
//			System.out.println(pascalNum);
			if(pascalNum > num) {
				n--;
			}
			// 左斜め上に移動
			else {
				n--;
				k--;
				num -= pascalNum;
				code = (int) (code + Math.pow(2, n));
			}
		}
		return code;
	}
	
	/**
	 * 底が２のlogの値を返す
	 * @param num
	 * @return
	 */
	public static double log2(double num) {
		return Math.log(num) / Math.log(2);
	}
	
	/**
	 * nの階乗を計算する
	 * @param n
	 * @return
	 */
	public static int factorial(int n) {
		return n <= 1 ? 1 : n * factorial(n-1);
	}
	
	/**
	 * nCkを計算する
	 * @param n
	 * @param k
	 * @return
	 */
	public static int combination(int n, int k) {
		if( n < k ) return 0;
		return factorial(n) / (factorial(k) * factorial(n-k));
	}
	
	/**
	 * numのハミングウェイとを計算する
	 * @param num
	 * @return
	 */
	public static int hamingWeight(int num) {
		int weight = 0;
		while(num != 0) {
			if(num%2 == 1) {
				weight++;
			}
			num /=2;
		}
		return weight;
	}
	
	/**
	 * PSNR値を計算する
	 * @param img1
	 * @param img2
	 * @param offset
	 * @return
	 */
	public static double PSNR(byte[] img1, byte[] img2, int offset) {
		
		if(img1.length != img2.length)
			return -1;
		
		double psnr = 0;
		double mse = 0;
		
		for(int i=offset; i<img1.length; i++) {
			mse += Math.pow(img1[i] - img2[i], 2);
//			Util.print(mse);
		}
		
		mse /= img1.length;
		psnr = mse == 0.0 ? -1 : 10 * Math.log10( Math.pow(255, 2) / mse );
		
		return psnr;
	}
}
