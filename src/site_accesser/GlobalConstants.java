package site_accesser;

import java.io.File;

public class GlobalConstants {
	/** keywords that identify search elements */
	public static final String searchKeywords = "(\"q\"|query|search|pesq(uisa)*|procura(r)*|busca(dor)*)";
	/** keywords that identify sort elements */
	public static final String sortKeywords = "(sort|asc\\s|desc\\s)";
	/** keywords that identify login elements */
	public static final String loginKeywords = "(user(name)?|pass(word)?|e?mail|(sign(ed)?(\\s|_)*(in|out)|log(ged)?(\\s|_)*(in|out)))";
	/** keywords that identify elements that SHOULD NOT BE ACCESSED */
	public static final String generalWordsToExclude = "(buy|sell|edit|delete|mailto|add\\sto\\scart|checkout)";
	/** history column separator */
	public static final String SEPARATOR = "\t";
	/** history file name */
	public static final String HISTORY_FILENAME = "history.csv";
	/** path to patterns file */
	public static final String HISTORY_FILEPATH = System.getProperty("user.dir")
			+ File.separatorChar + HISTORY_FILENAME;
	/** path to processed history file */
	public static final String PROCESSED_FILEPATH = System.getProperty("user.dir")
			+ File.separatorChar + "history.csv.processed";
	/** path to patterns file */
	public static final String PATTERNS_FILEPATH = System.getProperty("user.dir")
			+ File.separatorChar + "patterns.paradigm";
}
