package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import site_accesser.WebsiteExplorer;
import configuration.Configurator;

public class LogProcessor {
    private static Configurator conf;
    private static WebsiteExplorer we = WebsiteExplorer.getInstance();

    private static HashMap<String,String> patterns;

    public static void defaultSetupPatternList() {
        patterns = new HashMap<String,String>();
        patterns.put("login",
                "sign(ed)?(\\s|_)?(in|out)|log(ged)?(\\s|_)?(in|out)");
        patterns.put("submit", "submit");
        patterns.put("homeLink",
                "(home|main(\\s|_)?page|index|logo)");
        patterns.put("imageLink", "link(.*)img|img(.*)link");
        patterns.put("nextLink",
                "(link(.*)next|next(.*)link)");
        patterns.put("prevLink",
                "(prev(ious)?(.*)link|link(.*)prev(ious)?)");
        patterns.put("firstLink", "(first(.*)link)");
        patterns.put("lastLink", "(link(.*)last)");
        patterns.put("languageLink", "lang");
        patterns.put("buttonLink", "href.*(button|btn)");
        patterns.put("searchResultLink",
                "search.*result(.*row)?");
        patterns.put("link", "link|href");
        patterns.put("option", "option");
        patterns.put("username", "user(\\s|_)?(name|id)?");
        patterns.put("verifyPassword",
                "verify(\\s|_)?pass(word)?|pass(word)?(\\s|_)?confirm(ation)?");
        patterns.put("password", "pass(word)?");
        patterns.put("email", "e?mail");
        patterns.put("checkbox", "checkbox");
        patterns.put("collapseButton", "collapse");
        patterns.put("firstNameInput", "(input|textarea).*first(\\s|_)?name");
        patterns.put("lastNameInput", "(input|textarea).*last(\\s|_)?name");
        patterns.put("sort", "((input|select|textarea).*sort)");
        patterns.put(
                "search",
                "(input|select|textarea)(.*(=q|q(ue)?ry|s(ea)?rch|pesq(uisa)?|procura(r)?|busca(dor)?).*)");
        patterns.put("captcha", "captcha");
        patterns.put("auth", "auth");
        patterns.put("numberInput",
                "number|price|quantity|qty\\s|zip(\\s|_)?code");
        patterns.put(
                "input",
                "\\/\\/(input|textarea)\\[((?!(email|user|pass|search|sort|submit|checkbox|radio)).)*\\]");
        patterns.put("button", "button");
        patterns.put("clear", "clear");

    }

    public static void processHistoryFile() {
        conf = Configurator.getInstance();
        if (getPatterns() == null)
            defaultSetupPatternList();

        // open reading file in UTF8
        BufferedReader in = null;
        File file = new File(we.getFilepath() + conf.getHistoryFilepath());
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file), /* "UTF8" */"ISO-8859-1"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(2);
        }

        // open alphabet file
        FileWriter output = null;

        File actualOutputFile = new File(we.getFilepath()
                + conf.getProcessedHistoryFilepath());

        try {
            output = new FileWriter(actualOutputFile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        String lineBuffer = "";
        String line = "";
        // int lineNum = 1;
        try {
            while ((lineBuffer = in.readLine()) != null) {
                // System.out.println(lineNum++);
                line = processLine(lineBuffer);
                // output.write(lineBuffer + ";" + line + "\n");
                output.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close the streams
        try {
            in.close();
            output.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String processLine(String lineBuffer) {
        Pattern pattern = Pattern.compile("([0-9]+)?[a-zA-Z]+([0-9]+)?");
        Matcher matcher = pattern.matcher(lineBuffer);
        String words = "", action = "", line = "";
        boolean atLeastOne = false, first = true, pageChange = false;

        // split by words, except first word
        while (matcher.find()) {
            // Get the matching string, put in lower case, strip accents
            if (first) {
                action = org.apache.commons.lang3.StringUtils
                        .stripAccents(matcher.group());
                if (action.toLowerCase().matches(".*andwait.*")) {
                    action = action.toLowerCase().replaceAll("andwait", "");
                    pageChange = true;
                }
                first = false;
            } else
                words += org.apache.commons.lang3.StringUtils
                        .stripAccents(matcher.group().toLowerCase()) + " ";
        }
        // System.out.println("ORIGINAL_LINE="+words);
        // process all patterns
        Iterator<Map.Entry<String, String>> it = patterns.entrySet().iterator();
        while (it.hasNext()) {
        //for (Map<K, V>.Entry<K, V> p : getPatterns()) {
            Entry<String,String> p = it.next();
            if (words.matches(".*(" + p.getValue() + ").*")) {
                // build action type in camel case
                line += action
                        + Character.toUpperCase(p.getKey().charAt(0))
                        + p.getKey().substring(1) + " ";
                // System.out.println("LINE="+line);
                // words = words.replaceAll(
                // "(" + p.getGarbageRemovalRegex() + ")", " ");
                // System.out.println("WORDS_AFTER_REMOVAL="+words);
                if (!atLeastOne)
                    atLeastOne = true;
                break;
            }
        }

        if (!atLeastOne)
            line = action + " ";
        if (pageChange)
            line += "pageChange";

        return line;
    }

    /**
     * @param patterns
     *            the patterns to set
     */
    public static void setPatterns(HashMap<String,String> _patterns) {
        defaultSetupPatternList();
        Iterator<Map.Entry<String, String>> it = _patterns.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String,String> p = it.next();
            patterns.put(p.getKey(),p.getValue());
        }
    }

    /**
     * @return the patterns
     */
    public static HashMap<String,String> getPatterns() {
        return patterns;
    }

}
