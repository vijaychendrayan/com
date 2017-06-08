package com.cerner.automation;


import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Engine {
    //public int processRequestCont;
    //public String processDescr;
    //public String processUnitDescr;
    //public String processNum;
    //public String processUnitNum;
    private Dictionary dict = new Hashtable();
    private String [] colKey = new String[]{"prcsID","prcsDescr","prcsSeqNum","prcsSeqDescr","driver","action","type","match","parameter","active","screenShot","onError"};
    //public String colValue;
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
        //System.out.println("--Setting up ScreenShot File path---");
        screenShotPath = path;
    }

    public int processRequest(Row row) throws InterruptedException{
        copyHashTable(row);
        prcsStatus = 1;
        //System.out.println(dict.get("prcsID")+" "+dict.get("prcsSeqNum"));
        colDriver = (String) dict.get("driver");
        colAction = (String) dict.get("action");
        String logString = "======>"+dict.get("prcsSeqNum")+" : "+dict.get("prcsSeqDescr")+" : "+dict.get("driver");
        logString = logString +" : "+dict.get("action")+dict.get("type")+" : "+dict.get("match");
        logString = logString +" : "+dict.get("parameter")+" : "+dict.get("active");
        logString = logString +" : "+dict.get("screenShot")+" : "+dict.get("onError")+"<=====";

        //Navigate
        //System.out.println("Driver : "+colDriver+" colAction : "+colAction);
        if(colDriver.equals("Web")) {
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
        }
        if(colDriver.equals("Time")){
            if(colAction.equals("DelayBy")){
                prcsStatus = timeDelayBy(dict);
            }
            if(colAction.equals("PrintDateTime")){
                prcsStatus = printDateTime(dict);
            }
        }
        System.out.println(logString+"===>Return : "+prcsStatus);
        if(prcsStatus==1){

            webDriver.navigate().refresh();
        }
        return prcsStatus;
    }

    private void copyHashTable(Row row){

        for (int i=0; i<row.getLastCellNum();i++)
        {
            dict.put(colKey[i],row.getCell(i).getRichStringCellValue().getString());
        }
    }

    private int webNavigateHandler(WebDriver webdr, Dictionary dict) throws InterruptedException{
        int returnFlag = 0;
        errorString = " ";
        try {
                if(dict.get("type").toString().equals("Get")){
                    webdr.get(dict.get("match").toString());
                }
            if(dict.get("type").toString().equals("Refresh")){
                webdr.navigate().refresh();
            }

            if (dict.get("screenShot").toString().equals("Y")) {
                takeScreenshot(webdr);
            }

        }
        catch (Exception e){
            errorString = e.toString();
            returnFlag = 1;
        }
        return returnFlag;
    }

    private int windowEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
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
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            webElement.sendKeys(dict.get("parameter").toString());
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
            errorString = e.toString();
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
            errorString = e.toString();
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
                    returnFlag = 1;
                }
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }
        }catch (Exception e){
            errorString = e.toString();
            returnFlag = 1;
        }
        return  returnFlag;
    }
    private int compareEventHandler(WebDriver webdr, Dictionary dict) throws InterruptedException{
        WebElement webElement;
        int returnFlag = 1;
        try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            if(webElement.getText().equals(dict.get("parameter").toString())){
                System.out.println("====>String Matched<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>String NOT Matched<===");
                errorString = "====>String NOT Matched<===";
                returnFlag = 1;
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
        String currentURL = " ";
        List resourceURL = new ArrayList();
        int returnFlag = 0;
        int newLineCoutn = 0;
        currentURL = webdr.getCurrentUrl();
        System.out.print("Current URl : "+currentURL);


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
                System.out.println("Asset URL :"+ assetURL);

            }



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
    private List getResourceURL(String pageSource){
        System.out.println("Inside getResourceURL");
        List resultResourceURL = new ArrayList();
        String getCss ="href(\\s+=|=)(\\s+\"/|\"/).*\\.css";
        String getJs ="src(\\s+=|=)(\\s+\"/|\"/).*\\.js";

        Pattern css = Pattern.compile(getCss);
        Pattern js =  Pattern.compile(getJs);
        Matcher matchCss = css.matcher(pageSource);
        Matcher matchJs = js.matcher(pageSource);
        while (matchCss.find()){
            resultResourceURL.add(matchCss.group());
        }
        while (matchJs.find()){
            resultResourceURL.add(matchJs.group());
        }
        System.out.println("before return resultResourceURL "+resultResourceURL);
        return resultResourceURL;
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
                returnFlag = 1;
            }
        }catch (Exception e){
            errorString = e.toString();
            returnFlag = 1;
        }
        return returnFlag;
    }
    private WebElement getWebElement(WebDriver webdr,String searchBy,String match){
        WebElement webElement = null;
        if(searchBy.equals("Xpath")){
            webElement = webdr.findElement(By.xpath(match));
            return webElement;
        }
        if(searchBy.equals("ID")){
            webElement = webdr.findElement(By.id(match));
            return webElement;
        }
        if(searchBy.equals("CSSSelector")){
            webElement = webdr.findElement(By.cssSelector(match));
            return webElement;
        }
        if(searchBy.equals("ClassName")){
            webElement = webdr.findElement(By.className(match));
            return webElement;
        }

        return webElement;
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

    public Node getXMLProcessNode(Document document,Row row,int returnValue,long execTime,String errorString ){
        copyHashTable(row);
        String activeRow,result = "NORUN";
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
        Element executionTime = document.createElement("ExecutionTime");
        seqNum.appendChild(document.createTextNode(dict.get("prcsSeqNum").toString()));
        unitDescr.appendChild(document.createTextNode(dict.get("prcsSeqDescr").toString()));
        unitActive.appendChild(document.createTextNode(dict.get("active").toString()));
        unitResult.appendChild(document.createTextNode(result));
        errorStr.appendChild(document.createTextNode(errorString));
        executionTime.appendChild(document.createTextNode((String.valueOf(execTime))));

        unitCase.appendChild(seqNum);
        unitCase.appendChild(unitDescr);
        unitCase.appendChild(unitActive);
        unitCase.appendChild(unitResult);
        unitCase.appendChild(errorStr);
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

