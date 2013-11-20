import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import my.util.Calc;
import my.util.IO;

public class Main2 {
    private static final String IMAGE_PATH = "./img/";
    private static final String CSV_PATH = "./csv/";
    
    public static void run() {
        File img_dir = new File(IMAGE_PATH);
        PrintWriter pw = getPrintWriter(CSV_PATH, "img_status");
        pw.println("ファイル名,平均,分散,標準偏差");
        
        for (File f : img_dir.listFiles()) {
            byte[] img_buff = File2Buff(f);
            pw.print(f.getName() + ",");
            pw.print(Calc.average(img_buff) + ",");
            pw.print(Calc.despersion(img_buff) + ",");
            pw.println(Calc.standardDivision(img_buff));
        }
        pw.close();
        
        IO.println("finish");
    }
    
    private static byte[] File2Buff(File file) {
        byte[] buff = null, sizeBuff = new byte[4], offsetBuff = new byte[4];
   
        try {
            FileInputStream fis = new FileInputStream(file);
            
            // 画像のサイズを読み込む
            fis.skip(2);
            fis.read(sizeBuff);
            int size = sizeBuff[3] << 24 | sizeBuff[2] << 16 | sizeBuff[1] << 8 | sizeBuff[0];
            
            // 画像のオフセットを読み込む
            fis.skip(4);
            fis.read(offsetBuff);
            int offset = offsetBuff[3] << 24 | offsetBuff[2] << 16 | offsetBuff[1] << 8 | offsetBuff[0];
            
            // バッファにビットマップを読み込む
            buff = new byte[size - offset];
            
            fis.skip(offset - 6);
            fis.read(buff);
            fis.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
        
        return buff;
    }
    
    /**
     * PrinterWriterのインスタンスを取得する
     * @param fileName
     * @return
     */
    private static PrintWriter getPrintWriter(String file_path, String file_name) {
        String pw_name = file_path + file_name + ".csv";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter
                    (new FileOutputStream(new File(pw_name)),
                            "Shift_JIS"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pw;
    }
}
