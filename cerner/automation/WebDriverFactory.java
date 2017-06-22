package com.cerner.automation;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


import java.net.MalformedURLException;

/**
 * Created by VC024129 on 5/6/2017.
 */
class WebDriverFactory  {

    public WebDriver setWebDriver(String driverType, String prop, String path) throws MalformedURLException {
        if (driverType.equals("CHROME")) {
            ChromeWebDriver chromeWebDriver = new ChromeWebDriver();
            return chromeWebDriver.setWebDriver(prop, path);
        }
        if (driverType.equals("FIREFOX")) {
            FirefoxWebDriver firefoxWebDriver = new FirefoxWebDriver();
            return  firefoxWebDriver.setWebDriver(prop,path);

        }
        if(driverType.equals("ANDROIDCHROME")){
            AndroidChromeWebDriver androidChromeWebDriver = new AndroidChromeWebDriver();
            try {
                return androidChromeWebDriver.setWebDriver(prop, path);
            }catch (Exception e){}
        }
        return null;
    }
}