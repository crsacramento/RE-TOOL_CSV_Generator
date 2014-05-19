import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import inferrer.PatternInferrer;
import processor.LogProcessor;
import site_accesser.WebsiteExplorer;

public class Main {
    public static void main(String[] args) {
        // String baseURL = "https://www.amazon.com/";
        // String baseURL = "https://www.yahoo.com/";
        // String baseURL = "http://www.juventude.gov.pt/Paginas/default.aspx";
        // String baseURL = "http://www.fe.up.pt/";
        // String baseURL = "http://en.wikipedia.org";
        // String baseURL = "http://www.ebay.com/";
        // String baseURL = "http://www.youtube.com/";
        // String baseURL = "http://store.steampowered.com/";
        // String baseURL = "http://www.geforce.com/";
        // String baseURL = "http://www.reddit.com/";
        // String baseURL = "http://www.9gag.com/";
        String baseURL = "http://app.rasc.ch/tudu/welcome.action";
        // String baseURL =
        // "http://www.game-debate.com/games/index.php?g_id=625&game=The%20Elder%20Scrolls%20V";

        WebsiteExplorer we = WebsiteExplorer.getInstance();
        WebsiteExplorer.initialize(baseURL);
        we.getDriver().get(baseURL);
        
        
        WebElement user = we.getDriver().findElement(By.name("j_username"));
        WebElement pass = we.getDriver().findElement(By.name("j_password"));

        user.sendKeys("testes");
        pass.sendKeys("321abc");
        String prev = we.getDriver().getPageSource();
        user.submit();
        
        String after = we.getDriver().getPageSource();
        System.out.println(prev.equals(after));
        System.out.println(we.getDriver().findElement(By.xpath("//*[@id=\"menuTable\"]")).toString());
/*        try {
            we.exploreWebsite();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*LogProcessor.processHistoryFile();
        PatternInferrer.setBaseUrl(baseURL);
        PatternInferrer.setMenuElements(we.menuElements);
        PatternInferrer.setMasterElements(we.masterElements);
        PatternInferrer.setDetailElements(we.detailElements);
        PatternInferrer.startInferringProcess();*/
    }
}
