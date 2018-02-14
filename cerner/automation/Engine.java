package com.cerner.automation;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

class Engine {
    //public int processRequestCont;
    //public String processDescr;
    //public String processUnitDescr;
    //public String processNum;
    //public String processUnitNum;
    private Dictionary dict = new Hashtable();
    private Map<String,String> bindValue = new HashMap<String, String>();
    private String [] colKey = new String[]{"prcsID","prcsDescr","prcsSeqNum","prcsSeqDescr","driver","action","type","match","parameter","active","screenShot","onError"};
    //public String colValue;
    public String errorString = "NA";
    public String errorStringLong = "NA";
    public String screenShotName = " ";
    private String colDriver;
    private String colAction;
    private String screenShotPath;
    private int prcsStatus;
    private String key = "eMxzpUKnLmW1qfOeb9cEeg==";
    WebDriver webDriver;

    public  Engine ()
    {

    }

    public void setWebDriver(String driverType,String driverProp,String driverPath) throws MalformedURLException{
        System.out.println("---Setting up Web driver---");
        System.out.println(driverType+" "+driverPath+" "+driverProp);
        WebDriverFactory webDriverFactory = new WebDriverFactory();
        webDriver = webDriverFactory.setWebDriver(driverType,driverProp,driverPath);
    }

    public void setScreenShotPath(String path){
        //System.out.println("--Setting up ScreenShot File path---");
        screenShotPath = path;
    }

    public int processRequest(ProcessData processData) throws InterruptedException, MalformedURLException, ParserConfigurationException,TransformerException,IOException{
        copyHashTable(processData);
        prcsStatus = 1;

        //System.out.println(dict.get("prcsID")+" "+dict.get("prcsSeqNum"));
        colDriver = (String) dict.get("driver");
        colAction = (String) dict.get("action");
        String driverProp = null;
        String driverPath = null;
        String logString = "======>"+dict.get("prcsSeqNum")+" : "+dict.get("prcsSeqDescr")+" : "+dict.get("driver");
        logString = logString +" : "+dict.get("action")+dict.get("type")+" : "+dict.get("match");
        logString = logString +" : "+dict.get("parameter")+" : "+dict.get("active");
        logString = logString +" : "+dict.get("screenShot")+" : "+dict.get("onError")+"<=====";


        //Navigate
        //System.out.println("Driver : "+colDriver+" colAction : "+colAction);
        if(colDriver.equals("Web") || colDriver.equals("Mobile")) {
            //Setting up WebDriver
            if (dict.get("action").toString().equals("SetDriver")) {
                ClassLoader classLoader = getClass().getClassLoader();
                Properties prop = new Properties();
                FileInputStream input = null;
                try{
                    //input = new FileInputStream(classLoader.getResource("DriverConfig/WebDriverConfig.properties").getFile());
                    input = new FileInputStream("WebDriverConfig.properties");
                    //TempFix
                    //input = new FileInputStream("C:\\Users\\VC024129\\IdeaProjects\\Automation\\src\\Web.properties");
                    //input = new FileInputStream(new File("src\\main\\resources\\DriverConfig\\WebDriverConfig.properties").getAbsolutePath());
                    prop.load(input);
                }catch (Exception e){
                    System.out.println("Missing WebDriver Property file");
                    e.printStackTrace();
                }

                //Chrome
                if(dict.get("type").toString().equals("Chrome")){
                    driverProp = "webdriver.chrome.driver";
                    driverPath = prop.get("CHROME").toString();
                    System.out.println(driverPath);
                    setWebDriver("CHROME",driverProp,driverPath);
                }
                //FireFox
                if(dict.get("type").toString().equals("FireFox")){
                    driverProp = "webdriver.gecko.driver";
                    driverPath = prop.get("FIREFOX").toString();
                    setWebDriver("FIREFOX",driverProp,driverPath);
                }
                //MSFTEDGE
                if(dict.get("type").toString().equals("Edge")){

                    driverProp = "webdriver.edge.driver";
                    driverPath = prop.get("MSFTEDGE").toString();
                    setWebDriver("MSFTEDGE",driverProp,driverPath);

                }
                //MSFTIE
                if(dict.get("type").toString().equals("IE")){

                    driverProp = "webdriver.ie.driver";
                    driverPath = prop.get("MSFTIE").toString();
                    setWebDriver("MSFTIE",driverProp,driverPath);

                }
                //OPERA
                if(dict.get("type").toString().equals("Opera")){

                    driverProp = "webdriver.opera.driver";
                    driverPath = prop.get("OPERA").toString();
                    setWebDriver("OPERA",driverProp,driverPath);

                }
                //Safari
                // AndroidChrome
                if(dict.get("type").toString().equals("AndroidChrome")){
                    driverProp = "NA";
                    driverPath = "NA";
                    try {
                        setWebDriver("ANDROIDCHROME", driverProp, driverPath);
                    }catch (Exception e){
                        errorStringLong = e.getStackTrace().toString();
                        errorString = "Unable to initialize Mobile Chrome driver";
                        prcsStatus = 1;
                        return prcsStatus;
                    }
                }
                prcsStatus = 0;
            }
            if (dict.get("action").toString().equals("CloseDriver")) {
                System.out.println("In Close Driver");
                try {
                    webDriver.close();
                    prcsStatus = 0;
                }catch (Exception e){
                    errorString = "Unable to close webdriver";
                    errorStringLong = e.getStackTrace().toString();
                }

            }
            //System.out.println("In Web ***");
            if (dict.get("action").toString().equals("Navigate")) {
                //webDriver.get(dict.get("match").toString());
                //prcsStatus = webNavigate(webDriver, dict.get("match").toString(), dict.get("screenShot").toString());
                prcsStatus = webNavigateHandler(webDriver, dict);
            }
            //Window Event
            if (colAction.equals("Window")) {
                //System.out.println("In Window even handler");
                prcsStatus = windowEventHandler(webDriver, dict);
            }
            //Send Keys
            if (colAction.equals("SendKeys")) {
                //System.out.println("In Send keys handler");
                prcsStatus = sendKeysEventHandler(webDriver, dict);
            }

            //Send Keys Encrypted
            if (colAction.equals("SendKeysEncrypted")) {
                //System.out.println("In Send keys handler");
                prcsStatus = sendKeysEncryptionEventHandler(webDriver, dict);
            }
            // Send Text/Input
            //if (colAction.equals("SendKeys")) {
                // System.out.println("In Send keys handler");
            //    prcsStatus = sendKeysEventHandler(webDriver, key);
            //}
            //Clear
            if(colAction.equals("Clear")){
                //System.out.println("In Clear event");
                prcsStatus = clearEventHandler(webDriver,dict);
            }
            //Click Event
            if (colAction.equals("Click")) {
                //System.out.println("In Click handler");
                prcsStatus = clickEventHandler(webDriver, dict);
            }
            //Compare Event
            if (colAction.equals("Compare") && !dict.get("type").toString().equals("PageTitle")) {
                //System.out.println("In Compare handler");
                prcsStatus = compareEventHandler(webDriver, dict);
            }
            //Compare Page Title
            if (colAction.equals("Compare") && dict.get("type").toString().equals("PageTitle") ) {
                //System.out.println("In Compare Page Title");
                prcsStatus = comparePageTitle(webDriver, dict);
            }
            if(colAction.equals("CheckMinificaiton")){
                //System.out.println("In CheckMinificaiton");
                prcsStatus = checkMinification(webDriver,dict);
            }
            if(colAction.equals("CheckImageLoad")){
                //System.out.println("In CheckImageLoad");
                prcsStatus = checkImageLoad(webDriver,dict);
            }
            if(colAction.equals("CheckPageImageLoad")){
                //System.out.println("In CheckImageLoad");
                prcsStatus = checkPageImages(webDriver);
            }
            if(colAction.equals("Crawl")){
                prcsStatus = crawlLinks(webDriver);
            }

            if (colAction.equals("Hover")) {
                //System.out.println("In Compare handler");
                prcsStatus = hoverEventHandler(webDriver, dict);
            }
            if (colAction.equals("SwitchTab")) {
                //System.out.println("In Compare handler");
                prcsStatus = switchTab(webDriver, dict);
            }
            if (colAction.equals("IsDisplayed")) {
                prcsStatus = checkFieldIsPresent(webDriver, dict);
            }
            if (colAction.equals("CheckDropDownOptions")) {
                prcsStatus = checkDropDownOptions(webDriver, dict);
            }
            if (colAction.equals("Store")) {
                prcsStatus = storeBindValue(webDriver, dict);
            }
            //Accept Alert - Rachithra
            if(colAction.equals("AcceptAlert")){
                prcsStatus = acceptAlert(dict);
            }
            //Dismiss Alert - Rachithra
            if(colAction.equals("DismissAlert")){
                prcsStatus = dismissAlert(dict);
            }
            //IsEnabled - Rachithra
            if(colAction.equals("IsEnabled")){
                prcsStatus=isElementEnabled(webDriver,dict);
            }
            //IsEnabled - Rachithra
            if(colAction.equals("SendText")){
                prcsStatus=sendTextEventHandler(webDriver,dict);
            }

            //IsEnabled - Rachithra
            if(colAction.equals("IsSelected")){
                prcsStatus=isElementSelected(webDriver,dict);
            }


            //GetTagName
            if(colAction.equals("CompareTagName")){
                prcsStatus= getTagName(webDriver,dict);
            }

            //GetText
            if(colAction.equals("CompareText")){
                //prcsStatus= getTagName(webDriver,dict);
            }

            // Submit
            if(colAction.equals("Submit")){
                //prcsStatus= submitEvent(webDriver,dict);
            }

            // GetAttribute and Compare
            if(colAction.equals("CompareAttribute")){
                prcsStatus= compareAttribute(webDriver,dict);
            }

            // GetSize and Compare
            if(colAction.equals("CheckSize")){
                //prcsStatus= compareSize(webDriver,dict);
            }
            // GetLocation and Compare
            if(colAction.equals("CheckLocation")){
                //prcsStatus= compareLocation(webDriver,dict);
            }
            // Get CSS Value
            if(colAction.equals("CheckCssValue")){
                //prcsStatus= compareCssValue(webDriver,dict);
            }


        }

        if(colDriver.equals("Time")){
            if(colAction.equals("DelayBy")){
                prcsStatus = timeDelayBy(dict);
            }
            if(colAction.equals("PrintDateTime")){
                prcsStatus = printDateTime(dict);
            }
        }

        if(colDriver.equals("Encrypt")){
            try {
                  System.out.println("----------------------------------------------------------");
                  System.out.println(dict.get("parameter").toString() + " -> Encrypted String to -> "+encryptString(dict.get("parameter").toString(), stringToKey(key)));
                  System.out.println("----------------------------------------------------------");
                           }
            catch (Exception e){
                errorString = "Unable to Encrypt the given string";
                errorStringLong = e.toString();
                return 1;
            }


        }
        System.out.println(logString+"===>Return : "+prcsStatus);
        if(prcsStatus==1){
            if(webDriver != null)
                webDriver.navigate().refresh();
        }
        return prcsStatus;
    }

    private void copyHashTable(ProcessData pd){


        dict.put(colKey[0],pd.testCaseID );
        dict.put(colKey[1],pd.testCaseDescr );
        dict.put(colKey[2],pd.seqNo );
        dict.put(colKey[3],pd.testDescr );
        dict.put(colKey[4],pd.driver );
        dict.put(colKey[5],pd.action );
        dict.put(colKey[6],pd.type );
        dict.put(colKey[7],pd.match );
        dict.put(colKey[8],pd.param );
        dict.put(colKey[9],pd.active );
        dict.put(colKey[10],pd.screenShot );
        dict.put(colKey[11],pd.onError );



    }


    /*private void copyHashTable(Row row){

        for (int i=0; i<row.getLastCellNum();i++)
        {
            dict.put(colKey[i],row.getCell(i).getRichStringCellValue().getString());
        }
    }*/

    private int webNavigateHandler(WebDriver webdr, Dictionary dict) throws InterruptedException{
        int returnFlag = 0;
        errorString = " ";
        errorStringLong =" ";
        System.out.println("InWebNavigate");

        try {
                if(dict.get("type").toString().equals("Get")){
                    //webDriver.get(dict.get("match").toString());
                    webdr.get(dict.get("parameter").toString());
                }
                if(dict.get("type").toString().equals("Refresh")){
                    webdr.navigate().refresh();
                }

                if(dict.get("type").toString().equals("Forward")){
                    webdr.navigate().forward();
                }
                if(dict.get("type").toString().equals("Backward")){
                    webdr.navigate().back();
                }

                if (dict.get("screenShot").toString().equals("Y")) {
                    takeScreenshot(webdr);
                }

        }
        catch (Exception e){
            errorString = "Web Navigation Error";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return returnFlag;
    }

    private int windowEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        Dimension dimension;
        String jScript;
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

            if(dict.get("type").toString().equals("Scroll")){
                int width=0,height =0;
                String[] dimen = dict.get("parameter").toString().split(",");
                width = Integer.parseInt(dimen[0]) ;
                height = Integer.parseInt(dimen[1]) ;
                dimension = new Dimension(width,height);
                JavascriptExecutor scroll = (JavascriptExecutor)webdr;
                jScript = "scroll("+dimen[0]+","+dimen[1]+")";
                scroll.executeScript(jScript,"");
            }

            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Window Max/Min Error";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private int isElementEnabled(WebDriver webdr, Dictionary dict) throws InterruptedException{
        boolean element;
        int returnFlag = 1;
        try{
            element = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString()).isEnabled();
            if(element==true){
                System.out.println("====>Element is Enabled<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>Element NOT Enabled<===");
                errorString = "====>Element NOT Enabled<===";
                errorStringLong =  "====>Element NOT Enabled<===";
                returnFlag = 1;
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element is not enabled";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return  returnFlag;
    }

    private int isElementSelected(WebDriver webdr, Dictionary dict) throws InterruptedException{
        boolean element;
        int returnFlag = 1;
        try{
            element = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString()).isSelected();
            if(element==true){
                System.out.println("====>Element is Selected<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>Element NOT Selected<===");
                errorString = "====>Element NOT Selected<===";
                errorStringLong =  "====>Element NOT Selected<===";
                returnFlag = 1;
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element is not enabled";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return  returnFlag;
    }

    private int submitEvent(WebDriver webdr, Dictionary dict) throws InterruptedException{

        WebElement webElement;
        int returnFlag = 1;
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());

            if(webElement!=null){
                webElement.submit();
                System.out.println("====>Submitted successfully<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>Element NOT Found<===");
                errorString = "====>Element NOT Found<===";
                errorStringLong =  "====>Element NOT Found<===";
                returnFlag = 1;
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element is not enabled";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return  returnFlag;
    }
    private int sendTextEventHandler(WebDriver webdr, Dictionary dict) throws InterruptedException {
        WebElement webElement;
        try {
            webElement = getWebElement(webdr, dict.get("type").toString(), dict.get("match").toString());
            webElement.sendKeys(dict.get("parameter").toString() + Keys.ENTER);
            // Take ScreenShot
            if (dict.get("screenShot").toString().equals("Y")) {
                takeScreenshot(webdr);
            }
        } catch (Exception e) {
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private int sendKeysEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        String param;
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            // Check if bind variable is passed in parameter
            if (dict.get("parameter").toString().substring(0,1).equals(":")){

                param = bindValue.get(dict.get("parameter").toString());
                System.out.println("sendKeysEventHandler -- param bind -- : "+param);
            }else{
                param = dict.get("parameter").toString();
                System.out.println("sendKeysEventHandler -- param non bind -- : "+param);
            }
            webElement.sendKeys(param);
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        /*try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            webElement.sendKeys(dict.get("parameter").toString());
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }*/
        return 0;
    }

    private int sendKeysEncryptionEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        String param;
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            param = dict.get("parameter").toString();
            webElement.sendKeys(decryptString(stringToByte(param),stringToKey(key)));
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }

        return 0;
    }

    private int clearEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            webElement.clear();
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private int clickEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            webElement.click();
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private int hoverEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        Actions action= new Actions(webDriver);
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            action.moveToElement(webElement).build().perform();
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private int switchTab(WebDriver webdr, Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            for(String windowHandle: webdr.getWindowHandles())
            {
                webdr.switchTo().window(windowHandle);
            }
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private int comparePageTitle(WebDriver webdr, Dictionary dict) throws InterruptedException{
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
                    errorStringLong =  "====>Title NOT Matched<===";
                    returnFlag = 1;
                }
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return  returnFlag;
    }

    private int compareEventHandler(WebDriver webdr, Dictionary dict) throws InterruptedException{
        WebElement webElement;
        int returnFlag = 1;
        String param;
        System.out.println("CompareEventHandler");
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            // Check if bind variable is passed in parameter
            if (dict.get("parameter").toString().substring(0,1).equals(":")){

                param = bindValue.get(dict.get("parameter").toString());
                System.out.println("CompareEventHandler -- param bind -- : "+param);
            }else{
                param = dict.get("parameter").toString();
                System.out.println("CompareEventHandler -- param non bind -- : "+param);
            }
            if(webElement.getText().equals(param)){
                System.out.println("====>String Matched<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>String NOT Matched<===");
                errorString = "====>Compare string NOT Matched<===";
                errorStringLong =  "====>Compare string NOT Matched<===";
                returnFlag = 1;
            }
            /*if(webElement.getText().equals(dict.get("parameter").toString())){
                System.out.println("====>String Matched<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>String NOT Matched<===");
                errorString = "====>Compare string NOT Matched<===";
                errorStringLong =  "====>Compare string NOT Matched<===";
                returnFlag = 1;
            }*/
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return  returnFlag;
    }


    private int compareAttribute(WebDriver webdr, Dictionary dict) throws InterruptedException{
        WebElement webElement;
        int returnFlag = 1;
        String param;
        String[] attrib;
        System.out.println("CompareAttribute");
        try{

            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());

            // Check if bind variable is passed in parameter
            param = dict.get("parameter").toString();

            attrib = param.split(":");

            if(webElement.getAttribute(attrib[0]).equals(attrib[1])){
                System.out.println("====>Attribute Matched<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>Attribute NOT Matched<===");
                errorString = "====>Attribute NOT Matched<===";
                errorStringLong =  "====>Attribute NOT Matched<===";
                returnFlag = 1;
            }

            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return  returnFlag;
    }




    private int checkMinification(WebDriver webdr, Dictionary dict) throws MalformedURLException{
        String pageSource = " ";
        String returnMinfiResult = " ";
        String currentURL = " ";
        String assetURLForMsg = " ";
        List resourceURL = new ArrayList();
        int returnFlag = 0;
        int newLineCoutn = 0;
        currentURL = webdr.getCurrentUrl();
        String originalUrl = currentURL;
        System.out.print("Current URl : "+currentURL);
        currentURL = getDomainURL(currentURL);
        System.out.println("Current Domain URL :"+currentURL);
        //currentURL = "https://"+currentURL;

        // Read pageSource and find .css and .js files belong to Cerner.
        // load each item to list.
        // Get source for each list item and check for minification.
        // append the result to errorString.
        try {
            pageSource = webdr.getPageSource();
            System.out.println("Before getResourceURL");
            resourceURL = getResourceURL(pageSource);
            System.out.println("After getResourceURL");
            System.out.println("Resource URL "+ resourceURL);
            Iterator iterator = resourceURL.iterator();
            while (iterator.hasNext()){
                String assetURL = iterator.next().toString();

                if(assetURL.contains(".com")){
                    System.out.println("Contains .com");
                    //continue;
                }
                assetURLForMsg = assetURL;
                assetURL = currentURL+assetURL;
                System.out.println("Asset URL :"+ assetURL);
                System.out.println("Navigat to Asset URL");
                webdr.get(assetURL);
                pageSource = webdr.getPageSource();
                for (String str : pageSource.split("\n|\r")) {
                    newLineCoutn++;
                }
                returnMinfiResult = returnMinfiResult+assetURLForMsg +": "+"There are(is) "+ String.valueOf(newLineCoutn)+" CRLF >>>";

                if(newLineCoutn > 2){

                    returnFlag = 1;
                }
            }

            //returnMinfiResult = "There are(is) "+ String.valueOf(newLineCoutn)+" new line/carriage return character found";

        }catch (Exception e){
            errorString = "Minification issue";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        webdr.get(originalUrl);
        pageSource = webdr.getPageSource();
        for (String str : pageSource.split("\n|\r")) {
            newLineCoutn++;
        }
        returnMinfiResult = returnMinfiResult+assetURLForMsg +": "+"There are(is) "+ String.valueOf(newLineCoutn)+" new line/carriage return character found >>";
        errorString = "Minification Result";
        errorStringLong = returnMinfiResult;
        return returnFlag;
    }

    private List getResourceURL(String pageSource){
        System.out.println("Inside getResourceURL");
        List resultResourceURL = new ArrayList();
        String getCss ="href(\\s+=|=)(\\s+\"/|\"/).*?\\.css";
        String getJs ="src(\\s+=|=)(\\s+\"/|\"/).*?\\.js";
        String getQuotes = ".*=\"";
        Pattern css = Pattern.compile(getCss);
        Pattern js =  Pattern.compile(getJs);
        Pattern quotes = Pattern.compile(getQuotes);
        Matcher matchCss = css.matcher(pageSource);
        Matcher matchJs = js.matcher(pageSource);

        while (matchCss.find()){
            Matcher matchQuote = quotes.matcher(matchCss.group());
            resultResourceURL.add(matchQuote.replaceFirst(""));

            //resultResourceURL.add(matchCss.group());
        }
        while (matchJs.find()){
            Matcher matchQuote = quotes.matcher(matchJs.group());
            resultResourceURL.add(matchQuote.replaceFirst(""));
            //resultResourceURL.add(matchJs.group());
        }
        System.out.println("before return resultResourceURL "+resultResourceURL);
        return resultResourceURL;
    }

    private List getImageResourceURL(String pageSource){
        System.out.println("Inside getImageResourceURL");
        List resultImgResourceURL = new ArrayList();
        //String getImgUrl ="img.*src(\\s+=|=)(\\s+\"/|\"/|\").*?\\\"";
        String getImgUrl ="img(.*?)src=(.*?)\"(.*?)\"";
        String getSrc = "src.*=\".*?\"";
        String getQuote = "\".*?\"";
        Pattern imgUrlpattern = Pattern.compile(getImgUrl);
        Pattern imgSrc = Pattern.compile(getSrc);
        Pattern imgQuote = Pattern.compile(getQuote);
        Matcher imgMatch = imgUrlpattern.matcher(pageSource);
        while(imgMatch.find()){
            System.out.println("Inside Img page match");
            Matcher imgSrcMatch = imgSrc.matcher(imgMatch.group());
            System.out.println("Matched img : "+imgMatch.group().toString());
            System.out.println("Matched img scr : "+imgSrcMatch.find());
            System.out.println("Matched img scr group: "+imgSrcMatch.group());
            Matcher imgQuoteMatch = imgQuote.matcher(imgSrcMatch.group());
            System.out.println("Matched img scr quote -Find:"+ imgQuoteMatch.find());
            System.out.println("Match quote : "+ imgQuoteMatch.group());
            //System.out.println("Replace First : "+ imgQuoteMatch.group().replaceAll("\"",""));
            System.out.println("-----------------");

            resultImgResourceURL.add(imgQuoteMatch.group().replaceAll("\"",""));

        }

        return  resultImgResourceURL;

    }

    private String getDomainURL(String currentURL) throws MalformedURLException{
        URL domainURL=null;
        String hostUrl=" ";
        try{
            domainURL = new URL(currentURL);


        }catch (MalformedURLException e){
            System.out.println(e.getMessage());
        }
        hostUrl =  domainURL.getProtocol()+"://"+domainURL.getHost();
        System.out.println("hostUrl : "+hostUrl);
        return hostUrl;
    }

    private int checkImageLoad(WebDriver webdr,Dictionary dict){
        int returnFlag = 0;
        WebElement webElement;
        Boolean imageLodeStatus = Boolean.FALSE;
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            imageLodeStatus = (Boolean) ((JavascriptExecutor)webdr).executeScript("return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0",webElement);
            if(imageLodeStatus==Boolean.TRUE){
                return 0;
            }else{
                errorString = "Image not loaded";
                errorStringLong = "Image not loaded";
                returnFlag = 1;
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        return returnFlag;
    }

    private int checkPageImages(WebDriver webdr) throws MalformedURLException{
        String pageSource = " ";
        //String returnMinfiResult = " ";
        String currentURL = " ";
        //String assetURLForMsg = " ";
        List imgResourceURL = new ArrayList();
        URL  urlImage = null;
        HttpURLConnection httpImge = null;
        int returnFlag = 0;
        currentURL = webdr.getCurrentUrl();
        String originalUrl = currentURL;
        System.out.print("Current URl : "+currentURL);
        currentURL = getDomainURL(currentURL);
        System.out.println("Current Domain URL :"+currentURL);
        try {
            pageSource = webdr.getPageSource();
            //System.out.println(pageSource);
            System.out.println("Before getResourceURL");
            imgResourceURL = getImageResourceURL(pageSource);
            Iterator imgIterator = imgResourceURL.iterator();
            while(imgIterator.hasNext()){
                String imgAssetURL = imgIterator.next().toString();
                System.out.println("Substring 0,2"+imgAssetURL.substring(0,2));
                if(imgAssetURL.substring(0,2).equals("//")){
                    imgAssetURL = imgAssetURL.replaceFirst("//","http://");
                    System.out.println("after stripping "+imgAssetURL);

                }
                //System.out.println("Image Asset : "+imgAssetURL);
                if(imgAssetURL.contains(".com")){
                    System.out.println("Contains .com");
                   //continue;
                   // webdr.get(imgAssetURL);
                    //System.out.println("-----------------");
                    //System.out.println(webdr.getPageSource());
                    //System.out.println("-----------------");
                }
                else {
                    imgAssetURL = currentURL + imgAssetURL;
                    //System.out.println("Asset URL :" + imgAssetURL);
                    //System.out.println("Navigat to Asset URL");
                    //webdr.get(imgAssetURL);
                    //System.out.println("-----------------");
                    //System.out.println(webdr.getPageSource());
                    //System.out.println("-----------------");
                }

                urlImage = new URL(imgAssetURL);
                httpImge = (HttpURLConnection)urlImage.openConnection();
                int imgStagus = httpImge.getResponseCode();
                System.out.println("Image Status for URL "+imgAssetURL+" Status is "+imgStagus);
                if(imgStagus != 200){
                    errorString = "Image Load issue";
                    errorStringLong = errorStringLong + "Image Status for URL "+imgAssetURL+" Status is "+"NOT LOADED"+"---";
                    returnFlag = 1;
                }
            }


        }catch (Exception e){
            errorString = "Image Load issue";
            errorStringLong = e.toString();
            returnFlag = 1;
        }
        webdr.get(originalUrl);
        return returnFlag;
    }

    private int checkFieldIsPresent(WebDriver webdr, Dictionary dict) throws InterruptedException {
        WebElement element;
        int returnFlag = 1;
        try {
            element = getWebElement(webdr, dict.get("type").toString(), dict.get("match").toString());
            if (element.isDisplayed()) {
                returnFlag = 0;
            } else {
                errorString = "Field does not exist";
                returnFlag = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnFlag;

    }

    // Added By KV031709
    private int checkDropDownOptions(WebDriver webdr, Dictionary dict)
            throws InterruptedException {
        int returnFlag = 1, flag = 0;
        int noOfOptions = 0;
        String[] expectedOptions = null;
        try {
            String[] options = webdr.findElement(By.xpath(dict.get("match").toString())).getText().split("\n");
            noOfOptions = options.length;
            System.out.println("number=" + noOfOptions);
            expectedOptions = dict.get("parameter").toString().split(",");
            for (int j = 0; j < expectedOptions.length; j++) {
                System.out.println(expectedOptions[j]);
                if (expectedOptions[j].contains(options[j]))
                    flag = 0;
                else
                    flag = flag + 1;
            }
            System.out.println("count=" + flag);
            if (flag == 0) {
                returnFlag = 0;
            } else {
                errorString = "Options does not match";
                returnFlag = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnFlag;
    }

    private int storeBindValue(WebDriver  webdr, Dictionary dict){
        WebElement webElement;
        String bindKey=null;

        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            System.out.println(webElement.getTagName());
            System.out.println("Parameter : "+dict.get("parameter").toString());
            System.out.println("TextValue : "+ webElement.getText());
            bindKey = dict.get("parameter").toString();
            if(bindKey.substring(0,1).equalsIgnoreCase(":")){
                bindValue.put(bindKey,webElement.getText());
            }else {
                System.out.println("Invalid bind value, Valid Bind should start with ':' like :Bind1");
            }
            System.out.println(bindValue.get(bindKey));
            for(Map.Entry map: bindValue.entrySet()){
                System.out.println(map.getKey()+" "+map.getValue());
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;

    }

    private int getTagName(WebDriver  webdr, Dictionary dict){
        WebElement webElement;
        String bindKey=null;

        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            System.out.println(webElement.getTagName());
            System.out.println("Parameter : "+dict.get("parameter").toString());
            System.out.println("TextValue : "+ webElement.getText());
            bindKey = dict.get("parameter").toString();
            if(bindKey.substring(0,1).equalsIgnoreCase(":")){
                bindValue.put(bindKey,webElement.getText());
            }else {
                System.out.println("Invalid bind value, Valid Bind should start with ':' like :Bind1");
            }
            System.out.println(bindValue.get(bindKey));
            for(Map.Entry map: bindValue.entrySet()){
                System.out.println(map.getKey()+" "+map.getValue());
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;

    }

    //Alert handing - Accept Alert - Rachithra
    private int acceptAlert(Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            Alert alert= webDriver.switchTo().alert();
            System.out.println(alert.getText());
            alert.accept();
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    //Alert handing - Dismiss Alert - Rachithra
    private int dismissAlert(Dictionary dict)throws InterruptedException{
        WebElement webElement;
        try{
            Alert alert= webDriver.switchTo().alert();
            System.out.println(alert.getText());
            alert.dismiss();
        }
        catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private WebElement getWebElement(WebDriver webdr,String searchBy,String match){
        WebDriverWait wait = new WebDriverWait(webdr, 50);
        WebElement webElement = null;
        if(searchBy.equals("Xpath")){
            webElement = webdr.findElement(By.xpath(match));

            if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                return webElement;
            }

        }
        if(searchBy.equals("ID")){
            webElement = webdr.findElement(By.id(match));
            if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                return webElement;
            }
        }
        if(searchBy.equals("CSSSelector")){
            webElement = webdr.findElement(By.cssSelector(match));
            if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                return webElement;
            }
        }
        if(searchBy.equals("ClassName")){
            webElement = webdr.findElement(By.className(match));
            if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                return webElement;
            }
        }
        if(searchBy.equals("LinkText")){
            webElement = webdr.findElement(By.linkText(match));
            return webElement;
        }

        return webElement;
    }

    private int crawlLinks(WebDriver webdr) throws MalformedURLException,ParserConfigurationException, TransformerException, IOException {
        String pageSource;
        String currentURL;
        int URLResponse =0;
        int returnFlag = 0;
        String xmlCrawlFilePath,xslCrawlFilePath,htmlCrawlFilePath;
        DocumentBuilderFactory crawldbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder crawlDocBuilder = crawldbFactory.newDocumentBuilder();
        Document crawlDocument = crawlDocBuilder.newDocument();
        Element crawlRootElement = crawlDocument.createElement("CrawlResult");
        crawlDocument.appendChild(crawlRootElement);
        List crawlURLList = new ArrayList();
        URL crawlURL = null;
        HttpURLConnection httpCrawlink = null;
        DateFormat autoDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat htmlFileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date fileDate = new Date();
        String autoFileName = htmlFileDateFormat.format(fileDate).toString();
        currentURL = webdr.getCurrentUrl();
        String originalUrl = currentURL;
        currentURL = getDomainURL(currentURL);
        //---
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream("config.properties");
        prop.load(input);
        xmlCrawlFilePath = prop.getProperty("XMLCRAWL");
        xslCrawlFilePath = prop.getProperty("XSLTCRAWL");
        htmlCrawlFilePath = prop.getProperty("HTMLFILE");
        htmlCrawlFilePath = htmlCrawlFilePath+"/"+"Crawl_"+autoFileName+".html";
        input.close();
        //---

        try{
            pageSource = webdr.getPageSource();
            crawlURLList = getCrawlURLList(pageSource);
            Iterator crawlURLIterator = crawlURLList.iterator();
            while (crawlURLIterator.hasNext()){
                String crawlLink = crawlURLIterator.next().toString();
                System.out.println(crawlLink);
                if(crawlLink.contains(".com")|crawlLink.contains("http:")|crawlLink.contains("https:")){

                }else {
                    crawlLink = currentURL+crawlLink;
                }
                crawlURL = new URL(crawlLink);
                //webdr.get(crawlLink);
                try{
                    httpCrawlink = (HttpURLConnection)crawlURL.openConnection();
                    URLResponse = httpCrawlink.getResponseCode();
                    System.out.println("Status Code is : "+URLResponse);
                    if(URLResponse != 200){
                        errorString = "URL Crawl Error";
                        errorStringLong = errorStringLong + "URL "+crawlLink+" Status is "+"NOT LOADED"+"---";
                        returnFlag = 1;
                    }
                    Element xmlURLElement = crawlDocument.createElement("URL");
                    Element xmlURLLink = crawlDocument.createElement("URLLink");
                    Element xmlURLResponse = crawlDocument.createElement("Response");
                    xmlURLLink.appendChild(crawlDocument.createTextNode(crawlLink));
                    xmlURLResponse.appendChild(crawlDocument.createTextNode(String.valueOf(URLResponse)));
                    xmlURLElement.appendChild(xmlURLLink);
                    xmlURLElement.appendChild(xmlURLResponse);
                    crawlRootElement.appendChild(xmlURLElement);
                }catch (Exception e){
                    Element xmlURLElement = crawlDocument.createElement("URL");
                    Element xmlURLLink = crawlDocument.createElement("URLLink");
                    Element xmlURLResponse = crawlDocument.createElement("Response");
                    xmlURLLink.appendChild(crawlDocument.createTextNode(crawlLink));
                    xmlURLResponse.appendChild(crawlDocument.createTextNode(String.valueOf(URLResponse)));
                    xmlURLElement.appendChild(xmlURLLink);
                    xmlURLElement.appendChild(xmlURLResponse);
                    crawlRootElement.appendChild(xmlURLElement);
                    System.out.println(e.getStackTrace().toString());
                    errorString = "Error" ;
                    errorStringLong = e.getStackTrace().toString();
                    e.printStackTrace();
                    returnFlag = 1;

                }



            }
        }catch (Exception e){
            System.out.println(e.getStackTrace().toString());
            errorString = "Error" ;
            errorStringLong = e.getStackTrace().toString();
            e.printStackTrace();
            returnFlag = 1;
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(crawlDocument);
        //StreamResult crawlResult = new StreamResult(new File("C:\\Users\\VC024129\\Documents\\Vijay\\TestFrameWork\\ApplicationReports\\Crawl.xml"));
        StreamResult crawlResult = new StreamResult(new File(xmlCrawlFilePath));
        transformer.transform(source,crawlResult);
        TransformerFactory transformerFactoryHTML = TransformerFactory.newInstance();
        Transformer transformerHTMLCrawl = transformerFactoryHTML.newTransformer(new javax.xml.transform.stream.StreamSource(xslCrawlFilePath));
        transformerHTMLCrawl.transform(new javax.xml.transform.stream.StreamSource(xmlCrawlFilePath), new javax.xml.transform.stream.StreamResult(htmlCrawlFilePath));
        errorString = htmlCrawlFilePath;
        errorStringLong = "View the Crawl result by clicking the link";
        webdr.get(originalUrl);
        return returnFlag;
    }

    private List getCrawlURLList(String pageSource){
        List resultCrawlURL = new ArrayList();
        Set<String> tempSet = new HashSet<String>();
        String getAnchor = "<a(.*?)href=(.*?)\"(.*?)\"";
        String getHref = "href=(.*?)\"(.*?)\"";
        String getLink = "\"(.*?)\"";
        Pattern crawlURLAnchor = Pattern.compile(getAnchor);
        Pattern crawlURLHref = Pattern.compile(getHref);
        Pattern crawlURLLink = Pattern.compile(getLink);
        //System.out.println(pageSource);
        Matcher matchAnchor = crawlURLAnchor.matcher(pageSource);
        try{
            while (matchAnchor.find() ){
                //System.out.println("Anchor : "+matchAnchor.group());
                Matcher matchHref = crawlURLHref.matcher(matchAnchor.group());
                matchHref.find();
                //System.out.println("Href :"+matchHref.group());
                Matcher matchURLLink = crawlURLLink.matcher(matchHref.group());
                matchURLLink.find();
                resultCrawlURL.add(matchURLLink.group().replaceAll("\"",""));
            }
            // Remove duplicates from the List
            tempSet.addAll(resultCrawlURL);
            resultCrawlURL.clear();
            resultCrawlURL.addAll(tempSet);
            // Remove invalid items
            Iterator iterator = resultCrawlURL.iterator();
            while (iterator.hasNext()){
                String tmpStr = iterator.next().toString();
                if(tmpStr.equals("#")| tmpStr.equals("#footer")|tmpStr.equals("#menu")|tmpStr.equals("#header")|tmpStr.equals("#main-cont")|tmpStr.equals("/")){
                    iterator.remove();
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }


        return resultCrawlURL;
    }


    private int timeDelayBy(Dictionary dict)throws InterruptedException{
        //System.out.println("In DelayBy");
        String varTime;
        if(dict.get("action").toString().equals("DelayBy")){
          varTime = dict.get("parameter").toString();
          Thread.sleep(Long.valueOf(varTime));

        }
        return 0;
    }

    private int printDateTime(Dictionary dict){
        //System.out.println("In Print Date and Time");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        errorString = dateFormat.format(date);
        errorStringLong = errorString;
        return 0;
    }

    private void takeScreenshot(WebDriver webdr)throws InterruptedException {
        Thread.sleep(4000);
        DateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date fileDate = new Date();
        String autoFileName = fileDateFormat.format(fileDate).toString();
        File src = ((TakesScreenshot) webdr).getScreenshotAs(OutputType.FILE);
        try{
            screenShotName = dict.get("prcsID").toString()+"_"+ dict.get("prcsSeqNum")+"_screenShot_"+autoFileName+".png";
            String screenShotFileName = screenShotPath+"\\"+screenShotName;
            FileUtils.copyFile(src,new File(screenShotFileName));
        }catch (IOException e){
            errorString ="Unable to take ScreenShot";
            errorStringLong = e.toString();
        }

    }

    private byte[] stringToByte(String str){
        byte [] byteString;
        byteString = Base64.getDecoder().decode(str);
        return byteString;

    }

    private  String byteToString(byte[] bytes){
        String str;
        str = Base64.getEncoder().encodeToString(bytes);
        return  str;

    }

    private SecretKey stringToKey(String keyStr){
        byte[] decodeKey = stringToByte(keyStr);
        SecretKey secretKey = new SecretKeySpec(decodeKey,0,decodeKey.length,"AES");
        return  secretKey;
    }

    private String decryptString(byte[] byteCipherString, SecretKey secretKey) throws Exception{
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE,secretKey);
        byte[] decodedByte = aesCipher.doFinal(byteCipherString);
        return new String(decodedByte);
    }

    public String getBinvalue(String bValue){
       return bindValue.get(bValue);
    }

    public String encryptString(String str, SecretKey secretKey) throws Exception{
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] byteCipherText = aesCipher.doFinal(str.getBytes());
        return byteToString(byteCipherText);
    }

    public static String getEncryptedString(byte[] secKey){
        byte encoded[] = secKey;
        String encodedKey = Base64.getEncoder().encodeToString(encoded);
        return encodedKey;
    }

    public Node getXMLProcessNode(Document document,ProcessData processData,int returnValue,long execTime,String errorString,String errorStringLong,String screenShotName ){
        copyHashTable(processData);
        String activeRow,result = "NORUN";
        String shortErrorMsg = " ";
        activeRow =  dict.get("active").toString();
        if (activeRow.equals("A")){
            if(returnValue==0)
                result = "PASS";
            else
                result = "FAIL";

        }

        Element unitCase = document.createElement("TestUnit");
        Element seqNum = document.createElement("SeqNum");
        Element unitDescr = document.createElement("UnitDescr");
        Element unitActive = document.createElement("Active");
        Element unitResult = document.createElement("Result");
        Element errorStr = document.createElement("Exception");
        Element errorMsgStr = document.createElement("ExceptionMsg");
        Element screenShotNameElement = document.createElement("ScreenShotImageName");
        Element executionTime = document.createElement("ExecutionTime");
        seqNum.appendChild(document.createTextNode(dict.get("prcsSeqNum").toString()));
        unitDescr.appendChild(document.createTextNode(dict.get("prcsSeqDescr").toString()));
        unitActive.appendChild(document.createTextNode(dict.get("active").toString()));
        unitResult.appendChild(document.createTextNode(result));
        errorStr.appendChild(document.createTextNode(errorString));
        errorMsgStr.appendChild(document.createTextNode(errorStringLong));
        screenShotNameElement.appendChild(document.createTextNode(screenShotName));
        executionTime.appendChild(document.createTextNode((String.valueOf(execTime))));

        unitCase.appendChild(seqNum);
        unitCase.appendChild(unitDescr);
        unitCase.appendChild(unitActive);
        unitCase.appendChild(unitResult);
        unitCase.appendChild(errorStr);
        unitCase.appendChild(errorMsgStr);
        unitCase.appendChild(screenShotNameElement);
        unitCase.appendChild(executionTime);
        return unitCase;
    }

    public Node generateSummary(Document document){

        String summaryTestCaseID = " ";
        String summaryTestCaseDescr = " ";
        String summaryTestResult = " ";
        String summaryDTTM = " ";
        String summaryExecTimeStr = " ";

        long summaryExecTimeLong = 0;
        double totalExecTime = 0;
        int totalTestCase = 0;
        int passTestCase = 0;
        int noRunTestCase = 0;
        int failTestCase = 0;
        Element summaryElement = document.createElement("Summary");
        NodeList nodeList = document.getElementsByTagName("TestCase");
        System.out.println("===>Generating Test Execution Summary<===");
        //Reset Execution Time var

        for(int temp=0;temp < nodeList.getLength(); temp++){
            summaryExecTimeLong = 0;
            Element summaryTSTElement = document.createElement("SummaryTestCase");
            Node node = nodeList.item(temp);
            Element summaryElementTest=  (Element) node;
            summaryTestCaseID = summaryElementTest.getElementsByTagName("PrcsID").item(0).getTextContent();
            summaryTestCaseDescr = summaryElementTest.getElementsByTagName("PrcsDescr").item(0).getTextContent();
            summaryDTTM = summaryElementTest.getElementsByTagName("StartDTTM").item(0).getTextContent();

            //System.out.println(node.getNodeName());
            if( node.getNodeType() == Node.ELEMENT_NODE){
                Element summaryElementTU = (Element) node;
                NodeList summaryNodeList = summaryElementTU.getElementsByTagName("TestUnit");
                for(int count=0; count< summaryNodeList.getLength();count++){
                    Node summaryNode = summaryNodeList.item(count);
                    if(summaryNode.getNodeType() == Node.ELEMENT_NODE){
                        Element resultElement = (Element) summaryNode;
                        summaryTestResult = resultElement.getElementsByTagName("Result").item(0).getTextContent();
                        if (summaryTestResult.equals("FAIL")|| summaryTestResult.equals("NORUN") ){
                            break;
                        }
                    }
                }
            }
            // Sum up total execution time for Test Case
            if( node.getNodeType() == Node.ELEMENT_NODE){
                Element summaryElementTU = (Element) node;
                NodeList summaryNodeList = summaryElementTU.getElementsByTagName("TestUnit");
                for(int count=0; count< summaryNodeList.getLength();count++){
                    Node summaryNode = summaryNodeList.item(count);
                    if(summaryNode.getNodeType() == Node.ELEMENT_NODE){
                        Element resultElement = (Element) summaryNode;
                        summaryExecTimeStr = resultElement.getElementsByTagName("ExecutionTime").item(0).getTextContent();
                        summaryExecTimeLong = summaryExecTimeLong + Long.parseLong(summaryExecTimeStr);
                    }
                }
            }
            totalExecTime += summaryExecTimeLong;

            //System.out.println("TestCaseID : "+summaryTestCaseID+" TestCaseDescr : "+summaryTestCaseDescr+" TestResult : "+summaryTestResult);
            Element sumSeqNum = document.createElement("TestCaseSeqNum");
            Element sumTestCaseID = document.createElement("TestCaseID");
            Element sumTestCaseDescr = document.createElement("TestCaseDescr");
            Element sumResult = document.createElement("Result");
            Element sumDTTM = document.createElement("StartDTTM");
            Element sumExecTime = document.createElement("ExecutionTime");
            sumSeqNum.appendChild(document.createTextNode(( String.valueOf(temp+1))));
            sumTestCaseID.appendChild(document.createTextNode(summaryTestCaseID));
            sumTestCaseDescr.appendChild(document.createTextNode(summaryTestCaseDescr));
            sumResult.appendChild(document.createTextNode(summaryTestResult));
            sumDTTM.appendChild(document.createTextNode(summaryDTTM));
            sumExecTime.appendChild(document.createTextNode(String.valueOf(summaryExecTimeLong)));
            summaryTSTElement.appendChild(sumSeqNum);
            summaryTSTElement.appendChild(sumTestCaseID);
            summaryTSTElement.appendChild(sumTestCaseDescr);
            summaryTSTElement.appendChild(sumResult);
            summaryTSTElement.appendChild(sumDTTM);
            summaryTSTElement.appendChild(sumExecTime);
            summaryElement.appendChild(summaryTSTElement);
            //totalTestCase = temp + 1;
            if (summaryTestResult.equals("PASS")) passTestCase += 1;
            if(summaryTestResult.equals("FAIL")) failTestCase +=1;
            if(summaryTestResult.equals("NORUN")) noRunTestCase +=1;

        }
        totalTestCase = passTestCase + failTestCase + noRunTestCase;
        //totalExecTime =  (totalExecTime%3600)/60;
        int totalExecTimeWholeNum = (int)(totalExecTime % 3600)/60;
        double totalExecTimeFraction = (totalExecTime % 60)/100;
        //System.out.println("totalExecTime : "+totalExecTime);
        //System.out.println("totalExecTimeWholeNum : "+ totalExecTimeWholeNum);
        //System.out.println("totalExecTimeFraction : "+ totalExecTimeFraction);
        //totalExecTimeFraction = totalExecTimeFraction * 60;
        totalExecTime = totalExecTimeWholeNum + totalExecTimeFraction;
        System.out.println("==>totalExecTime : "+totalExecTime);
        DecimalFormat decimalFormat = new DecimalFormat("####.##");

        Element sumTotalTestCase = document.createElement("TotalTestCases");
        Element sumPassedTestCase = document.createElement("PassedTestCases");
        Element sumFailedTestCase = document.createElement("FailedTestCases");
        Element sumNoRunTestCase = document.createElement("NoRunTestCases");
        Element sumTotalExecTime = document.createElement("TotalExecutionTime");
        sumTotalTestCase.appendChild(document.createTextNode(String.valueOf(totalTestCase)));
        sumPassedTestCase.appendChild(document.createTextNode(String.valueOf(passTestCase)));
        sumFailedTestCase.appendChild(document.createTextNode(String.valueOf(failTestCase)));
        sumNoRunTestCase.appendChild(document.createTextNode(String.valueOf(noRunTestCase)));
        sumTotalExecTime.appendChild(document.createTextNode(decimalFormat.format(totalExecTime)));
        summaryElement.appendChild(sumTotalTestCase);
        summaryElement.appendChild(sumPassedTestCase);
        summaryElement.appendChild(sumFailedTestCase);
        summaryElement.appendChild(sumNoRunTestCase);
        summaryElement.appendChild(sumTotalExecTime);
        return summaryElement;
    }
}

