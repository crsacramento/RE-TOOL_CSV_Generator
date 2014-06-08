package site_accesser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class WebElementOrganizer {
	private static WebsiteExplorer we = WebsiteExplorer.getInstance();
	
	public static boolean itPassesAllGeneralChecks(WebElement e) {
		return e.isDisplayed()
				&& !we.isElementAlreadyVisited(e)
				&& !e.toString().toLowerCase()
						.matches(".*" + we.getConfigurator().getGeneralWordsToExclude() + ".*")
				&& !e.toString().toLowerCase().matches(".*(disabled|readonly|hidden).*");
	}
	
	private static ArrayList<HashSet<WebElement>> initialSetupElementList(){
		ArrayList<HashSet<WebElement>> elemList = new 
			ArrayList<HashSet<WebElement>>();
		/**
		 * 0 -> input
		 * 1 -> select
		 * 2 -> links
		 * 3 -> search
		 * 4 -> sort 
		 * 5 -> login
		 */
		for(int i = 0; i < 6;++i)
			elemList.add(new HashSet<WebElement>());
		return elemList;
	}

	private static ArrayList<HashSet<WebElement>> distributeElementsOverTheLists(
		HashSet<WebElement> fields, 
		ArrayList<HashSet<WebElement>> masterElemList) {

		ArrayList<HashSet<WebElement>> retList = masterElemList;
		
		/**
		 * 0 -> select
		 * 1 -> input
		 * 2 -> call
		 * 3 -> search
		 * 4 -> sort 
		 * 5 -> login
		 */
		
		// get all form inputs
		for (WebElement e : fields) {
		    String id = HTMLLocatorBuilder.getElementIdentifier(e);
			
            if (itPassesAllGeneralChecks(e)){
				// test for search
				if(id.matches(".*" + we.getConfigurator().getSearchKeywords() + ".*")){
					retList.get(3).add(e);
				}else{
					// test for sort
					if(id.matches(".*" + we.getConfigurator().getSortKeywords() + ".*")){
						retList.get(4).add(e);
					}else{
						// test for login
						if(id.matches(".*" + we.getConfigurator().getLoginKeywords() + ".*")){
							retList.get(5).add(e);
						}else{
							// doesn't go into a pattern, check for element type
							if(e.getTagName().toLowerCase().equals("textarea")
								|| e.getTagName().toLowerCase()
									.equals("input")){
								// text input
								retList.get(1).add(e);
							}
							else if(e.getTagName().toLowerCase()
								.equals("select")){
								// dropdown
								retList.get(0).add(e);
							}
							else /*if(e.getTagName().toLowerCase().equals("a"))*/{
								// link
								retList.get(2).add(e);
							}
						}
					}
				}
			}
		}

		return retList;
	}

	/**
	 * Searches page for all suitable anchor HTML elements.
	 * 
	 * @return list of suitable elements
	 */
	private static List<WebElement> getLinks() {

		List<WebElement> links, retList = new ArrayList<WebElement>();

		Pattern allFileExtensions = Pattern
				.compile(".*(\\.(css|js|bmp|gif|jpe?g"
						+ "|png|tiff?|mid|mp2|mp3|mp4"
						+ "|wav|avi|mov|mpeg|ram|m4v|pdf"
						+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
		// check if relative link
		Pattern startWithSlash = Pattern.compile("(href=\"/.+\")");
		// extracts domain and subdomain from href attr
		Pattern matchesDomainAndSubdomain = Pattern
				.compile("[^\\.\\/\\s]+\\.\\w+(?=\\/|$)");

		Matcher m = matchesDomainAndSubdomain.matcher(we.getBaseUrl());
		String baseUrlDomainAndSubdomain = "";
		if (m.find())
			baseUrlDomainAndSubdomain = m.group();

		// System.out.println("baseUrlDomainAndSubdomain: "
		// + baseUrlDomainAndSubdomain);
		// get all links that dont go out the website
		links = we.getDriver().findElements(By.xpath("//a[@href]"));
		for (WebElement e : links) {
			String lower = e.toString().toLowerCase();

			// link is visible, not a link to a file, and doesn't have login
			// or general keywords
			if (itPassesAllGeneralChecks(e)
					&& e.getAttribute("href") != null
					&& !allFileExtensions.matcher(e.getAttribute("href"))
						.matches()) {
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

	public static ArrayList<HashSet<WebElement>> setupElementList(){
		ArrayList<HashSet<WebElement>> elemList = initialSetupElementList();
		HashSet<WebElement> fields = new HashSet<WebElement>();

		// include elements
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='text']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='number']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='month']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='week']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='range']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='time']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='tel']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='url']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='search']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='password']")));
		fields.addAll(we.getDriver().findElements(By.xpath("//input[@type='email']")));
		fields.addAll(we.getDriver().findElements(By.tagName("textarea")));
		fields.addAll(we.getDriver().findElements(By.tagName("select")));
		// anchor elements need a more rigorous selection process, it's why they 
		// get their own function
		fields.addAll(getLinks());

		
		elemList = distributeElementsOverTheLists(fields, elemList);
			

		return elemList;
	}
}
