package my;

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
        Data[][][] data = new Data[covers.length][256][249];
        
        for(int i=0; i<files.length; i++) {
            covers[i] = new CoverData(files[i]);
        }

        calcData(msg, covers, data);
        outputRangeCSV(covers, data);
//        outputLengthCSV(covers, data);
//        outputImgCSV(covers, data);
//        outputAceDesCSV(covers, data);
//        outputBitCSV(covers, data);
        
        IO.print("埋め込み終了");
    }

    private static void calcData(int[] msg, CoverData[] covers, Data[][][] data) {
        StegoData stego;
        int i = 0;
        for(CoverData c : covers) {
            for(int range=0; range<=255; range++) {
                for(int length=8; length<=256; length++) {
                    stego = createStegoData(c, msg, length, range);
                    data[i][range][length - 8] = new Data(
                        range,
                        length,
                        stego.psnr(c),
                        stego.ssim(c),
                        stego.getErrorRate(),
                        stego.buff.length - stego.buff_offset
                    );
                }
            }
            i++;
        }
    }

    private static void outputRangeCSV(CoverData[] covers, Data[][][] data) {
        PrintWriter pw;
        Data d;
        for(int i = 0; i < covers.length; i++) {
            pw = getPrintWriter(BASE_RANGE_CSV_PATH, covers[i].file_name.replace(".bmp", ""));
            pw.println("埋め込み範囲,埋め込み率,PSNR,SSIM,誤り率");

            for(int range=0; range<=255; range++) {
                for(int length=8; length<=256; length++) {
                    d = data[i][range][length - 8];

                    pw.print(range + ",");
                    pw.print(d.embeding_rate + ",");
                    pw.print(d.psnr + ",");
                    pw.print(d.ssim + ",");
                    pw.println(d.error_rate);
                }
            }
            pw.close();
        }
    }

    private static void outputLengthCSV(CoverData[] covers, Data[][][] data) {
        PrintWriter pw;
        Data d;
        for (int i = 0; i < covers.length; i++) {
            pw = getPrintWriter(BASE_LENGTH_CSV_PATH, covers[i].file_name.replace(".bmp", ""));
            pw.println("埋め込み率,埋め込み範囲,PSNR,SSIM,誤り率");

            for(int length=8; length<=256; length++) {
                for(int range=0; range<=255; range++) {
                    d = data[i][range][length - 8];

                    pw.print(d.embeding_rate + ",");  // 埋め込み率
                    pw.print(range + ",");
                    pw.print(d.psnr + ",");
                    pw.print(d.ssim + ",");
                    pw.println(d.error_rate);
                }
            }
            
            pw.close();
        }
    }
    
    private static void outputImgCSV(CoverData[] covers, Data[][][] data) {
        PrintWriter pw;
        for(int range=0; range<=255; range++) {
            pw = getPrintWriter(BASE_IMG_CSV_PATH, "" + range);
            pw.print(",");   // 左上のマスを開ける
            for(CoverData cover : covers) {
                pw.print(cover.file_name + ",");
            }
            pw.println();
            
            for(int length=8; length<=256; length++) {
                pw.print(((double)8/length) * 100 + ",");
                for(int i = 0; i < covers.length; i++) {
                    pw.print(data[i][range][length-8].ssim + ",");
                }
                pw.println();
            }
            
            pw.close();
        }
    }

    private static void outputBitCSV(CoverData[] covers, Data[][][] data) {
        PrintWriter pw;
        Data d;
        for(int i = 0; i < covers.length; i++) {
            pw = getPrintWriter(BASE_BIT_CSV_PATH, "" + covers[i].file_name.replace(".bmp", ""));
            pw.print(",");
            for(int range=0; range<=255; range++) {
                pw.print(range + "bit PSNR,SSIM,");
            }
            pw.println();

            for(int length=8; length<=256; length++) {
                pw.print(((double)8/length) * 100 + ",");
                for(int range = 0; range < 8; range++) {
                    d = data[i][range][length - 8];
                    pw.print(d.psnr + "," + d.ssim + ",");
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
