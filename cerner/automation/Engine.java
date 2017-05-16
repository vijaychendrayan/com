package com.cerner.automation;

import jdk.nashorn.internal.ir.CatchNode;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;


class Engine {
    public int processRequestCont;
    public String processDescr;
    public String processUnitDescr;
    public String processNum;
    public String processUnitNum;
    private Dictionary dict = new Hashtable();
    private String [] colKey = new String[]{"prcsID","prcsDescr","prcsSeqNum","prcsSeqDescr","driver","action","type","match","parameter","active","screenShot","onError"};
    public String colValue;
    public String errorString = "NA";
    private String colDriver;
    private String colAction;
    private String screenShotPath;
    private int prcsStatus;
    WebDriver webDriver;


    public  Engine ()
    {

    }

    public void setWebDriver(String driverType,String driverProp,String driverPath){
        System.out.println("---Setting up Web driver---");
        System.out.println(driverType+" "+driverPath+" "+driverProp);
        WebDriverFactory webDriverFactory = new WebDriverFactory();
        webDriver = webDriverFactory.setWebDriver(driverType,driverProp,driverPath);
    }

    public void setScreenShotPath(String path){
        System.out.println("--Setting up ScreenShot File path---");
        screenShotPath = path;
    }

    public int processRequest(Row row) throws InterruptedException{
        copyHashtable(row);
        prcsStatus = 1;
        System.out.println(dict.get("prcsID")+" "+dict.get("prcsSeqNum"));
        colDriver = (String) dict.get("driver");
        colAction = (String) dict.get("action");
        //Navigate
        System.out.println("Driver : "+colDriver+" colAction : "+colAction);
        if(colDriver.equals("Web")) {
            System.out.println("In Web ***");
            if (dict.get("action").toString().equals("Navigate")) {
                //webDriver.get(dict.get("match").toString());
                prcsStatus = webNavigate(webDriver, dict.get("match").toString(), dict.get("screenShot").toString());
            }
            //Window Event
            if (colAction.equals("Window")) {
                System.out.println("In Window even handler");
                prcsStatus = windowEventhandler(webDriver, dict);
            }
            //Send Keys
            if (colAction.equals("SendKeys")) {
                System.out.println("In Send keys handler");
                prcsStatus = sendKeysEventHandler(webDriver, dict);
            }
            //Clear
            if(colAction.equals("Clear")){
                System.out.println("In Clear event");
                prcsStatus = clearEventhandler(webDriver,dict);
            }
            //Click Event
            if (colAction.equals("Click")) {
                System.out.println("In Click handler");
                prcsStatus = clickEventHandler(webDriver, dict);
            }
            //Compare Event
            if (colAction.equals("Compare")) {
                System.out.println("In Compare handler");
                prcsStatus = compareEventHandler(webDriver, dict);
            }
            if(colAction.equals("CheckMinificaiton")){
                System.out.println("In CheckMinificaiton");
                prcsStatus = checkMinification(webDriver,dict);
            }
        }

        if(colDriver.equals("Time")){
            prcsStatus = timeEvenHandler(dict);
        }
        return prcsStatus;
    }

    private void copyHashtable(Row row){

        for (int i=0; i<row.getLastCellNum();i++)
        {

            dict.put(colKey[i],row.getCell(i).getRichStringCellValue().getString());
        }

    }

    private int webNavigate(WebDriver webdr, String url, String screenShotFlag) throws InterruptedException{
        try {
            webdr.get(url);

            if (screenShotFlag.equals("Y")) {
                takeScreenshot(webdr);
            }
        }
        catch (Exception e){
            errorString = e.toString();
            return 1;

        }

        return 0;
    }

    private int windowEventhandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        Dimension dimension;
        try{

            if(dict.get("type").toString().equals("Maximize")){
                webdr.manage().window().maximize();
            }
            if(dict.get("type").toString().equals("Minimize")){
                int width=0,height =0;
                String[] dimen = dict.get("parameter").toString().split(",");
                width = Integer.parseInt(dimen[0]) ;
                height = Integer.parseInt(dimen[1]) ;
                dimension = new Dimension(width,height);
                webdr.manage().window().setSize(dimension);
            }
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }

        }catch (Exception e){
            errorString = e.toString();
            return 1;
        }
        return 0;

    }

    private int sendKeysEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            // By Xpath
            if(dict.get("type").toString().equals("Xpath")){
                webElement = webdr.findElement(By.xpath(dict.get("match").toString()));
                webElement.sendKeys(dict.get("parameter").toString());
            }
            // ID
            if(dict.get("type").toString().equals("ID")){
                webElement = webdr.findElement(By.id(dict.get("match").toString()));
                webElement.sendKeys(dict.get("parameter").toString());
            }
            // Class Name
            if(dict.get("type").toString().equals("ClassName")){
                webElement = webdr.findElement(By.className(dict.get("match").toString()));
                webElement.sendKeys(dict.get("parameter").toString());
            }
            //CSS Selector
            if(dict.get("type").toString().equals("CSSSelector")){
                webElement = webdr.findElement(By.cssSelector(dict.get("match").toString()));
                webElement.sendKeys(dict.get("parameter").toString());
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = e.toString();
            return 1;
        }
        return 0;
    }

    private int clearEventhandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            // By Xpath
            if(dict.get("type").toString().equals("Xpath")){
                webElement = webdr.findElement(By.xpath(dict.get("match").toString()));
                webElement.clear();
            }
            // ID
            if(dict.get("type").toString().equals("ID")){
                webElement = webdr.findElement(By.id(dict.get("match").toString()));
                webElement.clear();
            }
            // Class Name
            if(dict.get("type").toString().equals("ClassName")){
                webElement = webdr.findElement(By.className(dict.get("match").toString()));
                webElement.clear();
            }
            //CSS Selector
            if(dict.get("type").toString().equals("CSSSelector")){
                webElement = webdr.findElement(By.cssSelector(dict.get("match").toString()));
                webElement.clear();
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = e.toString();
            return 1;
        }
        return 0;
    }


    private int clickEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            // By Xpath
            if(dict.get("type").toString().equals("Xpath")){
                webElement = webdr.findElement(By.xpath(dict.get("match").toString()));
                webElement.click();
            }
            // ID
            if(dict.get("type").toString().equals("ID")){
                webElement = webdr.findElement(By.id(dict.get("match").toString()));
                webElement.click();
            }
            // Class Name
            if(dict.get("type").toString().equals("ClassName")){
                webElement = webdr.findElement(By.className(dict.get("match").toString()));
                webElement.click();
            }
            //CSS Selector
            if(dict.get("type").toString().equals("CSSSelector")){
                webElement = webdr.findElement(By.cssSelector(dict.get("match").toString()));
                webElement.click();
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = e.toString();
            return 1;
        }



        return 0;
    }

    private int compareEventHandler(WebDriver webdr, Dictionary dict) throws InterruptedException{
        WebElement webElement;
        int returnFlag = 1;
        try{

            // Check for Page Title
            if(dict.get("type").toString().equals("PageTitle")) {
                if (webdr.getTitle().equals(dict.get("parameter").toString())) {
                    System.out.println("====>Title Matched<====");
                    returnFlag = 0;
                } else {
                    System.out.println("====>Title NOT Matched<===");
                    errorString = "====>Title NOT Matched<===";
                    returnFlag = 1;
                }
            }
            // Check for string value compare.
            // Xpath
            if(dict.get("type").toString().equals("Xpath")){
                webElement = webdr.findElement(By.xpath(dict.get("match").toString()));
                if(webElement.getText().equals(dict.get("parameter").toString())){
                    System.out.println("====>Sting Matched<====");
                    returnFlag = 0;
                }
                else {
                    System.out.println("====>String NOT Matched<===");
                    errorString = "====>Sting NOT Matched<===";
                    returnFlag = 1;
                }
            }
            // ID
            if(dict.get("type").toString().equals("ID")){
                webElement = webdr.findElement(By.id(dict.get("match").toString()));
                if(webElement.getText().equals(dict.get("parameter").toString())){
                    System.out.println("====>Sting Matched<====");
                    returnFlag = 0;
                }
                else
                {
                    System.out.println("====>String NOT Matched<===");
                    errorString = "====>Sting NOT Matched<===";
                    returnFlag = 1;
                }
            }
            // CSS Selector
            if(dict.get("type").toString().equals("CSSSelector")){
                webElement = webdr.findElement(By.cssSelector(dict.get("match").toString()));
                if(webElement.getText().equals(dict.get("parameter").toString())){
                    System.out.println("====>Sting Matched<====");
                    returnFlag = 0;
                }
                else
                {
                    System.out.println("====>String NOT Matched<===");
                    errorString = "====>Sting NOT Matched<===";
                    returnFlag = 1;
                }
            }
            // Class Name
            if(dict.get("type").toString().equals("ClassName")){
                webElement = webdr.findElement(By.className(dict.get("match").toString()));
                if(webElement.getText().equals(dict.get("parameter").toString())){
                    System.out.println("====>Sting Matched<====");
                    returnFlag = 0;
                }
                else
                {
                    System.out.println("====>String NOT Matched<===");
                    errorString = "====>Sting NOT Matched<===";
                    returnFlag = 1;
                }
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }


        }catch (Exception e){
            errorString = e.toString();
            returnFlag = 1;
        }

        return  returnFlag;

    }

    private int checkMinification(WebDriver webdr, Dictionary dict){
        String pageSource = " ";
        String returnMinfiResult = " ";
        int returnFlag = 0;
        int newLineCoutn = 0;

        try {
            pageSource = webdr.getPageSource();
            for (String str : pageSource.split("\n|\r")) {
                newLineCoutn++;

            }
            returnMinfiResult = "There are(is) "+ String.valueOf(newLineCoutn)+" new line/carriage return character found";
            errorString = returnMinfiResult;

        }catch (Exception e){
            errorString = e.toString();
            returnFlag = 1;

        }

        return returnFlag;
    }

    private int timeEvenHandler(Dictionary dict)throws InterruptedException{
        System.out.println("In TimeEvenHandler");
        String varTime = " ";
        if(dict.get("action").toString().equals("DelayBy")){
          varTime = dict.get("parameter").toString();
          Thread.sleep(Long.valueOf(varTime).longValue());
        }
        return 0;

    }

    private void takeScreenshot(WebDriver webdr)throws InterruptedException {
        Thread.sleep(4000);
        File src = ((TakesScreenshot) webdr).getScreenshotAs(OutputType.FILE);
        try{
            String screenShotFielName = screenShotPath+"\\"+dict.get("prcsID").toString()+"_"+ dict.get("prcsSeqNum")+"_screenShot.png";
            FileUtils.copyFile(src,new File(screenShotFielName));
        }catch (IOException e){}

    }

   /* public Node getXMLProcessNode(Document document,Row row,int returnValue, String errorString){
        copyHashtable(row);
        Element prcsRow = document.createElement("TestCase");
        prcsRow.appendChild(getXMLPrcsElement(document,dict,returnValue,errorString));

        return  prcsRow;
    }*/

    public Node getXMLProcessNode(Document document,Row row,int returnValue,String errorString ){
        copyHashtable(row);
        String activeRow,result = "NORUN";
        activeRow =  dict.get("active").toString();
        if (activeRow.equals("A")){
            if(returnValue==0)
                result = "PASS";
            else
                result = "FAIL";

        }
        Element unitCase = document.createElement("TestUnit");
        //Element prcsID = document.createElement("PrcsID");
        //Element prcsDescr = document.createElement("PrcsDescr");
        Element seqNum = document.createElement("SeqNum");
        Element unitDescr = document.createElement("UnitDescr");
        Element unitActive = document.createElement("Active");
        Element unitResult = document.createElement("Result");
        Element errorStr = document.createElement("Exception");
        //prcsID.appendChild(document.createTextNode(dict.get("prcsID").toString()));
        //prcsDescr.appendChild(document.createTextNode(dict.get("prcsDescr").toString()));
        seqNum.appendChild(document.createTextNode(dict.get("prcsSeqNum").toString()));
        unitDescr.appendChild(document.createTextNode(dict.get("prcsSeqDescr").toString()));
        unitActive.appendChild(document.createTextNode(dict.get("active").toString()));
        unitResult.appendChild(document.createTextNode(result));
        errorStr.appendChild(document.createTextNode(errorString));

        //unitCase.appendChild(prcsID);
        //unitCase.appendChild(prcsDescr);
        unitCase.appendChild(seqNum);
        unitCase.appendChild(unitDescr);
        unitCase.appendChild(unitActive);
        unitCase.appendChild(unitResult);
        unitCase.appendChild(errorStr);
        return unitCase;
    }
}

