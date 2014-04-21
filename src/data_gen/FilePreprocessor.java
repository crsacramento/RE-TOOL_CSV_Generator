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
	static DicTrie dictionary = new DicTrie();
	
	public static void preprocessFile(String absolutePath) {
		// open reading file in UTF8
		BufferedReader in = null;
		File file = new File(absolutePath);
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), /*"UTF8"*/ "ISO-8859-1"));
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
		//Pattern pattern = Pattern.compile("[A-Z]?[\\p{L}]+");
		//Pattern  pattern = Pattern.compile("[A-Z]?[[\\p{L}]|[0-9]]+");
		 //Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
		Pattern pattern = Pattern.compile("([0-9]+)?[a-zA-Z]+([0-9]+)?");
		Matcher matcher = null;

		try {
			while ((x = in.readLine()) != null) {
				// split by words
				matcher = pattern.matcher(x);
				String words = "";

				// Find all matches

				while (matcher.find()) {
					// Get the matching string, add in lower case
					words += org.apache.commons.lang3.StringUtils.stripAccents(matcher.group().toLowerCase()) + " ";
				}

				String line = "";
				
				//TODO remove
				//output.write("words:"+words+"|");
				
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

					
					words = words.replaceAll("clickandwait", "");
					if (!match)
						line += "clickAndWait ";
					
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

					words = words.replaceAll("click", "");
					if (!match)
						line += "click ";
				}
				// process 'type' variants
				if (words.contains("type")) {
					boolean match = false;
					String result = "";
					String[] keywords = { "username", "user", "password",
							"pass","q", "search","pesquisa", "input", "email", "login",
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
							case "pesquisa":
							case "q":
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
								}else{
									line+="type"+ Character.toUpperCase(keywords[i].charAt(0))
										+ keywords[i].substring(1) + " ";
								}
								break;
							}
							default:
								line += result;
							}
						}
					}

					words = words.replaceAll("type", "");
					if (!match)
						line += "type ";
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
					words = words.replaceAll("select", "");
					if (!match) 
						line += "select ";
				}
				
				// Substitute some Selenium words
				if (words.contains("waitforpopupandwait")){
					line += "waitForPopupAndWait ";
					words = words.replaceAll("waitforpopupandwait",
							"");
				}
				if (words.contains("select window")){
					line += "selectWindow ";
					words = words.replaceAll("select window", "");
				}
				if (words.contains("typeandwait")){
					line += "typeAndWait ";
					words = words.replaceAll("typeandwait", "");
				}
				if (words.contains("assertconfirmation")){
					line += "assertConfirmation ";
					words = words.replaceAll("assertconfirmation",
							"");
				}

				// Remove noise words
				words = words.replaceAll("empty", "");
				words = words.replaceAll(" alt ", " ");
				words = words.replaceAll(" li ", " ");
				words = words.replaceAll(" span ", " ");

				// if not part of a previous pattern, these words are also
				// garbage
				words = words.replaceAll(" nav ", " ");
				words = words.replaceAll(" subnav ", " ");

				// remove typeAndWait garbage
				words = words.replaceAll(" ?andwait ", " ");
				
				// remove single letters
				words = words.replaceAll(" [a-z] ", "");
				
				// Remove excess spaces
				words = words.trim().replaceAll("\\ +", " ");
				
				// remove invalid words
				String[] wordArray = words.split(" ");
				for(String w : wordArray){
					if(!w.matches(".*\\d.*")){
						if(DicTrie.find(w))
							line += w+" ";
						//else line += '\"'+w+"\" ";
						words = words.replaceAll(" "+w+" ", " ");
					}
					else line += '\"'+w+"\" ";
				}

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
	
	public static void main(String[] args) {
		//File[] files = new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\CSVs").listFiles();
		DicTrie.setupTrie("C:\\Users\\gekka_000\\workspace\\re-tool_continued\\dictionaries\\dic_EN.txt");
		DicTrie.setupTrie("C:\\Users\\gekka_000\\workspace\\re-tool_continued\\dictionaries\\pt-PT.txt");
		
		//for(File file : files){
		File file = new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\merge.csv");
			if(!file.isDirectory() && !file.getAbsolutePath().matches("(.)*processed(.)*")){
				System.out.println(file.getName());
				FilePreprocessor.preprocessFile(file.getAbsolutePath());
			}
		//}
	}
}
