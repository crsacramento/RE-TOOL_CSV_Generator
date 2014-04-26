package processor;

public class PatternMapEntry {
	private String patternName;
	private String identifyingRegex;
	private String garbageRemovalRegex;

	public PatternMapEntry(String _patternName, String _identifyingRegex,
			String _garageRemovalRegex) {
		patternName = _patternName;
		identifyingRegex = _identifyingRegex;
		garbageRemovalRegex = _garageRemovalRegex;
	}

	public String getPatternName() {
		return patternName;
	}

	public String getIdentifyingRegex() {
		return identifyingRegex;
	}

	public String getGarbageRemovalRegex() {
		return garbageRemovalRegex;
	}
}
