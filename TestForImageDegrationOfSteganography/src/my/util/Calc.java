package my.util;

import java.math.BigInteger;

public class Calc {
	
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
	public static long factorial(int n) {
		return n <= 1 ? 1 : n%2 == 0 ? n/2 * factorial(n-1) : n * factorial(n-1);
	}
	
	/**
	 * n!/k!を効率よく計算する
	 * @param n
	 * @return
	 */
	public static long factorial(int n, int k, int evNum) {
		if(n==k) 
			return 1;
		else {
			if( n%2 == 0 && evNum > 0) {
				return n/2 * factorial(n-1, k, evNum-1);
			}
			else {
				return n * factorial(n-1, k, evNum);
			}
		}
	}
	
	/**
	 * nCkを計算する
	 * @param n
	 * @param k
	 * @return
	 */
	public static long combination(int n, int k) {
		if( n < k ) return 0;
		else if( k==0 ) return 1;
		
		boolean nf = n%2 == 0,
				kf = k%2 == 0;
		
		long num1, num2;
		
		if( n/k > 2 ) {
			int evNum = k/2;
			num1 = factorial(n, n-k, evNum);
			num2 = factorial(k);
		} else {
			int evNum = (n-k)/2;
			num1 = factorial(n, k, evNum);
			num2 = factorial(n-k);
		}
		
		return num1/num2;
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
		
		mse /= (img1.length - offset);
		psnr = mse == 0.0 ? -1 : 10 * Math.log10( Math.pow(255, 2) / mse );
		
//		Util.println("mse:%f psnr:%f", mse, psnr);
		
		return psnr;
	}
}
