package data_gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePreprocessor {
	public static void preprocessFile(String absolutePath) {
		// open reading file in UTF8
		BufferedReader in = null;
		File file = new File(absolutePath);
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF8"));
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

		// updates line to write
		String x = "";

		// Regex (also catches accented characters)
		Pattern pattern = Pattern.compile("[A-Z]?[\\p{L}]+");
		// Pattern pattern = Pattern.compile("[A-Z]?[\\p{L}]+|[0-9]+");
		Matcher matcher = null;

		try {
			while ((x = in.readLine()) != null) {
				// split by words
				matcher = pattern.matcher(x);
				String words = "";

				// Find all matches

				while (matcher.find()) {
					// Get the matching string, add in lower case
					words += matcher.group().toLowerCase() + " ";
				}

				String line = "";

				if (words.contains("homepage")) {
					words = words.replaceAll("homepage", "");
					line += "Home ";
				}
				if (words.contains("main page")) {
					words = words.replaceAll("main page", "");
					line += "Home ";
				}

				// process 'clickAndWait' variants
				if (words.contains("clickandwait")) {/*"checkbox", "submit", "collapse", "btn","button",
							"img", "search", "sort", "persist", "option",
							 "nav", "action", "edit",
							"add", "delete", "create", "about", "home","link","next", "prev",
							"refine", "lang",  "input" */
					String[] keywords = {"btn","button", "submit", "img",
							"option", "main page", "home","login","refine","link" };
					String result = "";
					Boolean match = false;

					for (int i = 0; i < keywords.length; ++i) {
						result = searchAndReturn(words, "clickAndWait",
								keywords[i]);
						if (!result.isEmpty()) {
							match = true;
							words = words.replaceAll(keywords[i], "");

							switch (keywords[i]) {
							case "btn": {
								line += "clickAndWaitButton ";
								break;
							}
							case "refine":
							case "img":
							case "login": {
								words = words.replaceAll("link", "");
								line += result;
								break;
							}
							case "submit": {
								words = words.replaceAll("input", "");
								words = words.replaceAll(" nav ", "");
								line += result;
								break;
							}
							default:
								line += result;
							}

						}
					}

					if (match)
						words = words.replaceAll("clickandwait", "");
					else
						words = words
								.replaceAll("clickandwait", "clickAndWait");
				}
				// process 'click' variants
				if (words.contains("click ")) {
					boolean match = false;
					String result = "";
					String[] keywords = { "checkbox", "submit", "collapse", "btn","button",
							"img", "search", "sort", "persist", "option",
							 "nav", "action", "edit",
							"add", "delete", "create", "about", "home","refine","link","next", "prev",
							 "lang",  "input" };
					for (int i = 0; i < keywords.length; ++i) {
						result = searchAndReturn(words, "click", keywords[i]);
						if (!result.isEmpty()) {
							match = true;
							words = words.replaceAll(keywords[i], "");

							switch (keywords[i]) {
							case "btn": {
								line += "clickButton ";
								break;
							}
							case "home": {
								line += "Home ";
								break;
							}
							case "edit":
							case "add":
							case "delete":
							case "create": {
								words = words.replaceAll("link", "");
								line += result;
								break;
							}
							case "link": {
								boolean matchSpecialCase = false;
								// Search for "clickLinkNext"
								result = searchAndReturn(words, "", "next");
								if (!result.isEmpty()) {
									matchSpecialCase = true;
									words = words.replaceAll("next", "");
									line += "clickNext ";
									result = "";
								}

								// Search for "clickLinkPrev"
								result = searchAndReturn(words, "", "prev");
								if (!result.isEmpty()) {
									matchSpecialCase = true;
									words = words.replaceAll("prev", "");
									line += "clickPrev ";
									result = "";
								}

								if (!matchSpecialCase)
									line += "clickLink ";
								break;
							}
							case "collapse": {
								// Search for "clickCollapseButton"
								result = searchAndReturn(words, "", "button");
								if (!result.isEmpty()) {
									words = words.replaceAll("button", "");
									line += "clickCollapseButton ";
								}
								break;
							}
							case "checkbox":
							case "submit": {
								words = words.replaceAll("input", "");
								words = words.replaceAll(" nav ", " ");
								line += result;
								break;
							}
							case "refine": {
								words = words.replaceAll("link", "");
								line += "clickRefine ";
								break;
							}
							case "lang": {
								line += "clickLanguage ";
								break;
							}
							default:
								line += result;
							}
						}
					}

					if (match)
						words = words.replaceAll("click", "");
				}
				// process 'type' variants
				if (words.contains("type")) {
					boolean match = false;
					String result = "";
					String[] keywords = { "username", "user", "password",
							"pass", "search", "input", "email", "login",
							"first", "last", "description", "desc", "priority",
							"date", "edit", "add", "delete", "create" };
					for (int i = 0; i < keywords.length; ++i) {
						result = searchAndReturn(words, "type", keywords[i]);
						if (!result.isEmpty()) {
							match = true;
							words = words.replaceAll(keywords[i], "");

							switch (keywords[i]) {
							case "edit":
							case "add":
							case "delete":
							case "create": {
								words = words.replaceAll("link", "");
								line += result;
								break;
							}
							case "user":
							case "username": {
								line += "typeUsername ";
								break;
							}
							case "pass":
							case "password": {
								// search for typeVerifyPassword
								result = searchAndReturn(words, "", "verify");
								if (!result.isEmpty()) {
									words = words.replaceAll("verify", "");
									line += "typeVerifyPassword ";
								} else {
									line += "typePassword ";
								}
								break;
							}
							case "search": {
								words = words.replaceAll("input", "");
								line += "typeSearch ";
								break;
							}
							case "input": {
								// eliminate 'text', not necessary
								words = words.replaceAll("text", "");
								line += result;
								break;
							}
							case "last":
							case "first": {
								// search for typeFirstName
								result = searchAndReturn(words, "", "name");
								if (!result.isEmpty()) {
									words = words.replaceAll("name", "");
									line += "type"
											+ Character.toUpperCase(keywords[i]
													.charAt(0))
											+ keywords[i].substring(1)
											+ "Name ";
								}
								break;
							}
							default:
								line += result;
							}
						}
					}

					if (match)
						words = words.replaceAll("type", "");
				}
				// process 'select' variants
				if (words.contains("select")) {
					boolean match = false;
					String result = "";
					String[] keywords = { "sort", "search" };
					for (int i = 0; i < keywords.length; ++i) {
						result = searchAndReturn(words, "select", keywords[i]);
						if (!result.isEmpty()) {
							match = true;
							words = words.replaceAll(keywords[i], "");
							line += result;
						}
					}
					if (match)
						words = words.replaceAll("select", "");
				}
				
				// Substitute some Selenium words
				if (words.contains("waitforpopupandwait"))
					words = words.replaceAll("waitforpopupandwait",
							"waitForPopupAndWait");
				if (words.contains("selectwindow"))
					words = words.replaceAll("selectwindow", "selectWindow");
				if (words.contains("typeandwait"))
					words = words.replaceAll("typeandwait", "typeAndWait");
				if (words.contains("assertconfirmation"))
					words = words.replaceAll("assertconfirmation",
							"assertConfirmation");

				// Remove noise words
				words = words.replaceAll("empty", "");
				words = words.replaceAll(" na ", "");
				words = words.replaceAll("div ", " ");
				words = words.replaceAll(" ul ", " ");
				words = words.replaceAll(" li ", " ");
				words = words.replaceAll(" [a-zA-Z] ", " ");
				words = words.replaceAll(" span ", " ");
				words = words.replaceAll(" css ", " ");
				words = words.replaceAll(" alt ", " ");
				words = words.replaceAll(" id ", " ");
				words = words.replaceAll(" txt ", " ");
				words = words.replaceAll(" jpg ", " ");
				words = words.replaceAll(" type ", " ");
				words = words.replaceAll(" value ", " ");
				words = words.replaceAll(" label ", " ");
				words = words.replaceAll(" bold ", " ");
				words = words.replaceAll(" src ", " ");
				words = words.replaceAll(" http ", " ");
				words = words.replaceAll(" com ", " ");
				// words = words.replaceAll(" text ", " ");
				words = words.replaceAll(" em ", " ");
				words = words.replaceAll(" form ", " ");
				words = words.replaceAll(" www ", " ");
				words = words.replaceAll(" href ", " ");
				words = words.replaceAll(" ui ", " ");
				words = words.replaceAll(" mw ", " ");
				words = words.replaceAll(" xpath ", " ");
				words = words.replaceAll(" li ", " ");
				words = words.replaceAll(" script ", " ");
				words = words.replaceAll(" ref ", " ");
				words = words.replaceAll(" tr ", " ");
				words = words.replaceAll(" td ", " ");
				// words = words.replaceAll("all", "All");

				// if not part of a previous pattern, these words are also
				// garbage
				words = words.replaceAll(" nav ", " ");
				words = words.replaceAll(" subnav ", " ");

				// remove typeAndWait garbage
				words = words.replaceAll(" ?andwait ", " ");

				// Remove excess spaces
				words = words.trim().replaceAll(" +", " ");

				// write rest of words to line
				line += words + " ";

				// write line to file
				line += "\n";
				output.write(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finished pre-processing.");
	}

	/**
	 * Searches for keyword in words
	 * 
	 * @param words
	 * @param prevKeyword
	 * @param keyword
	 * @param line
	 * @return "" if not found or new camelcase keyword (like clickSubmit)
	 */
	private static String searchAndReturn(String words, String prevKeyword,
			String keyword) {
		String line = "";
		if (words.contains(keyword)) {
			line += prevKeyword + Character.toUpperCase(keyword.charAt(0))
					+ keyword.substring(1) + " ";
		}
		return line;
	}
}
