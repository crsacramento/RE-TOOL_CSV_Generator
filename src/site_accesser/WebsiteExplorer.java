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

import configuration.Configurator;

public class WebsiteExplorer {
    /**
     * the URL where the crawler will start and the domain to which it will
     * restrict itself
     */
    private String baseUrl = "";

    /** action history */
    private static ArrayList<SeleniumTableRow> history = new ArrayList<SeleniumTableRow>();

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

    /**
     * Constructor. Initializes Web driver.
     */
    private WebsiteExplorer() {
        driver = new HtmlUnitDriver();
        // driver.setJavascriptEnabled(true);
        // driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    /**
     * Checks exploring history to see if element e was visited already.
     * 
     * @param e
     *            HTML element
     * @return element was visited or not
     */
    public boolean isElementAlreadyVisited(WebElement e) {
        if (visitedElements.isEmpty())
            return false;
        else {
            // Iterator<Entry<String, WebElement>> it =
            // visitedElements.entrySet().iterator();
            Iterator<String> it = visitedElements.iterator();
            while (it.hasNext()) {
                // Map.Entry<String, WebElement> pair = it.next();
                String pair = it.next();
                if (// p.equals(pair.getKey().toString())&&
                e.toString().equals(pair))
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
            if (!list.get(i).isEmpty()
                    && (i == 0 || i > 0 && isSearchingForPattern(TYPES[i]))) {
                nonEmpties.add(i);
                type += TYPES[i] + "=" + list.get(i).size() + "|";
                allOptions += list.get(i).size();
            }
        }
        System.out.println("non_empty: " + type + "total: " + allOptions);
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
    private boolean isElementTextInputable(WebElement e) {
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
    private boolean isElementRelatedToLogin(WebElement e) {
        return e.toString().toLowerCase()
                .matches(".*" + configurator.getLoginKeywords() + ".*");
    }

    /**
     * Checks if text input element is expecting numbers
     * 
     * @param e
     *            element to check
     */
    private boolean isInputElementExpectingNumbers(WebElement e) {
        String types = "((type=\\\")?number|price|quantity|qty\\s|zip\\s?code)";
        if (e.toString().matches(".*" + types + ".*"))
            return true;

        return false;
    }

    /**
     * Does necessary operations to interact with an anchor element
     * 
     * @param e
     *            element to interact with
     */
    private void processAnchorElements(WebElement element) {
        // link
        SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
        // element.toString(),
                HTMLLocatorBuilder.getElementIdentifier(element), "EMPTY");
        history.add(row);
        writeToHistoryFile(row, true);
        System.out.println("row: " + row.toString());

        // add visited elements to list
        // visitedElements.add(element.toString());
        visitedElements.add(HTMLLocatorBuilder.getElementIdentifier(element));

        element.click();
    }

    /**
     * Does necessary operations to interact with an input element
     * 
     * @param e
     *            element to interact with
     */
    private void processInputElement(WebElement element) {
        String identifier = HTMLLocatorBuilder.getElementIdentifier(element);
        SeleniumTableRow row;

        // clear inside text
        element.clear();

        // if form doesn't submit dynamically, submit
        // form manually
        if (!element.toString().matches(".*onchange=.*submit.*")) {

            // verify if input wants numbers or words
            if (isInputElementExpectingNumbers(element)) {
                // it wants an integer value
                int rand1 = (int) Math.round(Math.random() * 100) + 1;
                row = new SeleniumTableRow("type",
                // element.toString(),
                        identifier, "\"" + rand1 + "\"");
                System.out.println("row: " + row.toString());
                history.add(row);
                writeToHistoryFile(row, true);
                // visitedElements.add(element.toString());
                visitedElements.add(HTMLLocatorBuilder
                        .getElementIdentifier(element));

                // interact
                element.sendKeys(Integer.toString(rand1));
            } else {
                // insert random keyword
                int rand1 = (int) Math.round(Math.random()
                        * (configurator.getTypedKeywords().length - 1));

                row = new SeleniumTableRow("type",
                // element.toString(),
                        identifier, "\""
                                + configurator.getTypedKeywords()[rand1] + "\"");
                System.out.println("row: " + row.toString());
                history.add(row);
                writeToHistoryFile(row, true);
                // visitedElements.add(element.toString());
                visitedElements.add(HTMLLocatorBuilder
                        .getElementIdentifier(element));

                // interact
                element.sendKeys(configurator.getTypedKeywords()[rand1]);
            }

            handleFormSubmission(element);
        } else {
            // add 'andWait' suffix to mark page change

            if (isInputElementExpectingNumbers(element)) {
                // it wants an integer value
                int rand1 = (int) Math.round(Math.random() * 100) + 1;
                row = new SeleniumTableRow("typeAndWait",
                // element.toString(),
                        identifier, "\"" + rand1 + "\"");
                System.out.println("row: " + row.toString());
                history.add(row);
                writeToHistoryFile(row, true);
                // visitedElements.add(element.toString());
                visitedElements.add(HTMLLocatorBuilder
                        .getElementIdentifier(element));
                element.sendKeys(Integer.toString(rand1));
            } else {
                // insert random keyword
                int rand1 = (int) Math.round(Math.random()
                        * (configurator.getTypedKeywords().length - 1));

                row = new SeleniumTableRow("typeAndWait",
                // element.toString(),
                        identifier, "\""
                                + configurator.getTypedKeywords()[rand1] + "\"");
                System.out.println("row: " + row.toString());
                history.add(row);
                writeToHistoryFile(row, true);

                // add visited elements to list
                // visitedElements.add(element.toString());
                visitedElements.add(HTMLLocatorBuilder
                        .getElementIdentifier(element));

                // interact
                element.sendKeys(configurator.getTypedKeywords()[rand1]);
            }
        }
    }

    /**
     * Submits a previously visited form.
     * 
     * @param element
     *            element inside the form to be visited.
     */
    private void handleFormSubmission(WebElement element) {
        // search for conventional submit elements
        List<WebElement> submit = element.findElements(By
                .xpath("//input[@type='submit']"));
        if (submit.size() > 0) {
            for (WebElement sub : submit)
                System.out.println("SUBMIT: " + sub.toString());
            SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
                    // submit.get(0).toString(),
                    HTMLLocatorBuilder.getElementIdentifier(submit.get(0)),
                    "EMPTY");
            history.add(row);
            writeToHistoryFile(row, true);
            // visitedElements.add(submit.get(0).toString());
            visitedElements.add(HTMLLocatorBuilder.getElementIdentifier(submit
                    .get(0)));

            // interact
            submit.get(0).click();
        } else {
            // search for elements that dynamically submit forms
            List<WebElement> dynamicSubmits = element.findElements(By
                    .xpath("//*[contains(@onclick,'submit')]"));

            if (dynamicSubmits.size() > 0) {
                for (WebElement sub : dynamicSubmits)
                    System.out.println("DYN_SUBMIT: " + sub.toString());
                SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
                // dynamicSubmits.get(0).toString(),
                        HTMLLocatorBuilder.getElementIdentifier(dynamicSubmits
                                .get(0)), "EMPTY");
                history.add(row);
                writeToHistoryFile(row, true);
                // element.submit();
                // visitedElements.add(dynamicSubmits.get(0).toString());
                visitedElements.add(HTMLLocatorBuilder
                        .getElementIdentifier(dynamicSubmits.get(0)));

                // interact
                dynamicSubmits.get(0).click();
            } else {
                System.out.println("THERE ARE NO SUBMITS");
            }
        }

    }

    /**
     * Does necessary operations to interact with a dropdown menu element
     * 
     * @param element
     *            to interact with
     */
    private void processSelectElement(WebElement element) {
        String identifier = HTMLLocatorBuilder.getElementIdentifier(element);
        Select select = new Select(element);

        // select random option
        List<WebElement> options = element.findElements(By.xpath(".//option"));
        if (options.size() == 0) {
            // invalid, do something else
            currAction--;
            wait = false;
        } else {
            System.out.println("options:" + options.size());
            int rand1 = (int) Math.round(Math.random() * (options.size() - 1));
            System.out.println("OPTION TEXT:" + options.get(rand1).getText());

            if (options.get(rand1).getText().isEmpty()) {
                // invalid, do something else
                currAction--;
                wait = false;
            } else {
                // it's a valid option
                select.selectByIndex(rand1);

                if (!element.toString().matches(".*onchange=\".*submit\".*")) {
                    // if form doesn't submit dynamically, submit
                    // form manually
                    SeleniumTableRow row = new SeleniumTableRow("select",
                            identifier, "label=\""
                                    + options.get(rand1).getText() + '"');
                    System.out.println("row: " + row.toString());
                    history.add(row);
                    writeToHistoryFile(row, true);
                    // visitedElements.add(element.toString());
                    visitedElements.add(identifier);
                    handleFormSubmission(element);
                } else {
                    // add 'andWait' suffix to mark page change
                    SeleniumTableRow row = new SeleniumTableRow(
                            "selectAndWait",// element.toString(),
                            identifier, "label=\""
                                    + options.get(rand1).getText() + '"');
                    System.out.println("row: " + row.toString());
                    history.add(row);
                    writeToHistoryFile(row, true);
                    visitedElements.add(identifier);
                }
            }
        }
    }

    /**
     * Visits a login form completely inserting invalid words, or clicks on a
     * login link.
     * 
     * @param element
     *            chosen element to visit
     * @return if login form was visited or not
     */
    private boolean processInvalidLogin(WebElement element) {
        if (element.getTagName().toLowerCase().equals("input")
                || element.getTagName().toLowerCase().equals("select")) {
            // login form, fill all form inputs
            WebElement form = findParentForm(element);
            List<WebElement> children = findInputAndSelectChildNodesGivenParentForm(form);
            SeleniumTableRow row = null;

            for (WebElement e : children) {
                if (e.getTagName().toLowerCase().equals("input")) {
                    switch (e.getAttribute("type")) {
                        case "text":
                        case "password":
                        case "email":
                            int rand = (int) (Math.random() * (configurator
                                    .getTypedKeywords().length - 1));
                            row = new SeleniumTableRow(
                                    "type",// e.toString(),
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    configurator.getTypedKeywords()[rand]);
                            System.out.println("row: " + row.toString());
                            history.add(row);
                            writeToHistoryFile(row, true);
                            // visitedElements.add(e.toString());
                            visitedElements.add(HTMLLocatorBuilder
                                    .getElementIdentifier(e));
                            // interact
                            e.clear();
                            e.sendKeys(configurator.getTypedKeywords()[rand]);
                            break;
                        case "radio":
                        case "checkbox":
                            row = new SeleniumTableRow(
                                    "click",// e.toString(),
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            System.out.println("row: " + row.toString());
                            history.add(row);
                            writeToHistoryFile(row, true);
                            // visitedElements.add(e.toString());
                            visitedElements.add(HTMLLocatorBuilder
                                    .getElementIdentifier(e));
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
                        row = new SeleniumTableRow(
                                "select",// e.toString(),
                                HTMLLocatorBuilder.getElementIdentifier(e),
                                "label=\"" + options.get(rand1).getText() + '"');
                        System.out.println("row: " + row.toString());
                        history.add(row);
                        writeToHistoryFile(row, true);
                        // visitedElements.add(e.toString());
                        visitedElements.add(HTMLLocatorBuilder
                                .getElementIdentifier(e));
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
     * Includes element siblings (other inputs and selects that are children of
     * the element's form parent) and lists them on the history file without
     * interacting with them.
     * 
     * @param element
     *            element to interact with
     */
    private void includeFormChildrenAndVisitElement(WebElement element) {
        WebElement form = findParentForm(element);
        List<WebElement> children = findInputAndSelectChildNodesGivenParentForm(form);
        SeleniumTableRow row = null;

        for (WebElement e : children) {
            if (!e.toString().equals(element.toString())) {

                row = new SeleniumTableRow("list",
                        HTMLLocatorBuilder.getElementIdentifier(e), "EMPTY");
                System.out.println("list siblings|row: " + row.toString());
                history.add(row);
                writeToHistoryFile(row, true);
            }
        }

        // process element
        if (isElementTextInputable(element))
            processInputElement(element);
        else
            processSelectElement(element);
    }

    /**
     * Given a form element, returns all input and select leaf nodes
     * 
     * @param form
     * @return all input and select leaf nodes
     */
    private List<WebElement> findInputAndSelectChildNodesGivenParentForm(
            WebElement form) {
        List<WebElement> all = form.findElements(By.xpath("*"));
        List<WebElement> descendants = null;
        if (all.isEmpty()) {
            return null;
        }

        List<WebElement> ret = new ArrayList<WebElement>();
        for (WebElement item : all) {
            if (item.getTagName().toLowerCase().equals("input")) {
                switch (item.getAttribute("type")) {
                    case "text":
                    case "password":
                    case "email":
                    case "radio":
                    case "checkbox": {
                        ret.add(item);
                    }
                }
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
    private List<WebElement> findChildrenAnchorNodesGivenParent(
            WebElement parent) {
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
     * Given a parent element, returns all anchor leaf nodes
     * 
     * @param parent
     *            parent node
     * @return all anchor leaf nodes
     */
    private List<WebElement> findAllChildrenOfAnElement(WebElement parent) {
        List<WebElement> all = parent.findElements(By.xpath("*"));
        List<WebElement> descendants = null;
        if (all.isEmpty()) {
            return null;
        }

        List<WebElement> ret = new ArrayList<WebElement>();
        for (WebElement item : all) {
            ret.add(item);
            descendants = findAllChildrenOfAnElement(item);
            if (!(descendants == null || descendants.isEmpty())) {
                for (WebElement i : descendants) {
                    ret.add(i);
                }
            }

        }
        return ret;
    }

    private ArrayList<String> returnFullTextOfAnElement(WebElement parent) {
        List<WebElement> children = findAllChildrenOfAnElement(parent);
        ArrayList<String> ret = new ArrayList<String>();
        ret.add(parent.toString());
        for (WebElement item : children)
            ret.add(item.toString());

        return ret;
    }

    private void findMasterDetailElementsInSearchResultPage() {
        Set<String> masterRetSet = new HashSet<String>(), detailRetSet = new HashSet<String>();

        // extract master elements
        List<WebElement> masterList = new ArrayList<WebElement>();

        for (String id : configurator.getMasterIdentifiers()) {
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
        for (String id : configurator.getMasterIdentifiers()) {
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
            if (!(children == null || children.isEmpty()))
                // masterRetSet.add(elem.toString());
                masterRetSet.add(HTMLLocatorBuilder.getElementIdentifier(elem));
        }

        for (WebElement elem : detailList) {
            List<WebElement> children = findChildrenAnchorNodesGivenParent(elem);
            if (!(children == null || children.isEmpty()))
                // detailRetSet.add(elem.toString());
                detailRetSet.add(HTMLLocatorBuilder.getElementIdentifier(elem));
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
        System.out.println("menuElements size:" + menuElements.size());
    }

    private void findMenuElementsInPage() {
        Set<String> retSet = new HashSet<String>();

        // search for <nav> tag
        List<WebElement> masterList = new ArrayList<WebElement>();
        masterList.addAll(driver.findElements(By.tagName("nav")));
        masterList.addAll(driver.findElements(By.tagName("header")));
        masterList.addAll(driver.findElements(By.tagName("footer")));

        // search for class, name and id = "[a-zA-z]+[nav][a-zA-z]+"
        for (String id : configurator.getMenuIdentifiers()) {
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
     * Given a WebElement, finds the parent form
     * 
     * @param element
     *            child element node
     * @return parent form
     */
    private WebElement findParentForm(WebElement element) {
        WebElement current = element;
        while (!(current == null
                || current.getTagName().toLowerCase().equals("form")
                || current.getTagName().toLowerCase().equals("html") || current
                .getTagName().toLowerCase().equals("body"))) {
            current = current.findElement(By.xpath(".."));
        }
        return current;
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
            output = new FileWriter(
                    new File(configurator.getHistoryFilepath()), append);
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
    private boolean goBackToHome(String URL) {
        driver.get(baseUrl);
        SeleniumTableRow row = new SeleniumTableRow("open", URL, "EMPTY");
        history.add(row);
        writeToHistoryFile(row, true);
        wait = true;
        homeRedirections++;
        currAction--;

        if (homeRedirections > configurator.getNumRedirects()) {
            System.out.println("Maximum redirects, stopping.");
            return true;
        } else {
            System.out
                    .println("No suitable element found, going back to base URL. "
                            + "Redirects left:"
                            + (configurator.getNumRedirects() - homeRedirections));
            return false;
        }
    }

    public boolean isSearchingForPattern(String pattern) {
        if (configurator.getPatternsToSearch().length == 1
                && configurator.getPatternsToSearch()[0].toLowerCase().equals(
                        "all"))
            return true;
        else {
            for (String s : configurator.getPatternsToSearch()) {
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
            System.setErr(new PrintStream(new File("err.txt")));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            System.setOut(new PrintStream(new File("out.txt")));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        // write 'open' action
        String r = baseUrl.replaceFirst(
                "/^(\\w+:\\/\\/[\\w\\.-]+(:\\d+)?)\\/.*/", "");
        SeleniumTableRow row = new SeleniumTableRow("open", r, "EMPTY");
        history.add(row);
        writeToHistoryFile(row, false);
        System.out.println(history.get(0).toString());

        while (currAction < configurator.getNumActions()) {
            currentPage = driver.getCurrentUrl();

            System.out.println("Action #" + currAction + " | Page URL is: "
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
                boolean stop = goBackToHome(r);
                if (stop)
                    break;
            } else {

                System.out.println("ELEMENT: "
                        + element.toString()
                        + (!element.getText().isEmpty() ? " (has text)"
                                : " (no text)"));

                System.out.println("LOCATOR:"
                        + HTMLLocatorBuilder.getElementIdentifier(element));

                if (isElementRelatedToLogin(element)
                        && isSearchingForPattern("login")) {
                    if (configurator.getLoginConfiguration().isEmpty()) {
                        boolean processedLogin = processInvalidLogin(element);
                        if (processedLogin)
                            goBackToHome(r);
                    } else {
                        boolean processedLogin = processValidLogin(element);
                        if (processedLogin)
                            goBackToHome(r);
                    }
                }// dropdown list
                else if (element.getTagName().toLowerCase().equals("select")) {
                    if (!configurator.includeChildrenNodesOnInteraction())
                        processSelectElement(element);
                    else
                        includeFormChildrenAndVisitElement(element);
                } else if (isElementTextInputable(element)) {
                    if (!configurator.includeChildrenNodesOnInteraction())
                        processInputElement(element);
                    else
                        includeFormChildrenAndVisitElement(element);
                } else {
                    processAnchorElements(element);
                }

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

    private boolean processValidLogin(WebElement element) {
        if (element.getTagName().toLowerCase().equals("input")
                || element.getTagName().toLowerCase().equals("select")) {
            // login form, fill all form inputs
            WebElement form = findParentForm(element);
            List<WebElement> children = findInputAndSelectChildNodesGivenParentForm(form);
            SeleniumTableRow row = null;

            for (WebElement e : children) {
                if (e.getTagName().toLowerCase().equals("input")
                        && e.toString().matches("user(name)?|e?mail")) {
                    row = new SeleniumTableRow(
                            "type",// e.toString(),
                            HTMLLocatorBuilder.getElementIdentifier(e),
                            configurator.getLoginConfiguration()
                                    .get("username"));
                    writeToHistoryFile(row, true);
                    visitedElements.add(HTMLLocatorBuilder
                            .getElementIdentifier(e));
                    e.clear();
                    e.sendKeys(configurator.getLoginConfiguration().get(
                            "username"));
                } else if (e.getTagName().toLowerCase().equals("input")
                        && e.toString().matches("pass(word)?")) {
                    row = new SeleniumTableRow(
                            "type",// e.toString(),
                            HTMLLocatorBuilder.getElementIdentifier(e),
                            configurator.getLoginConfiguration()
                                    .get("username"));
                    writeToHistoryFile(row, true);
                    visitedElements.add(HTMLLocatorBuilder
                            .getElementIdentifier(e));
                    e.clear();
                    e.sendKeys(configurator.getLoginConfiguration().get(
                            "username"));
                } else if (configurator.includeChildrenNodesOnInteraction()) {
                    if (e.getTagName().toLowerCase().equals("input")) {
                        switch (e.getAttribute("type")) {
                            case "text":
                            case "password":
                            case "email":
                                row = new SeleniumTableRow(
                                        "list",// e.toString(),
                                        HTMLLocatorBuilder
                                                .getElementIdentifier(e),
                                        "EMPTY");
                                System.out.println("row: " + row.toString());
                                history.add(row);
                                writeToHistoryFile(row, true);
                                break;
                            case "radio":
                            case "checkbox":
                                row = new SeleniumTableRow(
                                        "list",// e.toString(),
                                        HTMLLocatorBuilder
                                                .getElementIdentifier(e),
                                        "EMPTY");
                                System.out.println("row: " + row.toString());
                                history.add(row);
                                writeToHistoryFile(row, true);
                                break;
                        }
                    } else if (e.getTagName().toLowerCase().equals("select")) {
                        List<WebElement> options = element.findElements(By
                                .xpath(".//option"));
                        if (options.size() != 0) {
                            row = new SeleniumTableRow(
                                    "listSelect",// e.toString(),
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            System.out.println("row: " + row.toString());
                            history.add(row);
                            writeToHistoryFile(row, true);
                        }
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
}
