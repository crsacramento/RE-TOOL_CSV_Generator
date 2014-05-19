package site_accesser;

import configuration.Configurator;

public class SeleniumTableRow {
	private String action, target, value;
	private Configurator c = Configurator.getInstance();
	
	SeleniumTableRow(String a, String t, String v) {
		action = a;
		target = t;
		value = v;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return action + c.getSeparator() + target
				+ c.getSeparator() + value;
	}
}
