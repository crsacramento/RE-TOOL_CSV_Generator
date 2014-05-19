import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import configuration.Configurator;
import inferrer.PatternInferrer;
import processor.LogProcessor;
import site_accesser.WebsiteExplorer;

public class Main {
    public static void main(String[] args) {
        //String baseURL = "https://www.amazon.com/";
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
        //String baseURL = "http://www.game-debate.com/games/index.php?g_id=625&game=The%20Elder%20Scrolls%20V";
        
        Configurator c = Configurator.getInstance();  
        
        //WebsiteExplorer we = WebsiteExplorer.getInstance();
        //WebsiteExplorer.initialize(baseURL);
        //we.getDriver().get(baseURL);
        //we.exploreWebsite();
        /*WebElement master = we.getDriver().findElement(By.xpath("//select[@name=\"p_make\"]"));
        System.out.println(master.toString()+"\n");
        WebElement form = WebsiteExplorer.findParentForm(master);
        System.out.println(form.toString()+"\n");
        List<String> children = WebsiteExplorer.returnFullTextOfAnElement(form);
        
        Select select = new Select(master);
        List<WebElement> options = master.findElements(By.xpath(".//option"));
        int rand1 = (int) Math.round(Math.random() * (options.size() - 1));
        select.selectByIndex(rand1);

        master = we.getDriver().findElement(By.xpath("//select[@name=\"p_make\"]"));
        System.out.println(master.toString()+"\n");
        WebElement form2 = WebsiteExplorer.findParentForm(master);
        System.out.println(form2.toString()+"\n");
        List<String> children2 = WebsiteExplorer.returnFullTextOfAnElement(form);
        
        if(children.size() != children2.size())
            System.out.println("different size");
        else{
            for(int i = 0; i < children.size();++i){
                if(!children.get(i).equals(children2.get(i))){
                    System.out.println(children.get(i)+"|"+children2.get(i));
                }
            }
        }*/
        /*LogProcessor.processHistoryFile();
        PatternInferrer.setBaseUrl(baseURL);
        PatternInferrer.setMenuElements(we.menuElements);
        PatternInferrer.setMasterElements(we.masterElements);
        PatternInferrer.setDetailElements(we.detailElements);
        PatternInferrer.startInferringProcess();*/
    }
}
