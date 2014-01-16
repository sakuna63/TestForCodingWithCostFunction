package my.img;

import my.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CoverData{

    public byte[] buff;
    public int buff_offset;
    public String file_name;

    public CoverData() {}

    public CoverData(File file) {
        this.file_name = file.getName();
        File2Buff(file);
    }

    protected void File2Buff(File file) {
        byte[] sizeBuff = new byte[4], offsetBuff = new byte[4];

        try {
            FileInputStream fis = new FileInputStream(file);

            // 画像のサイズを読み込む
            fis.skip(2);
            fis.read(sizeBuff);
            int size = sizeBuff[3] << 24 | sizeBuff[2] << 16 | sizeBuff[1] << 8 | sizeBuff[0];

            // 画像のオフセットを読み込む
            fis.skip(4);
            fis.read(offsetBuff);
            buff_offset = offsetBuff[3] << 24 | offsetBuff[2] << 16 | offsetBuff[1] << 8 | offsetBuff[0];

            // バッファにビットマップを読み込む
            buff = new byte[size];

            fis.close();
            fis = new FileInputStream(file);

            fis.read(buff);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] calcBuffWithoutOffset() {
        byte[] buff_wo = new byte[buff.length - buff_offset];
        System.arraycopy(buff, buff_offset, buff_wo, 0, buff_wo.length);
        return buff_wo;
    }

    public void output(String name_dir, String name_file) {
        File file_dir = new File(name_dir);

        if(!file_dir.exists()) {
            file_dir.mkdir();
        }

        File file_stego = new File(name_dir + name_file);

        if (file_stego.exists())
            file_stego.delete();

        try {
            file_stego.createNewFile();

            FileOutputStream fos = new FileOutputStream(file_stego);
            fos.write(buff);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param msg
     * @param msg_length
     * @param error_code_length
     * @param num_target_bit_dec
     * @return
     */
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

    protected void embedingError(StegoData stego, byte[] error_arr, int msg_index, int code_length, int[] target_bits) {
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
