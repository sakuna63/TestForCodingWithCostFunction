package my;

import my.util.Calc;
import my.util.IO;

import java.io.*;

public class Main {
    private static final String IMG_PATH = "./img/";
    private static final String CSV_PATH = "./csv/";
    private static final String ORIGIN_IMG_PATH = IMG_PATH + "origin/";
    private static final String EMBEDED_IMG_PATH = IMG_PATH + "embeded_img/";
    private static final String BASE_RANGE_CSV_PATH = CSV_PATH + "base_range/";
    private static final String BASE_LENGTH_CSV_PATH = CSV_PATH + "base_length/";
    private static final String BASE_IMG_CSV_PATH = CSV_PATH + "base_image/";
    private static final String AVE_DES_CSV_PATH = CSV_PATH + "ave_despersion/";
    private static final String BASE_BIT_CSV_PATH = CSV_PATH + "base_bit/";
    
    // CHARACTER_CODEのサイズ
    private static final int CHARACTER_SIZE = 8;
    
    // 画像の一辺のサイズ
    private static final int IMAGE_SIZE = 256;
    
    public static void main(String[] args) {
        int[] msg = createMsg(0, IMAGE_SIZE * IMAGE_SIZE);
        File[] files = new File(ORIGIN_IMG_PATH).listFiles();
        CoverData[] covers = new CoverData[files.length];
        
        for(int i=0; i<files.length; i++) {
            covers[i] = new CoverData(files[i]);
        }
        
        outputRangeCSV(msg, covers);
        outputLengthCSV(msg, covers);
        outputImgCSV(msg, covers);
        outputAceDesCSV(covers);
//        outputBitCSV(msg, covers);
        
        IO.print("埋め込み終了");
    }
    
    private static void outputRangeCSV(int[] msg, CoverData[] covers) {
        PrintWriter pw;
        StegoData stego;
//      File f = files[0];
        for(CoverData c : covers) {
            pw = getPrintWriter(BASE_RANGE_CSV_PATH, c.file_name.replace(".bmp", ""));
            pw.println("埋め込み範囲,埋め込み率,PSNR,SSIM,誤り率");

            for(int range=0; range<=255; range++) {
                for(int length=8; length<=256; length++) {
                    stego = createStegoData(c, msg, length, range);

                    pw.print(range + ",");
                    pw.print(((double)8/length) * 100 + ",");  // 埋め込み率
                    pw.print(stego.psnr(c) + ",");
                    pw.print(stego.ssim(c) + ",");
                    pw.println(stego.getErrorRate());
                }
            }
            
            pw.close();
        }
    }

    private static void outputLengthCSV(int[] msg, CoverData[] covers) {
        PrintWriter pw;
        StegoData stego;
//      File f = files[0];
        for(CoverData c : covers) {
            pw = getPrintWriter(BASE_LENGTH_CSV_PATH, c.file_name.replace(".bmp", ""));
            pw.println("埋め込み率,埋め込み範囲,PSNR,SSIM,誤り率");

            for(int length=8; length<=256; length++) {
                for(int range=0; range<=255; range++) {
                    stego = createStegoData(c, msg, length, range);

                    pw.print(((double)8/length) * 100 + ",");  // 埋め込み率
                    pw.print(range + ",");
                    pw.print(stego.psnr(c) + ",");
                    pw.print(stego.ssim(c) + ",");
                    pw.println(stego.getErrorRate());
                }
            }
            
            pw.close();
        }
    }
    
    private static void outputImgCSV(int[] msg, CoverData[] covers) {
        PrintWriter pw;
        StegoData stego;
        for(int range=0; range<=255; range++) {
            pw = getPrintWriter(BASE_IMG_CSV_PATH, "" + range);
            pw.print(",");   // 左上のマスを開ける
            for(CoverData cover : covers) {
                pw.print(cover.file_name + ",");
            }
            pw.println();
            
            for(int length=8; length<=256; length++) {
                pw.print(((double)8/length) * 100 + ",");
                for(CoverData cover : covers) {
                    stego = createStegoData(cover, msg, length, range);
                    pw.print(stego.ssim(cover) + ",");
                }
                pw.println();
            }
            
            pw.close();
        }
    }
    
    private static void outputAceDesCSV(CoverData[] covers) {
        PrintWriter pw = getPrintWriter(AVE_DES_CSV_PATH, "img_status");
        pw.println("ファイル名,平均,分散,標準偏差,ブロック分散平均2,ブロック分散平均4,ブロック分散平均8,ブロック分散平均16,ブロック分散平均32,ブロック分散平均64");
        
        for (CoverData c : covers) {
            byte[] buff = c.calcBuffWithoutOffset();
            pw.print(c.file_name + ",");
            pw.print(Calc.average(buff) + ",");
            pw.print(Calc.despersion(buff) + ",");
            pw.print(Calc.standardDivision(buff) + ",");
            pw.print(Calc.splitedAreaDespersion(buff, IMAGE_SIZE, 2) + ",");
            pw.print(Calc.splitedAreaDespersion(buff, IMAGE_SIZE, 4) + ",");
            pw.print(Calc.splitedAreaDespersion(buff, IMAGE_SIZE, 8) + ",");
            pw.print(Calc.splitedAreaDespersion(buff, IMAGE_SIZE, 16) + ",");
            pw.print(Calc.splitedAreaDespersion(buff, IMAGE_SIZE, 32) + ",");
            pw.println(Calc.splitedAreaDespersion(buff, IMAGE_SIZE, 64));
        }
        pw.close();
        
        IO.println("finish");
    }

    private static void outputBitCSV(int[] msg, CoverData[] covers) {
        PrintWriter pw;
        StegoData stego;
//        covers = new CoverData[]{covers[0]};
        for(CoverData cover : covers) {
            pw = getPrintWriter(BASE_BIT_CSV_PATH, "" + cover.file_name.replace(".bmp", ""));
            pw.print(",");
            for(int range=0; range<=255; range++) {
                pw.print(range + "bit PSNR,SSIM,");
            }
            pw.println();

            for(int length=8; length<=256; length++) {
                pw.print(((double)8/length) * 100 + ",");
                for(int range = 0; range < 8; range++) {
                    stego = createStegoData(cover, msg, length, (int)Math.pow(2, range));
                    pw.print(stego.psnr(cover) + "," + stego.ssim(cover) + ",");
                }
                pw.println();
            }
            pw.close();
        }
    }

    private static StegoData createStegoData(CoverData cover, int[] msg, int code_length, int range) {
        
        IO.println("run %s codeLength:%d range:%d", cover.file_name, code_length, range);
        
        StegoData stego = cover.embeding(msg, code_length, range);
        stego.output(EMBEDED_IMG_PATH + cover.file_name.replace(".bmp", "") + "/" +  code_length + "_" + range + "_" + cover.file_name);
        
        int[] embededMsg = stego.extracting(cover);
        if( !compMsg(msg, embededMsg, code_length, range) )
            IO.println("メッセージの取り出しに失敗しました");
        
        return stego;
    }
    
    /**
     * ２つのメッセージを比較する
     * @param origin
     * @param embuded
     * @param code_length
     * @param range
     * @return
     */
    private static boolean compMsg(int[] origin, int[] embuded, int code_length, int range) {
        for (int i=0; i<embuded.length; i++) {
            if( origin[i] != embuded[i]) {
                IO.println(String.format("length:%d, range:%d, i: %d, origin:%d, embuded:%d",code_length, range, i, origin[i], embuded[i]));
                return false;
            }
        }
        return true;
    }

    /**
     * 乱数列を生成する
     * @param num
     * @return
     */
    private static int[] createMsg(int seed, int num) {
        Sfmt s = new Sfmt(seed);
        int[] randByteArray = new int[num];
    
        for( int i=0; i<num; i++)
            randByteArray[i] = s.NextInt((int) Math.pow(2, CHARACTER_SIZE));
        
        return randByteArray;
    }

    /**
     * PrinterWriterのインスタンスを取得する
     * @param file_path
     * @param file_name
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
