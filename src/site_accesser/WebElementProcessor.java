package site_accesser;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class WebElementProcessor {
    private static WebsiteExplorer we = WebsiteExplorer.getInstance();
    
    public static void processElement(WebElement element){
        System.out.println("ELEMENT: "
                + element.toString()
                + (!element.getText().isEmpty() ? " (has text)"
                        : " (no text)")
                        +" | LOCATOR:"
                + HTMLLocatorBuilder.getElementIdentifier(element));

        if (WebsiteExplorer.isElementRelatedToLogin(element)) {
            boolean processedLogin = processLogin(element);
            if (processedLogin)
                we.goBackToHome();
        }// dropdown list
        else if (element.getTagName().toLowerCase().equals("select")) {
            if (!WebsiteExplorer.getConfigurator().includeChildrenNodesOnInteraction())
                processSelectElement(element);
            else
                includeFormChildrenAndVisitElement(element);
        } else if (WebsiteExplorer.isElementTextInputable(element)) {
            if (!WebsiteExplorer.getConfigurator().includeChildrenNodesOnInteraction())
                processInputElement(element);
            else
                includeFormChildrenAndVisitElement(element);
        } else {
            processAnchorElements(element);
        }
    }
    
    /**
     * Does necessary operations to interact with an anchor element
     * 
     * @param e
     *            element to interact with
     */
    private static void processAnchorElements(WebElement element) {
        // link
        WebsiteExplorer.saveRow("clickAndWait",
                HTMLLocatorBuilder.getElementIdentifier(element), "EMPTY");

        element.click();
        // TODO remove - we.getTesting()
        we.getTesting().incrementHTML(we.getDriver().getPageSource(), we.getDriver().getCurrentUrl());
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
    private static boolean processLogin(WebElement element) {
        if (element.getTagName().toLowerCase().equals("input")
                || element.getTagName().toLowerCase().equals("select")) {
            // login form, fill all form inputs
            WebElement form = findParentForm(element);
            List<WebElement> children = findInputAndSelectChildNodesGivenParentForm(form);
            String username = "username", password = "password";
            if (!WebsiteExplorer.getConfigurator().getLoginConfiguration().isEmpty()) {
                username = WebsiteExplorer.getConfigurator().getLoginConfiguration().get("username");
                password = WebsiteExplorer.getConfigurator().getLoginConfiguration().get("password");
            }
            String content = "";

            for (WebElement e : children) {
                if (e.getTagName().toLowerCase().equals("input")) {
                    switch (e.getAttribute("type")) {
                        case "text":
                        case "password":
                        case "email":
                            if (e.toString().matches(
                                    "user(\\s|_)?(name|id)?|e?mail")) {
                                content = username;
                            } else if (e.toString().matches("pass(word)?")) {
                                content = password;
                            } else {
                                int rand = (int) (Math.random() * (WebsiteExplorer.getConfigurator()
                                        .getTypedKeywords().length - 1));
                                content = WebsiteExplorer.getConfigurator().getTypedKeywords()[rand];
                            }

                            WebsiteExplorer.saveRow("type",
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    content);
                            // TODO remove - we.getTesting()
                            we.getTesting().incrementCorrelation();
                            // interact
                            e.clear();
                            e.sendKeys(content);
                            break;
                        case "radio":
                        case "checkbox":
                        case "button":
                            WebsiteExplorer.saveRow("click",
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            // TODO remove - we.getTesting()
                            we.getTesting().incrementCorrelation();
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
                        WebsiteExplorer.saveRow("select",
                                HTMLLocatorBuilder.getElementIdentifier(e),
                                "label=\"" + options.get(rand1).getText() + '"');
                        // TODO remove - we.getTesting()
                        we.getTesting().incrementCorrelation();
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
    private static void processInputElement(WebElement element) {
        String identifier = HTMLLocatorBuilder.getElementIdentifier(element);

        // if form doesn't submit dynamically, submit
        // form manually
        if (!element.toString().matches(".*onchange=.*submit.*")) {

            // verify if input wants numbers or words
            if (WebsiteExplorer.isInputElementExpectingNumbers(element)) {
                // it wants an integer value
                int rand1 = (int) Math.round(Math.random() * 100) + 1;
                WebsiteExplorer.saveRow("type", identifier, "" + rand1);
                // TODO remove - we.getTesting()
                we.getTesting().incrementCorrelation();

                // interact
                element.clear();
                element.sendKeys(Integer.toString(rand1));
            } else {
                // insert random keyword
                int rand1 = (int) Math.round(Math.random()
                        * (WebsiteExplorer.getConfigurator().getTypedKeywords().length - 1));

                WebsiteExplorer.saveRow("type", identifier, WebsiteExplorer.getConfigurator()
                        .getTypedKeywords()[rand1]);
                // TODO remove - we.getTesting()
                we.getTesting().incrementCorrelation();

                // interact
                element.clear();
                element.sendKeys(WebsiteExplorer.getConfigurator().getTypedKeywords()[rand1]);
            }

            handleFormSubmission(element);
        } else {
            // add 'andWait' suffix to mark page change

            if (WebsiteExplorer.isInputElementExpectingNumbers(element)) {
                // it wants an integer value
                int rand1 = (int) Math.round(Math.random() * 100) + 1;
                WebsiteExplorer.saveRow("typeAndWait", identifier, "" + rand1);
                // TODO remove - we.getTesting()
                we.getTesting().incrementCorrelation();

                element.clear();
                element.sendKeys(Integer.toString(rand1));
            } else {
                // insert random keyword
                int rand1 = (int) Math.round(Math.random()
                        * (WebsiteExplorer.getConfigurator().getTypedKeywords().length - 1));

                WebsiteExplorer.saveRow("typeAndWait", identifier, WebsiteExplorer.getConfigurator()
                        .getTypedKeywords()[rand1]);
                // TODO remove - we.getTesting()

                // interact
                element.clear();
                element.sendKeys(WebsiteExplorer.getConfigurator().getTypedKeywords()[rand1]);

                // TODO
                we.getTesting().incrementHTML(we.getDriver().getPageSource(),
                        we.getDriver().getCurrentUrl());
            }
        }
    }

    /**
     * Submits a previously visited form.
     * 
     * @param element
     *            element inside the form to be visited.
     */
    private static void handleFormSubmission(WebElement element) {
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
                                ".*" + WebsiteExplorer.getConfigurator().getGeneralWordsToExclude()
                                        + ".*")) {
                    System.out.println("SUBMIT: " + e.toString());

                    submit.add(e);
                }
        }
        if (submit.size() > 0) {

            if (submit.size() > 0) {
                System.out.println("SUBMIT=len:" + submit.size());
                WebsiteExplorer.saveRow("clickAndWait",
                        HTMLLocatorBuilder.getElementIdentifier(submit.get(0)),
                        "EMPTY");
                // interact
                submit.get(0).click();
                // TODO remove - we.getTesting()
                we.getTesting().incrementHTML(we.getDriver().getPageSource(),
                        we.getDriver().getCurrentUrl());
            } else {
                we.handleError("THERE ARE NO SUBMITS",element);
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
                                            + WebsiteExplorer.getConfigurator()
                                                    .getGeneralWordsToExclude()
                                            + ".*")) {
                        System.out.println("DYN_SUBMIT: " + sub.toString());
                        dynamicSubmits.add(sub);
                    }
                }
            }
            if (dynamicSubmits.size() > 0) {
                System.out.println("DYN_SUBMIT=len:" + submit.size());
                WebsiteExplorer.saveRow("clickAndWait",
                        HTMLLocatorBuilder.getElementIdentifier(dynamicSubmits
                                .get(0)), "EMPTY");

                // interact
                dynamicSubmits.get(0).click();
                // TODO remove - we.getTesting()
                we.getTesting().incrementHTML(we.getDriver().getPageSource(),
                        we.getDriver().getCurrentUrl());
            } else {
                we.handleError("THERE ARE NO DYNAMIC SUBMITS", element);
            }
        }

    }

    /**
     * Does necessary operations to interact with a dropdown menu element
     * 
     * @param element
     *            to interact with
     */
    private static void processSelectElement(WebElement element) {
        String identifier = HTMLLocatorBuilder.getElementIdentifier(element);
        Select select = new Select(element);

        // select random option
        List<WebElement> options = element.findElements(By.xpath(".//option"));
        if (options.size() == 0) {
            we.handleError("SELECT HAS NO OPTIONS", element);
        } else {
            System.out.println("options:" + options.size());
            int rand1 = (int) Math.round(Math.random() * (options.size() - 1));
            System.out.println("OPTION TEXT:" + options.get(rand1).getText());

            if (options.get(rand1).getText().isEmpty()) {
                we.handleError("RANDOM OPTION HAS NO TEXT", element);
            } else {
                // it's a valid option
                select.selectByIndex(rand1);

                if (!element.toString().matches(".*onchange=\".*submit\".*")) {
                    // if form doesn't submit dynamically, submit
                    // form manually
                    WebsiteExplorer.saveRow("select", identifier,
                            "label=\"" + options.get(rand1).getText() + '"');
                    // TODO remove - we.getTesting()
                    we.getTesting().incrementCorrelation();

                    handleFormSubmission(element);
                } else {
                    // add 'andWait' suffix to mark page change
                    WebsiteExplorer.saveRow("selectAndWait",// element.toString(),
                            identifier, "label=\""
                                    + options.get(rand1).getText() + '"');
                    // TODO remove - we.getTesting()
                    we.getTesting().incrementHTML(we.getDriver().getPageSource(),
                            we.getDriver().getCurrentUrl());
                }
            }
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
    private static void includeFormChildrenAndVisitElement(WebElement element) {
        WebElement form = findParentForm(element);
        List<WebElement> children = findInputAndSelectChildNodesGivenParentForm(form);

        for (WebElement e : children) {
            if (!e.toString().equals(element.toString())) {
                if (e.getTagName().equals("input")) {
                    switch (e.getAttribute("type")) {
                        case "text":
                        case "password":
                        case "email":
                            WebsiteExplorer.saveRow("SIBLING:","type",// e.toString(),
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            // TODO remove - we.getTesting()
                            we.getTesting().incrementCorrelation();
                            break;
                        case "radio":
                        case "checkbox":
                        case "button":
                            WebsiteExplorer.saveRow("SIBLING:","click",
                                    HTMLLocatorBuilder.getElementIdentifier(e),
                                    "EMPTY");
                            // TODO remove - we.getTesting()
                            we.getTesting().incrementCorrelation();
                            break;
                    }
                } else if (e.getTagName().toLowerCase().equals("select")) {
                    List<WebElement> options = element.findElements(By
                            .xpath(".//option"));
                    if (options.size() != 0) {
                        WebsiteExplorer.saveRow("SIBLING:","select",// e.toString(),
                                HTMLLocatorBuilder.getElementIdentifier(e),
                                "EMPTY");
                        // TODO remove - we.getTesting()
                        we.getTesting().incrementCorrelation();
                    }
                }
            }
        }

        // process element
        if (WebsiteExplorer.isElementTextInputable(element))
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
    private static List<WebElement> findInputAndSelectChildNodesGivenParentForm(
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
    public static List<WebElement> findChildrenAnchorNodesGivenParent(
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
     * Given a WebElement, finds the parent form
     * 
     * @param element
     *            child element node
     * @return parent form
     */
    private static WebElement findParentForm(WebElement element) {
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
