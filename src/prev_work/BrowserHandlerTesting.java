package prev_work;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import configuration.Configurator;
import difflib.Patch;

public class BrowserHandlerTesting {
    private String pageURL = "http://www.facebook.com";
    // private ArrayList<String> pageSources;
    private ArrayList<Patch> pageChanges = new ArrayList<Patch>();
    private int HTMLFileIndex = 1;
    // Urls of the pages visited
    private ArrayList<PageInfo> pageInfo = new ArrayList<PageInfo>();
    private int correlationBetweenSeleniumStepAndHtmlFiles = 1;
    private String historyFilepath = "";
    private PatternRegister patterns = new PatternRegister();

    // private PatternRegister patterns=new PatternRegister();

    public void setHistoryFilepath(String historyFilepath) {
        this.historyFilepath = historyFilepath;
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public void init(String url, String pageSource) {
        deleteFolder(new File(System.getProperty("user.dir")
                + File.separatorChar + "HTMLtemp" + File.separatorChar));
        new File(System.getProperty("user.dir") + File.separatorChar
                + "HTMLtemp" + File.separatorChar).mkdirs();
        deleteFolder(new File(System.getProperty("user.dir")
                + File.separatorChar + "HTMLfinal" + File.separatorChar));
        new File(System.getProperty("user.dir") + File.separatorChar
                + "HTMLfinal" + File.separatorChar).mkdirs();

        pageURL = url;

        Filesystem.saveToFile("temp", Integer.toString(HTMLFileIndex),
                pageSource, false);
        HTMLFileIndex++;
        pageInfo.add(new PageInfo(pageURL,
                correlationBetweenSeleniumStepAndHtmlFiles));
        System.out.println("INIT|correlation:"
                + correlationBetweenSeleniumStepAndHtmlFiles + "|HTML index:"
                + HTMLFileIndex+"\n");
        patterns.initializePatternRegister();
    }

    public void escapeProcess() {
        System.out.println("start");
        processList();
        System.out.println("processList");
        PatternNonSeleniumFinder.ProcessUrlsAndHTMLSize(pageInfo);
        System.out
                .println("PatternNonSeleniumFinder.ProcessUrlsAndHTMLSize(pageInfo)");
        try {
            processSeleniumActions();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("processSeleniumActions");
        PatternNonSeleniumFinder.testForMasterDetail();
        System.out.println("PatternNonSeleniumFinder.testForMasterDetail();");
        patterns.endPatternRegister();
    }

    public void incrementHTML(String pageSource, String url) {
        Filesystem.saveToFile("temp", Integer.toString(HTMLFileIndex),
                pageSource, false);
        HTMLFileIndex++;
        correlationBetweenSeleniumStepAndHtmlFiles++;
        // System.out.println("correlation: "+correlationBetweenSeleniumStepAndHtmlFiles);
        pageInfo.add(new PageInfo(url,
                correlationBetweenSeleniumStepAndHtmlFiles));
        System.out.println("INC_HTML|correlation:"
                + correlationBetweenSeleniumStepAndHtmlFiles + "|HTML index:"
                + HTMLFileIndex);
    }

    public void incrementCorrelation() {
        correlationBetweenSeleniumStepAndHtmlFiles++;
        System.out.println("INC_CORR|correlation:"
                + correlationBetweenSeleniumStepAndHtmlFiles + "|HTML index:"
                + HTMLFileIndex);
    }

    private void processSeleniumActions() throws IOException {

        ArrayList<SeleniumIDEElement> actions = new ArrayList<SeleniumIDEElement>();

        actions = parseTableFromHistoryFile();

        // saves the last indexes of "type" actions in Selenium HTML
        // resets after a clickAndWait action
        // useful for, when a clickAndWait action occurs, to know if there was
        // one or more types before it
        ArrayList<TypeActionHandlers> lastTypeIndexes = new ArrayList<TypeActionHandlers>();

        for (SeleniumIDEElement element : actions) {
            if (element.getAction().equals("type")) {
                Boolean exists = false;
                for (int i = 0; i != lastTypeIndexes.size(); i++) {
                    // if there is already an input in that textbox and it finds
                    // another one
                    // the Selenium step index is replaced by the most recent
                    // in this way, only the most recent string of each text box
                    // is analysed
                    if (lastTypeIndexes.get(i).getTextBoxId()
                            .equals(element.getLink())) {
                        lastTypeIndexes.get(i).setIndex(
                                actions.indexOf(element) + 1);
                        exists = true;
                    }
                }

                // if there is no input on that text box
                // plus one so it corresponds to Selenium steps
                if (!exists)
                    lastTypeIndexes.add(new TypeActionHandlers(element
                            .getLink(), actions.indexOf(element) + 1));

            }

            if (element.getAction().equals("clickAndWait")) {
                if (!lastTypeIndexes.isEmpty()) {
                    SeleniumHTMLInteraction.testForPatterns(actions,
                            lastTypeIndexes, actions.indexOf(element) + 1,
                            pageInfo);
                }

                // reset lastTypeIndexes array
                lastTypeIndexes.clear();
            }

            // at the end of Selenium steps, checks for types that were not
            // analysed for input patterns
            if (actions.get(actions.size() - 1).equals(element)
                    && element.getAction().equals("clickAndWait") != true) {
                SeleniumHTMLInteraction.testForInputs(actions, lastTypeIndexes,
                        actions.indexOf(element) + 1, pageInfo);
            }

        }
    }

    private ArrayList<SeleniumIDEElement> parseTableFromHistoryFile() {
        Configurator c = Configurator.getInstance();
        ArrayList<SeleniumIDEElement> ret = new ArrayList<SeleniumIDEElement>();
        BufferedReader br = null;

        File file = new File(historyFilepath + c.getHistoryFilepath());
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file.getAbsoluteFile()), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(c.getSeparator());
                SeleniumIDEElement e = new SeleniumIDEElement(parts[0],
                        parts[1], parts[2]);
                ret.add(e);
            }

            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ret;
    }

    private void processList() {

        for (int i = 2; i <= HTMLFileIndex - 1; i++) {
            pageChanges.add(DiffUtility.differenceBetweenFiles(i - 1, i));
            System.out.println("\t" + (i - 1) + "|" + i);
        }

        int file = 2;

        for (Patch patch : pageChanges) {
            String changes = DiffUtility.convertPatchToString(patch);
            Filesystem.saveToFile("final", Integer.toString(file), changes,
                    false);
            System.out.println("\tfinal" + Integer.toString(file));
            file++;
        }

    }
}
