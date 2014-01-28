package my;

import my.util.Util;

public class Data {
    public double psnr;
    public double ssim;
    public double error_rate;

    public double embedding_rate; // 埋め込み率
    public int msg_num; // 埋め込み文字数

    public Data(int num_target_bit_dec, int error_code_length, double psnr, double ssim, double error_rate, int embedding_limit_per_bit) {
        this.psnr = psnr;
        this.ssim = ssim;
        this.error_rate = error_rate;

        this.embedding_rate = (double) 8 / error_code_length * 100;
        this.msg_num = embedding_limit_per_bit * Util.calcTargetBits(num_target_bit_dec).length / error_code_length;
    }
}
