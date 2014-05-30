package inferrer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import configuration.Configurator;
import site_accesser.WebsiteExplorer;
import utilities.Filesystem;
import inferrer.PatternRegister;

public class PatternInferrer {
    private static final String[] LOGIN_STATES = { "LOGIN", "LOGIN_USER",
            "LOGIN_PASS", "LOGIN_USER_PASS", "LOGIN_USER_PASS_SUBMIT" };
    private static final String[] INPUT_STATES = { "INPUT", "INPUT_SUBMIT" };
    private static final String[] SORT_STATES = { "SORT", "SORT_SUBMIT" };
    private static final String[] SEARCH_STATES = { "SEARCH", "SEARCH_SUBMIT" };
    private static final String[][] STATE_ENUM = { LOGIN_STATES, INPUT_STATES,
            SORT_STATES, SEARCH_STATES };

    private static Configurator conf = Configurator.getInstance();
    private static WebsiteExplorer we = WebsiteExplorer.getInstance();

    private static int firstIndex = -1;
    private static int secondIndex = -1;
    private static String currentState = "NONE";
    private static ArrayList<Integer> lines = new ArrayList<Integer>();
    private static boolean alreadyWroteOnThisLine = false;
    private static int patternIndex = 1;
    private static LinkedHashMap<String, ArrayList<Integer>> patternsFound = new LinkedHashMap<String, ArrayList<Integer>>();

    private static HashMap<String, HashSet<String>> menuElements,
            masterElements, detailElements;
    private static String baseUrl = "default";

    private static int writtenPatternIndex = 1;

    /**
     * @param menuElements
     *            the menuElements to set
     */
    public static void setMenuElements(
            HashMap<String, HashSet<String>> menuElements) {
        PatternInferrer.menuElements = menuElements;
    }

    public static void setBaseUrl(String baseURL) {
        baseUrl = baseURL;
    }

    public static void setMasterElements(
            HashMap<String, HashSet<String>> masterElements) {
        PatternInferrer.masterElements = masterElements;
    }

    public static void setDetailElements(
            HashMap<String, HashSet<String>> detailElements) {
        PatternInferrer.detailElements = detailElements;
    }

    public static void startInferringProcess() {
        /*
         * try { System.setOut(new PrintStream(new File("out_p.txt"))); } catch
         * (FileNotFoundException e1) { e1.printStackTrace(); }
         */

        // open processed file
        System.out.println("********************************************");
        BufferedReader in = null;
        File file = new File(we.getFilepath()
                + conf.getProcessedHistoryFilepath());
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file), /* "UTF8" */"ISO-8859-1"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(2);
        }

        String lineBuffer = "";
        // String line = "";
        int lineNum = 0;
        try {
            while ((lineBuffer = in.readLine()) != null) {
                processLine(lineBuffer, lineNum);
                lineNum++;
                // System.out.println(lineBuffer + ";" + line );
                // System.out.println(line );
                // updateCurrentState();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write trailing sorts or searches
        testForTrailingSort(lineNum);
        testForTrailingSearch(lineNum);
        testForTrailingLogin(lineNum);
        testForTrailingInput(lineNum);

        // write to file
        writeParadigmFile();

        // close the streams
        try {
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testForTrailingSort(int lineNum) {
        if (firstIndex == 2 && secondIndex == 0) {
            // sort without submit is still valid
            patternsFound.put("SORT_" + patternIndex, lines);
            patternIndex++;
            String line = "";
            for (int i : lines)
                line += i + " ";
            System.out.println(lineNum + 1 + ": SORT|lines: " + line);
            resetStates();
        }
    }

    private static void testForTrailingLogin(int lineNum) {
        if (firstIndex == 0 && secondIndex == 3) {
            // sort without submit is still valid
            patternsFound.put("LOGIN_" + patternIndex, lines);
            patternIndex++;
            String line = "";
            for (int i : lines)
                line += i + " ";
            System.out.println(lineNum + 1 + ": LOGIN|lines: " + line);
            resetStates();
        }
    }

    private static void testForTrailingSearch(int lineNum) {
        if (firstIndex == 3 && secondIndex == 0) {
            // search without submit is still valid
            patternsFound.put("SEARCH_" + patternIndex, lines);
            patternIndex++;
            String line = "";
            for (int i : lines)
                line += i + " ";
            System.out.println(lineNum + 1 + ": SEARCH|lines: " + line);
            resetStates();
        }
    }
    private static void testForTrailingInput(int lineNum) {
        if (firstIndex == 1 && secondIndex == 0) {
            // search without submit is still valid
            patternsFound.put("INPUT_" + patternIndex, lines);
            patternIndex++;
            String line = "";
            for (int i : lines)
                line += i + " ";
            System.out.println(lineNum + 1 + ": INPUT|lines: " + line);
            resetStates();
        }
    }

    private static void writeParadigmFile() {
        // write menu
        PatternRegister.initializePatternRegister(baseUrl);
        if (!(menuElements == null || menuElements.isEmpty())
                && we.isSearchingForPattern("menu")) {
            PatternRegister.startPattern("menu", writtenPatternIndex);// number
                                                                      // doesnt
                                                                      // matter

            Iterator<Entry<String, HashSet<String>>> it = menuElements
                    .entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, HashSet<String>> entry = it.next();
                PatternRegister.enterMenuItemContent(entry.getKey(),
                        entry.getValue());
            }

            PatternRegister.closePattern();
            writtenPatternIndex++;
        }

        // write search result pages' master detail
        if (!(masterElements == null || detailElements == null
                || masterElements.isEmpty() || detailElements.isEmpty())
                && we.isSearchingForPattern("masterdetail")) {
            Set<String> keys = masterElements.keySet();

            for (String k : keys) {
                PatternRegister.startPattern("masterDetail",
                        writtenPatternIndex);// number
                // doesn't
                // matter
                Set<String> masters = masterElements.get(k);
                Set<String> details = null;
                if (detailElements.containsKey(k))
                    details = detailElements.get(k);

                PatternRegister.enterMasterDetailContent(masters, details, k);
                PatternRegister.closePattern();

                writtenPatternIndex++;
            }
        }

        // write rest of patterns
        Iterator<Entry<String, ArrayList<Integer>>> it = patternsFound
                .entrySet().iterator();

        String[] line = null;
        while (it.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> entry = it.next();
            String[] pattern = entry.getKey().split("_");

            int start = entry.getValue().get(0);

            if (entry.getValue().size() > 1)
                line = Filesystem.getLinesInFile(
                        we.getFilepath() + conf.getHistoryFilepath(), start,
                        entry.getValue().get(entry.getValue().size() - 1));
            else
                line = Filesystem.getLinesInFile(
                        we.getFilepath() + conf.getHistoryFilepath(), start);

            ArrayList<String> actions = new ArrayList<String>(), targets = new ArrayList<String>(), parameters = new ArrayList<String>();

            if (!(line == null)) {
                for (int i = 0; i < line.length; ++i) {
                    if (line[i] == null)
                        continue;
                    String[] splits = line[i].split(conf.getSeparator());

                    actions.add(splits[0]);
                    targets.add(splits[1]);
                    parameters.add(splits[2]);
                }
            }

            String patternType = pattern[0];
            writeWholePattern(patternType.toLowerCase(), actions, targets,
                    parameters);

        }

        PatternRegister.endPatternRegister(writtenPatternIndex);
    }

    private static void writeWholePattern(String patternType,
            ArrayList<String> actions, ArrayList<String> targets,
            ArrayList<String> parameters) {
        if (we.isSearchingForPattern(patternType)) {
            ArrayList<String> cleanTargets = new ArrayList<String>(), cleanActions = new ArrayList<String>(), cleanParams = new ArrayList<String>();
            for (int i = 0; i < targets.size(); ++i) {
                if (!PatternRegister.isAlreadyWritten(
                        patternType.toLowerCase(), targets.get(i))) {
                    cleanActions.add(actions.get(i));
                    cleanTargets.add(targets.get(i));
                    cleanParams.add(parameters.get(i));
                }
            }
            
            if (cleanTargets.size() > 0) {

                if (patternType.equals("input")) {
                    for (int i = 0; i < cleanActions.size(); ++i) {
                        // ignore submits
                        if (targets.get(i).matches(".*(submit).*"))
                            continue;
                        ArrayList<String> a = new ArrayList<String>(),
                                t = new ArrayList<String>(),
                                p = new ArrayList<String>();
                        a.add(cleanActions.get(i));
                        t.add(cleanTargets.get(i));
                        p.add(cleanParams.get(i));
                        
                        PatternRegister.startPattern(patternType,
                                writtenPatternIndex);
                        PatternRegister.enterInputContent(a,
                                t, p);
                        PatternRegister.closePattern();
                        
                        writtenPatternIndex++;
                    }
                } else {
                    PatternRegister.startPattern(patternType,
                            writtenPatternIndex);

                    if (patternType.equals("login"))
                        PatternRegister.enterLoginContent(cleanActions,
                                cleanTargets, cleanParams);
                    else if (patternType.equals("search"))
                        PatternRegister.enterFindContent(cleanActions,
                                cleanTargets, cleanParams);
                    else if (patternType.equals("sort"))
                        PatternRegister.enterSortContent(cleanActions,
                                cleanTargets, cleanParams);
                    else if (patternType.equals("call"))
                        PatternRegister.enterCallContent(cleanActions,
                                cleanTargets, cleanParams);

                    PatternRegister.closePattern();
                }
                writtenPatternIndex++;
            }
        }

    }

    private static void updateCurrentState() {
        if (firstIndex >= 0 && secondIndex >= 0)
            currentState = STATE_ENUM[firstIndex][secondIndex];
        else
            currentState = "NONE";
    }

    private static void processLine(String lineBuffer, int lineNum) {
        ArrayList<String> words = new ArrayList<String>();

        Pattern pattern = Pattern.compile("([0-9]+)?[a-zA-Z]+([0-9]+)?");
        Matcher matcher = pattern.matcher(lineBuffer);
        while (matcher.find()) {
            words.add(matcher.group());
        }

        String line = "";
        String word = "";

        // { LOGIN_STATES, INPUT_STATES, SORT_STATES, SEARCH_STATES };
        if (lineBuffer != null) {
            if (matchLink(words)) {
                processLink(words, lineNum);
            } else if (matchSubmit(words)) {
                // might be a submit to login, input, search or sort
                switch (firstIndex) {
                    case 0:
                        processLogin(words, lineNum);
                        break;
                    case 1:
                        processInput(words, lineNum);
                        break;
                    case 2:
                        processSort(words, lineNum);
                        break;
                    case 3:
                        processSearch(words, lineNum);
                        break;
                    default:
                        resetStates();
                }
            } else if (matchSearch(words)) {
                processSearch(words, lineNum);
            } else if (matchSort(words)) {
                processSort(words, lineNum);
            } else if (matchInput(words)) {
                processInput(words, lineNum);
            } else if (matchLogin(words)) {
                processLogin(words, lineNum);
            } else {
                testForTrailingSort(lineNum);
                testForTrailingSearch(lineNum);
                testForTrailingLogin(lineNum);
                testForTrailingInput(lineNum);

                System.out
                        .println("doesnt match call, login, sort, search or input - ignored");
                resetStates();
            }
        }
        updateCurrentState();

        if (!alreadyWroteOnThisLine) {
            line = "";
            word = "";
            if (lines.size() > 0)
                for (int i : lines)
                    line += i + " ";
            for (String i : words)
                word += i + " ";
            System.out.println((lineNum+1) + ": words:" + word + "|" + currentState
                    + (!line.isEmpty() ? "|lines: " + line : ""));
        } else
            alreadyWroteOnThisLine = false;

    }

    private static void processLink(ArrayList<String> words, int lineNum) {
        testForTrailingSort(lineNum);
        testForTrailingSearch(lineNum);
        testForTrailingLogin(lineNum);
        testForTrailingInput(lineNum);

        System.out.println(lineNum + ": CALL");
        ArrayList<Integer> a = new ArrayList<Integer>();
        a.add(lineNum);
        patternsFound.put("CALL_" + patternIndex, a);
        patternIndex++;
        alreadyWroteOnThisLine = true;
        resetStates();
    }

    private static void resetStates() {
        System.out.println("***RESET***");
        firstIndex = -1;
        secondIndex = -1;
        updateCurrentState();
        lines = new ArrayList<Integer>();
    }

    private static void setStates(int first, int second) {
        firstIndex = first;
        secondIndex = second;
        updateCurrentState();
    }

    private static void processSearch(ArrayList<String> words, int lineNum) {
        // check if trailing sort or search
        testForTrailingSort(lineNum);
        testForTrailingLogin(lineNum);
        testForTrailingInput(lineNum);

        if (matchSubmit(words)) {
            if (firstIndex == 3) {
                if (secondIndex == 0) {
                    // full search
                    setStates(firstIndex, 1);
                    lines.add(lineNum);
                    updateCurrentState();

                    String line = "";
                    for (int i : lines)
                        line += i + " ";
                    System.out.println(lineNum + ": " + currentState
                            + "|lines: " + line);
                    patternsFound.put("SEARCH_" + patternIndex, lines);
                    patternIndex++;
                    alreadyWroteOnThisLine = true;
                    resetStates();
                }
            } else {
                System.out.println(lineNum + "|invalid state: expected 3 got "
                        + firstIndex);
            }
        } else if (matchSearch(words)) {
            lines.add(lineNum);
            setStates(3, 0);
        } else {
            System.out.println("SEARCH:error invalid input");
            resetStates();
        }
    }

    private static void processSort(ArrayList<String> words, int lineNum) {
        // check if trailing sort or search
        testForTrailingSearch(lineNum);
        testForTrailingLogin(lineNum);
        testForTrailingInput(lineNum);

        if (matchSubmit(words) && (firstIndex == 2) && (secondIndex == 0)) {
            // full search
            setStates(firstIndex, 1);
            lines.add(lineNum);
            updateCurrentState();

            String line = "";
            for (int i : lines)
                line += i + " ";
            System.out
                    .println(lineNum + ": " + currentState + "|lines:" + line);
            patternsFound.put("SORT_" + patternIndex, lines);
            patternIndex++;
            alreadyWroteOnThisLine = true;
            resetStates();
        } else if (matchSort(words)) {
            lines.add(lineNum);
            setStates(2, 0);
        } else {
            System.out.println("SORT:error invalid input");
            resetStates();
        }
    }

    private static void processInput(ArrayList<String> words, int lineNum) {
        // check if trailing sort or search
        testForTrailingSort(lineNum);
        testForTrailingSearch(lineNum);
        testForTrailingLogin(lineNum);

        if (matchSubmit(words) && (firstIndex == 1) && (secondIndex == 0)) {
            // full input
            setStates(firstIndex, 1);
            lines.add(lineNum);
            updateCurrentState();

            String line = "";
            for (int i : lines)
                line += i + " ";
            System.out.println(lineNum + 1 + ": " + currentState + "|lines: "
                    + line);
            patternsFound.put("INPUT_" + patternIndex, lines);
            patternIndex++;
            alreadyWroteOnThisLine = true;
            resetStates();
        } else if (matchInput(words)) {
            lines.add(lineNum);
            setStates(1, 0);
        } else {
            System.out.println("INPUT:error invalid input");
            resetStates();
        }
    }

    private static void processLogin(ArrayList<String> words, int lineNum) {
        // check if trailing sort or search
        testForTrailingSort(lineNum);
        testForTrailingSearch(lineNum);
        testForTrailingInput(lineNum);

        // "LOGIN","LOGIN_USER","LOGIN_PASS","LOGIN_USER_PASS","LOGIN_USER_PASS_SUBMIT"
        if (matchSubmit(words)) {
            if (firstIndex == 0) {
                // end of login
                if (secondIndex == 3) {
                    // valid
                    setStates(0, 3);
                    lines.add(lineNum);
                    updateCurrentState();

                    String line = "";
                    for (int i : lines)
                        line += i + " ";
                    System.out.println(lineNum + ": " + currentState
                            + "|lines:" + line);
                    patternsFound.put("LOGIN_" + patternIndex, lines);
                    patternIndex++;
                    alreadyWroteOnThisLine = true;
                    resetStates();
                } else {
                    System.out.println("LOGIN: premature submit ");
                    resetStates();
                }
            } else {
                System.out.println("LOGIN: invalid state: expected 2 got "
                        + firstIndex);
            }
        } else if (words.get(0).toLowerCase()
                .matches(".*(login|auth|captcha).*")) {
            if (firstIndex == 0) {
                // doesnt alter states, can go in any state
                lines.add(lineNum);
            } else {
                // starts login
                lines.add(lineNum);
                setStates(0, 0);
            }
        } else if (words.get(0).toLowerCase().matches(".*(user|email).*")) {
            if (firstIndex == 0) {
                if (secondIndex == 0) {// curr state LOGIN
                    setStates(0, 1);// LOGIN_USER
                    lines.add(lineNum);
                } else if (secondIndex == 1) {// curr state LOGIN_USER
                    // duplicate writes on user, reset
                    resetStates();
                } else if (secondIndex == 2) {// curr state LOGIN_PASS
                    // has password, got user, change state
                    lines.add(lineNum);
                    setStates(0, 3);// LOGIN_USER_PASS
                } else if (secondIndex == 3) {// curr state LOGIN_USER_PASSWORD
                    // reset
                    resetStates();
                }
            } else {
                // starts login
                lines.add(lineNum);
                setStates(0, 1);
            }
        } else if (words.get(0).toLowerCase().matches(".*password.*")) {
            if (firstIndex == 0) {
                if (secondIndex == 0) {// curr state LOGIN
                    lines.add(lineNum);
                    setStates(0, 2); // LOGIN_PASS
                } else if (secondIndex == 1) {// curr state LOGIN_USER
                    lines.add(lineNum);
                    setStates(0, 3);
                } else if (secondIndex == 2) {// curr state LOGIN_PASS
                    // 2 passwords means it's a register instead of a login form - reset
                    resetStates();
                } else if (secondIndex == 3) {// curr state LOGIN_USER_PASS
                    // same as above
                    resetStates();
                }
            } else {
                // starts login
                lines.add(lineNum);
                setStates(0, 2);// LOGIN_PASS
            }
        } else {
            System.out.println("LOGIN:error invalid input");
            resetStates();
        }
    }

    private static boolean matchSubmit(ArrayList<String> words) {
        return (words.size() == 1 ? false : words.get(0).toLowerCase()
                .matches(".*submit.*")
                && words.get(1).toLowerCase().equals("pagechange"));
    }

    private static boolean matchSearch(ArrayList<String> words) {
        return words.get(0).toLowerCase().matches(".*search.*");
    }

    private static boolean matchSort(ArrayList<String> words) {
        return words.get(0).toLowerCase().matches(".*sort.*");
    }

    private static boolean matchLink(ArrayList<String> words) {
        return (words.size() == 1 ? false : words.get(0).toLowerCase()
                .matches(".*link.*")
                && words.get(1).toLowerCase().equals("pagechange"));
    }

    private static boolean matchLogin(ArrayList<String> words) {
        return words.get(0).toLowerCase()
                .matches(".*(login|user|email|password|auth|captcha).*");
    }

    private static boolean matchInput(ArrayList<String> words) {
        return words.get(0).toLowerCase().matches(".*(input|(first|last)name).*");
    }
}
