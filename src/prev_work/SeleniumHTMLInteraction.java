package prev_work;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import data_gen.ExtendedSeleniumIDEElement;


public class SeleniumHTMLInteraction {

    public static int NUMBER_OF_TEXT_OCURRENCES_IN_HTML_TO_BE_CONSIDERED_A_SEARCH = 5;

    // Get the actions table from the selenium html execution trace
    public static ArrayList<ExtendedSeleniumIDEElement> parseTableFromSeleniumHTML()
            throws IOException {

        ArrayList<ExtendedSeleniumIDEElement> actionSequence = new ArrayList<ExtendedSeleniumIDEElement>();

        File input = new File(System.getProperty("user.dir") + "\\HTMLfinal"
                + "\\" + "selenium" + ".html");
        Document doc = Jsoup.parse(input, "UTF-8");

        // gets the table element
        Element table = doc.select("table").first();

        // finds the tr element
        Iterator<Element> ite = table.select("tr").iterator();
        ite.next();

        // iterates through all the tr elements
        do {
            Element currentTableLine = ite.next();
            Iterator<Element> component = currentTableLine.select("td")
                    .iterator();

            // Get the information from the <td>'s
            String action = component.next().text();
            String link = component.next().text();
            String parameter = component.next().text();

            // adds to ActionSequence all the information from the current
            // execution trace step
            actionSequence.add(new ExtendedSeleniumIDEElement(action, link, parameter));

        } while (ite.hasNext());

        return actionSequence;

    }
    
    // this function is only called if there is a possibility of a search
    // pattern being found
    // will tell the user, with what text boxes/check boxes the search was made
    // compare current url with previous (or current)html
    public static void findURLSearchVariablesInHTML(String url, int pageWanted)
            throws IOException {
        // split the url, only the part after the last '/' will be considered
        String[] urlparts = url.split("/");
        String urlToQuery = urlparts[urlparts.length - 1];
        // parameters start with the character '?'
        String parameters = urlToQuery.split("\\?")[urlToQuery.split("\\?").length - 1];
        // and are divided with the character '&'
        String[] parametersSeparated = parameters.split("&");

        // opens the html to search for the parameters in it
        int pageWhereSearchWasQueried = pageWanted - 1;
        File input = new File(System.getProperty("user.dir") + "\\HTMLtemp"
                + "\\" + pageWhereSearchWasQueried + ".txt");
        Document doc = Jsoup.parse(input, "UTF-8");

        // stores the search parameters, so it will not print duplicates
        ArrayList<String> searchParameters = new ArrayList<String>();

        // gets all the inputs
        Elements typea = doc.select("input");
        for (int i = 0; i != typea.size(); i++) {
            for (int j = 0; j != parametersSeparated.length; j++) {
                // System.out.println(parametersSeparated[j]);
                if (typea.get(i).attr("name")
                        .equals(parametersSeparated[j].split("=")[0])) {
                    String sentence = "FOUND SEARCH WITH TYPE "
                            + typea.get(i).attr("type") + ", CATEGORY "
                            + typea.get(i).attr("name") + " AND OPTION "
                            + parametersSeparated[j].split("=")[1];
                    if (!searchParameters.contains(sentence)) {
                        searchParameters.add(sentence);
                        Filesystem.saveToFile("final",
                                Integer.toString(pageWanted) + "extraInfo",
                                sentence + "\n", true);
                        System.out.println(sentence);
                    }

                }
            }

        }

        // gets all the selects
        Elements types = doc.select("select");
        for (int i = 0; i != types.size(); i++) {
            for (int j = 0; j != parametersSeparated.length; j++) {
                // System.out.println(parametersSeparated[j]);
                if (types.get(i).attr("name")
                        .equals(parametersSeparated[j].split("=")[0])) {
                    String sentence = "FOUND SEARCH WITH TYPE " + "select"
                            + ", CATEGORY " + types.get(i).attr("name")
                            + " AND OPTION "
                            + parametersSeparated[j].split("=")[1];
                    if (!searchParameters.contains(sentence)) {
                        searchParameters.add(sentence);
                        Filesystem.saveToFile("final",
                                Integer.toString(pageWanted) + "extraInfo",
                                sentence + "\n", true);
                        System.out.println(sentence);
                    }
                }
            }

        }

    }

    /*
     * public static void main(String[] args) throws IOException {
     * 
     * 
     * 
     * }
     */

    public static void testForPatterns(ArrayList<ExtendedSeleniumIDEElement> actions,
            ArrayList<TypeActionHandlers> lastTypeIndexes,
            int indexOfCurrentElement, ArrayList<PageInfo> pageInfo)
            throws IOException {

        String searchTextBox = testForSearch(actions, lastTypeIndexes,
                indexOfCurrentElement, pageInfo);

        // if a search was found, the search text box has to be removed from the
        // lastTypeIndexes
        if (!searchTextBox.equals("")) {
            for (int i = 0; i != lastTypeIndexes.size(); i++) {
                if (lastTypeIndexes.get(i).getTextBoxId().equals(searchTextBox)) {
                    lastTypeIndexes.remove(i);
                    break;
                }
            }
        }

        ArrayList<String> loginPassTextBoxes = new ArrayList<String>();
        if (lastTypeIndexes.size() >= 2)
            loginPassTextBoxes = testForLogin(actions, lastTypeIndexes,
                    indexOfCurrentElement, pageInfo);

        // if a login pattern was found, the login/pass text boxes have to be
        // removed from the lastTypeIndexes
        if (loginPassTextBoxes.size() != 0) {
            for (int j = 0; j != loginPassTextBoxes.size(); j++) {
                for (int i = 0; i != lastTypeIndexes.size(); i++) {
                    if (lastTypeIndexes.get(i).getTextBoxId()
                            .equals(loginPassTextBoxes.get(j))) {
                        lastTypeIndexes.remove(i);
                        break;
                    }
                }
            }
        }

        if (lastTypeIndexes.size() > 0)
            testForInputs(actions, lastTypeIndexes, indexOfCurrentElement,
                    pageInfo);

    }

    // test for input patterns
    public static void testForInputs(ArrayList<ExtendedSeleniumIDEElement> actions,
            ArrayList<TypeActionHandlers> lastTypeIndexes,
            int indexOfCurrentElement, ArrayList<PageInfo> pageInfo) {
        int pageWanted = 0;
        // cycle through the pageInfo array to get the URL for the page which is
        // right after the click and wait action
        for (int i = 0; i != pageInfo.size(); i++) {

            // plus one because it is the URL after the click and wait action
            if (pageInfo.get(i).getSeleniumStepCorrespondent() == indexOfCurrentElement) {
                pageWanted = i + 1;
            }

        }

        for (int i = 0; i != lastTypeIndexes.size(); i++) {
            int typeIndex = lastTypeIndexes.get(i).getIndex() - 1; // minus 1 to
                                                                    // convert
                                                                    // from
                                                                    // Selenium
                                                                    // Step to
                                                                    // array
                                                                    // actions
                                                                    // (which
                                                                    // starts in
                                                                    // 0 instead
                                                                    // of 1)
            System.out.println("Found input pattern");

            // registo para padrao INPUT
            PatternWeightCalculator inputCheck = new PatternWeightCalculator(
                    "Input", 1.0);
            inputCheck.checkPattern();

            Filesystem.saveToFile("final", Integer.toString(pageWanted)
                    + "extraInfo", "INPUT-PATTERN-TEXTBOX:"
                    + lastTypeIndexes.get(i).getTextBoxId() + "\n", true);
            //XXX
            actions.get(pageWanted).setINPUT_PATTERN_TEXTBOX(lastTypeIndexes.get(i).getTextBoxId());
            Filesystem.saveToFile("final", Integer.toString(pageWanted)
                    + "extraInfo",
                    "INPUT-PATTERN-TEXT:"
                            + actions.get(typeIndex).getParameter() + "\n",
                    true);
            //XXX
            actions.get(pageWanted).setINPUT_PATTERN_TEXT(lastTypeIndexes.get(i).getTextBoxId());

        }

    }

    public static String testForSearch(ArrayList<ExtendedSeleniumIDEElement> actions,
            ArrayList<TypeActionHandlers> lastTypeIndexes,
            int indexOfCurrentElement, ArrayList<PageInfo> pageInfo)
            throws IOException {

        // registo para padrao Search
        PatternWeightCalculator searchCheck = new PatternWeightCalculator(
                "Search", 0.0);

        // Task: verify if next page has the parameter of one of the types in
        // next page url

        // 1: Fetch the previous action "types"
        ArrayList<String> previousTypes = new ArrayList<String>();

        String pageUrl = "";
        int pageWanted = 0;
        // cycle through the pageInfo array to get the URL for the page which is
        // right after the click and wait action
        for (int i = 0; i != pageInfo.size(); i++) {

            // plus one because it is the URL after the click and wait action
            if (pageInfo.get(i).getSeleniumStepCorrespondent() == indexOfCurrentElement) {
                pageUrl = pageInfo.get(i).getPageURL();
                pageWanted = i + 1;
            }

        }

        // cycle through the lastTypeIndexes and Store the Strings in the
        // PreviousTypes array
        for (int i = 0; i != lastTypeIndexes.size(); i++) {
            int typeIndex = lastTypeIndexes.get(i).getIndex() - 1; 
            // minus 1 to convert from Selenium Step to array actions
            // (which starts in 0 instead of 1)
            previousTypes.add(actions.get(typeIndex).getParameter());

            // verifies if there is one textbox which contains the keyword search
            // If it does, good chance of a "Search" pattern
            if (StringUtils.containsIgnoreCase(lastTypeIndexes.get(i)
                    .getTextBoxId(), "search")) {
                System.out.println("Possible search text box here");
                // saves this information into the correspondent pageExtraInfo
                Filesystem.saveToFile("final", Integer.toString(pageWanted)
                        + "extraInfo", "POSSIBLE-SEARCH-TEXT-BOX:"
                        + lastTypeIndexes.get(i).getTextBoxId() + "\n", true);
                //XXX
                actions.get(pageWanted).setPOSSIBLE_SEARCH_TEXT_BOX(lastTypeIndexes.get(i).getTextBoxId());
                searchCheck.addToWeight(0.8);
            }
        }

        // cycle through the previousTypes array and try to find one of the
        // Strings in the String pageURL
        // if it does, there is a big change that this page contains a search
        String textWanted = "";
        for (String s : previousTypes) {
            // since in the url there can be no spaces, we have to split the
            // search keywords and search for
            // them individually. For example, when looking for "daft punk", in
            // the url it can appear "daft+punk"
            // or "daft_punk", etc. We have to search for "daft" and "punk"
            String[] splitted = s.split(" ");
            // procedure for only one word
            if (splitted.length == 1) {
                if (StringUtils.containsIgnoreCase(pageUrl, s)) // pageUrl.contains(s)
                {
                    System.out.println("Possible search here with " + s
                            + " keyword");
                    // saves this information into the correspondent
                    // pageExtraInfo
                    Filesystem.saveToFile("final", Integer.toString(pageWanted)
                            + "extraInfo", "POSSIBLE-SEARCH-KEYWORD-IN-URL:"
                            + s + "\n", true);
                    //XXX
                    actions.get(pageWanted).setPOSSIBLE_SEARCH_KEYWORD_IN_URL(s);
                    searchCheck.addToWeight(0.6);
                    textWanted = s;
                    break;
                }
            } else // procedure for more than 1 word
            {
                int count = 0;
                for (int i = 0; i != splitted.length; i++) {
                    if (StringUtils.containsIgnoreCase(pageUrl, splitted[i]))
                        count++;
                }
                if (count == splitted.length) {
                    System.out.println("Possible search here with " + s
                            + " keyword");
                    // saves this information into the correspondent
                    // pageExtraInfo
                    Filesystem.saveToFile("final", Integer.toString(pageWanted)
                            + "extraInfo", "POSSIBLE-SEARCH-KEYWORD-IN-URL:"
                            + s + "\n", true);
                    //XXX
                    actions.get(pageWanted).setPOSSIBLE_SEARCH_KEYWORD_IN_URL(s);
                    searchCheck.addToWeight(0.6);
                    textWanted = s;
                    break;
                }
            }
        }

        // if the url contains the world "search", possible search pattern
        // exists
        if (StringUtils.containsIgnoreCase(pageUrl, "search")) {
            System.out
                    .println("Possible search here with \"search\" in the url");
            // saves this information into the correspondent pageExtraInfo
            Filesystem.saveToFile("final", Integer.toString(pageWanted)
                    + "extraInfo", "SEARCH-WORD-IN-URL\n", true);
            //XXX
            actions.get(pageWanted).setSEARCH_WORD_IN_URL("T");
            searchCheck.addToWeight(0.7);
        }

        // sees the pageWanted HTML for the keyword. If it appears in sufficient
        // number, big chance of search
        if (textWanted != "") {
            File input = new File(System.getProperty("user.dir") + "\\HTMLtemp"
                    + "\\" + pageWanted + ".txt");
            try {
                Document doc = Jsoup.parse(input, "UTF-8");
                String text = doc.text();
                // also consider String text = doc.body().text();
                // System.out.println("HERE COMES HTML: " + text);
                int count = countNumberOfOcurrencesInAString(text, textWanted);
                if (count > NUMBER_OF_TEXT_OCURRENCES_IN_HTML_TO_BE_CONSIDERED_A_SEARCH) {
                    Filesystem.saveToFile("final", Integer.toString(pageWanted)
                            + "extraInfo", "NUMBER-OF-KEYWORD-IN-HTML:" + count
                            + "\n", true);
                    //XXX
                    actions.get(pageWanted).setNUMBER_OF_KEYWORD_IN_HTML(Integer.toString(count));
                    searchCheck.addToWeight(0.8);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(12);
            }
        }

        String searchTextBox = "";
        // this is done to remove the searchTextBox for the next patterns to be found
        // Ex: when searching for login pattern, this text box will not be considered
        for (int i = 0; i != lastTypeIndexes.size(); i++) {
            int typeIndex = lastTypeIndexes.get(i).getIndex() - 1; 
            // minus 1 to convert from Selenium Step to array actions 
            //(which starts in 0 instead of 1)
            if (actions.get(typeIndex).getParameter().equals(textWanted)) {
                searchTextBox = lastTypeIndexes.get(i).getTextBoxId();
                break;
            }
        }

        // print the search information
        if (searchTextBox.equals(""))
            findURLSearchVariablesInHTML(pageUrl, pageWanted);

        searchCheck.checkPattern();

        return searchTextBox;

    }

    public static ArrayList<String> testForLogin(
            ArrayList<ExtendedSeleniumIDEElement> actions,
            ArrayList<TypeActionHandlers> lastTypeIndexes,
            int indexOfCurrentElement, ArrayList<PageInfo> pageInfo) {

        // registo para padrao Login
        PatternWeightCalculator loginCheck = new PatternWeightCalculator(
                "Login", 0.0);

        // array to save the login pass text boxes so that they can be removed
        // in finding new patterns
        ArrayList<String> loginPassTextBoxes = new ArrayList<String>();

        // 1: fecth the previous action "types"
        ArrayList<String> previousTypes = new ArrayList<String>();

        // cycle through the lastTypeIndexes and Store the Strings in the
        // PreviousTypes array

        Boolean passAsTextBoxID = false;
        for (int i = 0; i != lastTypeIndexes.size(); i++) {
            // Verify the id of the text boxes - if one of them has "pass" it is
            // possible to be a login pattern
            if (StringUtils.containsIgnoreCase(lastTypeIndexes.get(i)
                    .getTextBoxId(), "pass")) {
                passAsTextBoxID = true;
                // array to save the login pass text boxes so that they can be
                // removed in finding new patterns
                loginPassTextBoxes.add(lastTypeIndexes.get(i).getTextBoxId());
            }

            // Verify the id of the text boxes - if one of them has "login" or
            // "user" it is possible to be a login pattern
            if (StringUtils.containsIgnoreCase(lastTypeIndexes.get(i)
                    .getTextBoxId(), "user")
                    || StringUtils.containsIgnoreCase(lastTypeIndexes.get(i)
                            .getTextBoxId(), "login")) {
                loginCheck.addToWeight(0.8);
                // array to save the login pass text boxes so that they can be
                // removed in finding new patterns
                loginPassTextBoxes.add(lastTypeIndexes.get(i).getTextBoxId());
            }

            int typeIndex = lastTypeIndexes.get(i).getIndex() - 1; 
            // minus 1 to convert from Selenium Step to array actions (which
            // starts in 0 instead of 1)
            previousTypes.add(actions.get(typeIndex).getParameter());
        }

        String pageUrl = "";
        int pageWanted = 0;
        // cycle through the pageInfo array to get the URL for the page which is
        // right after the click and wait action
        for (int i = 0; i != pageInfo.size(); i++) {

            // plus one because it is the URL after the click and wait action
            if (pageInfo.get(i).getSeleniumStepCorrespondent() == indexOfCurrentElement) {
                pageUrl = pageInfo.get(i).getPageURL();
                pageWanted = i + 1;
            }

        }

        // if the url contains the world "login", possible login pattern exists
        if (StringUtils.containsIgnoreCase(pageUrl, "login")
                || StringUtils.containsIgnoreCase(pageUrl, "logon")) {
            System.out.println("Possible Login here with \"login\" in the url");
            // saves this information into the correspondent pageExtraInfo
            Filesystem.saveToFile("final", Integer.toString(pageWanted)
                    + "extraInfo", "LOGIN-WORD-IN-URL\n", true);
            //XXX
            actions.get(pageWanted).setLOGIN_WORD_IN_URL("T");
            loginCheck.addToWeight(0.8);
        }

        // saves the information regarding the existence of a text box with
        // "pass" in its id
        if (passAsTextBoxID) {
            Filesystem.saveToFile("final", Integer.toString(pageWanted)
                    + "extraInfo", "POSSIBLE-LOGIN-PASS-TEXT-BOX" + "\n", true);
            //XXX
            actions.get(pageWanted).setPOSSIBLE_LOGIN_PASS_TEX_BOX("T");
            loginCheck.addToWeight(0.8);
        }

        // count the number of ocurrences of the values of the textboxes in
        // HTML, can be also a good indicator
        for (String s : previousTypes) {
            File input = new File(System.getProperty("user.dir") + "\\HTMLtemp"
                    + "\\" + pageWanted + ".txt");
            try {
                Document doc = Jsoup.parse(input, "UTF-8");
                String text = doc.text();
                // also consider String text = doc.body().text();
                // System.out.println("HERE COMES HTML: " + text);
                int count = countNumberOfOcurrencesInAString(text, s);
                if (count > 0) {
                    // this is done to remove the loginText for the next
                    // patterns to be found
                    // Ex: when searching for input pattern, this text box will
                    // not be considered
                    for (int i = 0; i != lastTypeIndexes.size(); i++) {
                        int typeIndex = lastTypeIndexes.get(i).getIndex() - 1; 
                        // minus
                                                                                // 1
                                                                                // to
                                                                                // convert
                                                                                // from
                                                                                // Selenium
                                                                                // Step
                                                                                // to
                                                                                // array
                                                                                // actions
                                                                                // (which
                                                                                // starts
                                                                                // in
                                                                                // 0
                                                                                // instead
                                                                                // of
                                                                                // 1)
                        if (actions.get(typeIndex).getParameter().equals(s)) {
                            // array to save the login pass text boxes so that
                            // they can be removed in finding new patterns
                            loginPassTextBoxes.add(lastTypeIndexes.get(i)
                                    .getTextBoxId());
                        }
                    }
                    Filesystem.saveToFile("final", Integer.toString(pageWanted)
                            + "extraInfo", "POSSIBLE-LOGIN-WITH-:" + count
                            + "-OCURRENCES-OF:" + s + "\n", true);
                    loginCheck.addToWeight(0.4);

                    loginCheck.checkPattern();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(13);
            }
        }

        return loginPassTextBoxes;

    }

    public static int countNumberOfOcurrencesInAString(String allText,
            String textToBeFound) {
        // Pattern.compile(Pattern.quote(strptrn),
        // Pattern.CASE_INSENSITIVE).matcher(str1).find();
        Pattern p = Pattern.compile(Pattern.quote(textToBeFound),
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(allText);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
