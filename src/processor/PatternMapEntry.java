package processor;

public class PatternMapEntry {
	private String patternName;
	private String identifyingRegex;

	public PatternMapEntry(String _patternName, String _identifyingRegex) {
		patternName = _patternName;
		identifyingRegex = _identifyingRegex;
	}

	public String getPatternName() {
		return patternName;
	}

	public String getIdentifyingRegex() {
		return identifyingRegex;
	}
}
