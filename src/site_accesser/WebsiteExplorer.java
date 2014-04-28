package site_accesser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;

public class WebsiteExplorer {
	/**
	 * the URL where the crawler will start and the domain to which it will
	 * restrict itself
	 */
	static String BASE_URL = "https://www.amazon.com/";
	// static String BASE_URL = "https://www.yahoo.com/";
	// static String BASE_URL =
	// "http://www.juventude.gov.pt/Paginas/default.aspx";
	// static String BASE_URL = "http://www.fe.up.pt/";
	// static String BASE_URL = "http://en.wikipedia.org";
	//static String BASE_URL = "http://www.ebay.com/";
	//static String BASE_URL = "http://www.facebook.com/";
	// static String BASE_URL = "http://store.steampowered.com/";

	/** keywords that identify search elements */
	static String searchKeywords = "(\"q\"|query|search|pesq(uisa)*|procura(r)*|busca(dor)*)";

	/** keywords that identify sort elements */
	static String sortKeywords = "(sort|asc\\s|desc\\s)";

	/** keywords that identify login elements */
	static String loginKeywords = "(user(name)*|pass(word)*|email|(sign(ed)?(\\s|_)*(in|out)|log(ged)?(\\s|_)*(in|out)))";
	/** keywords that identify elements that SHOULD NOT BE ACCESSED */
	static String generalWordsToExclude = "(buy|sell|edit|delete|mailto|add\\sto\\scart)";
	/** number of actions the crawler will execute before stopping */
	static final int NUM_ACTIONS = 20;
	/** number of redirects to the home page the crawler will do before stopping */
	static final int NUM_ERRORS = 3;

	/** words to insert in text input elements */
	static String[] typedKeywords = { "curtains", "coffee", "phone", "shirt",
			"computer", "dress", "banana", "sandals" };

	/** crawling history */
	static ArrayList<SeleniumTableRow> history = new ArrayList<SeleniumTableRow>();

	/** list of visited elements in each page (identified by its URL) */
	static HashMap<String, WebElement> visitedElements = new HashMap<String, WebElement>();

	/** URL of current page */
	static String currentPage = "";

	/** index of current action */
	private static int currAction = 0;

	/** says if politeness delay is to be done (false if error occurs) */
	private static boolean wait = true;

	public static WebDriver driver = null;

	/**
	 * Checks crawling history to see if element e was visited in page with URL
	 * p.
	 * 
	 * @param p
	 *            current page URL
	 * @param e
	 *            HTML element
	 * @return element was visited or not
	 */
	static boolean isElementAlreadyVisited(String p, WebElement e) {
		if (visitedElements.isEmpty())
			return false;
		else {
			Iterator<Entry<String, WebElement>> it = visitedElements.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<String, WebElement> pair = it.next();
				if (p.equals(pair.getKey().toString())
						&& e.toString().equals(pair.getValue().toString()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Searches page for all suitable anchor HTML elements.
	 * 
	 * @return list of suitable elements
	 */
	static List<WebElement> getLinks() {

		List<WebElement> links, retList = new ArrayList<WebElement>();

		Pattern allFileExtensions = Pattern
				.compile(".*(\\.(css|js|bmp|gif|jpe?g"
						+ "|png|tiff?|mid|mp2|mp3|mp4"
						+ "|wav|avi|mov|mpeg|ram|m4v|pdf"
						+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

		Pattern startWithSlash = Pattern.compile("(href=\"/.+\")");
		Pattern matchesDomainAndSubdomain = Pattern
				.compile("[^\\.\\/\\s]+\\.\\w+(?=\\/|$)");

		Matcher m = matchesDomainAndSubdomain.matcher(BASE_URL);
		String baseUrlDomainAndSubdomain = "";
		if (m.find())
			baseUrlDomainAndSubdomain = m.group();

		// System.out.println("baseUrlDomainAndSubdomain: "
		// + baseUrlDomainAndSubdomain);
		// get all links that dont go out the website
		links = driver.findElements(By.xpath("//a[@href]"));
		for (WebElement e : links) {
			String lower = e.toString().toLowerCase();

			// link is visible, not a link to a file, and doesn't have login
			// or general keywords
			if (itPassesAllGeneralChecks(e)
					&& !allFileExtensions.matcher(lower).matches()
					&& !lower.matches(".*" + loginKeywords + ".*")) {
				// verify if it belongs to the home page's domain and
				// subdomain, or href starts with a '/', aka a subpage
				m = matchesDomainAndSubdomain.matcher(lower);
				String x = "";
				if (m.find()) {
					x = m.group();

					if (baseUrlDomainAndSubdomain.equals(x)) {
						retList.add(e);
					}
				} else if (startWithSlash.matcher(lower).find()) {
					retList.add(e);
				}
			}
		}

		return retList;
	}

	/**
	 * Searches page for all suitable input HTML elements.
	 * 
	 * @return list of suitable elements
	 */
	static List<WebElement> getTextFields() {
		String annoyingAttributes = "(disabled|readonly)";

		List<WebElement> text, retList = new ArrayList<WebElement>();
		// get all form inputs
		text = driver.findElements(By.xpath("//input[@type='text']"));
		for (WebElement e : text) {
			if (itPassesAllGeneralChecks(e)
					&& !e.toString().toLowerCase()
							.matches(".*" + loginKeywords + ".*")
					&& !e.toString().toLowerCase()
							.matches(".*" + annoyingAttributes + ".*")) {
				retList.add(e);
			}

		}

		// cover HTML5 types
		text = driver.findElements(By.xpath("//input[@type='search']"));
		for (WebElement e : text) {
			if (itPassesAllGeneralChecks(e)
					&& !e.toString().toLowerCase()
							.matches(".*" + loginKeywords + ".*")
					&& !e.toString().toLowerCase()
							.matches(".*" + annoyingAttributes + ".*"))
				retList.add(e);
		}

		text = driver.findElements(By.xpath("//input[@type='number']"));
		for (WebElement e : text) {
			if (itPassesAllGeneralChecks(e)
					&& !e.toString().toLowerCase()
							.matches(".*" + loginKeywords + ".*")
					&& !e.toString().toLowerCase()
							.matches(".*" + annoyingAttributes + ".*"))
				retList.add(e);
		}

		// get all textareas
		text = driver.findElements(By.tagName("textarea"));
		for (WebElement e : text) {
			if (itPassesAllGeneralChecks(e)
					&& !e.toString().toLowerCase()
							.matches(".*" + loginKeywords + ".*")
					&& !e.toString().toLowerCase()
							.matches(".*" + annoyingAttributes + ".*"))
				retList.add(e);
		}

		return retList;
	}

	/**
	 * Does all checks that all elements have to respect (not visited, not
	 * hidden, doesn't have keywords that no element should have)
	 * 
	 * @param e
	 * @return
	 */
	static boolean itPassesAllGeneralChecks(WebElement e) {
		return e.isDisplayed()
				&& !isElementAlreadyVisited(currentPage, e)
				&& !e.toString().toLowerCase()
						.matches(".*" + generalWordsToExclude + ".*");
	}

	/**
	 * Searches page for all suitable select HTML elements.
	 * 
	 * @return list of suitable elements
	 */
	static List<WebElement> getSelectFields() {

		List<WebElement> text, retList = new ArrayList<WebElement>();

		// get all textareas
		text = driver.findElements(By.tagName("select"));
		for (WebElement e : text) {
			if (itPassesAllGeneralChecks(e))
				retList.add(e);
		}

		return retList;
	}

	/**
	 * Searches page for all suitable search-related HTML elements.
	 * 
	 * @return list of suitable elements
	 */
	static ArrayList<WebElement> getSearchFields() {
		List<WebElement> textInputs = getTextFields();
		// List<WebElement> selectInputs = getSelectFields(driver);
		ArrayList<WebElement> searchInputs = new ArrayList<WebElement>();

		Iterator<WebElement> it = textInputs.iterator();
		while (it.hasNext()) {
			WebElement e = it.next();
			if (itPassesAllGeneralChecks(e)
					&& e.toString().toLowerCase()
							.matches(".*" + searchKeywords + ".*")) {
				searchInputs.add(e);
				it.remove();
				break;
			}
		}
		/*
		 * it = selectInputs.iterator(); while (it.hasNext()) { WebElement e =
		 * it.next(); for (String s : searchKeywords) { if
		 * (e.toString().toLowerCase().matches(".*" + s + ".*")) {
		 * searchInputs.add(e); it.remove(); break; } } }
		 */
		return searchInputs;
	}

	/**
	 * Searches page for all suitable sorting-related HTML elements.
	 * 
	 * @param driver
	 * @return list of suitable elements
	 */
	static ArrayList<WebElement> getSortFields() {
		List<WebElement> selectInputs = getSelectFields();
		ArrayList<WebElement> sortInputs = new ArrayList<WebElement>();

		Iterator<WebElement> it = selectInputs.iterator();
		while (it.hasNext()) {
			WebElement e = it.next();
			if (itPassesAllGeneralChecks(e)
					&& e.toString().toLowerCase()
							.matches(".*" + sortKeywords + ".*")) {
				sortInputs.add(e);
				it.remove();
				break;
			}
		}
		return sortInputs;
	}

	/**
	 * Searches page for all suitable login-related HTML elements.
	 * 
	 * @param driver
	 * @return
	 */
	static ArrayList<WebElement> getLoginFields() {
		List<WebElement> textInputs = driver.findElements(By
				.xpath("//input[@type='text']"));
		ArrayList<WebElement> loginInputs = new ArrayList<WebElement>();

		List<WebElement> text = driver.findElements(By
				.xpath("//input[@type='password']"));
		for (WebElement e : text) {
			textInputs.add(e);
		}

		text = driver.findElements(By.xpath("//input[@type='email']"));
		for (WebElement e : text) {
			textInputs.add(e);
		}

		Iterator<WebElement> it = textInputs.iterator();
		while (it.hasNext()) {
			WebElement e = it.next();
			if (itPassesAllGeneralChecks(e)
					&& e.toString().toLowerCase()
							.matches(".*" + loginKeywords + ".*")) {
				loginInputs.add(e);
				it.remove();
				break;
			}
		}
		return loginInputs;
	}

	// -------------------------------------------------------------------------
	/**
	 * Chooses a random element to interact with.
	 * 
	 * @param driver
	 * @return random element
	 */
	static WebElement chooseNextElement() {
		String[] TYPES = { "TEXT", "SELECT", "LINKS", "SEARCH", "SORT", "LOGIN" };

		// Find the text input element by its name
		List<List<WebElement>> list = new ArrayList<List<WebElement>>();

		// Get relevant elements
		list.add(getTextFields());
		list.add(getSelectFields());
		list.add(getLinks());
		list.add(getSearchFields());
		list.add(getSortFields());
		list.add(getLoginFields());

		ArrayList<Integer> nonEmpties = new ArrayList<Integer>();
		String type = "";

		for (int i = 0; i < list.size(); ++i) {
			// type = TYPES[i];
			// System.out.println(i + ": " + type);

			if (!list.get(i).isEmpty()) {
				nonEmpties.add(i);
				type += TYPES[i] + "|";
				// for (WebElement e : list.get(i)) {
				// System.out.println(e.toString());
				// }

			} else
				System.out.println(type + ": empty");
		}
		System.out.println("non_empty: " + type);
		if (nonEmpties.size() != 0) {
			// Choose random element of random list
			int rand1 = (int) Math.round(Math.random()
					* (nonEmpties.size() - 1));
			if (rand1 >= 0) {
				List<WebElement> randChosenList = list.get(nonEmpties
						.get(rand1));
				int rand2 = (int) Math.round(Math.random()
						* (randChosenList.size() - 1));
				if (rand2 >= 0)
					return randChosenList.get(rand2);
				else
					return null;
			} else
				return null;
		} else
			return null;
	}

	/**
	 * Checks if HTML element is an element one can insert text in.
	 * 
	 * @param e
	 *            element to check
	 */
	static boolean isElementTextInputable(WebElement e) {
		if (e.getTagName().toLowerCase().equals("textarea"))
			return true;
		if (e.getTagName().toLowerCase().equals("input")) {
			String types = "(type=\"(text|search|email)\")";
			if (e.toString().matches(".*" + types + ".*"))
				return true;
		}

		return false;
	}

	/**
	 * Checks if HTML element has something to do with logins.
	 * 
	 * @param e
	 *            element to check
	 */
	static boolean isElementRelatedToLogin(WebElement e) {
		return e.toString().toLowerCase()
				.matches(".*" + loginKeywords + ".*");
	}

	/**
	 * Checks if text input element is expecting numbers
	 * 
	 * @param e
	 *            element to check
	 */
	static boolean isInputElementExpectingNumbers(WebElement e) {
		String types = "((type=\\\")*number|price|quantity|qty\\s|zip\\s?code)";
		if (e.toString().matches(".*" + types + ".*"))
			return true;

		return false;
	}

	/**
	 * Does necessary operations to interact with an anchor element
	 * 
	 * @param e
	 *            element to interact with
	 */
	private static void processAnchorElements(WebElement element) {
		// link
		SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
				HTMLLocatorBuilder.getElementIdentifier(element), "EMPTY");
		history.add(row);
		writeToHistoryFile(row, true);
		System.out.println("row: " + row.toString());
		element.click();

		// add visited elements to list
		visitedElements.put(currentPage, element);
	}

	/**
	 * Does necessary operations to interact with an input element
	 * 
	 * @param e
	 *            element to interact with
	 */
	private static void processInputElement(WebElement element) {
		String identifier = HTMLLocatorBuilder.getElementIdentifier(element);
		SeleniumTableRow row;

		// clear inside text
		element.clear();

		// verify if input wants numbers or words
		if (isInputElementExpectingNumbers(element)) {
			// it wants an integer value
			int rand1 = (int) Math.round(Math.random() * 100);
			row = new SeleniumTableRow("type", identifier, "\"" + rand1 + "\"");
			System.out.println("row: " + row.toString());
			history.add(row);
			writeToHistoryFile(row, true);
			element.sendKeys(Integer.toString(rand1));
		} else {
			// insert random keyword
			int rand1 = (int) Math.round(Math.random()
					* (typedKeywords.length - 1));

			row = new SeleniumTableRow("type", identifier, "\""
					+ typedKeywords[rand1] + "\"");
			System.out.println("row: " + row.toString());
			history.add(row);
			writeToHistoryFile(row, true);
			element.sendKeys(typedKeywords[rand1]);
		}

		// add visited elements to list
		visitedElements.put(currentPage, element);

		handleFormSubmission(element);
	}

	private static void handleFormSubmission(WebElement element) {
		// search for conventional submit elements
		List<WebElement> submit = element.findElements(By
				.xpath("//input[@type='submit']"));
		if (submit.size() > 0) {
			for (WebElement sub : submit)
				System.out.println("SELECT:SUBMIT: " + sub.toString());
			SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
					HTMLLocatorBuilder.getElementIdentifier(submit.get(0)),
					"EMPTY");
			history.add(row);
			writeToHistoryFile(row, true);
			// element.submit();
			submit.get(0).click();
			visitedElements.put(currentPage, submit.get(0));
		} else {
			// search for elements that dynamically submit forms
			List<WebElement> dynamicSubmits = element.findElements(By
					.xpath("//*[contains(@onclick,'submit')]"));
			if (dynamicSubmits.size() > 0) {
				for (WebElement sub : dynamicSubmits)
					System.out.println("DYN_SELECT:SUBMIT: " + sub.toString());
				SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
						HTMLLocatorBuilder.getElementIdentifier(dynamicSubmits
								.get(0)), "EMPTY");
				history.add(row);
				writeToHistoryFile(row, true);
				// element.submit();
				dynamicSubmits.get(0).click();
				visitedElements.put(currentPage, dynamicSubmits.get(0));
			}
		}

	}

	/**
	 * Does necessary operations to interact with a dropdown menu element
	 * 
	 * @param element
	 *            to interact with
	 */
	private static void processSelectElement(WebElement element) {
		String identifier = HTMLLocatorBuilder.getElementIdentifier(element);
		Select select = new Select(element);

		// select random option
		List<WebElement> options = element.findElements(By.xpath(".//option"));
		if (options.size() == 0) {
			// invalid, do something else
			currAction--;
			wait = false;
		} else {
			System.out.println("options:" + options.size());
			int rand1 = (int) Math.round(Math.random() * (options.size() - 1));
			System.out.println("OPTION TEXT:" + options.get(rand1).getText());

			if (options.get(rand1).getText().isEmpty()) {
				// invalid, do something else
				currAction--;
				wait = false;
			} else {
				// it's a valid option
				select.selectByIndex(rand1);

				SeleniumTableRow row = new SeleniumTableRow("select",
						identifier, "label=\"" + options.get(rand1).getText()
								+ '"');
				System.out.println("row: " + row.toString());
				history.add(row);
				writeToHistoryFile(row, true);

				if (!element.toString().matches(
						".*" + "onchange=\"this.form.submit();\"" + ".*")) {
					// if form doesn't submit dynamically, submit
					// form manually
					handleFormSubmission(element);
				}
				// add visited elements to list
				visitedElements.put(currentPage, element);
			}
		}
	}

	private static void processLogin(WebElement element) {
		WebElement form = findParentForm(element);
		List<WebElement> children = findInputChildNodes(form);
		SeleniumTableRow row = null;
		
		for (WebElement e : children) {
			if(e.getTagName().toLowerCase().equals("input")) {
		        switch(e.getAttribute("type")) {
		        case "text":
		        case "password": 
		        case "email":
		        	e.clear();
		        	int rand = (int) (Math.random() * (typedKeywords.length - 1));
		        	e.sendKeys(typedKeywords[rand]);
		        	row = new SeleniumTableRow("type",
							HTMLLocatorBuilder.getElementIdentifier(e), 
							typedKeywords[rand]);
					System.out.println("row: " + row.toString());
					history.add(row);
					writeToHistoryFile(row, true);
		            break;
		        case "radio":
		        case "checkbox":
		            e.click();
		            row = new SeleniumTableRow("click",
							HTMLLocatorBuilder.getElementIdentifier(e), 
							"EMPTY");
					System.out.println("row: " + row.toString());
					history.add(row);
					writeToHistoryFile(row, true);
		            break;
		        }
		    }
		    else if(e.getTagName().toLowerCase().equals("select")) {
		    	List<WebElement> options = element.findElements(By.xpath(".//option"));
				if (options.size() != 0) {
					int rand1 = (int) Math.round(Math.random() * (options.size() - 1));
					Select select = new Select(e);
					select.selectByIndex(rand1);
					row = new SeleniumTableRow("select",
							HTMLLocatorBuilder.getElementIdentifier(e), 
							"label=\"" + options.get(rand1).getText()
									+ '"');
					System.out.println("row: " + row.toString());
					history.add(row);
					writeToHistoryFile(row, true);
				}
		    }  
		}
		
		handleFormSubmission(form);
	}

	private static List<WebElement> findInputChildNodes(WebElement form) {
		List<WebElement> all = form.findElements(By.xpath("*"));
		List<WebElement> descendants = null;
		if(all.isEmpty()){
			return null;
		}
		
		List<WebElement> ret = new ArrayList<WebElement>();
		for (WebElement item : all) {
			if (item.getTagName().toLowerCase().equals("input")) {
				 switch(item.getAttribute("type")) {
			        case "text":
			        case "password": 
			        case "email":
			        case "radio":
			        case "checkbox":{
			        	ret.add(item);
			        }
				 }
			}else if (item.getTagName().toLowerCase().equals("select")) {
				ret.add(item);
			}
			else{
				descendants = findInputChildNodes(item);
				if(!(descendants == null || descendants.isEmpty())){
					for (WebElement i : descendants) {
						ret.add(i);
					}
				}
			}
		}
		return ret;
	}

	private static WebElement findParentForm(WebElement element) {
		WebElement current = element;
		while (!(current == null
				|| current.getTagName().toLowerCase().equals("form") || current
				.getTagName().toLowerCase().equals("html"))) {
			current = current.findElement(By.xpath(".."));
		}
		return current;
	}

	/**
	 * Writes crawling history to history file
	 * 
	 * @param r
	 *            action to write
	 * @param append
	 *            true=append to file contents;false=reset file contents
	 */
	static void writeToHistoryFile(SeleniumTableRow r, boolean append) {
		// Write history to file
		FileWriter output = null;
		try {
			output = new FileWriter(new File("history.csv"), append);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			output.write(r.toString() + "\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		int homeRedirections = 0;

		// Create a new instance of the html unit driver
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		driver = new HtmlUnitDriver();

		// set logger config
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
				.setLevel(java.util.logging.Level.SEVERE);
		driver.get(BASE_URL);

		try {
			System.setErr(new PrintStream(new File("err.txt")));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			System.setOut(new PrintStream(new File("out.txt")));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// write 'open' action
		String r = BASE_URL.replaceFirst(
				"/^(\\w+:\\/\\/[\\w\\.-]+(:\\d+)?)\\/.*/", "");
		SeleniumTableRow row = new SeleniumTableRow("open", r, "EMPTY");
		history.add(row);
		writeToHistoryFile(row, false);
		System.out.println(history.get(0).toString());

		while (currAction < NUM_ACTIONS) {
			currentPage = driver.getTitle();
			System.out.println("Action #" + currAction + " | Page title is: "
					+ driver.getTitle());

			WebElement element = chooseNextElement();

			if (element == null) {
				// if no element is found, go back to home page
				driver.get(BASE_URL);
				row = new SeleniumTableRow("open", r, "EMPTY");
				history.add(row);
				writeToHistoryFile(row, true);
				wait = false;
				homeRedirections++;
				currAction--;

				if (homeRedirections > NUM_ERRORS) {
					System.out.println("Maximum redirects, stopping.");
					break;
				} else {
					System.out
							.println("No suitable element found, going back to base URL. "
									+ "Redirects left:"
									+ (NUM_ERRORS - homeRedirections));
				}
			} else {
				System.out.println("ELEMENT: "
						+ element.toString()
						+ "\""
						+ (!element.getText().isEmpty() ? " (text)" : "(no text)"));

				System.out.println("LOCATOR:"
						+ HTMLLocatorBuilder.getElementIdentifier(element));
				if (isElementRelatedToLogin(element)) {
					processLogin(element);
				}// dropdown list
				else if (element.getTagName().toLowerCase().equals("select")) {
					processSelectElement(element);
				} else if (isElementTextInputable(element)) {
					processInputElement(element);
				} else {
					processAnchorElements(element);
				}

				if (wait) {
					// politeness delay
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					wait = true;
				}

				// increments action
				currAction++;
			}
		}

		driver.quit();
	}
}
