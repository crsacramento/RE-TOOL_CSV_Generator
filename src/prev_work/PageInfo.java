package prev_work;

public class PageInfo {

	private String pageURL;
	private int SeleniumStepCorrespondent;
	
	public String getPageURL() {
		return pageURL;
	}

	public void setPageURL(String pageURL) {
		this.pageURL = pageURL;
	}

	public int getSeleniumStepCorrespondent() {
		return SeleniumStepCorrespondent;
	}

	public void setSeleniumStepCorrespondent(int seleniumStepCorrespondent) {
		SeleniumStepCorrespondent = seleniumStepCorrespondent;
	}

	
	
	public PageInfo(String pageURL, int SeleniumStepCorrespondent){
		this.pageURL=pageURL;
		this.SeleniumStepCorrespondent=SeleniumStepCorrespondent;
	}
	
	
	
}
