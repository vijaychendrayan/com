package com.cerner.automation;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Created by VC024129 on 5/6/2017.
 */

public class ChromeWebDriver implements GenericWebDriver {

    public  WebDriver setWebDriver(String prop,String path){
        System.setProperty(prop,path);
        return new ChromeDriver();

    }

}
