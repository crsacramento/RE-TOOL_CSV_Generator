package site_accesser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
	private String baseUrl = "";

	/** number of actions the crawler will execute before stopping */
	private static final int NUM_ACTIONS = 30;
	/** number of redirects to the home page the crawler will do before stopping */
	private static final int NUM_ERRORS = 5;

	/** words to insert in text input elements */
	private static String[] typedKeywords = { "curtains", "coffee", "phone",
			"shirt", "computer", "dress", "banana", "sandals" };

	/** action history */
	private static ArrayList<SeleniumTableRow> history = new ArrayList<SeleniumTableRow>();

	/** set of visited elements */
	private static Set<String> visitedElements = new HashSet<String>();

	/** set of visited elements */
	public HashMap<String, HashSet<String>> menuElements = new HashMap<String, HashSet<String>>();

	/** URL of current page */
	private String currentPage = baseUrl;

	/** index of current action */
	private static int currAction = 0;

	/** number of times explorer went back to home page */
	private int homeRedirections = 0;

	/** says if politeness delay is to be done (false if error occurs) */
	private static boolean wait = true;

	/** instance of this class (singleton enforcement) */
	private static WebsiteExplorer instance = null;

	/** web driver */
	private WebDriver driver = null;

	/**
	 * Returns web driver.
	 * 
	 * @return driver
	 */
	public WebDriver getDriver() {
		return driver;
	}

	/**
	 * Returns base URL
	 * 
	 * @return base URL
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Singleton enforcement.
	 * 
	 * @return WebsiteExplorer instance
	 */
	public static WebsiteExplorer getInstance() {
		if (instance == null) {
			instance = new WebsiteExplorer();
		}
		return instance;
	}

	/**
	 * Initialize base url.
	 * 
	 * @param URL
	 */
	public static void initialize(String URL) {
		WebsiteExplorer.instance.baseUrl = URL;
	}

	/**
	 * Constructor. Initializes Web driver.
	 */
	private WebsiteExplorer() {
		driver = new HtmlUnitDriver();
	}

	/**
	 * Checks exploring history to see if element e was visited already.
	 * 
	 * @param e
	 *            HTML element
	 * @return element was visited or not
	 */
	public static boolean isElementAlreadyVisited(WebElement e) {
		if (visitedElements.isEmpty())
			return false;
		else {
			// Iterator<Entry<String, WebElement>> it =
			// visitedElements.entrySet().iterator();
			Iterator<String> it = visitedElements.iterator();
			while (it.hasNext()) {
				// Map.Entry<String, WebElement> pair = it.next();
				String pair = it.next();
				if (// p.equals(pair.getKey().toString())&&
				e.toString().equals(pair))
					return true;
			}
		}
		return false;
	}

	/**
	 * Chooses a random element to interact with.
	 * 
	 * @return random element
	 */
	private static WebElement chooseNextElement() {
		String[] TYPES = { "TEXT", "SELECT", "LINKS", "SEARCH", "SORT", "LOGIN" };

		List<ArrayList<WebElement>> list = WebElementOrganizer
				.setupElementList();

		ArrayList<Integer> nonEmpties = new ArrayList<Integer>();
		String type = "";
		int allOptions = 0;

		for (int i = 0; i < list.size(); ++i) {
			if (!list.get(i).isEmpty()) {
				nonEmpties.add(i);
				type += TYPES[i] + "=" + list.get(i).size() + "|";
				allOptions += list.get(i).size();
			}
		}
		System.out.println("non_empty: " + type + "total: " + allOptions);
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
	private static boolean isElementTextInputable(WebElement e) {
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
	private static boolean isElementRelatedToLogin(WebElement e) {
		return e.toString().toLowerCase()
				.matches(".*" + GlobalConstants.loginKeywords + ".*");
	}

	/**
	 * Checks if text input element is expecting numbers
	 * 
	 * @param e
	 *            element to check
	 */
	private static boolean isInputElementExpectingNumbers(WebElement e) {
		String types = "((type=\\\")?number|price|quantity|qty\\s|zip\\s?code)";
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
		// element.toString(),
				HTMLLocatorBuilder.getElementIdentifier(element), "EMPTY");
		history.add(row);
		writeToHistoryFile(row, true);
		System.out.println("row: " + row.toString());
		element.click();

		// add visited elements to list
		visitedElements.add(element.toString());
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

		// if form doesn't submit dynamically, submit
		// form manually
		if (!element.toString().matches(".*onchange=.*submit.*")) {

			// verify if input wants numbers or words
			if (isInputElementExpectingNumbers(element)) {
				// it wants an integer value
				int rand1 = (int) Math.round(Math.random() * 100) + 1;
				row = new SeleniumTableRow("type",
				// element.toString(),
						identifier, "\"" + rand1 + "\"");
				System.out.println("row: " + row.toString());
				history.add(row);
				writeToHistoryFile(row, true);
				visitedElements.add(element.toString());

				// interact
				element.sendKeys(Integer.toString(rand1));
			} else {
				// insert random keyword
				int rand1 = (int) Math.round(Math.random()
						* (typedKeywords.length - 1));

				row = new SeleniumTableRow("type",
				// element.toString(),
						identifier, "\"" + typedKeywords[rand1] + "\"");
				System.out.println("row: " + row.toString());
				history.add(row);
				writeToHistoryFile(row, true);
				visitedElements.add(element.toString());

				// interact
				element.sendKeys(typedKeywords[rand1]);
			}

			handleFormSubmission(element);
		} else {
			// add 'andWait' suffix to mark page change

			if (isInputElementExpectingNumbers(element)) {
				// it wants an integer value
				int rand1 = (int) Math.round(Math.random() * 100) + 1;
				row = new SeleniumTableRow("typeAndWait",
				// element.toString(),
						identifier, "\"" + rand1 + "\"");
				System.out.println("row: " + row.toString());
				history.add(row);
				writeToHistoryFile(row, true);
				visitedElements.add(element.toString());
				element.sendKeys(Integer.toString(rand1));
			} else {
				// insert random keyword
				int rand1 = (int) Math.round(Math.random()
						* (typedKeywords.length - 1));

				row = new SeleniumTableRow("typeAndWait",
				// element.toString(),
						identifier, "\"" + typedKeywords[rand1] + "\"");
				System.out.println("row: " + row.toString());
				history.add(row);
				writeToHistoryFile(row, true);

				// add visited elements to list
				visitedElements.add(element.toString());

				// interact
				element.sendKeys(typedKeywords[rand1]);
			}
		}
	}

	/**
	 * Submits a previously visited form.
	 * 
	 * @param element
	 *            element inside the form to be visited.
	 */
	private static void handleFormSubmission(WebElement element) {
		// search for conventional submit elements
		List<WebElement> submit = element.findElements(By
				.xpath("//input[@type='submit']"));
		if (submit.size() > 0) {
			for (WebElement sub : submit)
				System.out.println("SUBMIT: " + sub.toString());
			SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
					// submit.get(0).toString(),
					HTMLLocatorBuilder.getElementIdentifier(submit.get(0)),
					"EMPTY");
			history.add(row);
			writeToHistoryFile(row, true);
			visitedElements.add(submit.get(0).toString());

			// interact
			submit.get(0).click();
		} else {
			// search for elements that dynamically submit forms
			List<WebElement> dynamicSubmits = element.findElements(By
					.xpath("//*[contains(@onclick,'submit')]"));

			if (dynamicSubmits.size() > 0) {
				for (WebElement sub : dynamicSubmits)
					System.out.println("DYN_SUBMIT: " + sub.toString());
				SeleniumTableRow row = new SeleniumTableRow("clickAndWait",
				// dynamicSubmits.get(0).toString(),
						HTMLLocatorBuilder.getElementIdentifier(dynamicSubmits
								.get(0)), "EMPTY");
				history.add(row);
				writeToHistoryFile(row, true);
				// element.submit();
				visitedElements.add(dynamicSubmits.get(0).toString());

				// interact
				dynamicSubmits.get(0).click();
			} else {
				System.out.println("THERE ARE NO SUBMITS");
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

				if (!element.toString().matches(".*onchange=\".*submit\".*")) {
					// if form doesn't submit dynamically, submit
					// form manually
					SeleniumTableRow row = new SeleniumTableRow("select",// element.toString(),
							identifier, "label=\""
									+ options.get(rand1).getText() + '"');
					System.out.println("row: " + row.toString());
					history.add(row);
					writeToHistoryFile(row, true);
					visitedElements.add(element.toString());

					handleFormSubmission(element);
				} else {
					// add 'andWait' suffix to mark page change
					SeleniumTableRow row = new SeleniumTableRow(
							"selectAndWait",// element.toString(),
							identifier, "label=\""
									+ options.get(rand1).getText() + '"');
					System.out.println("row: " + row.toString());
					history.add(row);
					writeToHistoryFile(row, true);
				}
			}
		}
	}

	/**
	 * Visits a login form completely, or clicks on a login link.
	 * 
	 * @param element
	 *            chosen element to visit
	 * @return if login form was visited or not
	 */
	private static boolean processLogin(WebElement element) {
		if (element.getTagName().toLowerCase().equals("input")
				|| element.getTagName().toLowerCase().equals("select")) {
			// login form, fill all form inputs
			WebElement form = findParentForm(element);
			List<WebElement> children = findInputSelectChildNodes(form);
			SeleniumTableRow row = null;

			for (WebElement e : children) {
				if (e.getTagName().toLowerCase().equals("input")) {
					switch (e.getAttribute("type")) {
					case "text":
					case "password":
					case "email":
						int rand = (int) (Math.random() * (typedKeywords.length - 1));
						row = new SeleniumTableRow(
								"type",// e.toString(),
								HTMLLocatorBuilder.getElementIdentifier(e),
								typedKeywords[rand]);
						System.out.println("row: " + row.toString());
						history.add(row);
						writeToHistoryFile(row, true);
						visitedElements.add(e.toString());
						// interact
						e.clear();
						e.sendKeys(typedKeywords[rand]);
						break;
					case "radio":
					case "checkbox":
						row = new SeleniumTableRow(
								"click",// e.toString(),
								HTMLLocatorBuilder.getElementIdentifier(e),
								"EMPTY");
						System.out.println("row: " + row.toString());
						history.add(row);
						writeToHistoryFile(row, true);
						visitedElements.add(e.toString());
						// interact
						e.click();
						break;
					}
				} else if (e.getTagName().toLowerCase().equals("select")) {
					List<WebElement> options = element.findElements(By
							.xpath(".//option"));
					if (options.size() != 0) {
						int rand1 = (int) Math.round(Math.random()
								* (options.size() - 1));
						Select select = new Select(e);
						row = new SeleniumTableRow(
								"select",// e.toString(),
								HTMLLocatorBuilder.getElementIdentifier(e),
								"label=\"" + options.get(rand1).getText() + '"');
						System.out.println("row: " + row.toString());
						history.add(row);
						writeToHistoryFile(row, true);
						visitedElements.add(e.toString());
						// interact
						select.selectByIndex(rand1);
					}
				}
			}

			handleFormSubmission(form);
			return true;
		} else {
			// login link
			processAnchorElements(element);
			return false;
		}
	}

	/**
	 * Given a form element, returns all input and select leaf nodes
	 * 
	 * @param form
	 * @return all input and select leaf nodes
	 */
	private static List<WebElement> findInputSelectChildNodes(WebElement form) {
		List<WebElement> all = form.findElements(By.xpath("*"));
		List<WebElement> descendants = null;
		if (all.isEmpty()) {
			return null;
		}

		List<WebElement> ret = new ArrayList<WebElement>();
		for (WebElement item : all) {
			if (item.getTagName().toLowerCase().equals("input")) {
				switch (item.getAttribute("type")) {
				case "text":
				case "password":
				case "email":
				case "radio":
				case "checkbox": {
					ret.add(item);
				}
				}
			} else if (item.getTagName().toLowerCase().equals("select")) {
				ret.add(item);
			} else {
				descendants = findInputSelectChildNodes(item);
				if (!(descendants == null || descendants.isEmpty())) {
					for (WebElement i : descendants) {
						ret.add(i);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Given a parent element, returns all anchor leaf nodes
	 * 
	 * @param parent
	 *            parent node
	 * @return all anchor leaf nodes
	 */
	private static List<WebElement> findAnchorNodes(WebElement parent) {
		List<WebElement> all = parent.findElements(By.xpath("*"));
		List<WebElement> descendants = null;
		if (all.isEmpty()) {
			return null;
		}

		List<WebElement> ret = new ArrayList<WebElement>();
		for (WebElement item : all) {
			if (item.getTagName().toLowerCase().equals("a")) {
				ret.add(item);
			} else {
				descendants = findAnchorNodes(item);
				if (!(descendants == null || descendants.isEmpty())) {
					for (WebElement i : descendants) {
						ret.add(i);
					}
				}
			}
		}
		return ret;
	}

	private void findMenuElementsInPage() {
		Set<String> retSet = new HashSet<String>();

		// search for <nav> tag
		List<WebElement> masterList = driver.findElements(By.tagName("nav"));
		// search for class, name and id = "[a-zA-z]+[nav][a-zA-z]+"
		masterList.addAll(driver.findElements(By
				.xpath("//*[contains(@class, \"nav\")]")));
		masterList.addAll(driver.findElements(By
				.xpath("//*[contains(@id, \"nav\")]")));
		masterList.addAll(driver.findElements(By
				.xpath("//*[contains(@name, \"nav\")]")));
		// search for class, name and id = "[a-zA-z]+[menu][a-zA-z]+"
		masterList.addAll(driver.findElements(By
				.xpath("//*[contains(@class, \"menu\")]")));
		masterList.addAll(driver.findElements(By
				.xpath("//*[contains(@id, \"menu\")]")));
		masterList.addAll(driver.findElements(By
				.xpath("//*[contains(@name, \"nav\")]")));


		for (WebElement elem : masterList) {
			List<WebElement> children = findAnchorNodes(elem);
			if (!(children == null || children.isEmpty()))
				for (WebElement element : children) {
					if(element.getAttribute("href") != null)
						retSet.add(element.toString());
				}
		}

		for (String s : retSet) {
			if (menuElements.containsKey(s)) {
				menuElements.get(s).add(currentPage);
			} else {
				HashSet<String> temp = new HashSet<String>();
				temp.add(currentPage);
				menuElements.put(s, temp);
			}
			//System.out.println("menu: "+s+"|"+menuElements.get(s).size());
		}
		System.out.println("size:"+menuElements.size());
	}

	/**
	 * Given a WebElement, finds the parent form
	 * 
	 * @param element
	 *            child element node
	 * @return parent form
	 */
	private static WebElement findParentForm(WebElement element) {
		WebElement current = element;
		while (!(current == null
				|| current.getTagName().toLowerCase().equals("form")
				|| current.getTagName().toLowerCase().equals("html") || current
				.getTagName().toLowerCase().equals("body"))) {
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
	private static void writeToHistoryFile(SeleniumTableRow r, boolean append) {
		// Write history to file
		FileWriter output = null;
		try {
			output = new FileWriter(new File(GlobalConstants.HISTORY_FILEPATH),
					append);
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

	/**
	 * returns driver to base url and checks if number of redirections is bigger
	 * than the limit.
	 * 
	 * @param URL
	 *            base URL
	 * @return if exploring cycle should stop or not
	 */
	private boolean goBackToHome(String URL) {
		driver.get(baseUrl);
		SeleniumTableRow row = new SeleniumTableRow("open", URL, "EMPTY");
		history.add(row);
		writeToHistoryFile(row, true);
		wait = true;
		homeRedirections++;
		currAction--;

		if (homeRedirections > NUM_ERRORS) {
			System.out.println("Maximum redirects, stopping.");
			return true;
		} else {
			System.out
					.println("No suitable element found, going back to base URL. "
							+ "Redirects left:"
							+ (NUM_ERRORS - homeRedirections));
			return false;
		}
	}

	public void exploreWebsite() {

		// set logger config
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
				.setLevel(java.util.logging.Level.SEVERE);
		driver.get(baseUrl);

		
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
		String r = baseUrl.replaceFirst(
				"/^(\\w+:\\/\\/[\\w\\.-]+(:\\d+)?)\\/.*/", "");
		SeleniumTableRow row = new SeleniumTableRow("open", r, "EMPTY");
		history.add(row);
		writeToHistoryFile(row, false);
		System.out.println(history.get(0).toString());

		while (currAction < NUM_ACTIONS) {
			currentPage = driver.getCurrentUrl();
			
			System.out.println("Action #" + currAction + " | Page URL is: "
					+ currentPage + "|actions:" + currAction);
			
			// search for menu elements (before the element is visited)
			findMenuElementsInPage();
			
			WebElement element = chooseNextElement();

			if (element == null) {
				// if no element is found, go back to home page
				boolean stop = goBackToHome(r);
				if (stop)
					break;
			} else {

				
				System.out.println("ELEMENT: "
						+ element.toString()
						+ (!element.getText().isEmpty() ? " (has text)"
								: " (no text)"));

				System.out.println("LOCATOR:"
						+ HTMLLocatorBuilder.getElementIdentifier(element));

				if (isElementRelatedToLogin(element)) {
					boolean processedLogin = processLogin(element);
					if (processedLogin)
						goBackToHome(r);
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
						TimeUnit.MILLISECONDS.sleep(750);
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
