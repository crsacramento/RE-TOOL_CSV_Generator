package site_accesser;

import java.io.File;

public class GlobalConstants {
    /** keywords that identify search elements */
    public static  String searchKeywords = "(\\sq\\s|query|qry|search|"
            + "pesq(uisa)?|procura(r)?|busca(dor)?)";
    /** keywords that identify sort elements */
    public static  String sortKeywords = "(sort|asc\\s|desc\\s)";
    /** keywords that identify login elements */
    public static  String loginKeywords = "(user(name)?|pass(word)?|"
            + "e?mail|(sign(ed)?(\\s|_)?(in|out)|log(ged)?(\\s|_)?(in|out)))";
    /** keywords that identify elements that SHOULD NOT BE ACCESSED */
    public static  String generalWordsToExclude = "(buy|sell|edit|"
            + "delete|mailto|add(\\s|_)?to(\\s|_)?cart|checkout)";

    /** keywords that identify menu elements */
    public static  String[] menuIdentifiers = {"nav","head","menu","top","head","foot"};
    /**
     * keywords that identify master elements (from MasterDetail) in a search
     * result page
     */
    public static  String[] masterIdentifiers = {"refine","relatedsearches","spell"};
    /**
     * keywords that identify detail elements (from MasterDetail) in a search
     * result page
     */
    public static  String[] detailIdentifiers = {"results","entry","item"};
    /** history column separator */
    public static  String SEPARATOR = "\t";
    /** history file name */
    public static  String HISTORY_FILENAME = "history.csv";
    /** path to patterns file */
    public static  String HISTORY_FILEPATH = System
            .getProperty("user.dir") + File.separatorChar + HISTORY_FILENAME;
    /** path to processed history file */
    public static  String PROCESSED_FILEPATH = System
            .getProperty("user.dir")
            + File.separatorChar
            + "history.csv.processed";
    /** path to patterns file */
    public static  String PATTERNS_FILEPATH = System
            .getProperty("user.dir") + File.separatorChar + "patterns.paradigm";
}
