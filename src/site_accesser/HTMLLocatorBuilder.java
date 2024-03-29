package site_accesser;

import org.openqa.selenium.WebElement;

public class HTMLLocatorBuilder {

    static String getElementIdentifier(WebElement e) {
        /*
         * System.out.println(e.getAttribute("id") + "|" +
         * e.getAttribute("class") + "|" + e.getAttribute("name") + "|" +
         * e.getAttribute("label") + "|" + e.getAttribute("href") + "|" +
         * e.getAttribute("type") + "|" + e.getAttribute("title") + "|" +
         * e.getTagName() + "|" + e.getText()+"|");
         */
        String identifier = "";
        // identifier = e.getTagName() + " ";
        // identifier += (getIdIdentifier(e) != null ? getIdIdentifier(e)+" ":""
        // );
        // if (identifier != null) {
        // return identifier;
        // }

        // identifier += (getLinkIdentifier(e) != null ?
        // getLinkIdentifier(e)+" ":"" );
        // if (identifier != null) {
        // return identifier;
        // }

        // identifier += (getNameIdentifier(e) != null ?
        // getNameIdentifier(e)+" ":"" );
        // if (identifier != null) {
        // return identifier;
        // }

        // identifier += (getCSSIdentifier(e) != null ?
        // getCSSIdentifier(e)+" ":"" );
        // if (identifier != null) {
        // return identifier;
        // }

        identifier += getXpathIdentifier(e);
        // if (identifier != null) {
        // return identifier;
        // }
        // search for 'class'
        // id += e.getAttribute("class") + "|";
        // search for 'css'

        // search for 'xpath'

        // else
        // return "tagName:" + e.getTagName();
        return identifier;
    }

    private static String getXpathIdentifier(WebElement e) {
        String[] PREFERRED_ATTRIBUTES = { "id", "class", "name",
                "alt", "src", "href", "type", "action", "onclick",
                "onchange","onfocus","onkeyup","onkeypress" };
        String locator = "//" + e.getTagName();
        boolean first = true;
        boolean openedSquareBracket = false;

        // searches for attributes in array
        for (String attr : PREFERRED_ATTRIBUTES) {
            if (e.getAttribute(attr) != null
                    && !e.getAttribute(attr).matches("^\\s*$")) {
                if (first){
                    locator += '[';
                    openedSquareBracket=true;
                }
                else
                    locator += " and ";

                if (!attr.equals("src"))
                    locator += '@' + attr + "=" + e.getAttribute(attr);
                else
                    locator += " and contains(@src," + e.getAttribute("src")
                            + ")";

                if (first = true) {
                    first = false;
                }
            }
        }

        // if link, get text
        if (e.getTagName().toLowerCase().equals("a")) {
            String text = e.getText();
            if (!text.isEmpty() && !text.matches("^\\s*$")) {
                locator += " and contains(text(),\'"
                        + text.replace("^\\s+", "").replace("\\s+$", "")
                        + "\')";
            }
        }

        if(openedSquareBracket)
            locator += ']';
        locator = locator.replaceAll("\\s+", " ");

        return locator;
    }
    /*
     * private static String getIdIdentifier(WebElement e) { if
     * (e.getAttribute("id") != null) return "id=" + e.getAttribute("id");
     * return null; }
     * 
     * private static String getNameIdentifier(WebElement e) { if
     * (e.getAttribute("name") != null) return "name=" + e.getAttribute("name");
     * return null; }
     * 
     * private static String getLinkIdentifier(WebElement e) { if
     * (e.getTagName().toLowerCase().equals("a")) { String text = e.getText();
     * if (text != null) { // if there's any white space if
     * (!text.matches("^\\s*$")) { return "link=\"" + text.replace("^\\s+",
     * "").replace("\\s+$", "") + "\""; } } } return null; }
     * 
     * private static String getCSSIdentifier(WebElement e) { WebsiteExplorer we
     * = WebsiteExplorer.getInstance(); String sub_path = getCSSSubPath(e);
     * WebElement current = e; WebElement parent =
     * current.findElement(By.xpath(".."));
     * 
     * //List<WebElement> list =
     * FirefoxCrawler.driver.findElements(By.cssSelector(sub_path));
     * //if(list.size() == 1){ //if(list.get(0).toString().equals(e.toString()))
     * //return "css=" + sub_path; //}else{ //if(list.size() > 1){
     * 
     * //} //}
     * 
     * try{ while (!we.getDriver().findElement(By.cssSelector(sub_path))
     * .toString().equals(e.toString()) &&
     * !current.getTagName().toLowerCase().equals("html")) { sub_path =
     * getCSSSubPath(current.findElement(By.xpath(".."))) + " > " + sub_path;
     * current = parent; parent = current.findElement(By.xpath("..")); }
     * }catch(NoSuchElementException exc){ exc.printStackTrace(); return null; }
     * 
     * return "css=" + sub_path; }
     * 
     * private static String getCSSSubPath(WebElement e) { String[]
     * cssAttributes = { "id", "name", "class", "type", "alt", "title", "value"
     * }; for (String attr : cssAttributes) { String value =
     * e.getAttribute(attr); if (value != null && !value.isEmpty()) { if (attr
     * == "id") return '#' + value; if (attr == "class") return
     * e.getTagName().toLowerCase() + '.' + value.replace(" ",
     * ".").replace("..", "."); return e.getTagName().toLowerCase() + '[' + attr
     * + "=\"" + value + "\"]"; } } int n = getNodeNumber(e); if (n != -1)
     * return e.getTagName().toLowerCase() + ":nth-of-type(" + getNodeNumber(e)
     * + ')'; else return e.getTagName().toLowerCase(); }
     * 
     * private static int getNodeNumber(WebElement e) { List<WebElement>
     * childNodes = e.findElements(By.xpath("//*")); int total = 0, index = -1;
     * for (WebElement child : childNodes) { if (child.getTagName() ==
     * e.getTagName()) { if (child == e) { index = total; } total++; } } return
     * index; }
     */
}
