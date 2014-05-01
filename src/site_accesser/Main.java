package site_accesser;

public class Main {
	// String BASE_URL = "https://www.amazon.com/";
	// String BASE_URL = "https://www.yahoo.com/";
	// String BASE_URL = "http://www.juventude.gov.pt/Paginas/default.aspx";
	// String BASE_URL = "http://www.fe.up.pt/";
	// String BASE_URL = "http://en.wikipedia.org";
	// String BASE_URL = "http://www.ebay.com/";
	// String BASE_URL = "http://www.youtube.com/";

	
	public static void main(String[] args) {
		String baseURL = "http://store.steampowered.com/";
		WebsiteExplorer.initialize(baseURL);
		WebsiteExplorer we = WebsiteExplorer.getInstance();
		we.exploreWebsite();
	}
}
