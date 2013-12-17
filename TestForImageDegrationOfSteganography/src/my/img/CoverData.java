package my.img;

import my.util.Util;

import java.io.File;


public class CoverData extends Img{

    public CoverData(File file) {
        super(file);
    }

    public StegoData embeding(int[] msg, int msg_length, int error_code_length, int num_target_bit_dec) {
        StegoData stego = new StegoData(this, error_code_length, num_target_bit_dec);
        int[] target_bits = Util.calcTargetBits(num_target_bit_dec);

        for (int i = 0; i < msg_length; i++) {
            int[] error_code = Util.message2Error(msg[i], error_code_length);
            byte[] error_code_arr = Util.splitError(error_code, error_code_length);

            embedingError(stego, error_code_arr, error_code_length, i, target_bits);
        }
        return stego;

    }

    public StegoData embeding(int[] msg, int error_code_length, int num_target_bit_dec) {
        int[] target_bits = Util.calcTargetBits(num_target_bit_dec);
        int limit_length_msg = (buff.length - buff_offset) * target_bits.length / error_code_length;
        return embeding(msg, limit_length_msg, error_code_length, num_target_bit_dec);
    }

    private void embedingError(StegoData stego, byte[] error_arr, int msg_index, int code_length, int[] target_bits) {
        int start_px = msg_index * code_length;
        int length_per_bit_space = buff.length - buff_offset;
        int i = 0;
        for (byte e : error_arr) {
            int target_px = (start_px + i) % length_per_bit_space + buff_offset;
            int target_bit = target_bits[(start_px + i) / length_per_bit_space];
            stego.buff[target_px] = (byte) (stego.buff[target_px] ^ (e << target_bit));
            stego.error_count += e;
            i++;
        }
    }
}
