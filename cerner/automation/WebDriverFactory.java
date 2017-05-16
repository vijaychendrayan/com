package com.cerner.automation;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Created by VC024129 on 5/6/2017.
 */
class WebDriverFactory {

    public WebDriver setWebDriver(String driverType, String prop, String path) {
        if (driverType.equals("CHROME")) {
            ChromeWebDriver chromeWebDriver = new ChromeWebDriver();
            return chromeWebDriver.setWebDriver(prop, path);
        }
        if (driverType.equals("FIREFOX")) {
            FirefoxWebDriver firefoxWebDriver = new FirefoxWebDriver();
            return  firefoxWebDriver.setWebDriver(prop,path);

        }
        return null;
    }
}