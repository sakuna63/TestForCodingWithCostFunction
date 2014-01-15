package my;

import my.img.CoverData;
import my.img.StegoData;
import my.util.Excel;
import my.util.IO;
import my.util.Util;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {
    private static final String UTF_8 = "utf-8";
    private static final String SHIFT_JIS = "Shift_JIS";

    // CHARACTER_CODEのサイズ
    private static final int CHARACTER_SIZE = 8;
    // 画像の一辺のサイズ
    private static final int IMAGE_SIZE = 256;

    public static void main(String[] args) {
        int[] msg = createMsg(0, IMAGE_SIZE * IMAGE_SIZE);
        File[] files_img = new File(Path.ORIGIN_IMG_PATH).listFiles();
        File[] files = new File(Path.ROW_DATA_PATH).listFiles();
        Arrays.sort(files_img);


        fin(files);

//        files = new File("./img/img_b/").listFiles();
//        ImgUtil.labeling(files);
//        ImgUtil.labeling1(files);


//        xlsx_based_enb_rate(files);
//        xlsx_based_range(files);
//        xlsx_based_img(files);
//        xlsx_base_msg_length(files);

        IO.print("埋め込み終了");
    }

    private static void fin(File[] files) {
        int[] ranges = {1,3,7,15};
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = null;
        Row row;
        Data[][] data;
        Data d;
        int rowNum;
        for (int range : ranges) {
            rowNum = 0;
            sheet = wb.createSheet(range + "");

            row = sheet.createRow(rowNum);
            for (int i=0; i<files.length; i++) {
                row.createCell(i + 1).setCellValue(chopExt(files[i].getName()));
            }

            data = readData(files[0].getName());
            for (int length = 8; length <= 256; length++) {
                d = data[range][length-1];
                row = sheet.createRow(++rowNum);
                row.createCell(0).setCellValue(d.embeding_rate);
                row.createCell(1).setCellValue(d.ssim);
            }



            for (int i = 1; i < files.length; i++) {
                data = readData(files[i].getName());
                rowNum = 0;
                for(int length = 8; length <= 256; length ++) {
                    d = data[range][length-1];
                    sheet.getRow(++rowNum).createCell(i+1).setCellValue(d.ssim);
                }
            }
            IO.println(range);
        }
        Excel.outputWorkbook(wb, Path.BASE_IMG_DATA_PATH, "fin.xlsx");
    }

    // csvファイルに計算結果を書き込む
    private static void writeData(int[] msg, CoverData[] covers) {
        StegoData stego;
        int msg_length, embeding_limit_per_bit = covers[0].calcBuffWithoutOffset().length;
        double embeding_rate;

        for (CoverData c : covers) {
            PrintWriter pw = getPrintWriter("./data/row_data/", c.file_name.replace(".bmp", ""), UTF_8);
            for (int range = 1; range <= 255; range++) {
                for (int length = 8; length <= 256; length++) {
                    stego = createStegoData(c, msg, length, range);
                    pw.print(range + ",");
                    pw.print(length + ",");
                    pw.print(stego.psnr(c) + ",");
                    pw.print(stego.ssim(c) + ",");
                    pw.print(stego.getErrorRate() + ",");
                    pw.print(embeding_limit_per_bit + ",");

                    embeding_rate = (double) 8 / length * 100;
                    msg_length = embeding_limit_per_bit * Util.calcTargetBits(range).length / length;

                    pw.print(embeding_limit_per_bit + ",");
                    pw.print(msg_length + ",");
                    pw.println(embeding_rate);
                }
            }
            pw.close();
        }
    }

    // csvファイルから計算結果を読み込む
    private static Data[][][] readData() {
        int i = 0, length, range;
        File[] files = new File("./data/row_data/").listFiles();
        Arrays.sort(files);
//        files = new File[]{files[0], files[1], files[2], files[3], files[4], files[5]};
        Data[][][] data = new Data[files.length][256][256];
        try {
            for (File file : files) {
                BufferedReader br = null;
                br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line, ",");

                    // 空行チェック
                    if (st.countTokens() <= 1) {
                        continue;
                    }

                    range = Integer.parseInt(st.nextToken());
                    length = Integer.parseInt(st.nextToken());
                    data[i][range][length - 1] = new Data(
                            range,
                            length,
                            Double.parseDouble(st.nextToken()),
                            Double.parseDouble(st.nextToken()),
                            Double.parseDouble(st.nextToken()),
                            Integer.parseInt(st.nextToken())
                    );
                }
                br.close();
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static Data[][] readData(String fileName) {
        Data[][] data = new Data[256][256];
        int range, length;
        try {
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(new File(Path.ROW_DATA_PATH + fileName)));

            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ",");

                // 空行チェック
                if (st.countTokens() <= 1) {
                    continue;
                }

                range = Integer.parseInt(st.nextToken());
                length = Integer.parseInt(st.nextToken());
                data[range][length - 1] = new Data(
                        range,
                        length,
                        Double.parseDouble(st.nextToken()),
                        Double.parseDouble(st.nextToken()),
                        Double.parseDouble(st.nextToken()),
                        Integer.parseInt(st.nextToken())
                );
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static CoverData[] createCovers(File[] files, int begin, int end, int length) {
        CoverData[] covers = new CoverData[length];
        for (int i = begin; i < end; i++) {
            covers[i - begin] = new CoverData(files[i]);
        }

        return covers;
    }

    // Format : 埋め込み率, 埋め込み範囲, PSNR, SSIM, 誤り率
    private static void xlsx_based_enb_rate(File[] files) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = null;
        Data[][] data;
        Data d;
        int rowNum;
        for (int i = 0; i < files.length; i++) {
            rowNum = 0;
            data = readData(files[i].getName());
            sheet = wb.createSheet(chopExt(files[i].getName()));
            Excel.setCellsString(sheet.createRow(rowNum), "埋め込み率,埋め込み範囲,PSNR,SSIM,誤り率");

            for (int length = 8; length <= 256; length++) {
                for (int range = 1; range <= 255; range++) {
                    d = data[range][length-1];

                    Excel.setCellsDouble(sheet.createRow(++rowNum), new double[]{
                            d.embeding_rate,
                            range,
                            d.psnr,
                            d.ssim,
                            d.error_rate
                    });
                }

            }
            IO.println(files[i].getName());
            if(i%6 == 5 || i == files.length - 1) {
                Excel.outputWorkbook(wb, Path.BASE_LENGTH_DATA_PATH, i + ".xlsx");
                wb = new XSSFWorkbook();
            }
        }
    }

    // Format : 埋め込み範囲, 埋め込み率, PSNR, SSIM, 誤り率
    private static void xlsx_based_range(File[] files){
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = null;
        Data[][] data;
        Data d;
        int rowNum;
        for (int i = 0; i < files.length; i++) {
            rowNum = 0;
            data = readData(files[i].getName());
            sheet = wb.createSheet(chopExt(files[i].getName()));
            Excel.setCellsString(sheet.createRow(rowNum), "埋め込み範囲,埋め込み率,PSNR,SSIM,誤り率");

            for (int range = 1; range <= 255; range++) {
                for (int length = 8; length <= 256; length++) {
                    d = data[range][length - 1];

                    Excel.setCellsDouble(sheet.createRow(++rowNum), new double[]{
                            range,
                            d.embeding_rate,
                            d.psnr,
                            d.ssim,
                            d.error_rate
                    });
                }
            }
            IO.println(files[i].getName());
            if(i%6 == 5 || i == files.length - 1) {
                Excel.outputWorkbook(wb, Path.BASE_RANGE_DATA_PATH, i + ".xlsx");
                wb = new XSSFWorkbook();
            }
        }
    }

    // Format : 横軸：いろいろ, 縦軸：メッセージ長
    private static void xlsx_base_msg_length(File[] files) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet;
        Data[][] data;
        Data d1,d2,d3;
        int rowNum = 0;
        for(File file : files) {
            rowNum = 0;
            sheet = wb.createSheet(chopExt(file.getName()));
            data = readData(file.getName());
            Excel.setCellsString(sheet.createRow(0), "メッセージ長, 誤り率(1bit),PSNR(1bit),SSIM(1bit),誤り率(2bit),PSNR(2bit),SSIM(2bit)");
            for (int length = 8; length <= 80; length++) {
                d1 = data[1][length-1];
                d2 = data[3][length*2-1];
                d3 = data[7][length*3-1];
                Excel.setCellsDouble(sheet.createRow(++rowNum), new double[]{
                        d1.msg_num,
                        d1.error_rate,
                        d1.psnr,
                        d1.ssim,
                        d2.error_rate,
                        d2.psnr,
                        d2.ssim,
                        d3.error_rate,
                        d3.psnr,
                        d3.ssim

                });
            }
        }
        Excel.outputWorkbook(wb, Path.BASE_MSG_LENGTH_DATA_PATH, "msg_length2.xlsx");
    }

    private static void outputMsgLenghImgCSV(CoverData[] covers, int[] msg) {
        int[] lengths = new int[]{
                8, 16, 32, 64
        };

        for (CoverData c : covers) {
            PrintWriter pw = getPrintWriter(Path.BASE_MSG_LENGTH_IMG_DATA_PATH, c.file_name.replace(".bmp", ""), SHIFT_JIS);
            pw.println("メッセージ長, SSIM(1bit), SSIM(2bit), SSIMの差");

            for (int length = 8; length <= 128; length++) {
                int msg_length = covers[0].calcBuffWithoutOffset().length / length;
                StegoData stego1 = createStegoData(c, msg, msg_length, length, 1);
                StegoData stego2 = createStegoData(c, msg, msg_length, length * 2, 3);
                double ssim1 = stego1.ssim(c), ssim2 = stego2.ssim(c);
                pw.print(msg_length + ",");
                pw.print(ssim1 + ",");
                pw.print(ssim2 + ",");
                pw.println(ssim1 - ssim2);
            }
        }
//        for (int length : lengths) {
        for (int length = 8; length <= 128; length++) {
            int msg_length = covers[0].calcBuffWithoutOffset().length / length;
            PrintWriter pw = getPrintWriter(Path.BASE_MSG_LENGTH_DATA_PATH, "" + msg_length, SHIFT_JIS);
            pw.println("メッセージ長, ファイル名, 誤り率(1bit), PSNR(1bit), SSIM(1bit), 誤り率(2bit), PSNR(2bit), SSIM(2bit), 誤り率(3bit), PSNR(3bit), SSIM(3bit)");
            for (CoverData c : covers) {
                pw.print(msg_length + ",");
                pw.print(c.file_name + ",");
                for (int i = 1; i <= 2; i++) {
                    StegoData stego = createStegoData(c, msg, msg_length, length * i, (int) Math.pow(2, i) - 1);

                    pw.print(stego.getErrorRate() + ",");
                    pw.print(stego.psnr(c) + ",");
                    pw.print(stego.ssim(c) + ",");
                }
                pw.println();
            }
            pw.close();
        }
    }

    // Format : 横軸：画像,縦軸：埋め込み率,要素：SSIM
    private static void xlsx_based_img(File[] files) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = null;
        Row row;
        Data[][] data;
        Data d;
        int rowNum;
        for (int range = 1; range <= 255; range++) {
            rowNum = 0;
            sheet = wb.createSheet(range + "");

            row = sheet.createRow(rowNum);
            for (int i=0; i<files.length; i++) {
                row.createCell(i + 1).setCellValue(chopExt(files[i].getName()));
            }

            data = readData(files[0].getName());
            for (int length = 8; length <= 256; length++) {
                d = data[range][length-1];
                row = sheet.createRow(++rowNum);
                row.createCell(0).setCellValue(d.embeding_rate);
                row.createCell(1).setCellValue(d.ssim);
            }



            for (int i = 1; i < files.length; i++) {
                data = readData(files[i].getName());
                rowNum = 0;
                for(int length = 8; length <= 256; length ++) {
                    d = data[range][length-1];
                    sheet.getRow(++rowNum).createCell(i+1).setCellValue(d.ssim);
                }
            }
            IO.println(range);
        }
        Excel.outputWorkbook(wb, Path.BASE_IMG_DATA_PATH, "img.xlsx");
    }

    private static void outputBitCSV(CoverData[] covers, Data[][][] data) {
        PrintWriter pw;
        Data d;
        for (int i = 0; i < covers.length; i++) {
            pw = getPrintWriter(Path.BASE_BIT_DATA_PATH, covers[i].file_name.replace(".bmp", ""), SHIFT_JIS);
            pw.print(",");
            for (int range = 0; range <= 255; range++) {
                pw.print(range + "bit PSNR,SSIM,");
            }
            pw.println();

            for (int length = 8; length <= 256; length++) {
                pw.print(((double) 8 / length) * 100 + ",");
                for (int range = 0; range < 8; range++) {
                    d = data[i][range][length - 8];
                    pw.print(d.psnr + "," + d.ssim + ",");
                }
                pw.println();
            }
            pw.close();
        }
    }

    private static StegoData createStegoData(CoverData cover, int[] msg, int code_length, int range) {
        return createStegoData(cover, msg, -1, code_length, range);
    }

    private static StegoData createStegoData(CoverData cover, int[] msg, int msg_length, int code_length, int range) {

        IO.println("run %s codeLength:%d range:%d", cover.file_name, code_length, range);

        StegoData stego = msg_length == -1 ? cover.embeding(msg, code_length, range) : cover.embeding(msg, msg_length, code_length, range);
        String name_dir = (msg_length == -1 ? Path.EMBEDED_IMG_PATH : Path.EMBEDED_IMG_MSG_PATH) + cover.file_name.replace(".bmp", "") + "/";
        String name_file = code_length + "_" + range + "_" + cover.file_name;
        stego.output(name_dir, name_file);

        int[] embededMsg = stego.extracting(cover);
        if (!compMsg(msg, embededMsg, code_length, range))
            IO.println("メッセージの取り出しに失敗しました");

        return stego;
    }

    /**
     * ２つのメッセージを比較する
     *
     * @param origin
     * @param embuded
     * @param code_length
     * @param range
     * @return
     */
    private static boolean compMsg(int[] origin, int[] embuded, int code_length, int range) {
        for (int i = 0; i < embuded.length; i++) {
            if (origin[i] != embuded[i]) {
                IO.println(String.format("length:%d, range:%d, i: %d, origin:%d, embuded:%d", code_length, range, i, origin[i], embuded[i]));
                return false;
            }
        }
        return true;
    }

    /**
     * 乱数列を生成する
     *
     * @param num
     * @return
     */
    private static int[] createMsg(int seed, int num) {
        Sfmt s = new Sfmt(seed);
        int[] randByteArray = new int[num];

        for (int i = 0; i < num; i++)
            randByteArray[i] = s.NextInt((int) Math.pow(2, CHARACTER_SIZE));

        return randByteArray;
    }

    /**
     * PrinterWriterのインスタンスを取得する
     *
     * @param file_path
     * @param file_name
     * @return
     */

    private static PrintWriter getPrintWriter(String file_path, String file_name, String character_code) {
        String pw_name = file_path + file_name + ".csv";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter
                    (new FileOutputStream(new File(pw_name)), character_code));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pw;
    }

    private static String chopExt(String str) {
        return str.replace(".bmp", "").replace(".csv", "");
    }
}
