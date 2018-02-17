package com.cerner.automation;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
// Concurrency
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by Vijay Chendrayan Chandrasekar on 5/6/2017.
 */

class ProcessQueue implements Runnable {

    private String filePathPQ;
    public ProcessQueue(String filePath){
        filePathPQ = filePath;
    }

    public void executeTask()throws InterruptedException, NullPointerException,IOException,ArrayIndexOutOfBoundsException, ParserConfigurationException{
        Engine engine = new Engine();
        File dirVar = new File (".");
        int prcsReturnValue =0;
        int totalTestCase = 0;
        int returnStatusOS = 0;
        boolean controlBlock = false;
        boolean ifBlockBegin = false;
        boolean ifBlockEnd = false;
        boolean ifBlockStatus =false;

        ArrayList<ProcessData> processDataList = new ArrayList<ProcessData>();
        ArrayList<ProcessData> funcDataList = new ArrayList<ProcessData>();
        int loopCount = 0;
        ProcessData processData = new ProcessData();
        //ProcessData funcData = new ProcessData();
        String prevTestCaseID = " ";
        String currentTestCaseID = " ";
        String currentTestCaseDescr = " ";
        String testCaseStatus = "PASS";
        String cmdFileName;
        String[] tempStr = filePathPQ.split("\\\\");
        String tempStr2 = tempStr[tempStr.length-1];
        System.out.println("File Name for temp2 is : "+ tempStr2);
        cmdFileName = tempStr2.substring(0,tempStr2.length()-5);
        System.out.println("The File name is : "+cmdFileName);
        DateFormat autoDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat htmlFileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date fileDate = new Date();
        String autoFileName = htmlFileDateFormat.format(fileDate);
        autoFileName = cmdFileName+"_"+autoFileName;
        System.out.println("File Name : "+autoFileName);
        // For XML//
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("Automation");
        document.appendChild(rootElement);
        Element originalRoot = rootElement;
        // - END- XML
        //String excelFilePath = args[0];
        String excelFilePath = filePathPQ;
        String driverType,driverPath,driverProp,screenShotFilePath,xmlFilePath,xslFilePath,htmlFilePath,htmlFileName;
        String xmlFileName;

        // Setting up Web Driver
        try{
            ExcelFileDriver excelFileDriver = new ExcelFileDriver();
            excelFileDriver.excelFilePath = excelFilePath;
            Workbook excelWorkbook = excelFileDriver.open();
            Sheet excelSheetTestCase = null;
            Iterator<Row> rowIterator = null;
            Iterator<Row> funcRowIterator = null;
            Row row = null;
            //----- End ------
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
            //--- Current Working folder
            System.out.println("Current Folder : "+ dirVar.getAbsolutePath());
            System.setProperty("user.dir",htmlFilePath);
            System.out.println("New Current Folder : "+ dirVar.getAbsolutePath());
            htmlFilePath = htmlFilePath+"/"+autoFileName;
            screenShotFilePath=htmlFilePath+"/"+"Images";
            if(new File(htmlFilePath).mkdir()){
                System.out.println("Directory "+autoFileName+" created");
            }else {
                System.out.println("Failed to create Directory ");
            }
            if(new File(screenShotFilePath).mkdir()){
                System.out.println("Directory "+autoFileName+" created");
            }else {
                System.out.println("Failed to create Directory ");
            }
            //-- End
            htmlFileName = "Auto_"+autoFileName+".html";
            xmlFileName = autoFileName+".xml";
            htmlFilePath = htmlFilePath+"/"+htmlFileName;
            xmlFilePath = xmlFilePath+"/"+xmlFileName;
            //Get Screenshot file path
            engine.setScreenShotPath(screenShotFilePath);
            Sheet sheet = excelWorkbook.getSheetAt(0);
            Sheet funcSheet = excelWorkbook.getSheet("Functions");
            //Load function in memory 17-Feb-2018 Start
            System.out.println("-----Loading functions----");
            funcRowIterator = funcSheet.iterator();
            int funcCount = 0;
            int funcRowCount = 0;
            String currentFuncName = null;
            String preFuncName = null;
            while (funcRowIterator.hasNext()){
                //Skip Header Row
                ProcessData funcDataNew = new ProcessData(funcRowIterator.next());
                if(funcRowCount == 0){
                    funcRowCount += 1;
                    //System.out.println("Skipping Header row");
                    continue;
                }
                funcDataList.add(funcDataNew);
                currentFuncName = funcDataNew.testCaseDescr;
                //System.out.println("Prev Function : "+preFuncName+" Current Function : "+currentFuncName);
                if(!currentFuncName.equals(preFuncName)){
                    funcCount += 1;
                }
                preFuncName = currentFuncName;
                funcRowCount += 1;
            }
            System.out.println(funcCount+ " Function(s) Loaded successfully");
            //Load function in memory 17-Feb-2018 End
            //excelWorkbook.getSheet(s)

            rowIterator = sheet.iterator();
            int i = 0;
            while (rowIterator.hasNext()){
                Row rowNext = rowIterator.next();
                //Skip the header row
                if (i==0) {
                    i++;
                    continue;
                }
                long startTime = System.currentTimeMillis();
                processData.copyData(rowNext);

                // Function Call 17/Feb/2018 - Start
                if(processData.driver.equals("Control") && processData.action.equals("Function") && processData.type.equals("Call") ){
                    // Function Call detected
                    System.out.println("Function Call detected");
                    boolean functionBegin = false;
                    boolean functionEnd = false;
                    Iterator<ProcessData> funcIterator = funcDataList.iterator();
                    while (funcIterator.hasNext()){
                        System.out.println("Inside function while");
                        ProcessData funcData =  funcIterator.next();
                        System.out.println("Func : "+funcData.testCaseDescr+" "+funcData.type+" PrcsData param : "+processData.param);
                        if(funcData.testCaseDescr.equals(processData.param) && funcData.type.equals("Begin")){
                            functionBegin = true;
                            continue;
                        }
                        if(funcData.testCaseDescr.equals(processData.param) && funcData.type.equals("End")){
                            functionEnd = true;
                            continue;
                        }
                        if(functionBegin && functionEnd){

                            break;
                        }

                        if(functionBegin && !functionEnd){
                            funcData.testCaseID = processData.testCaseID;
                            funcData.seqNo = processData.seqNo+"."+funcData.seqNo;
                            System.out.println("Calling from function");
                            prcsReturnValue = engine.processRequest(funcData);
                            // End Time
                            long endTime = System.currentTimeMillis();
                            // Time Taken
                            long execTime = endTime - startTime;
                            execTime = execTime/1000;
                            //System.out.println("Execution Time : "+execTime);
                            rootElement.appendChild(engine.getXMLProcessNode(document,funcData,prcsReturnValue,execTime,engine.errorString,engine.errorStringLong,engine.screenShotName));
                            //Resetting error string
                            engine.errorString = " ";
                            engine.errorStringLong =" ";
                            engine.screenShotName = " ";
                            //System.out.println("OnError : "+rowNext.getCell(10).getRichStringCellValue().getString()+" prcs : "+prcsReturnValue);
                            if(processData.onError.equals("Stop") && prcsReturnValue != 0){
                                break;
                            }
                        }


                    }
                    continue;
                }
                // Function Call 17/Feb/2018 - End
                // Looping Start - Capture Looping block in List
                if(processData.driver.equals("Control")){
                    if(processData.action.equals("For") &&
                            processData.type.equals("Begin")){
                        loopCount = Integer.parseInt(processData.param);
                        controlBlock = true;
                        continue;
                    }
                    if(processData.action.equals("For") &&
                            processData.type.equals("End")){
                        controlBlock = false;
                        continue;
                    }
                }
                // If Control block is identified with for - begin the move subsequent rows to Array List data.
                if(controlBlock){
                    ProcessData pd = new ProcessData(rowNext);
                    processDataList.add(pd);
                    continue;
                }

                Iterator itr = processDataList.iterator();
                int colCount = 0;
                while (loopCount > 0) {
                    while (itr.hasNext()) {
                        ProcessData pdtmp = (ProcessData) itr.next();
                        ProcessData pdtmpSheet = new ProcessData(pdtmp);
                        String paramLoop = pdtmp.param;
                        // 2/Feb/2018 - Start
                        currentTestCaseID = pdtmp.testCaseID;
                        currentTestCaseDescr = pdtmp.testCaseDescr;
                        System.out.println(pdtmp.testCaseID + " -- " + pdtmp.testCaseDescr + " -- "+ pdtmp.seqNo);
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
                        // Start Time

                        if (pdtmp.active.equals("A")){
                            //prcsReturnValue = engine.processRequest(rowNext);
                            if(pdtmp.param.substring(0,2).equals("S:")){
                                String[] cellInfoArray;
                                cellInfoArray = pdtmp.param.split(":");
                                String sheetName = cellInfoArray[1];
                                int cellRow = Integer.parseInt(cellInfoArray[2]);
                                int cellCol = Integer.parseInt(cellInfoArray[3]);
                                cellCol = cellCol+colCount;
                                System.out.println("Sheet "+sheetName+" row "+cellRow+" col "+cellCol);
                                pdtmpSheet.param = excelWorkbook.getSheet(sheetName).getRow(cellRow).getCell(cellCol).toString();
                                System.out.println("Cell Value ===> "+pdtmp.param);
                            }
                            // 14-Feb-2018 Vijay C -- Start
                            if(pdtmp.driver.equals("Control") && pdtmp.action.equals("If")){
                                ifBlockBegin = true;
                                ifBlockEnd = false;

                                if(engine.getBinvalue(pdtmp.param).equals(pdtmp.match)){
                                    System.out.println("IF success");
                                    ifBlockStatus = true;
                                    continue;
                                }else {
                                    ifBlockStatus = false;
                                }
                            }
                            if(pdtmp.driver.equals("Control") && pdtmp.action.equals("End_If")){
                                ifBlockEnd = true;
                                ifBlockBegin = false;
                                ifBlockStatus = false;
                                continue;
                            }
                            if (ifBlockBegin && !ifBlockStatus && !ifBlockEnd){
                                continue;
                            }
                            // 14-Feb-2018 Vijay C -- end
                            prcsReturnValue = engine.processRequest(pdtmpSheet);
                            pdtmp.param = paramLoop;
                            //System.out.println("Prcs Return Value : "+ prcsReturnValue);
                            if(prcsReturnValue > 0 && testCaseStatus.equals("PASS")){
                                testCaseStatus = "FAIL";
                            }
                            //Send test execution status to OS.
                            //if any one of test case is failed then return 1
                            if(prcsReturnValue > 0){
                                returnStatusOS = 1;
                            }
                        }
                        // End Time
                        long endTime = System.currentTimeMillis();
                        // Time Taken
                        long execTime = endTime - startTime;
                        execTime = execTime/1000;
                        //System.out.println("Execution Time : "+execTime);
                        rootElement.appendChild(engine.getXMLProcessNode(document,pdtmp,prcsReturnValue,execTime,engine.errorString,engine.errorStringLong,engine.screenShotName));
                        //Resetting error string
                        engine.errorString = " ";
                        engine.errorStringLong =" ";
                        engine.screenShotName = " ";
                        //System.out.println("OnError : "+rowNext.getCell(10).getRichStringCellValue().getString()+" prcs : "+prcsReturnValue);
                        if(pdtmp.onError.equals("Stop") && prcsReturnValue != 0){
                            break;
                        }
                        prevTestCaseID = currentTestCaseID;
                        // 2/Feb/2018 - End
                    }
                    System.out.println("LoopCount : "+loopCount);
                    itr = processDataList.iterator();

                    colCount += 1;
                    loopCount -= 1;
                }
                processDataList.clear();
                // Looping End

                /*currentTestCaseID = rowNext.getCell(0).getRichStringCellValue().getString();
                currentTestCaseDescr = rowNext.getCell(1).getRichStringCellValue().getString();*/

                currentTestCaseID = processData.testCaseID;
                currentTestCaseDescr = processData.testCaseDescr;
                System.out.println(currentTestCaseID+"---"+currentTestCaseDescr);
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
                //long startTime = System.currentTimeMillis();
                if (processData.active.equals("A")){
                    //prcsReturnValue = engine.processRequest(rowNext);
                    // 14-Feb-2018 Vijay C -- Start
                    if(processData.driver.equals("Control") && processData.action.equals("If")){
                        ifBlockBegin = true;
                        ifBlockEnd = false;
                        // for = (equal)
                        if(processData.type.equals("=")) {
                            if (engine.getBinvalue(processData.param).equals(processData.match)) {
                                System.out.println("IF success");
                                ifBlockStatus = true;
                                continue;
                            } else {
                                ifBlockStatus = false;
                            }
                        }
                        // for > greater than
                        if(processData.type.equals(">")) {
                            int param = Integer.parseInt(processData.param);
                            int match = Integer.parseInt(processData.match);
                            if (match > param) {
                                System.out.println("IF success");
                                ifBlockStatus = true;
                                continue;
                            } else {
                                ifBlockStatus = false;
                            }
                        }
                        // for < less than
                        if(processData.type.equals("<")) {
                            int param = Integer.parseInt(processData.param);
                            int match = Integer.parseInt(processData.match);
                            if (match < param) {
                                System.out.println("IF success");
                                ifBlockStatus = true;
                                continue;
                            } else {
                                ifBlockStatus = false;
                            }
                        }
                        // for >= greater than equal
                        if(processData.type.equals(">=")) {
                            int param = Integer.parseInt(processData.param);
                            int match = Integer.parseInt(processData.match);
                            if (match >= param) {
                                System.out.println("IF success");
                                ifBlockStatus = true;
                                continue;
                            } else {
                                ifBlockStatus = false;
                            }
                        }
                        // for <= less than equal
                        if(processData.type.equals("<=")) {
                            int param = Integer.parseInt(processData.param);
                            int match = Integer.parseInt(processData.match);
                            if (match <= param) {
                                System.out.println("IF success");
                                ifBlockStatus = true;
                                continue;
                            } else {
                                ifBlockStatus = false;
                            }
                        }
                        // for TRUE
                        if(processData.type.equals("TRUE")) {
                            if (engine.getBinvalue(processData.param).equals(processData.match)) {
                                System.out.println("IF success");
                                ifBlockStatus = true;
                                continue;
                            } else {
                                ifBlockStatus = false;
                            }
                        }
                        // for FALSE
                        if(processData.type.equals("FALSE")) {
                            if (engine.getBinvalue(processData.param).equals(processData.match)) {
                                System.out.println("IF success");
                                ifBlockStatus = true;
                                continue;
                            } else {
                                ifBlockStatus = false;
                            }
                        }
                    }
                    if(processData.driver.equals("Control") && processData.action.equals("End_If")){
                        ifBlockEnd = true;
                        ifBlockBegin = false;
                        ifBlockStatus = false;
                        continue;
                    }
                    if (ifBlockBegin && !ifBlockStatus && !ifBlockEnd){
                         continue;
                    }
                    // 14-Feb-2018 Vijay C -- end
                    if(!processData.driver.equals("Control") && !processData.action.equals("Function")) {
                        prcsReturnValue = engine.processRequest(processData);
                    }
                    //System.out.println("Prcs Return Value : "+ prcsReturnValue);
                    if(prcsReturnValue > 0 && testCaseStatus.equals("PASS")){
                        testCaseStatus = "FAIL";
                    }
                    //Send test execution status to OS.
                    //if any one of test case is failed then return 1
                    if(prcsReturnValue > 0){
                        returnStatusOS = 1;
                    }
                }
                // End Time
                long endTime = System.currentTimeMillis();
                // Time Taken
                long execTime = endTime - startTime;
                execTime = execTime/1000;
                //System.out.println("Execution Time : "+execTime);
                rootElement.appendChild(engine.getXMLProcessNode(document,processData,prcsReturnValue,execTime,engine.errorString,engine.errorStringLong,engine.screenShotName));
                //Resetting error string
                engine.errorString = " ";
                engine.errorStringLong =" ";
                engine.screenShotName = " ";
                //System.out.println("OnError : "+rowNext.getCell(10).getRichStringCellValue().getString()+" prcs : "+prcsReturnValue);
                if(processData.onError.equals("Stop") && prcsReturnValue != 0){
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
            //Generate HTML report using XSLT
            System.out.println("--------");
            //System.out.println("XML File : "+xmlFilePath);
            //System.out.println("XSL File : "+xslFilePath);
            System.out.println("HTML File : "+htmlFilePath);
            TransformerFactory transformerFactoryHTML = TransformerFactory.newInstance();
            Transformer transformHTML = transformerFactoryHTML.newTransformer(new javax.xml.transform.stream.StreamSource(xslFilePath));
            transformHTML.transform(new javax.xml.transform.stream.StreamSource(xmlFilePath), new javax.xml.transform.stream.StreamResult(htmlFilePath));
            excelWorkbook.close();
            System.out.println("---- Summary ---");

        }catch (NullPointerException e){e.printStackTrace();}
        catch (Exception e){e.printStackTrace();

        }
    }

    public String getCellData(Workbook workbook, String sheet,int row,int col, int incr){
        col = col+incr;
        return workbook.getSheet(sheet).getRow(row).getCell(col).toString();
    }

    public void run(){

        try {
            executeTask();
            TimeUnit.SECONDS.sleep(2);
        }
        catch (InterruptedException e){
            System.out.println(e.toString());

        }
        catch (IOException e){
            System.out.println(e.toString());
        }
        catch (ParserConfigurationException e){
            System.out.println(e.toString());
        }
    }
}

class ProcessData{
    String testCaseID;
    String testCaseDescr;
    String seqNo;
    String testDescr;
    String driver;
    String action;
    String type;
    String match;
    String param;
    String active;
    String screenShot;
    String onError;


    ProcessData(){

    }
    ProcessData(ProcessData processData){
        this.testCaseID = processData.testCaseID;
        this.testCaseDescr = processData.testCaseDescr;
        this.seqNo = processData.seqNo;
        this.testDescr = processData.testDescr;
        this.driver = processData.driver;
        this.action = processData.action;
        this.type = processData.type;
        this.match = processData.match;
        this.param = processData.param;
        this.active = processData.active;
        this.screenShot = processData.screenShot;
        this.onError = processData.onError;
    }
    ProcessData(Row rowNext){
        this.copyData(rowNext);

    }

    public void copyData(Row rowNext){
        this.testCaseID = rowNext.getCell(0).getRichStringCellValue().getString();
        this.testCaseDescr = rowNext.getCell(1).getRichStringCellValue().getString();
        this.seqNo = rowNext.getCell(2).getRichStringCellValue().getString();
        this.testDescr = rowNext.getCell(3).getRichStringCellValue().getString();
        this.driver = rowNext.getCell(4).getRichStringCellValue().getString();
        this.action = rowNext.getCell(5).getRichStringCellValue().getString();
        this.type = rowNext.getCell(6).getRichStringCellValue().getString();
        this.match = rowNext.getCell(7).getRichStringCellValue().getString();
        this.param = rowNext.getCell(8).getRichStringCellValue().getString();
        this.active = rowNext.getCell(9).getRichStringCellValue().getString();
        this.screenShot = rowNext.getCell(10).getRichStringCellValue().getString();
        this.onError = rowNext.getCell(11).getRichStringCellValue().getString();

    }
}
public class AutomationEngine  {


    public static void main(String []args) throws InterruptedException, NullPointerException,IOException,ArrayIndexOutOfBoundsException, ParserConfigurationException{

        String cmdFilePath = " ";
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        System.out.println("Argument Length : "+args.length);
        if(args.length > 0){
            for(int kk=0;kk<args.length;kk++){
                cmdFilePath = args[kk];
                System.out.println("Argument "+kk+" : "+cmdFilePath);
                ProcessQueue pq = new ProcessQueue(cmdFilePath);
                executor.execute(pq);
            }
            executor.shutdown();
        }
    }
}
