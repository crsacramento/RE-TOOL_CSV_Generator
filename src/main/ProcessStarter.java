package main;

import java.io.File;
import java.io.IOException;

import configuration.Configurator;
import inferrer.PatternInferrer;
import processor.LogProcessor;
import site_accesser.WebsiteExplorer;

public class ProcessStarter {
    public static void main(String[] args) {
        startReverseEngineeringProcess(args);
    }

    public static void startReverseEngineeringProcess(String[] args) {
        String baseURL = "", folderpath = "";
        File f = null;
        if (args.length != 1 && args.length != 2) {
            System.err
                    .println("Invalid arguments (there should only exist one "
                            + "parameter, \nwhich is the URL of the website to "
                            + "crawl) and an optional parameter specifying "
                            + "\nthe filepath for files");
            System.exit(-1);
        } else {
            if (args[0].matches("^(\\w+:\\/\\/[\\w\\.-]+(:\\d+)?)\\/.*"))
                baseURL = args[0];
            else {
                System.err.println("Invalid URL (should follow the form: "
                        + "\n[protocol]://[domain list separated by dots]/)"
                        + "\n\texample: http://www.fe.up.pt/");
                // System.exit(-1);
                return;
            }

            if (args.length == 2) {
                folderpath = args[1];
                f = new File(folderpath);
                try {
                    f.getCanonicalFile();
                } catch (IOException e) {
                    System.err
                            .println("ERROR: folder path passed as parameter is invalid");
                    e.printStackTrace();
                    return;
                }
                if (!f.exists())
                    f.mkdir();
                if (!f.isDirectory()) {
                    System.err
                            .println("ERROR: folder path must point to a directory");
                    return;
                }

            }
        }

        // baseURL = "http://app.rasc.ch/tudu/welcome.action";
        // baseURL = "https://www.amazon.com/";
        // baseURL = "https://www.yahoo.com/";
        // baseURL = "http://www.juventude.gov.pt/Paginas/default.aspx";
        // baseURL = "http://www.fe.up.pt/";
        // baseURL = "http://en.wikipedia.org";
        // baseURL = "http://www.facebook.com";
        // baseURL = "http://www.ebay.com/";
        // baseURL = "http://www.youtube.com/";
        // baseURL = "http://store.steampowered.com/";
        // baseURL = "http://www.geforce.com/";
        // baseURL = "http://www.reddit.com/";
        // baseURL = "http://www.9gag.com/";
        // baseURL = "http://www.gamestop.com";
        // baseURL = "http://www.gamespot.com";
        // baseURL = "http://www.mcgame.com/";

        Configurator c = Configurator.getInstance();
        WebsiteExplorer we = WebsiteExplorer.getInstance();
        if (!(f == null)) {
            WebsiteExplorer.initialize(baseURL, f.getAbsolutePath()
                    + File.separator);
            Configurator.initialize(f.getAbsolutePath() + File.separator);
        } else
            WebsiteExplorer.initialize(baseURL);

        c.loadConfig();
        WebsiteExplorer.setConfigurator(c);

        try {
            we.exploreWebsite();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogProcessor.processHistoryFile();
        PatternInferrer.setBaseUrl(baseURL);
        PatternInferrer.setMenuElements(we.menuElements);
        PatternInferrer.setMasterElements(we.masterElements);
        PatternInferrer.setDetailElements(we.detailElements);
        PatternInferrer.startInferringProcess();
    }
}
