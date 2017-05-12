package com.cerner.automation;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

//import javax.lang.model.element.Element;
import java.io.IOException;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// For XML
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import java.io.*;

/**
 * Created by VC024129 on 5/6/2017.
 */
public class AutomationEngine  {
    public static void main(String []args) throws InterruptedException, NullPointerException,IOException,ArrayIndexOutOfBoundsException, ParserConfigurationException{
        Engine engine = new Engine();
        int prcsReturnValue =0;

        // For XML//
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("Automation");
        document.appendChild(rootElement);
        // - END- XML
        String excelFilePath = "C:\\Users\\VC024129\\Documents\\Vijay\\TestFrameWork\\TestParameterPPM4.xlsx";
        String driverType,driverPath,driverProp,screenShotFilePath,xmlFilePath,xslFilePath,htmlFilePath;
        // Setting up Web Driver
        try{
            ExcelFileDriver excelFileDriver = new ExcelFileDriver();
            excelFileDriver.excelFilePath = excelFilePath;
            Workbook excelWorkbook = excelFileDriver.open();
            //Get Browser Configuration from "Configuration" worksheet
            Sheet excelSheetTestCase = excelWorkbook.getSheetAt(1);
            Iterator<Row> rowIterator = excelSheetTestCase.iterator();
            Row row = rowIterator.next();
            driverType = row.getCell(0).getRichStringCellValue().getString();
            driverPath = row.getCell(1).getRichStringCellValue().getString();
            driverProp = row.getCell(2).getRichStringCellValue().getString();
            //System.out.println(driverType+" "+driverPath+" "+driverProp);
            engine.setWebDriver(driverType,driverProp,driverPath);
            //Get XML File path
            excelSheetTestCase = excelWorkbook.getSheetAt(3);
            rowIterator = excelSheetTestCase.iterator();
            row = rowIterator.next();
            xmlFilePath = row.getCell(0).getRichStringCellValue().getString();
            row = rowIterator.next();
            xslFilePath = row.getCell(0).getRichStringCellValue().getString();
            row = rowIterator.next();
            htmlFilePath = row.getCell(0).getRichStringCellValue().getString();;
            //Get Screenshot file path
            excelSheetTestCase = excelWorkbook.getSheetAt(2);
            rowIterator = excelSheetTestCase.iterator();
            row = rowIterator.next();
            screenShotFilePath = row.getCell(0).getRichStringCellValue().getString();
            engine.setScreenShotPath(screenShotFilePath);
            Sheet sheet = excelWorkbook.getSheetAt(0);
            rowIterator = sheet.iterator();
            int i = 0;
            Element testCase = document.createElement("TestCase");
            rootElement.appendChild(testCase);
            while (rowIterator.hasNext()){
                Row rowNext = rowIterator.next();
                if (i==0) {
                    i++;
                    continue;
                }
                System.out.println("Line Status "+ rowNext.getCell(9).getRichStringCellValue().getString());
                if (rowNext.getCell(9).getRichStringCellValue().getString().equals("A")){
                    prcsReturnValue = engine.processRequest(rowNext);
                }
                rootElement.appendChild(engine.getXMLProcessNode(document,rowNext,prcsReturnValue));
                System.out.println("OnError : "+rowNext.getCell(10).getRichStringCellValue().getString()+" prcs : "+prcsReturnValue);
                if(rowNext.getCell(11).getRichStringCellValue().getString().equals("Stop") && prcsReturnValue != 0){
                    break;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFilePath));
            transformer.transform(source,result);
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source,consoleResult);
            //Thread.sleep(5000);
            // Generate HTML report using XSLT
            System.out.println("--------");
            System.out.println("XML File : "+xmlFilePath);
            System.out.println("XSL File : "+xslFilePath);
            System.out.println("HTML File : "+htmlFilePath);
            TransformerFactory transformerFactoryHTML = TransformerFactory.newInstance();
            Transformer transformHTML = transformerFactoryHTML.newTransformer(new javax.xml.transform.stream.StreamSource(xslFilePath));
            transformHTML.transform(new javax.xml.transform.stream.StreamSource(xmlFilePath), new javax.xml.transform.stream.StreamResult(htmlFilePath));
            engine.webDriver.get(htmlFilePath);
            Thread.sleep(5000);
            engine.webDriver.close();
            excelWorkbook.close();

        }catch (NullPointerException e){e.printStackTrace();}
        catch (Exception e){e.printStackTrace();}


    }
}
