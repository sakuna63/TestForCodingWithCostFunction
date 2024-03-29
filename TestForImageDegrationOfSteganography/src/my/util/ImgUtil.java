package my.util;

import my.img.Img;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Arrays;

public class ImgUtil {

    public static void labeling(File[] files) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = null;
        Img img;
        byte[][] buff;
        int[][] buff_c;
        int rownum = 0;




        for (File f : files) {
            if(f.getName().equals("brickhouse256.bmp7.bmp")) continue;
            img = new Img(f);
            if(sheet == null || !sheet.getSheetName().equals(img.file_name.split("\\.")[0])) {
                rownum = 0;
                sheet = wb.createSheet(img.file_name.split("\\.")[0]);
                Excel.setCellsString(sheet.createRow(0), "ファイル名,最大,最小,平均,分散,ラベル数");
            }
            IO.println(img.file_name);
            buff = toSq(img.calcBuffWithoutOffset());
//            cb(buff);
            buff_c = new int[256][256];

            int label_val = 0;

            // ラベリング
            for(int y=0; y < 256; y++) {
                for(int x=0; x < 256; x++) {
                    // buffがラベル付をスべき場所（白）でかつまだラベル付けされていない場合(buff_c = 0)
                    if (buff[y][x] == -1 && buff_c[y][x] == 0) {
                        label_val++;
                        search(buff, buff_c, x, y, label_val);
                    }
                }
            }

            int[] counts = new int[label_val];

            for(int y=0; y < 256; y++) {
                for(int x=0; x < 256; x++) {
                    if(buff_c[y][x] != 0)
                        counts[buff_c[y][x]-1]++;
                }
            }

            int max = max(counts);
            int min = label_val == 0 ? 0 : min(counts);
            double ave = label_val == 0 ? 0 : Calc.average(counts);
            double dep = label_val == 0 ? 0 : Calc.dispersion(counts);

            setCellsDouble(sheet.createRow(++rownum), img.file_name, new double[]{max, min, ave, dep, label_val});
        }

        Excel.outputWorkbook(wb, "./", "bi.xlsx");
    }

    public static void labeling1(File[] files) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet maxsheet = wb.createSheet("最大値");
        XSSFSheet minsheet = wb.createSheet("最小");
        XSSFSheet avesheet = wb.createSheet("平均");
        XSSFSheet dessheet = wb.createSheet("分散");
        XSSFSheet numsheet = wb.createSheet("ラベル数");

        Img img;
        byte[][] buff;
        int[][] buff_c;
        File[] file;

        Arrays.sort(files);

        StringBuilder builder = new StringBuilder(",");
        for(File f : files) {
            builder.append(f.getName());
            builder.append(",");
        }
        Excel.setCellsString(maxsheet.createRow(0), builder.toString());
        Excel.setCellsString(minsheet.createRow(0), builder.toString());
        Excel.setCellsString(avesheet.createRow(0), builder.toString());
        Excel.setCellsString(dessheet.createRow(0), builder.toString());
        Excel.setCellsString(numsheet.createRow(0), builder.toString());

        for(int k=0; k<files[0].listFiles().length; k++) {
            maxsheet.createRow(k+1).createCell(0).setCellValue(k);
            minsheet.createRow(k + 1).createCell(0).setCellValue(k);
            avesheet.createRow(k + 1).createCell(0).setCellValue(k);
            dessheet.createRow(k + 1).createCell(0).setCellValue(k);
            numsheet.createRow(k + 1).createCell(0).setCellValue(k);
        }


        
        for (int j =0; j < files.length; j++) {
            file = files[j].listFiles();
            IO.println(files[j].getName());
            for(int i=0; i<file.length; i++) {
                IO.println(file[i].getName());

//                if(file[i].getName().equals("brickhouse256.bmp7.bmp")) continue;

                img = new Img(file[i]);

                buff = toSq(img.calcBuffWithoutOffset());
                buff_c = new int[256][256];

                int label_val = 0;

                // ラベリング
                for(int y=0; y < 256; y++) {
                    for(int x=0; x < 256; x++) {
                        // buffがラベル付をスべき場所（白）でかつまだラベル付けされていない場合(buff_c = 0)
                        if (buff[y][x] == -1 && buff_c[y][x] == 0) {
                            label_val++;
                            search(buff, buff_c, x, y, label_val);
                        }
                    }
                }

                int[] counts = new int[label_val];

                for(int y=0; y < 256; y++) {
                    for(int x=0; x < 256; x++) {
                        if(buff_c[y][x] != 0)
                            counts[buff_c[y][x]-1]++;
                    }
                }

                int max = max(counts);
                int min = label_val == 0 ? 0 : min(counts);
                double ave = label_val == 0 ? 0 : Calc.average(counts);
                double dep = label_val == 0 ? 0 : Calc.dispersion(counts);

                maxsheet.getRow(i + 1).createCell(j + 1).setCellValue(max);
                minsheet.getRow(i + 1).createCell(j + 1).setCellValue(min);
                avesheet.getRow(i + 1).createCell(j + 1).setCellValue(ave);
                dessheet.getRow(i + 1).createCell(j + 1).setCellValue(dep);
                numsheet.getRow(i + 1).createCell(j + 1).setCellValue(label_val);
            }
        }

        int num = files[0].listFiles().length;
        maxsheet.createRow(num + 3).createCell(0).setCellValue("最大値");
        maxsheet.getRow(num + 3).createCell(1).setCellFormula("MAX(B2:B" + (num + 1) + ")");
        setCellsDouble(maxsheet.createRow(num + 4), "SSIM", new double[]{0.996489345, 0.998214126, 0.996448282, 0.999420996, 0.998781024, 0.998444163, 0.995857457, 0.998627797, 0.996700629, 0.997481098, 0.996846235, 0.999004726, 0.998642206, 0.997134099, 0.995772864, 0.996806564, 0.996507547, 0.995688226, 0.995740667, 0.997288, 0.998617902});
        
        Excel.outputWorkbook(wb, "./", "bi30-100.xlsx");
    }

    private static void setCellsDouble(Row row, String s, double[] nums) {
        row.createCell(0).setCellValue(s);
        for(int i=0; i < nums.length; i++) {
            row.createCell(i+1).setCellValue(nums[i]);
        }
    }

    private static int max(int[] counts) {
        int max = 0;

        for(int c : counts) {
            if(c > max) {
                max = c;
            }
        }

        return max;
    }

    private static int min(int[] counts) {
        int min = 60000;

        for(int c : counts) {
            if(c < min) {
                min = c;
            }
        }

        return min;
    }

    private static byte[][] toSq(byte[] buff) {
        byte[][] buff_sq = new byte[256][256];

        for(int y=0; y < 256; y++) {
            for(int x=0; x < 256; x++) {
                buff_sq[y][x] = buff[(y * 256) + x];
            }
        }

        return buff_sq;
    }

    private static void search(byte[][] buff, int[][] buff_c, int x, int y, int val) {
        if (inside(x, y) && buff[y][x] == -1 && buff_c[y][x] == 0) {

            buff_c[y][x] = val;
            search(buff, buff_c, x, y-1, val); // 上
            search(buff, buff_c, x, y+1, val); // 下
            search(buff, buff_c, x-1, y, val); // 左
            search(buff, buff_c, x+1, y, val); // 右
            // search(img, img_copy, x-1, y-1, val) // 左上
            // search(img, img_copy, x+1, y-1, val) // 右上
            // search(img, img_copy, x-1, y+1, val) // 左下
            // search(img, img_copy, x+1, y-1, val) # 右下
        }
    }

    private static boolean inside(int x, int y) {
        return x >= 0 && x < 256 && y >=0 && y < 256;
    }


}