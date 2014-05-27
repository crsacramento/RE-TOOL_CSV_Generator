package site_accesser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import prev_work.BrowserHandlerTesting;
import configuration.Configurator;

public class WebsiteExplorer {
    /**
     * the URL where the crawler will start and the domain to which it will
     * restrict itself
     */
    private String baseUrl = "";
    /**
     * filepath to folder where all files produced will be written
     */
    private static String folderpath = "";

    /** set of visited elements */
    private static Set<String> visitedElements = new HashSet<String>();

    /** set of menu elements (page -> menu elements within) */
    public HashMap<String, HashSet<String>> menuElements = new HashMap<String, HashSet<String>>();

    /**
     * set of detail elements belonging to a MasterDetail in a search results
     * page (page -> menu elements within)
     */
    public HashMap<String, HashSet<String>> detailElements = new HashMap<String, HashSet<String>>();

    /**
     * set of master elements belonging to a MasterDetail in a search results
     * page (page -> menu elements within)
     */
    public HashMap<String, HashSet<String>> masterElements = new HashMap<String, HashSet<String>>();

    /** URL of current page */
    private String currentPage = baseUrl;

    /** index of current action */
    private int currAction = 0;

    /** number of times explorer went back to home page */
    private int homeRedirections = 0;

    /** says if politeness delay is to be done (false if error occurs) */
    private boolean wait = true;

    /** instance of this class (singleton enforcement) */
    private static WebsiteExplorer instance = null;

    private static Configurator configurator = Configurator.getInstance();

    /** web driver */
    private HtmlUnitDriver driver = null;

    private static String lastAction = "NONE", currentAction = "NONE";

    private BrowserHandlerTesting testing = new BrowserHandlerTesting();

    /**
     * @return the filepath
     */
    public String getFilepath() {
        return folderpath;
    }

    /**
     * Returns web driver.
     * 
     * @return driver
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Returns base URL
     * 
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Singleton enforcement.
     * 
     * @return WebsiteExplorer instance
     */
    public static WebsiteExplorer getInstance() {
        if (instance == null) {
            instance = new WebsiteExplorer();
        }
        return instance;
    }

    /**
     * Initialize base url.
     * 
     * @param URL
     */
    public static void initialize(String URL) {
        WebsiteExplorer.instance.baseUrl = URL;
    }

    public static void initialize(String URL, String filepath) {
        WebsiteExplorer.getInstance().baseUrl = URL;
        WebsiteExplorer.folderpath = filepath;
    }

    /**
     * Constructor. Initializes Web driver.
     */
    private WebsiteExplorer() {
        driver = new HtmlUnitDriver();
    }

    /**
     * Checks exploring history to see if element e was visited already.
     * 
     * @param e
     *            HTML element
     * @return element was visited or not
     */
    protected boolean isElementAlreadyVisited(WebElement e) {
        if (visitedElements.isEmpty())
            return false;
        else {
            Iterator<String> it = visitedElements.iterator();
            while (it.hasNext()) {
                String pair = it.next();
                if (HTMLLocatorBuilder.getElementIdentifier(e).toLowerCase()
                        .equals(pair.toLowerCase()))
                    return true;
            }
        }
        return false;
    }

    /**
     * Chooses a random element to interact with.
     * 
     * @return random element
     */
    private WebElement chooseNextElement() {
        String[] TYPES = { "SELECT", "INPUT", "CALL", "SEARCH", "SORT", "LOGIN" };

        List<ArrayList<WebElement>> list = WebElementOrganizer
                .setupElementList();

        ArrayList<Integer> nonEmpties = new ArrayList<Integer>();
        String type = "";
        int allOptions = 0;

        for (int i = 0; i < list.size(); ++i) {
            if (!list.get(i).isEmpty() && (i == 0 || i > 0)) {
                nonEmpties.add(i);
                type += TYPES[i] + "=" + list.get(i).size() + "|";
                allOptions += list.get(i).size();
            }
        }
        System.out.println("non_empty: " + type + "|total: " + allOptions);
        if (nonEmpties.size() != 0) {
            // Choose random element of random list
            int rand1 = (int) Math.round(Math.random()
                    * (nonEmpties.size() - 1));
            if (rand1 >= 0) {
                lastAction = currentAction;
                currentAction = TYPES[nonEmpties.get(rand1)];

                List<WebElement> randChosenList = list.get(nonEmpties
                        .get(rand1));
                int rand2 = (int) Math.round(Math.random()
                        * (randChosenList.size() - 1));
                if (rand2 >= 0)
                    return randChosenList.get(rand2);
                else
                    return null;
            } else
                return null;
        } else
            return null;
    }

    /**
     * Checks if HTML element is an element one can insert text in.
     * 
     * @param e
     *            element to check
     */
    protected static boolean isElementTextInputable(WebElement e) {
        if (e.getTagName().toLowerCase().equals("textarea"))
            return true;
        if (e.getTagName().toLowerCase().equals("input")) {
            String types = "(type=\"(text|search|email)\")";
            if (e.toString().matches(".*" + types + ".*"))
                return true;
        }

        return false;
    }

    /**
     * Checks if HTML element has something to do with logins.
     * 
     * @param e
     *            element to check
     */
    protected static boolean isElementRelatedToLogin(WebElement e) {
        return e.toString().toLowerCase()
                .matches(".*" + getConfigurator().getLoginKeywords() + ".*");
    }

    /**
     * Checks if text input element is expecting numbers
     * 
     * @param e
     *            element to check
     */
    protected static boolean isInputElementExpectingNumbers(WebElement e) {
        String types = "((type=\\\")?number|price|quantity|qty\\s|zip\\s?code)";
        if (e.toString().matches(".*" + types + ".*"))
            return true;

        return false;
    }

    protected static void saveRow(String string, String action, String target,
            String parameter) {
        SeleniumTableRow row = new SeleniumTableRow(action, target, parameter);
        System.out.println(string + row.toString());
        writeToHistoryFile(row, true);
        visitedElements.add(target);

    }

    /*
     * Given a parent element, returns all anchor leaf nodes
     * 
     * @param parent parent node
     * 
     * @return all anchor leaf nodes
     * 
     * private List<WebElement> findAllChildrenOfAnElement(WebElement parent) {
     * List<WebElement> all = parent.findElements(By.xpath("*"));
     * List<WebElement> descendants = null; if (all.isEmpty()) { return null; }
     * 
     * List<WebElement> ret = new ArrayList<WebElement>(); for (WebElement item
     * : all) { ret.add(item); descendants = findAllChildrenOfAnElement(item);
     * if (!(descendants == null || descendants.isEmpty())) { for (WebElement i
     * : descendants) { ret.add(i); } }
     * 
     * } return ret; }
     */

    /*
     * private ArrayList<String> returnFullTextOfAnElement(WebElement parent) {
     * List<WebElement> children = findAllChildrenOfAnElement(parent);
     * ArrayList<String> ret = new ArrayList<String>();
     * ret.add(parent.toString()); for (WebElement item : children)
     * ret.add(item.toString());
     * 
     * return ret; }
     */

    private void findMasterDetailElementsInSearchResultPage() {
        Set<String> masterRetSet = new HashSet<String>(), detailRetSet = new HashSet<String>();

        // extract master elements
        List<WebElement> masterList = new ArrayList<WebElement>();

        for (String id : getConfigurator().getMasterIdentifiers()) {
            masterList.addAll(driver.findElements(By
                    .xpath("//*[contains(@class, \"" + id.toLowerCase()
                            + "\")]")));
            masterList
                    .addAll(driver.findElements(By.xpath("//*[contains(@id, \""
                            + id.toLowerCase() + "\")]")));
            masterList
                    .addAll(driver.findElements(By
                            .xpath("//*[contains(@name, \"" + id.toLowerCase()
                                    + "\")]")));
        }

        // extract detail elements
        List<WebElement> detailList = new ArrayList<WebElement>();

        // search for class, name and id = "[a-zA-z]+[id][a-zA-z]+"
        for (String id : getConfigurator().getDetailIdentifiers()) {
            detailList.addAll(driver.findElements(By
                    .xpath("//*[contains(@class, \"" + id.toLowerCase()
                            + "\")]")));
            detailList
                    .addAll(driver.findElements(By.xpath("//*[contains(@id, \""
                            + id.toLowerCase() + "\")]")));
            detailList
                    .addAll(driver.findElements(By
                            .xpath("//*[contains(@name, \"" + id.toLowerCase()
                                    + "\")]")));
        }

        for (WebElement elem : masterList) {
            List<WebElement> children = WebElementProcessor
                    .findChildrenAnchorNodesGivenParent(elem);
            String id = HTMLLocatorBuilder.getElementIdentifier(elem);

            if (!(children == null || children.isEmpty()))
                masterRetSet.add(id);
        }

        for (WebElement elem : detailList) {
            List<WebElement> children = WebElementProcessor
                    .findChildrenAnchorNodesGivenParent(elem);
            String id = HTMLLocatorBuilder.getElementIdentifier(elem);

            if (!(children == null || children.isEmpty()))
                detailRetSet.add(id);
        }

        // page -> elements
        for (String s : masterRetSet) {
            if (masterElements.containsKey(currentPage)) {
                masterElements.get(currentPage).add(s);
            } else {
                HashSet<String> temp = new HashSet<String>();
                temp.add(s);
                masterElements.put(currentPage, temp);
            }
        }

        for (String s : detailRetSet) {
            if (detailElements.containsKey(currentPage)) {
                detailElements.get(currentPage).add(s);
            } else {
                HashSet<String> temp = new HashSet<String>();
                temp.add(s);
                detailElements.put(currentPage, temp);
            }
        }

        System.out.println("masterElements size:" + masterElements.size() + "|"
                + "detailElements size:" + detailElements.size());
    }

    private void findMenuElementsInPage() {
        Set<String> retSet = new HashSet<String>();

        // search for <nav> tag
        List<WebElement> masterList = new ArrayList<WebElement>();
        masterList.addAll(driver.findElements(By.tagName("nav")));
        masterList.addAll(driver.findElements(By.tagName("header")));
        masterList.addAll(driver.findElements(By.tagName("footer")));

        // search for class, name and id = "[a-zA-z]+[nav][a-zA-z]+"
        for (String id : getConfigurator().getMenuIdentifiers()) {
            masterList.addAll(driver.findElements(By
                    .xpath("//*[contains(@class, \"" + id.toLowerCase()
                            + "\")]")));
            masterList
                    .addAll(driver.findElements(By.xpath("//*[contains(@id, \""
                            + id.toLowerCase() + "\")]")));
            masterList
                    .addAll(driver.findElements(By
                            .xpath("//*[contains(@name, \"" + id.toLowerCase()
                                    + "\")]")));
        }

        if (masterList.size() == 0)
            return;

        for (WebElement elem : masterList) {
            List<WebElement> children = WebElementProcessor
                    .findChildrenAnchorNodesGivenParent(elem);
            if (!(children == null || children.isEmpty()))
                retSet.add(HTMLLocatorBuilder.getElementIdentifier(elem));
        }
        if (retSet.size() == 0)
            return;

        int prevSize = 0;

        // page -> elements
        for (String s : retSet) {
            if (menuElements.containsKey(currentPage)) {
                menuElements.get(currentPage).add(s);
                prevSize = menuElements.get(currentPage).size();
            } else {
                HashSet<String> temp = new HashSet<String>();
                temp.add(s);
                menuElements.put(currentPage, temp);
                prevSize = menuElements.get(currentPage).size();
            }
        }
        System.out.println("menu elements found:" + prevSize);
    }

    /**
     * Writes crawling history to history file
     * 
     * @param r
     *            action to write
     * @param append
     *            true=append to file contents;false=reset file contents
     */
    private static void writeToHistoryFile(SeleniumTableRow r, boolean append) {
        // Write history to file
        FileWriter output = null;
        try {
            output = new FileWriter(new File(folderpath
                    + configurator.getHistoryFilepath()), append);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            output.write(r.toString() + "\n");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns driver to base url and checks if number of redirections is bigger
     * than the limit.
     * 
     * @param URL
     *            base URL
     * @return if exploring cycle should stop or not
     */
    public boolean goBackToHome() {
        driver.get(baseUrl);
        // TODO remove - testing
        testing.incrementHTML(driver.getPageSource(), driver.getCurrentUrl());
        saveRow("open", baseUrl, "EMPTY");
        wait = true;
        homeRedirections++;
        currAction--;

        if (homeRedirections > getConfigurator().getNumRedirects()) {
            System.out.println("Maximum redirects, stopping.");
            return true;
        } else {
            System.out
                    .println("No suitable element found, going back to base URL. "
                            + "Redirects left:"
                            + (getConfigurator().getNumRedirects() - homeRedirections));
            return false;
        }
    }

    public boolean isSearchingForPattern(String pattern) {
        if (getConfigurator().getPatternsToSearch().length == 1
                && getConfigurator().getPatternsToSearch()[0].toLowerCase()
                        .equals("all"))
            return true;
        else {
            for (String s : getConfigurator().getPatternsToSearch()) {
                if (s.toLowerCase().equals(pattern.toLowerCase()))
                    return true;
            }
        }
        return false;
    }

    public void exploreWebsite() {
        
        // set logger config
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
                .setLevel(java.util.logging.Level.SEVERE);
        driver.get(baseUrl);

        // TODO remove, for testing
        testing.init(driver.getCurrentUrl(), driver.getPageSource());
        testing.setHistoryFilepath(new File(folderpath
                + getConfigurator().getHistoryFilepath()).getAbsolutePath());

        try {
            System.setErr(new PrintStream(new File(folderpath + "err.txt")));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            System.setOut(new PrintStream(new File(folderpath + "out.txt")));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        // write 'open' action
        String r = baseUrl.replaceFirst(
                "/^(\\w+:\\/\\/[\\w\\.-]+(:\\d+)?)\\/.*/", "");
        SeleniumTableRow row = new SeleniumTableRow("open", r, "EMPTY");
        System.out.println(row.toString());
        writeToHistoryFile(row, false);

        while (currAction < getConfigurator().getNumActions()) {
            currentPage = driver.getCurrentUrl();

            System.out.println("\nAction #" + currAction + " | Page URL is: "
                    + currentPage);

            // search for menu elements (before the element is visited)
            if (isSearchingForPattern("menu"))
                findMenuElementsInPage();

            WebElement element = chooseNextElement();

            // see if one can search for master detail after search
            if (lastAction.equals("SEARCH")
                    && isSearchingForPattern("masterdetail")) {
                findMasterDetailElementsInSearchResultPage();
            }
            if (element == null) {
                // if no element is found, go back to home page
                boolean stop = goBackToHome();
                if (stop)
                    break;
            } else {
                WebElementProcessor.processElement(element);

                if (wait) {
                    // politeness delay
                    try {
                        TimeUnit.MILLISECONDS.sleep(750);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    wait = true;
                }

                // increments action
                currAction++;
            }
        }

        driver.quit();
    }

    protected static void saveRow(String action, String target, String parameter) {
        SeleniumTableRow row = new SeleniumTableRow(action, target, parameter);
        System.out.println(row.toString());
        writeToHistoryFile(row, true);
        visitedElements.add(target);
    }

    /**
     * @return the configurator
     */
    public static Configurator getConfigurator() {
        return configurator;
    }

    /**
     * @param configurator
     *            the configurator to set
     */
    public static void setConfigurator(Configurator configurator) {
        WebsiteExplorer.configurator = configurator;
    }

    public BrowserHandlerTesting getTesting() {
        // TODO Auto-generated method stub
        return testing;
    }

    public void handleError(String message, WebElement element) {
        System.out.println(element);
        // invalid, do something else
        currAction--;
        wait = false;
        visitedElements.add(HTMLLocatorBuilder
                .getElementIdentifier(element));
        
    }

}
