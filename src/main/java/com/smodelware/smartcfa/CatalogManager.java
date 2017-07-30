package com.smodelware.smartcfa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.smodelware.smartcfa.util.CONTENT_CONSTANT;
import com.smodelware.smartcfa.util.ContentType;


public class CatalogManager 
{
    public static void main(String[] args) throws FileNotFoundException 
    {
        String fileToParse = "C:/app/contentservice/testscoreservice/src/main/resources/SmartCFA.csv";
        InputStream in = new FileInputStream(new File(fileToParse));
        CatalogManager cm = new CatalogManager();
        
        Table<String, String, LinkedHashMap<String, ArrayList<String>>> catalogTable = cm.readCourseCatalog(in);
        
       System.out.println(catalogTable);
    }

	public  Table<String, String, LinkedHashMap<String, ArrayList<String>>> readCourseCatalog(InputStream ins) 
	{
		Table<String, String, LinkedHashMap<String,ArrayList<String>>> catalogTable = HashBasedTable.create();
        
        BufferedReader fileReader = null;
        final String DELIMITER = ",";
        try
        {
            String line = "";
            fileReader = new BufferedReader(new InputStreamReader(ins));
            while ((line = fileReader.readLine()) != null) 
            {
                String[] tokens = line.split(DELIMITER);
               	LinkedHashMap<String, ArrayList<String>> readingToLosMap = catalogTable.get(tokens[0]+"$"+tokens[1], tokens[2]+"$"+tokens[3])==null?new LinkedHashMap<String, ArrayList<String>>():catalogTable.get(tokens[0]+"$"+tokens[1], tokens[2]+"$"+tokens[3]);
               	catalogTable.put(tokens[0]+"$"+tokens[1], tokens[2]+"$"+tokens[3], readingToLosMap);
                	
               	ArrayList<String> losList = readingToLosMap.get(tokens[4]+"$"+tokens[5]) ==null?new ArrayList<String>():readingToLosMap.get(tokens[4]+"$"+tokens[5]);
               	losList.add(tokens[6]+"$"+tokens[7]+"$"+tokens[8]);
               	readingToLosMap.put(tokens[4]+"$"+tokens[5], losList);
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally
        {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		return catalogTable;
	}
	
	
	public  Table<String, String, Entity> constructLOSContentMap(InputStream ins, ContentType contentType)

	{
		Table<String, String, Entity> losToContentMap = HashBasedTable.create();
        
        final String DELIMITER = ",";
        try(BufferedReader fileReader = new BufferedReader(new InputStreamReader(ins))) //Trying AUTO CLOSE
        {
            String line = "";
           
            while ((line = fileReader.readLine()) != null) 
            {
                String[] tokens = line.split(DELIMITER);
                Entity aContent = new Entity(contentType.getContentType());
                aContent.setProperty(ContentType.LOS.getContentType(), tokens[0]);
                aContent.setProperty(ContentType.QUESTION.getContentType(), tokens[1]);
                aContent.setProperty(ContentType.ANSWER.getContentType(), tokens[2]);
                aContent.setProperty(CONTENT_CONSTANT.QUESTION_TEXT, tokens[3]);
                
                aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_A, tokens[4]);
                aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_B, tokens[5]);
                aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_C, tokens[6]);
                aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_D, tokens[7]);
                aContent.setProperty(CONTENT_CONSTANT.QUESTION_TYPE, tokens[8]);
                aContent.setProperty(CONTENT_CONSTANT.ANSWER_OPTION, tokens[9]);
                aContent.setProperty(CONTENT_CONSTANT.ANSWER_TEXT, tokens[10]);
                
                losToContentMap.put(tokens[0], tokens[1], aContent);
                
                /* 1)	Create a enum class to identify different content types
                 * 2)	Dynamically build a content based on content type (come up with a factory)	
                 * 
                 * */
                
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
  
		return losToContentMap;
	}


    public  Table<String, String, Entity> constructLOSContentMapFromExcel(InputStream ins, ContentType contentType, String sheetName)
    {
        Table<String, String, Entity> losToContentMap = HashBasedTable.create();
        try (Workbook workbook = new XSSFWorkbook(ins)) {
            if(!StringUtils.isEmpty(sheetName))
            {
                Sheet firstSheet = workbook.getSheet(sheetName);
                writeSheetContentToTable(contentType, losToContentMap, firstSheet);
            }
            else
            {
                for(int i=0;i<workbook.getNumberOfSheets();i++)
                {
                    Sheet firstSheet = workbook.getSheetAt(i);
                    writeSheetContentToTable(contentType, losToContentMap, firstSheet);
                }
            }
            ins.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return losToContentMap;
    }

    private void writeSheetContentToTable(ContentType contentType, Table<String, String, Entity> losToContentMap, Sheet firstSheet) {
        Iterator<Row> iterator = firstSheet.iterator();
        DataFormatter formatter = new DataFormatter(); 
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();
            Entity aContent = new Entity(contentType.getContentType());

            aContent.setProperty(ContentType.LOS.getContentType(), nextRow.getCell(0).getStringCellValue());
            aContent.setProperty(ContentType.QUESTION.getContentType(), nextRow.getCell(1).getStringCellValue());
            aContent.setProperty(ContentType.ANSWER.getContentType(), nextRow.getCell(2).getStringCellValue());
            aContent.setProperty(CONTENT_CONSTANT.LOS_TEXT, nextRow.getCell(3).getStringCellValue());
            aContent.setProperty(CONTENT_CONSTANT.QUESTION_TEXT, formatter.formatCellValue(nextRow.getCell(4)));

            aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_A, formatter.formatCellValue(nextRow.getCell(5)));
            aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_B, formatter.formatCellValue(nextRow.getCell(6)));
            aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_C, formatter.formatCellValue(nextRow.getCell(7)));
            aContent.setProperty(CONTENT_CONSTANT.QUESTION_OPTION_D, "");
            aContent.setProperty(CONTENT_CONSTANT.QUESTION_TYPE, nextRow.getCell(8).getStringCellValue());
            aContent.setProperty(CONTENT_CONSTANT.ANSWER_OPTION, nextRow.getCell(9).getStringCellValue());
            aContent.setProperty(CONTENT_CONSTANT.ANSWER_TEXT, formatter.formatCellValue(nextRow.getCell(10)));
            losToContentMap.put(nextRow.getCell(0).getStringCellValue(), nextRow.getCell(1).getStringCellValue(), aContent);
        }
    }


}