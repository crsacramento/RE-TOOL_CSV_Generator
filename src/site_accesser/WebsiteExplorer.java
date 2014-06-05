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
import org.openqa.selenium.support.ui.Select;

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
    private Set<String> visitedElements = new HashSet<String>();

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

    // private String lastAction = "NONE", currentAction = "NONE";

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
        instance.baseUrl = URL;
    }

    public static void initialize(String URL, String filepath) {
        instance.baseUrl = URL;
        folderpath = filepath;
    }

    /**
     * Constructor. Initializes Web driver.
     */
    private WebsiteExplorer() {
        driver = new HtmlUnitDriver();
    }

    /**
     * @return the configurator
     */
    public Configurator getConfigurator() {
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
        System.out.println(message);
        // invalid, do something else
        currAction--;
        wait = false;
        visitedElements.add(HTMLLocatorBuilder.getElementIdentifier(element));
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
                // lastAction = currentAction;
                // currentAction = TYPES[nonEmpties.get(rand1)];

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
    protected boolean isElementTextInputable(WebElement e) {
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
    protected boolean isElementRelatedToLogin(WebElement e) {
        return e.toString().toLowerCase()
                .matches(".*" + getConfigurator().getLoginKeywords() + ".*");
    }

    /**
     * Checks if text input element is expecting numbers
     * 
     * @param e
     *            element to check
     */
    protected boolean isInputElementExpectingNumbers(WebElement e) {
        String types = "((type=\\\")?number|price|quantity|qty\\s|zip\\s?code)";
        if (e.toString().matches(".*" + types + ".*"))
            return true;

        return false;
    }

    protected void saveRow(String string, String action, String target,
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
            List<WebElement> children = findChildrenAnchorNodesGivenParent(elem);
            String id = HTMLLocatorBuilder.getElementIdentifier(elem);

            if (!(children == null || children.isEmpty()))
                masterRetSet.add(id);
        }

        for (WebElement elem : detailList) {
            List<WebElement> children = findChildrenAnchorNodesGivenParent(elem);
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
            List<WebElement> children = findChildrenAnchorNodesGivenParent(elem);
            if (!(children == null || children.isEmpty()))
                for (WebElement c : children)
                    retSet.add(HTMLLocatorBuilder.getElementIdentifier(c));
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
    private void writeToHistoryFile(SeleniumTableRow r, boolean append) {
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

        // TODO remove, for testing
        testing.init(driver.getCurrentUrl(), driver.getPageSource());
        testing.setHistoryFilepath(new File(folderpath
                + getConfigurator().getHistoryFilepath()).getAbsolutePath());

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
            if (isSearchingForPattern("masterdetail")) {
                findMasterDetailElementsInSearchResultPage();
            }
            if (element == null) {
                // if no element is found, go back to home page
                boolean stop = goBackToHome();
                if (stop)
                    break;
            } else {
                boolean error = false;
                System.out.println("ELEMENT: "
                        + element.toString()
                        + (!element.getText().isEmpty() ? " (has text)"
                                : " (no text)") + " | LOCATOR:"
                        + HTMLLocatorBuilder.getElementIdentifier(element));
                // login
                if (isElementRelatedToLogin(element)) {
                    boolean processedLogin = processLogin(element);
                    if (processedLogin) {
                        driver.get(baseUrl);
                        // TODO remove - testing
                        testing.incrementHTML(driver.getPageSource(),
                                driver.getCurrentUrl());
                        saveRow("open", baseUrl, "EMPTY");
                    }

                } else if (element.getTagName().toLowerCase().equals("a")) {
                    processAnchorElements(element);
                } else if (getConfigurator()
                        .includeChildrenNodesOnInteraction()) {
                    // input or select
                    if (!error)
                        error = includeFormChildrenAndVisitElement(element);
                } else {
                    if (element.getTagName().toLowerCase().equals("select")) {
                        if (!error)
                            error = processSelectElement(element);
                    } else if (isElementTextInputable(element)) {
                        if (!error)
                            error = processInputElement(element);
                    }
                }

                if (error) {
                    // if no element is found, go back to home page
                    boolean stop = goBackToHome();
                    if (stop)
                        break;
                }

                if (wait) {
                    // politeness delay
                    try {
                        TimeUnit.MILLISECONDS.sleep(configurator
                                .getPolitenessDelay());
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

    protected void saveRow(String action, String target, String parameter) {
        SeleniumTableRow row = new SeleniumTableRow(action, target, parameter);
        System.out.println(row.toString());
        writeToHistoryFile(row, true);
        visitedElements.add(target);
    }

    /**
     * Does necessary operations to interact with an anchor element
     * 
     * @param e
     *            element to interact with
     */
    public void processAnchorElements(WebElement element) {
        // link
        saveRow("clickAndWait",
                HTMLLocatorBuilder.getElementIdentifier(element), "EMPTY");

        element.click();
        // TODO remove - testing
        testing.incrementHTML(getDriver().getPageSource(), getDriver()
                .getCurrentUrl());
    }

    /**
     * If element is input or select, visits a login form completely, inserting
     * invalid words (if no loginConfiguration was specified) or user-defined
     * login credentials and returns true; if element is a link, clicks on it
     * and returns false.
     * 
     * @param element
     *            chosen element to visit
     * @return if login form was visited or not
     */
    public boolean processLogin(WebElement element) {
        if (element.getTagName().toLowerCase().equals("input")
                || element.getTagName().toLowerCase().equals("select")) {
            // login form, fill all form inputs
            WebElement form = findParentForm(element);
            List<WebElement> children = findInputAndSelectChildNodesGivenParentForm(form);

            String username = "username", password = "password";
            if (!getConfigurator().getLoginConfiguration().isEmpty()) {
                username = getConfigurator().getLoginConfiguration().get(
                        "username");
                password = getConfigurator().getLoginConfiguration().get(
                        "password");
            }

            String content = "";

            for (WebElement e : children) {
                if (e.getTagName().toLowerCase().equals("input")) {
                    switch (e.getAttribute("type")) {
                        case "text":
                        case "password":
                        case "email":
                            if (e.toString()
                                    .matches(
                                            ".*(user(\\s|_)?(name|id)?|e?mail|log(\\s|_)?in).*")) {
                                content = username;
                            } else if (e.toString()
                                    .matches(".*(pass(word)?).*")) {
                                content = password;
                            } else {
                                int rand = (int) (Math.random() * (getConfigurator()
                                        .getTypedKeywords().length - 1));
                                content = getConfigurator().getTypedKeywords()[rand];
                            }

                            saveRow("type",
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    content);
                            // TODO remove - testing
                            testing.incrementCorrelation();
                            // interact
                            e.clear();
                            e.sendKeys(content);
                            break;
                        case "radio":
                        case "checkbox":
                        case "button":
                            saveRow("click",
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            // TODO remove - testing
                            testing.incrementCorrelation();
                            // interact
                            e.click();
                            break;
                    }
                } else if (e.getTagName().toLowerCase().equals("select")) {
                    List<WebElement> options = element.findElements(By
                            .xpath(".//option"));
                    if (options.size() != 0) {
                        int rand1 = (int) Math.round(Math.random()
                                * (options.size() - 1));
                        Select select = new Select(e);
                        saveRow("select",
                                HTMLLocatorBuilder.getElementIdentifier(e),
                                "label=\"" + options.get(rand1).getText() + '"');
                        // TODO remove - testing
                        testing.incrementCorrelation();
                        // interact
                        select.selectByIndex(rand1);
                    }
                }
            }

            handleFormSubmission(form);
            return true;
        } else {
            // login link
            processAnchorElements(element);
            return false;
        }
    }

    /**
     * Does necessary operations to interact with an input element
     * 
     * @param e
     *            element to interact with
     */
    public boolean processInputElement(WebElement element) {
        String identifier = HTMLLocatorBuilder.getElementIdentifier(element);

        // if form doesn't submit dynamically, submit
        // form manually
        if (!element.toString().matches(".*onchange=.*submit.*")) {

            // verify if input wants numbers or words
            if (isInputElementExpectingNumbers(element)) {
                // it wants an integer value
                int rand1 = (int) Math.round(Math.random() * 100) + 1;
                saveRow("type", identifier, "" + rand1);
                // TODO remove - testing
                testing.incrementCorrelation();

                // interact
                element.clear();
                element.sendKeys(Integer.toString(rand1));
            } else {
                // insert random keyword
                int rand1 = (int) Math.round(Math.random()
                        * (getConfigurator().getTypedKeywords().length - 1));

                saveRow("type", identifier, getConfigurator()
                        .getTypedKeywords()[rand1]);
                // TODO remove - testing
                testing.incrementCorrelation();

                // interact
                element.clear();
                element.sendKeys(getConfigurator().getTypedKeywords()[rand1]);
            }

            return handleFormSubmission(element);
        } else {
            // add 'andWait' suffix to mark page change

            if (isInputElementExpectingNumbers(element)) {
                // it wants an integer value
                int rand1 = (int) Math.round(Math.random() * 100) + 1;
                saveRow("typeAndWait", identifier, "" + rand1);
                // TODO remove - testing
                testing.incrementCorrelation();

                element.clear();
                element.sendKeys(Integer.toString(rand1));
            } else {
                // insert random keyword
                int rand1 = (int) Math.round(Math.random()
                        * (getConfigurator().getTypedKeywords().length - 1));

                saveRow("typeAndWait", identifier, getConfigurator()
                        .getTypedKeywords()[rand1]);
                // TODO remove - testing

                // interact
                element.clear();
                element.sendKeys(getConfigurator().getTypedKeywords()[rand1]);

                // TODO
                testing.incrementHTML(getDriver().getPageSource(), getDriver()
                        .getCurrentUrl());
            }
            return false;
        }
    }

    /**
     * Submits a previously visited form.
     * 
     * @param element
     *            element inside the form to be visited.
     */
    public boolean handleFormSubmission(WebElement element) {
        // search for conventional submit elements
        List<WebElement> elems = element.findElements(By
                .xpath("//input[@type='submit']"));
        List<WebElement> submit = new ArrayList<WebElement>();
        System.out.println("elems.size()=" + elems.size());

        if (elems.size() > 0) {
            for (WebElement e : elems)
                if (!e.toString()
                        .toLowerCase()
                        .matches(
                                ".*"
                                        + getConfigurator()
                                                .getGeneralWordsToExclude()
                                        + ".*")) {
                    System.out.println("SUBMIT: " + e.toString());

                    submit.add(e);
                }
        }
        if (submit.size() > 0) {

            if (submit.size() > 0) {
                System.out.println("SUBMIT=len:" + submit.size());
                saveRow("clickAndWait",
                        HTMLLocatorBuilder.getElementIdentifier(submit.get(0)),
                        "EMPTY");
                // interact
                submit.get(0).click();
                // TODO remove - testing
                testing.incrementHTML(getDriver().getPageSource(), getDriver()
                        .getCurrentUrl());
            } else {
                handleError("THERE ARE NO SUBMITS", element);
                return true;
            }
        } else {
            // search for elements that dynamically submit forms
            elems = element.findElements(By
                    .xpath("//*[contains(@onclick,'submit')]"));
            List<WebElement> dynamicSubmits = new ArrayList<WebElement>();
            if (elems.size() > 0) {
                for (WebElement sub : elems) {
                    if (!sub.toString()
                            .toLowerCase()
                            .matches(
                                    ".*"
                                            + getConfigurator()
                                                    .getGeneralWordsToExclude()
                                            + ".*")) {
                        System.out.println("DYN_SUBMIT: " + sub.toString());
                        dynamicSubmits.add(sub);
                    }
                }
            }
            if (dynamicSubmits.size() > 0) {
                System.out.println("DYN_SUBMIT=len:" + submit.size());
                saveRow("clickAndWait",
                        HTMLLocatorBuilder.getElementIdentifier(dynamicSubmits
                                .get(0)), "EMPTY");

                // interact
                dynamicSubmits.get(0).click();
                // TODO remove - testing
                testing.incrementHTML(getDriver().getPageSource(), getDriver()
                        .getCurrentUrl());
            } else {
                handleError("THERE ARE NO DYNAMIC SUBMITS", element);
                return true;
            }
        }
        return false;

    }

    /**
     * Does necessary operations to interact with a dropdown menu element
     * 
     * @param element
     *            to interact with
     */
    public boolean processSelectElement(WebElement element) {
        String identifier = HTMLLocatorBuilder.getElementIdentifier(element);
        Select select = new Select(element);

        // select random option
        List<WebElement> options = element.findElements(By.xpath(".//option"));
        if (options.size() == 0) {
            handleError("SELECT HAS NO OPTIONS", element);
            return true;
        } else {
            System.out.println("options:" + options.size());
            int rand1 = (int) Math.round(Math.random() * (options.size() - 1));
            System.out.println("OPTION TEXT:" + options.get(rand1).getText());

            if (options.get(rand1).getText().isEmpty()) {
                handleError("RANDOM OPTION HAS NO TEXT", element);
                return true;
            } else {
                // it's a valid option
                select.selectByIndex(rand1);

                if (!element.toString().matches(".*onchange=\".*submit\".*")) {
                    // if form doesn't submit dynamically, submit
                    // form manually
                    saveRow("select", identifier,
                            "label=\"" + options.get(rand1).getText() + '"');
                    // TODO remove - testing
                    testing.incrementCorrelation();

                    handleFormSubmission(element);
                } else {
                    // add 'andWait' suffix to mark page change
                    saveRow("selectAndWait",// element.toString(),
                            identifier, "label=\""
                                    + options.get(rand1).getText() + '"');
                    // TODO remove - testing
                    testing.incrementHTML(getDriver().getPageSource(),
                            getDriver().getCurrentUrl());
                }
            }
        }
        return false;
    }

    /**
     * Includes element siblings (other inputs and selects that are children of
     * the element's form parent) and lists them on the history file without
     * interacting with them.
     * 
     * @param element
     *            element to interact with
     * @return
     */
    public boolean includeFormChildrenAndVisitElement(WebElement element) {
        WebElement form = findParentForm(element);
        List<WebElement> children = findInputAndSelectChildNodesGivenParentForm(form);

        for (WebElement e : children) {
            if (!e.toString().equals(element.toString())) {
                if (e.getTagName().equals("input")) {
                    switch (e.getAttribute("type")) {
                        case "text":
                        case "password":
                        case "email":
                            saveRow("SIBLING:",
                                    "type",// e.toString(),
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            // TODO remove - testing
                            testing.incrementCorrelation();
                            break;
                        case "radio":
                        case "checkbox":
                        case "button":
                            saveRow("SIBLING:", "click",
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            // TODO remove - testing
                            testing.incrementCorrelation();
                            break;
                    }
                } else if (e.getTagName().toLowerCase().equals("select")) {
                    List<WebElement> options = element.findElements(By
                            .xpath(".//option"));
                    if (options.size() != 0) {
                        saveRow("SIBLING:",
                                "select",// e.toString(),
                                HTMLLocatorBuilder.getElementIdentifier(e),
                                "EMPTY");
                        // TODO remove - testing
                        testing.incrementCorrelation();
                    }
                }
            }
        }

        // process element
        if (isElementTextInputable(element))
            return processInputElement(element);
        else
            return processSelectElement(element);
    }

    /**
     * Given a form element, returns all input and select leaf nodes
     * 
     * @param form
     * @return all input and select leaf nodes
     */
    public List<WebElement> findInputAndSelectChildNodesGivenParentForm(
            WebElement form) {
        List<WebElement> all = form.findElements(By.xpath("*"));
        List<WebElement> descendants = null;
        if (all.isEmpty()) {
            return null;
        }

        List<WebElement> ret = new ArrayList<WebElement>();
        for (WebElement item : all) {
            if (item.getTagName().toLowerCase().equals("input")
                    && !item.getAttribute("type").equals("submit")) {
                ret.add(item);
            } else if (item.getTagName().toLowerCase().equals("select")) {
                ret.add(item);
            } else {
                descendants = findInputAndSelectChildNodesGivenParentForm(item);
                if (!(descendants == null || descendants.isEmpty())) {
                    for (WebElement i : descendants) {
                        ret.add(i);
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Given a parent element, returns all anchor leaf nodes
     * 
     * @param parent
     *            parent node
     * @return all anchor leaf nodes
     */
    public List<WebElement> findChildrenAnchorNodesGivenParent(WebElement parent) {
        List<WebElement> all = parent.findElements(By.xpath("*"));
        List<WebElement> descendants = null;
        if (all.isEmpty()) {
            return null;
        }

        List<WebElement> ret = new ArrayList<WebElement>();
        for (WebElement item : all) {
            if (item.getTagName().toLowerCase().equals("a")) {
                ret.add(item);
            } else {
                descendants = findChildrenAnchorNodesGivenParent(item);
                if (!(descendants == null || descendants.isEmpty())) {
                    for (WebElement i : descendants) {
                        ret.add(i);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Given a WebElement, finds the parent form
     * 
     * @param element
     *            child element node
     * @return parent form
     */
    public WebElement findParentForm(WebElement element) {
        WebElement current = element;
        while (!(current == null
                || current.getTagName().toLowerCase().equals("form")
                || current.getTagName().toLowerCase().equals("html") || current
                .getTagName().toLowerCase().equals("body"))) {
            current = current.findElement(By.xpath(".."));
        }
        return current;
    }

}
