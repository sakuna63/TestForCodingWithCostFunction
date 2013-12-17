package my.img;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Img {
    public byte[] buff;
    public int buff_offset;
    public String file_name;

    public Img(File file) {
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
}
