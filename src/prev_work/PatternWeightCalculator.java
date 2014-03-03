package prev_work;

public class PatternWeightCalculator {

	/**
	 * @param args
	 */
	
	String patternType;
	double currentWeight;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	

	public PatternWeightCalculator(String patternType, double currentWeight) {
		super();
		this.patternType = patternType;
		this.currentWeight = currentWeight;
	}



	public String getPatternType() {
		return patternType;
	}

	public void setPatternType(String patternType) {
		this.patternType = patternType;
	}

	public double getCurrentWeight() {
		return currentWeight;
	}

	public void setCurrentWeight(double currentWeight) {
		this.currentWeight = currentWeight;
	}
	
	public void addToWeight(double number){
		this.currentWeight+=number;
	}
	
	public void checkPattern(){
		if(this.currentWeight>=1.0)
			PatternRegister.addPattern(patternType);
	}

	
}
