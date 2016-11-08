package com.github.netudima.jmeter.junit.report;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DomXmlJUnitReportWriter implements Closeable {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder;

    private final String fileName;
    private final Document doc;
    private Element rootElement;

    private int tests;
    private int failures;
    private int errors;
    private int skipped;

    public DomXmlJUnitReportWriter(String fileName, String testSuiteName) {
        this.fileName = fileName;

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("fail to init XML writer", e);
        }
        doc = documentBuilder.newDocument();
        rootElement= doc.createElement("testsuite");
        rootElement.setAttribute("name", testSuiteName);
        doc.appendChild(rootElement);
    }


    public void write(JtlRecord jtlRecord) {
        tests++;

        Element testCase = doc.createElement("testcase");
        testCase.setAttribute("classname", "");
        testCase.setAttribute("name", jtlRecord.getLabel());
        if (!jtlRecord.isSuccess()) {
            failures++;
            Element failure = doc.createElement("failure");
            failure.setAttribute("message", jtlRecord.getFailureMessage());
            testCase.appendChild(failure);
        }
        rootElement.appendChild(testCase);
    }


    @Override
    public void close() throws IOException {
        try {
            rootElement.setAttribute("tests", Integer.toString(tests));
            rootElement.setAttribute("failures", Integer.toString(failures));
            rootElement.setAttribute("errors", Integer.toString(errors));
            rootElement.setAttribute("skipped", Integer.toString(skipped));
            flush();
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    private void flush() throws TransformerException, IOException {
        // output DOM XML to console
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult streamResult;
        if (fileName != null) {
            streamResult = new StreamResult(new BufferedWriter(new FileWriter(new File(fileName))));
        } else {
            streamResult = new StreamResult(System.out);
        }
        transformer.transform(source, streamResult);

    }
}
