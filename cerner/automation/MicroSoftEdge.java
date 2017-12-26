package com.cerner.automation;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;

/**
 * Created by VC024129 on 12/26/2017.
 */
public class MicroSoftEdge implements GenericWebDriver{

    public WebDriver setWebDriver(String prop, String path){
        System.setProperty(prop,path);
        return new EdgeDriver();
    }

}
