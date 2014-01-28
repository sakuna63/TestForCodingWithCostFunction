package my.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Excel {

    public static void setCellsString(Row row, String params) {
        String[] paramsArr = params.split(",");
        for(int i=0; i < paramsArr.length; i++) {
            row.createCell(i).setCellValue(paramsArr[i]);
        }
    }

    public static void setCellsDouble(Row row, double[] nums) {
        for(int i=0; i < nums.length; i++) {
            row.createCell(i).setCellValue(nums[i]);
        }
    }

    public static void outputWorkbook(XSSFWorkbook wb, String filePath, String fileName) {
        String name = filePath + fileName;
        File dir = new File(filePath);
        File xlsx = new File(filePath + fileName);
        FileOutputStream fio;
        try {
            if(!dir.exists()) dir.mkdir();
            if(xlsx.exists()) xlsx.delete();
            fio = new FileOutputStream(name);
            wb.write(fio);
            IO.println("出力しました");
            fio.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
