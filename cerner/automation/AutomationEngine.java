package com.cerner.automation;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Date;
import org.w3c.dom.Element;

// For XML
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.Properties;

/**
 * Created by VC024129 on 5/6/2017.
 */
public class AutomationEngine  {

    public static void main(String []args) throws InterruptedException, NullPointerException,IOException,ArrayIndexOutOfBoundsException, ParserConfigurationException{
        Engine engine = new Engine();
        int prcsReturnValue =0;
        int totalTestCase = 0;
        String prevTestCaseID = " ";
        String currentTestCaseID = " ";
        String currentTestCaseDescr = " ";
        String testCaseStatus = "PASS";
        DateFormat autoDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat htmlFileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date fileDate = new Date();
        String autoFileName = htmlFileDateFormat.format(fileDate).toString();
        System.out.println("File Name : "+autoFileName);
        // For XML//
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("Automation");
        document.appendChild(rootElement);
        Element originalRoot = rootElement;
        // - END- XML
        //String excelFilePath = "C:\\Users\\VC024129\\Documents\\Vijay\\TestFrameWork\\TestParameterPPM4.xlsx";
        //String excelFilePath = "C:\\Users\\VC024129\\Documents\\Vijay\\TestFrameWork\\TestParameterHomeTest.xlsx";
        //String excelFilePath = "C:\\Users\\VC024129\\Documents\\Vijay\\TestFrameWork\\CernerDotCom.xlsx";
        //String excelFilePath = "C:\\Users\\VC024129\\Documents\\Vijay\\TestFrameWork\\CernerDotComDemo.xlsx";
        String excelFilePath = args[0];

        String driverType,driverPath,driverProp,screenShotFilePath,xmlFilePath,xslFilePath,htmlFilePath,htmlFileName;

        System.out.println("Argument Length : "+args.length);
        if(args.length > 0){
            for(int kk=0;kk<args.length;kk++){
                System.out.println("Argument "+kk+" : "+args[kk]);
            }
        }
        // Setting up Web Driver
        try{
            ExcelFileDriver excelFileDriver = new ExcelFileDriver();
            excelFileDriver.excelFilePath = excelFilePath;
            Workbook excelWorkbook = excelFileDriver.open();
            Sheet excelSheetTestCase = null;
            Iterator<Row> rowIterator = null;
            Row row = null;
            //Get Browser Configuration from "Configuration" worksheet
            //Sheet excelSheetTestCase = excelWorkbook.getSheetAt(1);
            //Iterator<Row> rowIterator = excelSheetTestCase.iterator();
            //Row row = rowIterator.next();
            //driverType = row.getCell(0).getRichStringCellValue().getString();
            //driverPath = row.getCell(1).getRichStringCellValue().getString();
            //driverProp = row.getCell(2).getRichStringCellValue().getString();
            //System.out.println(driverType+" "+driverPath+" "+driverProp);
            //engine.setWebDriver(driverType,driverProp,driverPath);
            //Get XML File path
            //----- Start-----
            Properties prop = new Properties();
            FileInputStream input = new FileInputStream("config.properties");
            prop.load(input);
            xmlFilePath = prop.getProperty("XMLFILE");
            xslFilePath = prop.getProperty("XSLTFILE");
            htmlFilePath = prop.getProperty("HTMLFILE");
            screenShotFilePath = prop.getProperty("SCREENSHOTFILE");
            input.close();



            //----- End ------
            /*excelSheetTestCase = excelWorkbook.getSheetAt(3);
            rowIterator = excelSheetTestCase.iterator();
            row = rowIterator.next();
            xmlFilePath = row.getCell(0).getRichStringCellValue().getString();
            row = rowIterator.next();
            xslFilePath = row.getCell(0).getRichStringCellValue().getString();
            row = rowIterator.next();
            //Result HTML File path / name
            htmlFilePath = row.getCell(0).getRichStringCellValue().getString(); */
            htmlFileName = "Automation_"+autoFileName+".html";
            htmlFilePath = htmlFilePath+"/"+htmlFileName;
            //Get Screenshot file path
            /* excelSheetTestCase = excelWorkbook.getSheetAt(2);
            rowIterator = excelSheetTestCase.iterator();
            row = rowIterator.next();
            screenShotFilePath = row.getCell(0).getRichStringCellValue().getString();*/
            engine.setScreenShotPath(screenShotFilePath);
            Sheet sheet = excelWorkbook.getSheetAt(0);
            rowIterator = sheet.iterator();
            int i = 0;
            while (rowIterator.hasNext()){
                Row rowNext = rowIterator.next();
                //Skip the header row
                if (i==0) {
                    i++;
                    continue;
                }
                currentTestCaseID = rowNext.getCell(0).getRichStringCellValue().getString();
                currentTestCaseDescr = rowNext.getCell(1).getRichStringCellValue().getString();
                // Added - Vijay C Start

                if(!currentTestCaseID.equals(prevTestCaseID)){
                    System.out.println("===>TestCaseID Change<=== "+currentTestCaseID);
                    totalTestCase += 1;
                    Element testCase = document.createElement("TestCase");
                    Element prcsID = document.createElement("PrcsID");
                    Element prcsDescr = document.createElement("PrcsDescr");
                    Element tcStatus = document.createElement("Status");
                    Element TimeTaken = document.createElement("TimeTaken");
                    prcsID.appendChild(document.createTextNode(currentTestCaseID));
                    prcsDescr.appendChild(document.createTextNode(currentTestCaseDescr));
                    //Start DTTM //
                    Element startDTTM = document.createElement("StartDTTM");
                    Date startDate = new Date();
                    startDTTM.appendChild(document.createTextNode(autoDateFormat.format(startDate)));

                    //System.out.println("XML Node Gen : "+ testCaseStatus);
                    testCase.appendChild(prcsID);
                    testCase.appendChild(prcsDescr);
                    testCase.appendChild(startDTTM);
                    testCase.appendChild(TimeTaken);
                    originalRoot.appendChild(testCase);
                    rootElement = testCase;

                }
                // Added - Vijay C End
                //System.out.println("Line Status "+ rowNext.getCell(9).getRichStringCellValue().getString());
                // Start Time
                long startTime = System.currentTimeMillis();
                if (rowNext.getCell(9).getRichStringCellValue().getString().equals("A")){

                    prcsReturnValue = engine.processRequest(rowNext);

                    //System.out.println("Prcs Return Value : "+ prcsReturnValue);
                    if(prcsReturnValue > 0 && testCaseStatus.equals("PASS")){
                        testCaseStatus = "FAIL";

                    }
                }
                // End Time
                long endTime = System.currentTimeMillis();
                // Time Taken
                long execTime = endTime - startTime;
                execTime = execTime/1000;
                //System.out.println("Execution Time : "+execTime);

                rootElement.appendChild(engine.getXMLProcessNode(document,rowNext,prcsReturnValue,execTime,engine.errorString,engine.errorStringLong,engine.screenShotName));
                //Resetting error string
                engine.errorString = " ";
                engine.errorStringLong =" ";
                engine.screenShotName = " ";

                //System.out.println("OnError : "+rowNext.getCell(10).getRichStringCellValue().getString()+" prcs : "+prcsReturnValue);
                if(rowNext.getCell(11).getRichStringCellValue().getString().equals("Stop") && prcsReturnValue != 0){
                    break;
                }
                prevTestCaseID = currentTestCaseID;
            }
            //Generate Summary for test execution -- Start   --
            originalRoot.appendChild(engine.generateSummary(document));
            //Generate Summary for test execution -- End   --
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFilePath));
            transformer.transform(source,result);
            //StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source,consoleResult);
            //Thread.sleep(5000);
            //Generate HTML report using XSLT
            System.out.println("--------");
            System.out.println("XML File : "+xmlFilePath);
            System.out.println("XSL File : "+xslFilePath);
            System.out.println("HTML File : "+htmlFilePath);
            TransformerFactory transformerFactoryHTML = TransformerFactory.newInstance();
            Transformer transformHTML = transformerFactoryHTML.newTransformer(new javax.xml.transform.stream.StreamSource(xslFilePath));
            transformHTML.transform(new javax.xml.transform.stream.StreamSource(xmlFilePath), new javax.xml.transform.stream.StreamResult(htmlFilePath));
            //engine.webDriver.get(htmlFilePath);
            //Thread.sleep(5000);
            //engine.webDriver.close();
            excelWorkbook.close();
            System.out.println("---- Summary ---");
            //System.out.println("Total Test Cases : "+ totalTestCase);
            //System.out.println("Test Case Passed : "+ passTestCaseCount);
            //System.out.println("Test Case Failed : "+ failTestCaseCount);
        }catch (NullPointerException e){e.printStackTrace();}
        catch (Exception e){e.printStackTrace();

        }
    }
}
