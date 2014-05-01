package site_accesser;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
		String identifier = getIdIdentifier(e);
		if (identifier != null) {
			return identifier;
		}

		identifier = getLinkIdentifier(e);
		if (identifier != null) {
			return identifier;
		}

		identifier = getNameIdentifier(e);
		if (identifier != null) {
			return identifier;
		}

		identifier = getCSSIdentifier(e);
		if (identifier != null) {
			return identifier;
		}

		identifier = getXpathIdentifier(e);
		if (identifier != null) {
			return identifier;
		}
		// search for 'class'
		// id += e.getAttribute("class") + "|";
		// search for 'css'

		// search for 'xpath'

		else
			return "tagName:" + e.getTagName();
	}

	private static String getIdIdentifier(WebElement e) {
		if (e.getAttribute("id") != null)
			return "id=" + e.getAttribute("id");
		return null;
	}

	private static String getNameIdentifier(WebElement e) {
		if (e.getAttribute("name") != null)
			return "name=" + e.getAttribute("name");
		return null;
	}

	private static String getLinkIdentifier(WebElement e) {
		if (e.getTagName().toLowerCase().equals("a")) {
			String text = e.getText();
			if (text != null) {
				// if there's any white space
				if (!text.matches("^\\s*$")) {
					return "link=\""
							+ text.replace("^\\s+", "").replace("\\s+$", "")
							+ "\"";
				}
			}
		}
		return null;
	}

	private static String getXpathIdentifier(WebElement e) {
		// anchor tags
		if (e.getTagName().toLowerCase().equals("a")) {
			// identify link by text
			String text = e.getText();
			if (!text.isEmpty()) {
				// if there's any white space
				if (!text.matches("^\\s*$")) {
					return "//a[contains(text(),\'"
							+ text.replace("^\\s+", "").replace("\\s+$", "")
							+ "\')]";
				}
			} else if (e.getAttribute("href") != null) {
				String href = e.getAttribute("href");
				// if absolute url (distinction done cuz of IE)
				if (href.matches("^https?:\\/\\/.*")) {
					return "//a[@href=" + href + ']';
				} else {
					return "//a[contains(@href, " + href + ")]";
				}
			}
		}// img tags
		else if (e.getTagName().toLowerCase().equals("img")) {
			if (e.getAttribute("alt") != null) {
				return "//img[@alt=" + e.getAttribute("alt") + "]";
			} else if (e.getAttribute("title") != null) {
				return "//img[@title=" + e.getAttribute("title") + "]";
			} else if (e.getAttribute("src") != null) {
				return "//img[contains(@src," + e.getAttribute("src") + ")]";
			}
		}// go through attributes
		else {
			String[] PREFERRED_ATTRIBUTES = { "id", "name", "value", "type",
					"action", "onclick" };
			String locator = "";
			boolean first = true;

			// searches for attributes in array
			for (String attr : PREFERRED_ATTRIBUTES) {
				if (e.getAttribute(attr) != null
						&& !e.getAttribute(attr).matches("^\\s*$")) {
					if (first)
						locator = "//" + e.getTagName() + '[';
					else
						locator += " and ";

					locator += '@' + attr + "=" + e.getAttribute(attr);
					if (first = true) {
						first = false;
					}
				}
			}

			if (!locator.isEmpty())
				return locator + ']';
		}
		return null;
	}

	private static String getCSSIdentifier(WebElement e) {
		WebsiteExplorer we = WebsiteExplorer.getInstance();
		String sub_path = getCSSSubPath(e);
		WebElement current = e;
		WebElement parent = current.findElement(By.xpath(".."));

		/*List<WebElement> list = FirefoxCrawler.driver.findElements(By.cssSelector(sub_path));
		if(list.size() == 1){
			if(list.get(0).toString().equals(e.toString()))
				return "css=" + sub_path;
		}else{
			if(list.size() > 1){
			
			}
		}*/
		
		try{
			while (!we.getDriver().findElement(By.cssSelector(sub_path))
					.toString().equals(e.toString())
					&& !current.getTagName().toLowerCase().equals("html")) {
				sub_path = getCSSSubPath(current.findElement(By.xpath("..")))
						+ " > " + sub_path;
				current = parent;
				parent = current.findElement(By.xpath(".."));
			}
		}catch(NoSuchElementException exc){
			exc.printStackTrace();
			return null;
		}

		return "css=" + sub_path;
	}

	private static String getCSSSubPath(WebElement e) {
		String[] cssAttributes = { "id", "name", "class", "type", "alt",
				"title", "value" };
		for (String attr : cssAttributes) {
			String value = e.getAttribute(attr);
			if (value != null) {
				if (attr == "id")
					return '#' + value;
				if (attr == "class")
					return e.getTagName().toLowerCase() + '.'
							+ value.replace(" ", ".").replace("..", ".");
				return e.getTagName().toLowerCase() + '[' + attr + "=\""
						+ value + "\"]";
			}
		}
		int n = getNodeNumber(e);
		if (n != -1)
			return e.getTagName().toLowerCase() + ":nth-of-type("
					+ getNodeNumber(e) + ')';
		else
			return e.getTagName().toLowerCase();
	}

	private static int getNodeNumber(WebElement e) {
		List<WebElement> childNodes = e.findElements(By.xpath("//*"));
		int total = 0, index = -1;
		for (WebElement child : childNodes) {
			if (child.getTagName() == e.getTagName()) {
				if (child == e) {
					index = total;
				}
				total++;
			}
		}
		return index;
	}
}
