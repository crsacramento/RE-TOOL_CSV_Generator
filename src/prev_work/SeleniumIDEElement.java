package prev_work;
public class SeleniumIDEElement {

	
	String action;
	String link;
	String parameter;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	public SeleniumIDEElement(String action, String link, String parameter) {
		super();
		this.action = action;
		this.link = link;
		this.parameter = parameter;
	}
	
	
	
	
}
