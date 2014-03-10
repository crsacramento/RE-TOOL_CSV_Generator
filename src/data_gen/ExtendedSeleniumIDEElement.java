package data_gen;


public class ExtendedSeleniumIDEElement{

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
	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	public String getPRESENT_SORT_KEYWORD() {
		return PRESENT_SORT_KEYWORD;
	}
	public void setPRESENT_SORT_KEYWORD(String pRESENT_SORT_KEYWORD) {
		PRESENT_SORT_KEYWORD = pRESENT_SORT_KEYWORD;
	}
	public String getRATIOTOTAL() {
		return RATIOTOTAL;
	}
	public void setRATIOTOTAL(String rATIOTOTAL) {
		RATIOTOTAL = rATIOTOTAL;
	}
	public String getRATIOPREVIOUS() {
		return RATIOPREVIOUS;
	}
	public void setRATIOPREVIOUS(String rATIOPREVIOUS) {
		RATIOPREVIOUS = rATIOPREVIOUS;
	}
	public String getSELENIUMSTEP() {
		return SELENIUMSTEP;
	}
	public void setSELENIUMSTEP(String sELENIUMSTEP) {
		SELENIUMSTEP = sELENIUMSTEP;
	}
	public String getPOSSIBLE_SEARCH_TEXT_BOX() {
		return POSSIBLE_SEARCH_TEXT_BOX;
	}
	public void setPOSSIBLE_SEARCH_TEXT_BOX(String pOSSIBLE_SEARCH_TEXT_BOX) {
		POSSIBLE_SEARCH_TEXT_BOX = pOSSIBLE_SEARCH_TEXT_BOX;
	}
	public String getPOSSIBLE_SEARCH_KEYWORD_IN_URL() {
		return POSSIBLE_SEARCH_KEYWORD_IN_URL;
	}
	public void setPOSSIBLE_SEARCH_KEYWORD_IN_URL(
			String pOSSIBLE_SEARCH_KEYWORD_IN_URL) {
		POSSIBLE_SEARCH_KEYWORD_IN_URL = pOSSIBLE_SEARCH_KEYWORD_IN_URL;
	}
	public String getSEARCH_WORD_IN_URL() {
		return SEARCH_WORD_IN_URL;
	}
	public void setSEARCH_WORD_IN_URL(String sEARCH_WORD_IN_URL) {
		SEARCH_WORD_IN_URL = sEARCH_WORD_IN_URL;
	}
	public String getNUMBER_OF_KEYWORD_IN_HTML() {
		return NUMBER_OF_KEYWORD_IN_HTML;
	}
	public void setNUMBER_OF_KEYWORD_IN_HTML(String nUMBER_OF_KEYWORD_IN_HTML) {
		NUMBER_OF_KEYWORD_IN_HTML = nUMBER_OF_KEYWORD_IN_HTML;
	}
	
	public String getLOGIN_WORD_IN_URL() {
		return LOGIN_WORD_IN_URL;
	}
	public void setLOGIN_WORD_IN_URL(String lOGIN_WORD_IN_URL) {
		LOGIN_WORD_IN_URL = lOGIN_WORD_IN_URL;
	}
	public String getINPUT_PATTERN_TEXT() {
		return INPUT_PATTERN_TEXT;
	}
	public void setINPUT_PATTERN_TEXT(String iNPUT_PATTERN_TEXT) {
		INPUT_PATTERN_TEXT = iNPUT_PATTERN_TEXT;
	}

	
	public String getINPUT_PATTERN_TEXTBOX() {
		return INPUT_PATTERN_TEXTBOX;
	}
	public void setINPUT_PATTERN_TEXTBOX(String iNPUT_PATTERN_TEXTBOX) {
		INPUT_PATTERN_TEXTBOX = iNPUT_PATTERN_TEXTBOX;
	}
	public String getPOSSIBLE_LOGIN_PASS_TEX_BOX() {
		return POSSIBLE_LOGIN_PASS_TEXT_BOX;
	}
	public void setPOSSIBLE_LOGIN_PASS_TEX_BOX(String pOSSIBLE_LOGIN_PASS_TEX_BOX) {
		POSSIBLE_LOGIN_PASS_TEXT_BOX = pOSSIBLE_LOGIN_PASS_TEX_BOX;
	}
	public String getPOSSIBLE_MASTER_DETAIL() {
		return POSSIBLE_MASTER_DETAIL;
	}
	public void setPOSSIBLE_MASTER_DETAIL(String pOSSIBLE_MASTER_DETAIL) {
		POSSIBLE_MASTER_DETAIL = pOSSIBLE_MASTER_DETAIL;
	}
	//***************************************************************************************************************************************
	// Obligatory attributes
	String action;
	String link;
	String parameter;
	
	// Optional attributes
	String URL = "";
	String PRESENT_SORT_KEYWORD = "";
	String RATIOTOTAL = "";
	String RATIOPREVIOUS =  "";
	String SELENIUMSTEP = "";
	String POSSIBLE_SEARCH_TEXT_BOX = "";
	String POSSIBLE_SEARCH_KEYWORD_IN_URL = "";
	String SEARCH_WORD_IN_URL = "";
	String NUMBER_OF_KEYWORD_IN_HTML = "";
	String INPUT_PATTERN_TEXTBOX = "";
	String INPUT_PATTERN_TEXT = "";
	String LOGIN_WORD_IN_URL = "";
	String POSSIBLE_LOGIN_PASS_TEXT_BOX = "";
	String POSSIBLE_MASTER_DETAIL = "";
	
	public ExtendedSeleniumIDEElement(String action, String link, String parameter) {
		this.action=action;
		this.link= link;
		this.parameter=parameter;
	}
	public String toString(){
		String content = '\"' + action + "\",\"" + link + "\",\"" + (parameter.isEmpty() ? "NA" : parameter )+ "\",";
		content += URL.isEmpty() ? "NA," : '\"' + URL + "\",";
		content += PRESENT_SORT_KEYWORD.isEmpty() ? "NA," : '\"' + PRESENT_SORT_KEYWORD + "\",";
		content += RATIOTOTAL.isEmpty() ? "0," : '\"' + RATIOTOTAL + "\",";
		content += RATIOPREVIOUS.isEmpty() ? "0," :'\"' + RATIOPREVIOUS + "\",";
		content += SELENIUMSTEP.isEmpty() ? "NA," : '\"' + SELENIUMSTEP + "\",";
		content += POSSIBLE_SEARCH_TEXT_BOX.isEmpty() ? "NA," : '\"' + POSSIBLE_SEARCH_TEXT_BOX + "\",";
		content += POSSIBLE_SEARCH_KEYWORD_IN_URL.isEmpty() ? "NA," : '\"' + POSSIBLE_SEARCH_KEYWORD_IN_URL + "\",";
		content += SEARCH_WORD_IN_URL.isEmpty() ? "F," : '\"' + SEARCH_WORD_IN_URL + "\",";
		content += NUMBER_OF_KEYWORD_IN_HTML.isEmpty() ? "0," : '\"' + NUMBER_OF_KEYWORD_IN_HTML + "\",";
		content += INPUT_PATTERN_TEXTBOX.isEmpty() ? "0," : '\"' + INPUT_PATTERN_TEXTBOX + "\",";
		content += INPUT_PATTERN_TEXT.isEmpty() ? "0," : '\"' + INPUT_PATTERN_TEXT + "\",";
		content += LOGIN_WORD_IN_URL.isEmpty() ? "0," : '\"' + LOGIN_WORD_IN_URL + "\",";
		content += POSSIBLE_LOGIN_PASS_TEXT_BOX.isEmpty() ? "F," : '\"' + POSSIBLE_LOGIN_PASS_TEXT_BOX + "\",";
		content += POSSIBLE_MASTER_DETAIL.isEmpty() ? "F" : '\"' + POSSIBLE_MASTER_DETAIL + '\"';//last line, no comma
		
		content += "\n";
		return content;
	}
}
