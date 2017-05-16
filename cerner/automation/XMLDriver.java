package com.cerner.automation;

/**
 * Created by VC024129 on 5/8/2017.
 */

import javax.xml.crypto.dsig.TransformException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.concurrent.ExecutionException;

class XMLDriver  {
    private DocumentBuilderFactory dbFactory;
    private DocumentBuilder documentBuilder;
    private Document doc;


    void  XMLDriver() throws ParserConfigurationException{
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = dbFactory.newDocumentBuilder();
            doc = documentBuilder.newDocument();
        }catch (ParserConfigurationException e){

        }


    }

    public Element xmlCreateElement(String elementName){
        return doc.createElement(elementName);
    }


    public Node xmlAddElement(Element element){

    return doc.appendChild(element);

    }

    public void xmlDocWriteToFile() throws Exception{
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("C:\\Temp\\automation.xml"));
        try {
            transformer.transform(source, result);
        }catch (Exception e){
           e.printStackTrace();

        }
    }

}
