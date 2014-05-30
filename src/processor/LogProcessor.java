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

import site_accesser.WebsiteExplorer;
import configuration.Configurator;

public class LogProcessor {
	private static Configurator conf;
	private static WebsiteExplorer we = WebsiteExplorer.getInstance();

	// static String imageKeywords = "(img)";
	// static String refineKeywords = "(refine)";

	private static ArrayList<PatternMapEntry> patterns;

	/*public static void defaultSetupPatternList() {
		setPatterns(new ArrayList<PatternMapEntry>());
	
		patterns.add(new PatternMapEntry("submit", "submit"));
		
		// identify home page
		patterns.add(new PatternMapEntry("homeLink",
				"(home|main\\s?page|index|logo)"));
		patterns.add(new PatternMapEntry("imageLink", "img"));
		//patterns.add(new PatternMapEntry("searchRefineLink", "refine",
			//	"refine|link"));
		//patterns.add(new PatternMapEntry("navLink", "\\s?nav\\s",
			//	"\\s?nav\\s"));
		// specific types of links (next,previous,first,last)
		patterns.add(new PatternMapEntry("nextLink", "link(.*)next"
				));
		patterns.add(new PatternMapEntry("previousLink", "link(.*)prev(ious)?"
				));
		patterns.add(new PatternMapEntry("firstLink", "link(.*)first"
				));
		patterns.add(new PatternMapEntry("lastLink", "link(.*)last"
				));
		patterns.add(new PatternMapEntry("languageLink", "lang"
				));
		patterns.add(new PatternMapEntry("buttonLink", "href.*(button|btn)"
				));
		patterns.add(new PatternMapEntry("link", "link|href|button|btn"
				));		
		patterns.add(new PatternMapEntry("searchResultLink",
				"search.*result(.*row)?"));
		patterns.add(new PatternMapEntry("option", "option" ));

		// session start links
		patterns.add(new PatternMapEntry("login",
				"sign(ed)?(\\s|_)?(in|out)|log(ged)?(\\s|_)?(in|out)"
				));
		// login related patterns
		patterns.add(new PatternMapEntry("username", "user(\\s|_)?(name|id)?"
				));
		patterns.add(new PatternMapEntry("password", "pass(word)?"));
		patterns.add(new PatternMapEntry("verifyPassword",
				"verify(\\s|_)?pass(word)?"));
		patterns.add(new PatternMapEntry("email", "e?mail"));
		
		// mostly related to 'click' actions
		patterns.add(new PatternMapEntry("checkbox", "checkbox"
				));
		patterns.add(new PatternMapEntry("collapse", "collapse"));
		// first/last name
		patterns.add(new PatternMapEntry("firstName", "first(.*)name"));
		patterns.add(new PatternMapEntry("lastName", "last(.*)name"));

		patterns.add(new PatternMapEntry("sort", "sort|asc\\s|desc\\s"));
		patterns.add(new PatternMapEntry("search",
				"\\sq\\s|query|qry|search|pesq(uisa)?|procura(r)?|busca(dor)?"));
		patterns.add(new PatternMapEntry("button", "button|btn"));

		patterns.add(new PatternMapEntry("captcha", "captcha"));
		patterns.add(new PatternMapEntry("auth", "auth"));
		patterns.add(new PatternMapEntry("numberInput",
				"number|price|quantity|qty\\s|zip\\s?code"));
		patterns.add(new PatternMapEntry("input", "input"));
		patterns.add(new PatternMapEntry("clear", "clear"));
	}*/

	public static void processHistoryFile() {
	    conf = Configurator.getInstance();
		//if(getPatterns() == null)
		//    defaultSetupPatternList();
		
		// open reading file in UTF8
		BufferedReader in = null;
		File file = new File(we.getFilepath()+conf.getHistoryFilepath());
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), /* "UTF8" */"ISO-8859-1"));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(2);
		}

		// open alphabet file
		FileWriter output = null;
		
		File actualOutputFile = new File(we.getFilepath()+conf.getProcessedHistoryFilepath());

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
