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

public class LogProcessor {
	static DicTrie dictionary = new DicTrie();

	static String imageKeywords = "(img)";
	static String refineKeywords = "(refine)";

	static ArrayList<PatternMapEntry> patterns;

	public static void defaultSetupPatternList() {
		patterns = new ArrayList<PatternMapEntry>();
		// identify home page
		patterns.add(new PatternMapEntry("home", "(home|main\\s?page|index)",
				""));
		// session start links
		patterns.add(new PatternMapEntry("login",
				"sign(ed)?(\\s|_)?(in|out)|log(ged)?(\\s|_)?(in|out)",
				"(sign(ed)?(\\s|_)?(in|out)|log(ged)?(\\s|_)?(in|out)|link)"));
		// login related patterns
		patterns.add(new PatternMapEntry("username", "user(name)?", ""));
		patterns.add(new PatternMapEntry("password", "pass(word)?", ""));
		patterns.add(new PatternMapEntry("verifyPassword",
				"verify(\\s|_)?pass(word)?", "verify|pass(word)?"));
		patterns.add(new PatternMapEntry("email", "e?mail", "e?mail"));
		patterns.add(new PatternMapEntry("search",
				"\\sq\\s|query|search|pesq(uisa)?|procura(r)?|busca(dor)?",
				"\\sq\\s|query|search|pesq(uisa)?|procura(r)?|busca(dor)?|input"));
		patterns.add(new PatternMapEntry("sort", "sort|asc\\s|desc\\s",
				"sort|asc\\s|desc\\s"));
		patterns.add(new PatternMapEntry("button", "button|btn", "button|btn"));

		patterns.add(new PatternMapEntry("formSubmit", "submit",
				"submit|input|nav"));
		patterns.add(new PatternMapEntry("imageLink", "img", "link|img"));
		patterns.add(new PatternMapEntry("option", "option", "option"));
		patterns.add(new PatternMapEntry("refine", "refine", "refine|link"));
		// mostly related to 'click' actions
		patterns.add(new PatternMapEntry("checkbox", "checkbox",
				"input|nav|checkbox"));
		patterns.add(new PatternMapEntry("collapse", "collapse",
				"collapse|button|btn"));
		patterns.add(new PatternMapEntry("navigation", "\\s?nav\\s",
				"\\s?nav\\s"));
		// specific types of links (next,previous,first,last)
		patterns.add(new PatternMapEntry("next", "link(.*)next", "link|next"));
		patterns.add(new PatternMapEntry("previous", "link(.*)prev(ious)?",
				"link|prev(ious)?"));
		patterns.add(new PatternMapEntry("firstLink", "link(.*)first", "link|first"));
		patterns.add(new PatternMapEntry("lastLink", "link(.*)last", "link|last"));
		// first/last name
		patterns.add(new PatternMapEntry("firstName", "first(.*)name",
				"name|first"));
		patterns.add(new PatternMapEntry("lastName", "last(.*)name",
				"name|last"));
		// CRUD ops
		patterns.add(new PatternMapEntry("create", "add|create|new",
				"add|create|new|link"));

		patterns.add(new PatternMapEntry("language", "lang", "lang"));
		patterns.add(new PatternMapEntry("link", "link|a\\s|href",
				"link|a\\s|href"));
		patterns.add(new PatternMapEntry("input", "input|text", "input|text"));
	}

	public static void processFile(String absolutePath) {
		// open reading file in UTF8
		BufferedReader in = null;
		File file = new File(absolutePath);
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), /* "UTF8" */"ISO-8859-1"));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(2);
		}

		// open alphabet file
		FileWriter output = null;
		String dirName = file.getParentFile().toPath().toAbsolutePath()
				.toString();
		File dir = new File(dirName);
		File actualOutputFile = new File(dir, file.getName() + ".processed");

		try {
			output = new FileWriter(actualOutputFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String lineBuffer = "";
		String line = "";

		try {
			while ((lineBuffer = in.readLine()) != null) {
				line = processLine(lineBuffer);
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
		for (PatternMapEntry p : patterns) {
			if (words.matches(".*(" + p.getIdentifyingRegex() + ").*")) {
				// build action type in camel case
				line += action
						+ Character.toUpperCase(p.getPatternName().charAt(0))
						+ p.getPatternName().substring(1) + " ";
				// System.out.println("LINE="+line);
				words = words.replaceAll(".*(" + p.getGarbageRemovalRegex()
						+ ").*", "");
				// System.out.println("WORDS_AFTER_REMOVAL="+words+"\n");
				if (!atLeastOne)
					atLeastOne = true;
			}
		}

		if (!atLeastOne)
			line = action + " ";
		if (pageChange)
			line += "PageChange";

		return line;
	}

	public static void main(String[] args) {
		defaultSetupPatternList();
		File file = new File(
				"C:\\Users\\gekka_000\\workspace\\re-tool_continued\\"
						+ "crawler_histories\\portaldajuventude.csv");
		if (!file.isDirectory()
				&& !file.getAbsolutePath().matches("(.)*processed(.)*")) {
			LogProcessor.processFile(file.getAbsolutePath());
			System.out.println(file.getName() + " done.");
		}
	}
}