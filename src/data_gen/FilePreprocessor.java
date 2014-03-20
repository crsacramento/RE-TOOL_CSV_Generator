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

				// process 'clickAndWait' variants
				if (words.contains("clickandwait")) {
					String result = "";
					Boolean match = false;
					result = searchAndReturn(words, "clickAndWait", "link");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("link", "");
						line += result;
					}
					result = searchAndReturn(words, "clickAndWait", "button");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("button", "");
						line += result;
					}
					result = searchAndReturn(words, "clickAndWait", "submit");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("submit", "");
						// eliminate 'text', not necessary
						words = words.replaceAll("input", "");
						line += result;
					}
					result = searchAndReturn(words, "clickAndWait", "img");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("img", "");
						line += result;
					}
					result = searchAndReturn(words, "clickAndWait", "option");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("option", "");
						line += result;
					}
					result = searchAndReturn(words, "clickAndWait",
							"main page");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("main page", "");
						line += "clickAndWaitHome ";
					}
					result = searchAndReturn(words, "clickAndWait", "home");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("home", "");
						line += result;
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
					result = searchAndReturn(words, "click", "link");
					if (!result.isEmpty()) {
						match = true;
						line += result;

						// Search for "clickLinkNext"
						result = searchAndReturn(words, "", "next");
						if (!result.isEmpty()) {
							words = words.replaceAll("next", "");
							line = line.substring(0, line.length() - 1);
							line += result;
						}
						// Search for "clickLinkPrev"
						result = searchAndReturn(words, "", "prev");
						if (!result.isEmpty()) {
							words = words.replaceAll("prev", "");
							line = line.substring(0, line.length() - 1);
							line += result;
						}
						words = words.replaceAll("link", "");
					}

					result = searchAndReturn(words, "click", "collapse");
					if (!result.isEmpty()) {
						match = true;

						// Search for "clickCollapseButton"
						result = searchAndReturn(words, "", "button");
						if (!result.isEmpty()) {
							words = words.replaceAll("button", "");
							line += "clickCollapseButton ";
						}
						words = words.replaceAll("collapse", "");
					}
					result = searchAndReturn(words, "click", "button");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("button", "");
						line += result;
					}
					result = searchAndReturn(words, "click", "submit");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("submit", "");
						// eliminate 'input', not necessary
						words = words.replaceAll("input", "");
						line += result;
					}
					result = searchAndReturn(words, "click", "img");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("img", "");
						line += result;
					}
					result = searchAndReturn(words, "click", "search");
					if (!result.isEmpty()) {
						match = true;
						line += result;
						words = words.replaceAll("search", "");
					}
					result = searchAndReturn(words, "click", "sort");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("sort", "");
						line += result;
					}
					result = searchAndReturn(words, "click", "persist");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("persist", "");
						line += result;
					}
					result = searchAndReturn(words, "click", "option");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("option", "");
						line += result;
					}
					result = searchAndReturn(words, "click", "next");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("next", "");
						line += result;
					}
					result = searchAndReturn(words, "click", "prev");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("prev", "");
						line += result;
					}

					result = searchAndReturn(words, "click", "nav");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("nav", "");
						line += result;
					}

					result = searchAndReturn(words, "click", "input");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("input", "");
						line += result;
					}

					result = searchAndReturn(words, "click", "action");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("nav", "");
						line += result;
					}

					result = searchAndReturn(words, "click", "edit");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("edit", "");
						line += result;
					}

					result = searchAndReturn(words, "click", "about");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("about", "");
						line += result;
					}

					result = searchAndReturn(words, "click", "home");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("nav", "");
						line += result;
					}
					if (match)
						words = words.replaceAll("click", "");
				} 
				// process 'type' variants
				if (words.contains("type")) {
					boolean match = false;
					String result = "";

					result = searchAndReturn(words, "type", "username");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("username", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "user");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("user", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "password");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("password", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "pass");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("pass", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "search");
					if (!result.isEmpty()) {
						match = true;
						line += result;

						// Search for "typeSearchInput"
						result = searchAndReturn(words, "", "input");
						if (!result.isEmpty()) {
							words = words.replaceAll("input", "");
							line = line.substring(0, line.length() - 1);
							line += result;

						}
						words = words.replaceAll("search", "");
					}

					result = searchAndReturn(words, "type", "input");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("input", "");
						// eliminate 'text', not necessary
						words = words.replaceAll("text", "");
						line += result;
					}

					result = searchAndReturn(words, "type", "email");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("email", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "description");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("description", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "desc");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("desc", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "priority");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("priority", "");
						line += result;
					}
					result = searchAndReturn(words, "type", "date");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("date", "");
						line += result;
					}
					if (match)
						words = words.replaceAll("type", "");
				} 
				// process 'select' variants
				if (words.contains("select")) {
					boolean match = false;
					String result = "";
					result = searchAndReturn(words, "select", "sort");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("sort", "");
						line += result;
					}
					result = searchAndReturn(words, "select", "search");
					if (!result.isEmpty()) {
						match = true;
						words = words.replaceAll("search", "");
						line += result;
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

				// Remove noise words
				words = words.replaceAll("empty", "");
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
				words = words.replaceAll(" text ", " ");
				words = words.replaceAll(" em ", " ");
				words = words.replaceAll(" form ", " ");
				words = words.replaceAll(" www ", " ");
				words = words.replaceAll(" href ", " ");
				words = words.replaceAll(" ui ", " ");
				words = words.replaceAll(" mw ", " ");
				words = words.replaceAll(" xpath ", " ");
				words = words.replaceAll(" li ", " ");
				words = words.replaceAll(" script ", " ");
				//words = words.replaceAll("all", "All");

				// if not part of a previous pattern, these words are also
				// garbage
				words = words.replaceAll(" nav ", " ");
				words = words.replaceAll(" subnav ", " ");

				// remove typeAndWait garbage
				words = words.replaceAll(" andwait ", " ");

				// Remove excess spaces
				words = words.replaceAll(" +", " ");

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
