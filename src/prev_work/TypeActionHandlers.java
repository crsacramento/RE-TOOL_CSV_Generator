package prev_work;

public class TypeActionHandlers {

	
	//identifies the type action text box
	String textBoxId;
	//index corresponds to Selenium step
	int index;
	
	
	public TypeActionHandlers(String textBoxId, int index) {
		super();
		this.textBoxId = textBoxId;
		this.index = index;
	}


	public String getTextBoxId() {
		return textBoxId;
	}


	public void setTextBoxId(String textBoxId) {
		this.textBoxId = textBoxId;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String b="batatas";
		String[] a=b.split(" ");
		System.out.println(a.length);
		
		String c="daft punk";
		String[] d=c.split(" ");
		System.out.println(d.length);
	}

}
