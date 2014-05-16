import inferrer.PatternInferrer;
import processor.LogProcessor;
import site_accesser.GlobalConstants;
import site_accesser.WebsiteExplorer;

public class Main {
    public static void main(String[] args) {
        String baseURL = "https://www.amazon.com/";
        //String baseURL = "https://www.yahoo.com/";
        //String baseURL = "http://www.juventude.gov.pt/Paginas/default.aspx";
        //String baseURL = "http://www.fe.up.pt/";
        //String baseURL = "http://en.wikipedia.org";
        //String baseURL = "http://www.ebay.com/";
        //String baseURL = "http://www.youtube.com/";
        //String baseURL = "http://store.steampowered.com/";
        //String baseURL = "http://www.geforce.com/";
        //String baseURL = "http://www.reddit.com/";
        //String baseURL = "http://www.9gag.com/";
        
        WebsiteExplorer we = WebsiteExplorer.getInstance();
        WebsiteExplorer.initialize(baseURL);
        we.exploreWebsite();
        LogProcessor.processFile(GlobalConstants.HISTORY_FILEPATH);
        PatternInferrer.setBaseUrl(baseURL);
        PatternInferrer.setMenuElements(we.menuElements);
        PatternInferrer.setMasterElements(we.masterElements);
        PatternInferrer.setDetailElements(we.detailElements);
        PatternInferrer.startInferringProcess();
    }
}
