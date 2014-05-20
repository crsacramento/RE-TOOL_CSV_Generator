import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import inferrer.PatternInferrer;
import processor.LogProcessor;
import site_accesser.WebsiteExplorer;

public class Main {
    public static void main(String[] args) {
        String baseURL = "";
        if (args.length != 1) {
            System.err
                    .println("Invalid arguments (there should only exist one "
                            + "parameter, \nwhich is the URL of the website to "
                            + "crawl)");
            System.exit(-1);
        }else{
            if(args[0].matches("^(\\w+:\\/\\/[\\w\\.-]+(:\\d+)?)\\/.*"))
                baseURL = args[0];
            else{
                System.err
            .println("Invalid URL (should follow the form: "
                    +"\n[protocol]://[domain list separated by dots]/)"
                    +"\n\texample: http://www.fe.up.pt/");
                System.exit(-1);
            }
        }
        //System.exit(0);

        // baseURL = "https://www.amazon.com/";
        // baseURL = "https://www.yahoo.com/";
        // baseURL = "http://www.juventude.gov.pt/Paginas/default.aspx";
        // baseURL = "http://www.fe.up.pt/";
        // baseURL = "http://en.wikipedia.org";
        // baseURL = "http://www.ebay.com/";
        // baseURL = "http://www.youtube.com/";
        // baseURL = "http://store.steampowered.com/";
        // baseURL = "http://www.geforce.com/";
        // baseURL = "http://www.reddit.com/";
        // baseURL = "http://www.9gag.com/";
        // baseURL = "http://app.rasc.ch/tudu/welcome.action";
        // baseURL =
        // "http://www.game-debate.com/games/index.php?g_id=625&game=The%20Elder%20Scrolls%20V";
baseURL="http://www.amazon.com/s/ref=nb_sb_noss_1?url=search-alias%3Daps&field-keywords=shoes";
        WebsiteExplorer we = WebsiteExplorer.getInstance();
        WebsiteExplorer.initialize(baseURL);
        we.getDriver().get(baseURL);
        we.findMasterDetailElementsInSearchResultPage();
         /* WebElement user = we.getDriver().findElement(By.name("j_username"));
         * WebElement pass = we.getDriver().findElement(By.name("j_password"));
         * 
         * user.sendKeys("testes"); 
         * pass.sendKeys("321abc"); 
         * String prev =
         * we.getDriver().getPageSource(); 
         * user.submit();
         * 
         * String after = we.getDriver().getPageSource();
         * System.out.println(prev.equals(after));
         * System.out.println(we.getDriver
         * ().findElement(By.xpath("//*[@id=\"menuTable\"]")).toString());
         */
        
        //try {
          //  we.exploreWebsite();
        //} catch (Exception e) {
          //  e.printStackTrace();
        //}

        LogProcessor.processHistoryFile();
        PatternInferrer.setBaseUrl(baseURL);
        PatternInferrer.setMenuElements(we.menuElements);
        PatternInferrer.setMasterElements(we.masterElements);
        PatternInferrer.setDetailElements(we.detailElements);
        PatternInferrer.startInferringProcess();
    }
}
