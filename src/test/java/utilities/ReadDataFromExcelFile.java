package utilities;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

public class ReadDataFromExcelFile {
    public static Object[][] readExcel(String filePath, String fileName, String sheetName) throws Exception{
        //Create an object of File class to open xlsx file

        File file = new File(filePath + "//" + fileName);
        //Create an object of FileInputStream class to read excel file
        FileInputStream inputStream = new FileInputStream(file);
        System.out.println(file.getAbsolutePath());
        XSSFWorkbook workbook= new XSSFWorkbook(inputStream);
        //Read sheet inside the workbook by its name
        System.out.println("Workbook Sheet ---- "+ sheetName);
        XSSFSheet workbookSheet = workbook.getSheet(sheetName);
        //Find number of rows in excel file
        int rowCount = workbookSheet.getLastRowNum() - workbookSheet.getFirstRowNum();
        System.out.println("rowCount -- " + rowCount);
        //System.out.println(workbookSheet.getRow(0).getLastCellNum());
        //Create a loop over all the rows of excel file to read it
        Object[][] projectDetails=new Object[rowCount][1];
        DataFormatter formatter = new DataFormatter();
        //HashMap<String, String> hashMap=new HashMap<String, String>();
        for (int i = 1; i <=rowCount; i++) {
            HashMap<String, String> hashMap=new HashMap<String, String>();
            Row row = workbookSheet.getRow(i);
            //Create a loop to print cell values in a row
            for (int j = 0; j < row.getLastCellNum(); j++) {
                //Print Excel data in console-- make excel header as keys and each row as hashmap
                hashMap.put(formatter.formatCellValue(workbookSheet.getRow(0).getCell(j)),formatter.formatCellValue(row.getCell(j)));
                // System.out.println(formatter.formatCellValue(workbookSheet.getRow(0).getCell(j)));
                //System.out.println(hashMap.get(formatter.formatCellValue(workbookSheet.getRow(0).getCell(j))));
            }
            projectDetails[i-1][0]=hashMap;
        }
        workbook.close();
        inputStream.close();

        return projectDetails;

    }
}
