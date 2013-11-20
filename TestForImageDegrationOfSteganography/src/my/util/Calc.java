package my.util;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

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
	
	public static double average(byte[] nums) {
	    double sum = 0.0;
	    
	    for(byte num : nums) {
	        // byteだと-128~128なのでintにキャストして合計を計算する
	        sum += num & 0x000000ff; 
	    }
	    
	    return sum / nums.length;
	}
	
	public static double despersion(byte[] nums) {
	    double sum_m2 = 0.0;
	    double ave = average(nums);
	    
	    for(byte num : nums) {
	        sum_m2 += Math.pow(num & 0x000000ff, 2);
	    }
	    
	    
	    return sum_m2 / nums.length - ave * ave;
	}
	
	public static double standardDivision(byte[] nums) {
        return Math.sqrt(despersion(nums));
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
		}
		
		mse /= (img1.length - offset);
		psnr = mse == 0.0 ? -1 : 10 * Math.log10( Math.pow(255, 2) / mse );
		
		return psnr;
	}
	
	/**
	 * 
	 * @author sakuna63
	 * 参考：http://pendientedemigracion.ucm.es/info/fismed/SSIM_family/SSIM_archivos/frame.htm
	 */
	public static class SOption {
		private static final int BITS_PER_PIX = 8;
		private static final double K1 = 0.01; 			/** SSIMindexの計算時の不安定さを避けるための定数C1のこと **/
		private static final double K2 = 0.03;			/** SSIMindexの計算時の不安定さを避けるための定数C2のこと **/
		
		public double sigma_gauss = 1.5;	/** ガウシアンフィルタを掛ける際に使う偏差値 **/
		public int filter_width = 11;		/** フィルタのサイズ。だいたい7~15 **/
		public double C1;
		public double C2;

		
		public SOption() {
			C1 = (Math.pow(2, BITS_PER_PIX) - 1)*K1;
			C1= C1*C1;
			C2 = (Math.pow(2, BITS_PER_PIX) - 1)*K2;
			C2=C2*C2;
		}
		
		public SOption(double sigma_gauss, int filter_width, double K1, double K2) {
			this.sigma_gauss = sigma_gauss;
			this.filter_width = filter_width;
			
			C1 = (Math.pow(2, BITS_PER_PIX) - 1)*K1;
			C1= C1*C1;
			C2 = (Math.pow(2, BITS_PER_PIX) - 1)*K2;
			C2=C2*C2;
		}
	}
	
	public static double SSIM(SOption opt, byte[] stego, byte[] cover, int offset) {
		int pointer;
		int filter_length = opt.filter_width * opt.filter_width;
		float window_weights [] = new float [filter_length];
		double[] array_gauss_window = new double [filter_length];
		
		/**
		 * フィルター作成処理
		 */
		double distance = 0;
		int center = (opt.filter_width/2);
  		double total = 0;
		double sigma_sq=opt.sigma_gauss*opt.sigma_gauss;
		
      	  	for (int y = 0; y < opt.filter_width; y++){
			for (int x = 0; x < opt.filter_width; x++){
         				distance = Math.abs(x-center)*Math.abs(x-center)+Math.abs(y-center)*Math.abs(y-center);
				pointer = y*opt.filter_width + x;
                			array_gauss_window[pointer] = Math.exp(-0.5*distance/sigma_sq);
				total = total + array_gauss_window[pointer];
  			}
    		}
		for (pointer=0; pointer < filter_length; pointer++) {	
			array_gauss_window[pointer] = array_gauss_window[pointer] / total;
			window_weights [pointer] = (float) array_gauss_window[pointer];
		}
		
		/**
		 * SSIM計算
		 */
		int image_height = 256;
		int image_width = 256;
		int image_dimension = image_width*image_height;
	
		ImageProcessor mu1_ip = new FloatProcessor (image_width, image_height);
		ImageProcessor mu2_ip = new FloatProcessor (image_width, image_height);
		float [] array_mu1_ip = (float []) mu1_ip.getPixels();
		float [] array_mu2_ip = (float []) mu2_ip.getPixels();
	
		float [] array_mu1_ip_copy = new float [image_dimension];
		float [] array_mu2_ip_copy = new float [image_dimension];
	
		int a,b;
		for (pointer = offset; pointer<stego.length; pointer++) {	
	
			a = (0xff & cover[pointer]);
			b = (0xff & stego[pointer]);

			array_mu1_ip [pointer - offset] = array_mu1_ip_copy [pointer - offset] = a; // Float.intBitsToFloat(a);
			array_mu2_ip [pointer - offset] = array_mu2_ip_copy [pointer - offset] = b; //Float.intBitsToFloat(b);
		}
		mu1_ip.convolve (window_weights, opt.filter_width, opt.filter_width);
		mu2_ip.convolve (window_weights, opt.filter_width, opt.filter_width);
	
		double [] mu1_sq = new double [image_dimension];
		double [] mu2_sq = new double [image_dimension];
		double [] mu1_mu2 = new double [image_dimension];
	
		for (pointer =0; pointer<image_dimension; pointer++) {
			mu1_sq[pointer] = (double) (array_mu1_ip [pointer]*array_mu1_ip [pointer]);
			mu2_sq[pointer] = (double) (array_mu2_ip[pointer]*array_mu2_ip[pointer]);
			mu1_mu2 [pointer]= (double) (array_mu1_ip [pointer]*array_mu2_ip[pointer]);
		}
	
		double [] sigma1_sq = new double [image_dimension];
		double [] sigma2_sq = new double [image_dimension];
		double [] sigma12 = new double [image_dimension];
	
		for (pointer =0; pointer<image_dimension; pointer++) {
				
			sigma1_sq[pointer] =(double) (array_mu1_ip_copy [pointer]*array_mu1_ip_copy [pointer]);
			sigma2_sq[pointer] =(double) (array_mu2_ip_copy [pointer]*array_mu2_ip_copy [pointer]);
			sigma12 [pointer] =(double) (array_mu1_ip_copy [pointer]*array_mu2_ip_copy [pointer]);
		}
		
		ImageProcessor soporte_1_ip = new FloatProcessor (image_width, image_height);
		ImageProcessor soporte_2_ip = new FloatProcessor (image_width, image_height);
		ImageProcessor soporte_3_ip = new FloatProcessor (image_width, image_height);
		float [] array_soporte_1 =  (float []) soporte_1_ip.getPixels();
		float [] array_soporte_2 =  (float []) soporte_2_ip.getPixels();
		float [] array_soporte_3 =  (float []) soporte_3_ip.getPixels();
	
		for (pointer =0; pointer<image_dimension; pointer++) {
			array_soporte_1[pointer] = (float) sigma1_sq[pointer];
			array_soporte_2[pointer] = (float) sigma2_sq[pointer];
			array_soporte_3[pointer] = (float) sigma12[pointer];
		}
		soporte_1_ip.convolve (window_weights, opt.filter_width,  opt.filter_width);
		soporte_2_ip.convolve (window_weights, opt.filter_width,  opt.filter_width); 
		soporte_3_ip.convolve (window_weights, opt.filter_width,  opt.filter_width);
	
		for (pointer =0; pointer<image_dimension; pointer++) {
			sigma1_sq[pointer] =  array_soporte_1[pointer] - mu1_sq[pointer];
			sigma2_sq[pointer] =  array_soporte_2[pointer ]- mu2_sq[pointer];
			sigma12[pointer] =  array_soporte_3[pointer] - mu1_mu2[pointer];
		}
		
		double[] ssim_map = new double [image_dimension];
		double suma=0;
		for (pointer =0; pointer<image_dimension; pointer++) {
			ssim_map[pointer] = (double) (( 2*mu1_mu2[pointer] + opt.C1)* (2*sigma12[pointer] + opt.C2)) / ((mu1_sq[pointer]+mu2_sq[pointer] + opt.C1) * (sigma1_sq[pointer] + sigma2_sq[pointer] + opt.C2));
			suma = suma + ssim_map[pointer];
		}	
		
		return (double) suma / image_dimension;
	}
}
