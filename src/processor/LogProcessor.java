package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import configuration.Configurator;

public class LogProcessor {
	private static Configurator conf;

	// static String imageKeywords = "(img)";
	// static String refineKeywords = "(refine)";

	private static ArrayList<PatternMapEntry> patterns;

	public static void defaultSetupPatternList() {
		setPatterns(new ArrayList<PatternMapEntry>());
	
		getPatterns().add(new PatternMapEntry("submit", "submit"));
		
		// identify home page
		getPatterns().add(new PatternMapEntry("homeLink",
				"(home|main\\s?page|index|logo)"));
		getPatterns().add(new PatternMapEntry("imageLink", "img"));
		//patterns.add(new PatternMapEntry("searchRefineLink", "refine",
			//	"refine|link"));
		//patterns.add(new PatternMapEntry("navLink", "\\s?nav\\s",
			//	"\\s?nav\\s"));
		// specific types of links (next,previous,first,last)
		getPatterns().add(new PatternMapEntry("nextLink", "link(.*)next"
				));
		getPatterns().add(new PatternMapEntry("previousLink", "link(.*)prev(ious)?"
				));
		getPatterns().add(new PatternMapEntry("firstLink", "link(.*)first"
				));
		getPatterns().add(new PatternMapEntry("lastLink", "link(.*)last"
				));
		getPatterns().add(new PatternMapEntry("languageLink", "lang"
				));
		getPatterns().add(new PatternMapEntry("buttonLink", "href.*(button|btn)"
				));
		getPatterns().add(new PatternMapEntry("link", "link|href|button|btn"
				));		
		getPatterns().add(new PatternMapEntry("searchResultLink",
				"search.*result(.*row)?"));
		getPatterns().add(new PatternMapEntry("option", "option" ));

		// session start links
		getPatterns().add(new PatternMapEntry("login",
				"sign(ed)?(\\s|_)?(in|out)|log(ged)?(\\s|_)?(in|out)"
				));
		// login related patterns
		getPatterns().add(new PatternMapEntry("username", "user(\\s|_)?(name|id)?"
				));
		getPatterns().add(new PatternMapEntry("password", "pass(word)?"));
		getPatterns().add(new PatternMapEntry("verifyPassword",
				"verify(\\s|_)?pass(word)?"));
		getPatterns().add(new PatternMapEntry("email", "e?mail"));
		
		// mostly related to 'click' actions
		getPatterns().add(new PatternMapEntry("checkbox", "checkbox"
				));
		getPatterns().add(new PatternMapEntry("collapse", "collapse"));
		// first/last name
		getPatterns().add(new PatternMapEntry("firstName", "first(.*)name"));
		getPatterns().add(new PatternMapEntry("lastName", "last(.*)name"));

		getPatterns().add(new PatternMapEntry("sort", "sort|asc\\s|desc\\s"));
		getPatterns().add(new PatternMapEntry("search",
				"\\sq\\s|query|qry|search|pesq(uisa)?|procura(r)?|busca(dor)?"));
		getPatterns().add(new PatternMapEntry("button", "button|btn"));

		getPatterns().add(new PatternMapEntry("captcha", "captcha"));
		getPatterns().add(new PatternMapEntry("auth", "auth"));
		getPatterns().add(new PatternMapEntry("numberInput",
				"number|price|quantity|qty\\s|zip\\s?code"));
		getPatterns().add(new PatternMapEntry("input", "input"));
		getPatterns().add(new PatternMapEntry("clear", "clear"));
	}

	public static void processHistoryFile() {
	    conf = Configurator.getInstance();
		if(getPatterns() == null)
		    defaultSetupPatternList();
		
		// open reading file in UTF8
		BufferedReader in = null;
		File file = new File(conf.getHistoryFilepath());
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), /* "UTF8" */"ISO-8859-1"));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(2);
		}

		// open alphabet file
		FileWriter output = null;
		
		File actualOutputFile = new File(conf.getProcessedHistoryFilepath());

		try {
			output = new FileWriter(actualOutputFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String lineBuffer = "";
		String line = "";
//int lineNum = 1;
		try {
			while ((lineBuffer = in.readLine()) != null) {
	//			System.out.println(lineNum++);
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
			// Get the matching string, add in lower case, strip accents
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
		for (PatternMapEntry p : getPatterns()) {
			if (words.matches(".*(" + p.getIdentifyingRegex() + ").*")) {
				// build action type in camel case
				line += action
						+ Character.toUpperCase(p.getPatternName().charAt(0))
						+ p.getPatternName().substring(1) + " ";
				// System.out.println("LINE="+line);
				//words = words.replaceAll(
					//	"(" + p.getGarbageRemovalRegex() + ")", " ");
				//System.out.println("WORDS_AFTER_REMOVAL="+words);
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
     * @param patterns the patterns to set
     */
    public static void setPatterns(ArrayList<PatternMapEntry> patterns) {
        LogProcessor.patterns = patterns;
    }

    /**
     * @return the patterns
     */
    public static ArrayList<PatternMapEntry> getPatterns() {
        return patterns;
    }

    
}
