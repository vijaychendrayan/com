package com.cerner.automation;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by VC024129 on 5/6/2017.
 */
class ExcelFileDriver {
    public Workbook fileHandler;
    public String excelFilePath;

    public void ExcelFileDriver(String filePath) {

        this.excelFilePath = filePath;
    }
    public Workbook open() throws IOException    {
        FileInputStream fileInputStream = new FileInputStream(new File(this.excelFilePath));
        return new XSSFWorkbook(fileInputStream);
    }
    public void close(Workbook workbook)throws IOException{
        workbook.close();

    }

}

