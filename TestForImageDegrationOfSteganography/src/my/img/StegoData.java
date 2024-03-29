package my.img;

import my.util.Calc;
import my.util.Util;


public class StegoData extends CoverData {

    public int error_count = 0;
    public final int error_code_length;
    public final int num_target_bit_dec;

    public StegoData(CoverData cover, int error_code_length, int num_target_bit_dec) {
        this.buff = cover.buff.clone();
        this.buff_offset = cover.buff_offset;
        this.error_code_length = error_code_length;
        this.num_target_bit_dec = num_target_bit_dec;
    }

    public double psnr(CoverData cover) {
        return Calc.PSNR(buff, cover.buff, buff_offset);
    }

    public double ssim(CoverData cover) {
        return Calc.SSIM(new Calc.SOption(), buff, cover.buff, buff_offset);
    }

    /**
     * 指定された文字数(msg_length)を抽出する
     *
     * @param cover
     * @param msg_length
     * @return
     */
    public int[] extracting(CoverData cover, int msg_length) {
        int[] target_bits = Util.calcTargetBits(num_target_bit_dec);
        int[] msg = new int[msg_length];

        for (int i = 0; i < msg_length; i++) {
            int[] error = extractError(cover.buff, i, target_bits);
            // 誤りパターンから埋め込みデータを復元する
            msg[i] = (Util.error2Message(error, error_code_length));
        }

        return msg;
    }

    /**
     * 埋め込み範囲をもとに文字を抽出する
     *
     * @param cover
     * @return
     */
    public int[] extracting(CoverData cover) {
        int[] target_bits = Util.calcTargetBits(num_target_bit_dec);
        int limit_length_msg = (buff.length - buff_offset) * target_bits.length / error_code_length;
        return extracting(cover, limit_length_msg);
    }

    public double getErrorRate() {
        int count_target_bit = Util.calcTargetBits(num_target_bit_dec).length;
        int count_target_px = (buff.length - buff_offset) * count_target_bit;
        double error_rate = (double) error_count / count_target_px;
        return error_rate * 100;
    }

    /**
     * ステゴデータから誤りパターンを抽出する
     *
     * @param cover_buff
     * @param msg_index
     * @param target_bits
     * @return
     */
    private int[] extractError(byte[] cover_buff, int msg_index, int[] target_bits) {
        int[] error_code = new int[8];
        int start_px = msg_index * error_code_length;
        int length_per_bit_space = buff.length - buff_offset;

        for (int i = 0; i < error_code_length; i++) {
            int pos = (start_px + i) % length_per_bit_space + buff_offset;
            int bit = target_bits[(start_px + i) / length_per_bit_space];
            byte error_bit = Util.extractErrorBit(buff[pos], cover_buff[pos], bit);
            // 誤りビットが1だったとき、対応するビットを1にする
            if (error_bit == 0x01) Util.raiseBit(error_code, error_code_length - i - 1);
        }
        return error_code;
    }
}
