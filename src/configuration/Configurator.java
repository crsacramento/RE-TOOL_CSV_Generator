package configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import processor.LogProcessor;
import processor.PatternMapEntry;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Configurator {
    /** singleton instance */
    private static Configurator instance = null;

    /**
     * @return the instance
     */
    public static Configurator getInstance() {
        if (instance == null)
            instance = new Configurator();
        return instance;
    }

    private static String filepath = null;

    public Configurator() {
        // loadConfig();
    }

    public static void initialize(String f) {
        filepath = f;
    }

    /** number of actions the crawler will execute before stopping */
    private int numActions = 30;
    /** number of redirects to the home page the crawler will do before stopping */
    private int numRedirects = 5;
    /** time to wait (in milliseconds) after each action */
    private int politenessDelay = 500;
    /** words to insert in text input elements */
    private String[] typedKeywords = { "curtains", "coffee", "phone", "shirt",
            "computer", "dress", "banana", "sandals" };
    /** list of patterns to restrict which patterns to search for */
    private String[] patternsToSearch = { "all" };
    private boolean includeChildrenNodesOnInteraction = false;

    /** keywords that identify search elements */
    private String searchKeywords = "(\\sq\\s|query|qry|search|"
            + "pesq(uisa)?|procura(r)?|busca(dor)?)";
    /** keywords that identify sort elements */
    private String sortKeywords = "(sort|asc\\s|desc\\s)";
    /** keywords that identify login elements */
    private String loginKeywords = "(user(name)?|pass(word)?|"
            + "e?mail|(sign(ed)?(\\s|_)?(in|out)|log(ged)?(\\s|_)?(in|out)))";
    /** keywords that identify elements that SHOULD NOT BE ACCESSED */
    private String generalWordsToExclude = "(buy|sell|edit|"
            + "delete|mailto|add(\\s|_)?to(\\s|_)?cart|checkout)";
    /** keywords that identify menu elements */
    private String[] menuIdentifiers = { "nav", "head", "menu", "top", "head",
            "foot" };
    /**
     * keywords that identify master elements (from MasterDetail) in a search
     * result page
     */
    private String[] masterIdentifiers = { "refine", "relatedsearches", "spell" };
    /**
     * keywords that identify detail elements (from MasterDetail) in a search
     * result page
     */
    private String[] detailIdentifiers = { "results", "entry", "searchResults" };

    /** history column separator */
    private String separator = "\t";

    /** path to patterns file */
    private String historyFilepath = System.getProperty("user.dir")
            + File.separatorChar + "history.csv";
    /** path to processed history file */
    private String processedHistoryFilepath = System.getProperty("user.dir")
            + File.separatorChar + "history.csv.processed";
    /** path to patterns file */
    private String patternsFilepath = System.getProperty("user.dir")
            + File.separatorChar + "patterns.paradigm";

    /** path to patterns file */
    private HashMap<String, String> loginConfiguration = new HashMap<String, String>();

    /**
     * @return the numActions
     */
    public int getNumActions() {
        return numActions;
    }

    /**
     * @return the patternsToSearch
     */
    public String[] getPatternsToSearch() {
        return patternsToSearch;
    }

    /**
     * @return the numRedirects
     */
    public int getNumRedirects() {
        return numRedirects;
    }

    /**
     * @return the politenessDelay
     */
    public int getPolitenessDelay() {
        return politenessDelay;
    }

    /**
     * @return the includeChildrenNodesOnInteraction
     */
    public boolean includeChildrenNodesOnInteraction() {
        return includeChildrenNodesOnInteraction;
    }

    /**
     * @return the typedKeywords
     */
    public String[] getTypedKeywords() {
        return typedKeywords;
    }

    /**
     * @return the searchKeywords
     */
    public String getSearchKeywords() {
        return searchKeywords;
    }

    /**
     * @return the sortKeywords
     */
    public String getSortKeywords() {
        return sortKeywords;
    }

    /**
     * @return the loginKeywords
     */
    public String getLoginKeywords() {
        return loginKeywords;
    }

    /**
     * @return the generalWordsToExclude
     */
    public String getGeneralWordsToExclude() {
        return generalWordsToExclude;
    }

    /**
     * @return the menuIdentifiers
     */
    public String[] getMenuIdentifiers() {
        return menuIdentifiers;
    }

    /**
     * @return the masterIdentifiers
     */
    public String[] getMasterIdentifiers() {
        return masterIdentifiers;
    }

    /**
     * @return the detailIdentifiers
     */
    public String[] getDetailIdentifiers() {
        return detailIdentifiers;
    }

    /**
     * @return the separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @return the historyFilepath
     */
    public String getHistoryFilepath() {
        return historyFilepath;
    }

    /**
     * @return the processedHistoryFilepath
     */
    public String getProcessedHistoryFilepath() {
        return processedHistoryFilepath;
    }

    /**
     * @return the patternsFilepath
     */
    public String getPatternsFilepath() {
        return patternsFilepath;
    }

    /**
     * @return the loginConfiguration
     */
    public HashMap<String, String> getLoginConfiguration() {
        return loginConfiguration;
    }

    private final String[] TAGNAMES = { "actions", "redirections",
            "includeChildrenNodesOnInteraction", "typedKeywords",
            "searchKeywords", "sortKeywords", "loginKeywords",
            "generalWordsToExclude", "menuIdentifiers", "masterIdentifiers",
            "detailIdentifiers", "historyFilepath", "processedHistoryFilepath",
            "patternsFilepath", "patternsToFind", "loginConfiguration",
            "tokenizerPatterns" };

    public void loadConfig() {
        File userOverride;
        if (filepath == null)
            userOverride = new File("conf.xml");
        else
            userOverride = new File(filepath + "conf.xml");

        if (!userOverride.exists()) {
            try {
                // userOverride.createNewFile();
                CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING };
                Files.copy(getClass()
                        .getResourceAsStream("/resources/conf.xml"),
                        userOverride.toPath(), options);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        Document doc = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(userOverride);
            // Do something with the document here.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // normalize text representation
        doc.getDocumentElement().normalize();

        for (String s : TAGNAMES) {
            NodeList tag = doc.getElementsByTagName(s);
            if (tag.getLength() == 0)
                continue;
            else if (tag.getLength() > 1) {
                System.err.println("More than one tag with name " + s
                        + " present, using first one");
            }
            boolean processed = processSimpleTag(tag.item(0).getNodeName(), tag
                    .item(0).getFirstChild().getNodeValue());
            if (!processed) {
                if (s.equals("typedKeywords") || s.equals("menuIdentifiers")
                        || s.equals("masterIdentifiers")
                        || s.equals("detailIdentifiers")
                        || s.equals("patternsToFind")) {
                    processItemListTag(tag);
                } else if (s.equals("tokenizerPatterns")) {
                    processTokenizerPatternList(tag);
                } else if (s.equals("loginConfiguration")) {
                    NodeList children = tag.item(0).getChildNodes();
                    String username = null, password = null;
                    for (int i = 0; i < children.getLength(); i++) {

                        Node firstPersonNode = children.item(i);
                        if (firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (firstPersonNode.getNodeName()
                                    .equals("username")) {
                                username = firstPersonNode.getFirstChild()
                                        .getNodeValue();
                            } else if (firstPersonNode.getNodeName().equals(
                                    "password")) {
                                password = firstPersonNode.getFirstChild()
                                        .getNodeValue();
                            }
                        }
                    }
                    if (!(username == null || password == null)) {
                        loginConfiguration.put("username", username);
                        loginConfiguration.put("password", password);
                    } else {
                        System.err
                                .println("In a login configuration there must exist a username and a password node");
                    }
                }
            }
        }
    }

    private void processTokenizerPatternList(NodeList tag) {
        NodeList children = tag.item(0).getChildNodes();
        if (children.getLength() == 0) {
            System.err.println("tag " + tag.item(0).getNodeName()
                    + " must have at least one child");
            return;
        }

        boolean b = false;
        ArrayList<PatternMapEntry> array = new ArrayList<PatternMapEntry>();
        String regex = null, name = null;

        for (int i = 0; i < children.getLength(); ++i) {
            Node n = children.item(i);
            if (n.getNodeName().equals("patternEntry")) {

                Element patternEntryElement = (Element) n;
                NodeList nameList = patternEntryElement
                        .getElementsByTagName("name");
                Element firstNameElement = (Element) nameList.item(0);
                NodeList textFNList = firstNameElement.getChildNodes();
                name = ((Node) textFNList.item(0)).getNodeValue().trim();

                NodeList regexList = patternEntryElement
                        .getElementsByTagName("regex");
                Element firstRegexElement = (Element) regexList.item(0);
                NodeList regexFNList = firstRegexElement.getChildNodes();
                regex = ((Node) regexFNList.item(0)).getNodeValue().trim();

                if (name == null || regex == null) {
                    System.err.println("No name or regex, invalid");
                } else {
                    array.add(new PatternMapEntry(name, regex));
                    b = true;
                }
            }
        }
        if (b) {
            LogProcessor.setPatterns(array);
        }
    }

    private void processItemListTag(NodeList tag) {
        NodeList children = tag.item(0).getChildNodes();
        if (children.getLength() == 0) {
            System.err.println("tag " + tag.item(0).getNodeName()
                    + " must have at least one child");
            return;
        }

        boolean b = false;
        ArrayList<String> arr = new ArrayList<String>();
        for (int i = 0; i < children.getLength(); ++i) {
            Node n = children.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                NodeList itemList = e.getChildNodes();
                Node textList = itemList.item(0);
                String text = textList.getNodeValue().trim();
                b = true;

                arr.add(text);
            }
        }
        String[] array = new String[arr.size()];
        for (int i = 0; i < arr.size(); ++i)
            array[i] = arr.get(i);

        if (b) {
            String s = tag.item(0).getNodeName();
            if (s.equals("typedKeywords")) {
                typedKeywords = array;
            } else if (s.equals("menuIdentifiers")) {
                menuIdentifiers = array;
            } else if (s.equals("masterIdentifiers")) {
                masterIdentifiers = array;
            } else if (s.equals("detailIdentifiers")) {
                detailIdentifiers = array;
            } else if (s.equals("patternsToFind")) {
                // patternsToSearch = array;
                ArrayList<String> arr2 = new ArrayList<String>();
                boolean b1 = false;
                for (String str : arr) {
                    if (str.toLowerCase().equals("all")) {
                        b1 = true;
                        break;
                    } else if (str.toLowerCase().matches(
                            "search|sort|masterdetail|call|input|login|menu")) {
                        arr2.add(str);
                    }
                }
                if (!b1 && arr2.size() > 0) {
                    patternsToSearch = new String[arr2.size()];
                    for (int i = 0; i < arr2.size(); ++i)
                        patternsToSearch[i] = arr2.get(i);
                } else {
                    patternsToSearch = new String[] { "all" };
                }

            }
        }
    }

    /**
     * Processes tag according to its name and content
     * 
     * @param name
     *            tag name
     * @param content
     *            tag value
     */
    private boolean processSimpleTag(String name, String content) {
        if (name.equals("includeChildrenNodesOnInteraction")) {
            if (content.matches("t(rue)?|1")) {
                includeChildrenNodesOnInteraction = true;
            } else if (content.matches("f(alse)?|0"))
                includeChildrenNodesOnInteraction = true;
            else
                System.err.println("Invalid content on tag " + name
                        + ", must have boolean value");
            return true;
        } else if (name.equals("actions") || name.equals("redirections")
                || name.equals("politenessDelay")) {
            int parse = -1;
            try {
                parse = Integer.parseInt(content);
            } catch (NumberFormatException e) {
                System.err.println("invalid content for tag " + name
                        + ": should contain only numbers\nusing default value");
                e.printStackTrace();
                return false;
            }

            if (name.equals("actions"))
                numActions = parse;
            else if (name.equals("redirections"))
                numRedirects = parse;
            else
                politenessDelay= parse;
            return true;
        } else if (name.equals("searchKeywords") || name.equals("sortKeywords")
                || name.equals("loginKeywords")
                || name.equals("generalWordsToExclude")) {
            try {
                Pattern.compile(content);
            } catch (PatternSyntaxException e) {
                System.err.println("tag " + name
                        + " has an invalid regex\nusing default value");
                e.printStackTrace();
                return false;
            }

            if (name.equals("searchKeywords")) {
                searchKeywords = content;
            } else if (name.equals("sortKeywords")) {
                sortKeywords = content;
            } else if (name.equals("loginKeywords")) {
                loginKeywords = content;
            } else if (name.equals("generalWordsToExclude")) {
                generalWordsToExclude = content;
            }
            return true;
        } else if (name.equals("historyFilepath")
                || name.equals("processedHistoryFilepath")
                || name.equals("patternsFilepath")) {
            File f = new File(content);
            try {
                f.getCanonicalPath();
            } catch (IOException e) {
                System.err.println("content of tag " + name
                        + " is not a valid file path\nusing default value");
                e.printStackTrace();
                return false;
            }

            if (name.equals("historyFilepath")) {
                historyFilepath = content;
            } else if (name.equals("processedHistoryFilepath")) {
                processedHistoryFilepath = content;
            } else if (name.equals("patternsFilepath")) {
                patternsFilepath = content;
            }
            return true;
        } else
            return false;
    }

}
