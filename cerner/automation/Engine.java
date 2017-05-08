package com.cerner.automation;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;


public class Engine {
    public int processRequestCont;
    public String processDescr;
    public String processUnitDescr;
    public String processNum;
    public String processUnitNum;
    private Dictionary dict = new Hashtable();
    private String [] colKey = new String[]{"prcsID","prcsDescr","prcsSeqNum","prcsSeqDescr","driver","action","type","match","param","active","screenShot","onError"};
    public String colValue;
    public String colDriver;
    public String screenShotPath;
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
        System.out.println(dict.get("prcsID")+" "+dict.get("prcsSeqNum"));
        colDriver = (String) dict.get("driver");
        if(colDriver.equals("Web")){
            System.out.println("In Web");
            if(dict.get("action").toString().equals("Navigate")){
                //webDriver.get(dict.get("match").toString());
                webNavigate(webDriver,dict.get("match").toString(),dict.get("screenShot").toString());
            }

        }

        return 0;
    }

    public void copyHashtable(Row row){

        for (int i=0; i<row.getLastCellNum();i++)
        {

            dict.put(colKey[i],row.getCell(i).getRichStringCellValue().getString());
        }

    }

    public void webNavigate(WebDriver webdr,String url,String screenShotFlag) throws InterruptedException{
        webdr.get(url);

        if(screenShotFlag.equals("Y")){
           takeScreenshot(webdr);
        }
    }

    public void takeScreenshot(WebDriver webdr)throws InterruptedException {
        Thread.sleep(4000);
        File src = ((TakesScreenshot) webdr).getScreenshotAs(OutputType.FILE);
        try{
            String screenShotFielName = screenShotPath+"\\"+dict.get("prcsID").toString()+"_"+ dict.get("prcsSeqNum")+"_screenShot.png";
            FileUtils.copyFile(src,new File(screenShotFielName));
        }catch (IOException e){}

    }

    public Node getXMLProcessNode(Document document,Row row,int returnValue){
        copyHashtable(row);
        Element prcsRow = document.createElement("TestCase");
        prcsRow.setAttribute("id",dict.get("prcsID").toString());
        prcsRow.setAttribute("prcsDescr",dict.get("prcsDescr").toString());
        prcsRow.appendChild(getXMLPrcsElement(document,dict,returnValue));
        return  prcsRow;
    }

    public Node getXMLPrcsElement(Document document,Dictionary dict,int returnValue){
        String result;
        if(returnValue==0)
            result = "PASS";
        else
            result = "FAIL";
        if(dict.get("active").toString().equals("I"))
            result ="NORUN";

        Element unitCase = document.createElement("UnitCase");
        Element seqNum = document.createElement("SeqNum");
        Element unitDescr = document.createElement("UnitDescr");
        Element unitActive = document.createElement("Active");
        Element unitResult = document.createElement("Result");
        seqNum.appendChild(document.createTextNode(dict.get("prcsSeqNum").toString()));
        unitDescr.appendChild(document.createTextNode(dict.get("prcsSeqDescr").toString()));
        unitActive.appendChild(document.createTextNode(dict.get("active").toString()));
        unitResult.appendChild(document.createTextNode(result));

        unitCase.appendChild(seqNum);
        unitCase.appendChild(unitDescr);
        unitCase.appendChild(unitActive);
        unitCase.appendChild(unitResult);
        return unitCase;
    }
}

