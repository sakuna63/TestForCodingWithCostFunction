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
            } else {
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

    public static double average(double[] nums) {
        double sum = 0.0;
        for (double num : nums) {
            sum += num;
        }
        return sum / nums.length;
    }
    
    public static double dispersion(byte[] nums) {
        double sum_m2 = 0.0;
        double ave = average(nums);

        for(byte num : nums) {
            sum_m2 += Math.pow(num & 0x000000ff, 2);
        }


        return sum_m2 / nums.length - ave * ave;
    }

    public static double standardDivision(byte[] nums) {
        return Math.sqrt(dispersion(nums));
    }

    public static double convariance(byte[] nums1, byte[] nums2) {
        if (nums1.length != nums2.length) return -100.0;

        double sum = 0.0;
        double ave1 = average(nums1), ave2 = average(nums2);

        for (int i=0; i < nums1.length; i++) {
            int n1 = nums1[0] & 0x000000ff;
            int n2 = nums2[0] & 0x000000ff;
            sum += (n1 - ave1) * (n2 - ave2);
        }

        return sum / nums1.length;
    }

    public static double correlationCoefficient(byte[] nums1, byte[] nums2) {
        return convariance(nums1, nums2) / (standardDivision(nums1) * standardDivision(nums2));
    }

    public static double dividedAreaDispersion(byte[] img_buff, int img_size, int area_size) {
        boolean isAvailableCalculating = img_buff.length % (area_size * area_size) != 0 && img_size % area_size != 0;
        if (isAvailableCalculating) return -1;


        int area_num = img_buff.length / (area_size * area_size);
        byte[][] areas = new byte[area_num][area_size * area_size];
        int pos_ver, pos_hor, pos_img;
        for (int i=0; i < area_num; i++) {
            pos_ver = (i * area_size / img_size) * area_size;
            pos_hor = i * area_size % img_size;
            for(int j=0; j < area_size; j++) {
                for(int k=0; k < area_size; k++) {
                    pos_img = pos_ver * img_size + pos_hor + j * img_size + k;
                    areas[i][j * area_size + k] = img_buff[pos_img];
                }
            }
        }

        double[] area_dispersion = new double[area_num];
        for (int i=0; i < area_num; i++) {
            area_dispersion[i] = dispersion(areas[i]);
        }

        return average(area_dispersion);
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
        private static final double K1 = 0.01;          /** SSIM_indexの計算時の不安定さを避けるための定数C1のこと **/
        private static final double K2 = 0.03;          /** SSIM_indexの計算時の不安定さを避けるための定数C2のこと **/

        public double sigma_gauss = 1.5;    /** ガウシアンフィルタを掛ける際に使う偏差値 **/
        public int filter_width = 11;       /** フィルタのサイズ。だいたい7~15 **/
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
        int filter_length = opt.filter_width * opt.filter_width;
        float window_weights [] = new float [filter_length];

        /**
         * ガウシアンフィルターを作成する
         */
        int index, center_p = (opt.filter_width/2);
        double distance, total = 0;
        double sigma_m=opt.sigma_gauss*opt.sigma_gauss;
        double[] array_gauss_window = new double [filter_length];

        for (int y = 0; y < opt.filter_width; y++){
            for (int x = 0; x < opt.filter_width; x++){
                distance = Math.abs(x-center_p)*Math.abs(x-center_p)+Math.abs(y-center_p)*Math.abs(y-center_p);
                index = y*opt.filter_width + x;
                // フィルタの各座標の重みを計算する
                array_gauss_window[index] = Math.exp(-0.5*distance/sigma_m);
                // 重みの合計を計算する（あとで除算するため）
                total = total + array_gauss_window[index];
            }
        }
        for (int i = 0; i < filter_length; i++) {
            array_gauss_window[i] = array_gauss_window[i] / total;
            window_weights [i] = (float) array_gauss_window[i];
        }

        /**
         * SSIM計算
         */
        int image_height = 256;
        int image_width = 256;
        int image_dimension = image_width*image_height;

        /**
         * ImageProcessorのconvolveというメソッドがフィルタ配列とそのサイズを渡すことで画像の全ピクセルに対して自動でフィルタリングを行ってくれる。
         * そのためImageProcessorを使用する。
         * getPixelsはImageProcessorのインスタンスの各ピクセルへの参照を渡す。cloneしてるわけではない。
         */
        ImageProcessor mu1_ip = new FloatProcessor (image_width, image_height);
        ImageProcessor mu2_ip = new FloatProcessor (image_width, image_height);
        float [] img1 = (float []) mu1_ip.getPixels();
        float [] img2 = (float []) mu2_ip.getPixels();
        double [] img1_sq = new double[image_dimension];
        double [] img2_sq = new double[image_dimension];
        double [] img1_img2 = new double[image_dimension];

        // 引数からImageProcessor変数に値を移す
        for (int i = 0; i < stego.length - offset; i++) {
            img1 [i] = (0xff & cover[i + offset]); // Float.intBitsToFloat(a);
            img2 [i] = (0xff & stego[i + offset]); //Float.intBitsToFloat(b);
            img1_sq[i] = img1[i] * img1[i];
            img2_sq[i] = img2[i] * img2[i];
            img1_img2[i] = img1[i] * img2[i];
        }

        // フィルタリングする
        mu1_ip.convolve (window_weights, opt.filter_width, opt.filter_width);
        mu2_ip.convolve (window_weights, opt.filter_width, opt.filter_width);

        double [] mu1_sq = new double [image_dimension];
        double [] mu2_sq = new double [image_dimension];
        double [] mu1_mu2 = new double [image_dimension];

        for (i =0; i<image_dimension; i++) {
            mu1_sq[i] = (double) (array_mu1_ip [i]*array_mu1_ip [i]);
            mu2_sq[i] = (double) (array_mu2_ip[i]*array_mu2_ip[i]);
            mu1_mu2 [i]= (double) (array_mu1_ip [i]*array_mu2_ip[i]);
        }

        double [] sigma1_sq = new double [image_dimension];
        double [] sigma2_sq = new double [image_dimension];
        double [] sigma12 = new double [image_dimension];

        for (i =0; i<image_dimension; i++) {

            sigma1_sq[i] =(double) (array_mu1_ip_copy [i]*array_mu1_ip_copy [i]);
            sigma2_sq[i] =(double) (array_mu2_ip_copy [i]*array_mu2_ip_copy [i]);
            sigma12 [i] =(double) (array_mu1_ip_copy [i]*array_mu2_ip_copy [i]);
        }

        ImageProcessor soporte_1_ip = new FloatProcessor (image_width, image_height);
        ImageProcessor soporte_2_ip = new FloatProcessor (image_width, image_height);
        ImageProcessor soporte_3_ip = new FloatProcessor (image_width, image_height);
        float [] array_soporte_1 =  (float []) soporte_1_ip.getPixels();
        float [] array_soporte_2 =  (float []) soporte_2_ip.getPixels();
        float [] array_soporte_3 =  (float []) soporte_3_ip.getPixels();

        for (i =0; i<image_dimension; i++) {
            array_soporte_1[i] = (float) sigma1_sq[i];
            array_soporte_2[i] = (float) sigma2_sq[i];
            array_soporte_3[i] = (float) sigma12[i];
        }
        soporte_1_ip.convolve (window_weights, opt.filter_width,  opt.filter_width);
        soporte_2_ip.convolve (window_weights, opt.filter_width,  opt.filter_width);
        soporte_3_ip.convolve (window_weights, opt.filter_width,  opt.filter_width);

        for (i =0; i<image_dimension; i++) {
            sigma1_sq[i] =  array_soporte_1[i] - mu1_sq[i];
            sigma2_sq[i] =  array_soporte_2[i ]- mu2_sq[i];
            sigma12[i] =  array_soporte_3[i] - mu1_mu2[i];
        }

        double[] ssim_map = new double [image_dimension];
        double suma=0;
        for (i =0; i<image_dimension; i++) {
            ssim_map[i] = (double) (( 2*mu1_mu2[i] + opt.C1)* (2*sigma12[i] + opt.C2)) / ((mu1_sq[i]+mu2_sq[i] + opt.C1) * (sigma1_sq[i] + sigma2_sq[i] + opt.C2));
            suma = suma + ssim_map[i];
        }   
        
        return (double) suma / image_dimension;
    }
}
