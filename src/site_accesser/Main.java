package site_accesser;

import org.openqa.selenium.WebElement;

public class Main {
	public static void main(String[] args) {
		String baseURL = "https://www.amazon.com/";
		// String baseURL = "https://www.yahoo.com/";
		// String baseURL = "http://www.juventude.gov.pt/Paginas/default.aspx";
		// String baseURL = "http://www.fe.up.pt/";
		// String baseURL = "http://en.wikipedia.org";
		// String baseURL = "http://www.ebay.com/";
		// String baseURL = "http://www.youtube.com/";
		// String baseURL = "http://store.steampowered.com/";
		WebsiteExplorer we = WebsiteExplorer.getInstance();
		WebsiteExplorer.initialize(baseURL);
		we.getDriver().get(baseURL);
		WebElement e = we.chooseNextElement();
		System.out.println("result: " + (e == null ? "null" : e.toString()));
	}
}
