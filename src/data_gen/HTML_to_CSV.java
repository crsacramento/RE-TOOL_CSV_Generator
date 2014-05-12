package data_gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HTML_to_CSV {
    private static char SEPARATOR = ',';
    /**
     * Takes Selenium HTML action table file and converts to a CSV file
     * 
     * @param args
     *            path to file
     */
    public static void convertFile(File file, File output) {
        /*
         * if (args.length >= 2) {
         * System.err.println("Usage: HTML_to_CSV <filePath>"); System.exit(1);
         * } filePath = args[1];
         */

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
        }

        DocumentBuilder builder = null;
        Document doc = null;
        FileWriter outputFW = null;
        String lineToWrite = "";
        //int lineNumber = 1;
        
        try {
            //String dirName = file.getParentFile().toPath().toAbsolutePath().toString();
            //String fileName = file.getName() + ".csv";
            //File dir = new File (dirName);
            //File actualFile = new File (dir, fileName);
            outputFW = new FileWriter(output);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            doc = builder.parse(file);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // normalize text representation
        doc.getDocumentElement().normalize();

        // get table lines
        NodeList lines = doc.getElementsByTagName("tr");

        // we ignore the first tr line (has title only)
        for (int s = 1; s < lines.getLength(); ++s) {
            Node firstTD = lines.item(s);
            if (firstTD.getNodeType() == Node.ELEMENT_NODE) {
                Element firstTD_elem = (Element) firstTD;
                NodeList tdList = firstTD_elem.getElementsByTagName("td");
                
                // builds line
                for (int x = 0; x < tdList.getLength(); ++x) {
                    Element tdElement = (Element)tdList.item(x);
                    String content = tdElement.getTextContent().trim();
                    content = content.replace("\"","\\\"");
                    if(!content.isEmpty())
                        lineToWrite += '\"' + org.apache.commons.lang3.StringUtils.stripAccents(content) + '\"';
                    else
                        lineToWrite += "EMPTY";
                    if(x != tdList.getLength() - 1 && !content.isEmpty())
                        lineToWrite += SEPARATOR;
                }
                lineToWrite += "\n";
                //lineToWrite = lineNumber + SEPARATOR + lineToWrite;
                // writes line to file
                try {
                    outputFW.write(lineToWrite);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                lineToWrite = "";
                //lineNumber++;
            }
        }

        // close file writer
        try {
            outputFW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        
        File[] files = new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\HTMLs").listFiles();
        for (File file : files) {
            System.out.println(file.getName());
            convertFile(file,new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\CSVs\\"+file.getName()+".csv"));
            //System.out.println(file.getName() + " done.");
        }
        //File file = new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\HTMLs\\revistabrasileiros.html");
        //convertFile(file);
    }
}