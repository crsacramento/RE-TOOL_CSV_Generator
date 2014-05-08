package inferrer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file_utilities.Filesystem;
import inferrer.PatternRegister;

public class PatternInferrer {
	private static final String[] LOGIN_STATES = { "LOGIN", "LOGIN_USER",
			"LOGIN_PASS", "LOGIN_USER_PASS", "LOGIN_USER_PASS_SUBMIT" };
	private static final String[] INPUT_STATES = { "INPUT", "INPUT_SUBMIT" };
	private static final String[] SORT_STATES = { "SORT", "SORT_SUBMIT" };
	private static final String[] SEARCH_STATES = { "SEARCH", "SEARCH_SUBMIT" };
	private static final String[][] STATE_ENUM = { LOGIN_STATES, INPUT_STATES,
			SORT_STATES, SEARCH_STATES };

	private static int firstIndex = -1;
	private static int secondIndex = -1;
	private static String currentState = "NONE";
	private static FileWriter output = null;
	private static ArrayList<Integer> lines = new ArrayList<Integer>();
	private static boolean alreadyWroteOnThisLine = false;
	private static int patternIndex = 1;
	private static LinkedHashMap<String, ArrayList<Integer>> patternsFound = new LinkedHashMap<String, ArrayList<Integer>>();

	public static void startInferringProcess() {
		/*
		 * try { System.setOut(new PrintStream(new File("out_p.txt"))); } catch
		 * (FileNotFoundException e1) { e1.printStackTrace(); }
		 */

		// open processed file
		BufferedReader in = null;
		File file = new File("history.csv.processed");
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), /* "UTF8" */"ISO-8859-1"));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(2);
		}

		// open alphabet file

		File actualOutputFile = new File(System.getProperty("user.dir")
				+ File.separatorChar + "patterns.paradigm");
		;

		try {
			output = new FileWriter(actualOutputFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String lineBuffer = "";
		// String line = "";
		int lineNum = 1;
		try {
			while ((lineBuffer = in.readLine()) != null) {
				processLine(lineBuffer, lineNum);
				lineNum++;
				// System.out.println(lineBuffer + ";" + line );
				// System.out.println(line );
				// updateCurrentState();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// write to file
		writeParadigmFile(output);

		// close the streams
		try {
			in.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeParadigmFile(FileWriter output) {
		PatternRegister.initializePatternRegister();
		Iterator<Entry<String, ArrayList<Integer>>> it = patternsFound
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<Integer>> entry = it.next();
			String patternName = entry.getKey().split("_")[0];
			int number = Integer.parseInt(entry.getKey().split("_")[1]);
			PatternRegister.startPattern(patternName, number);

			String[] line = null;
			int start = entry.getValue().get(0);
			int size = entry.getValue().size();

			if (size > 1)
				line = Filesystem.getLinesInFile("history.csv", start, entry
						.getValue().get(entry.getValue().size() - 1));
			else
				line = Filesystem.getLinesInFile("history.csv", start);

			for (int i = 0; i < line.length; ++i) {
				if (line[i] == null)
					continue;
				String[] splits = line[i].split("\t");
				PatternRegister.enterPatternContent(splits[0], splits[1],
						splits[2]);
			}
			PatternRegister.closePattern();
		}
		PatternRegister.endPatternRegister(patternIndex + 1);
	}

	private static void updateCurrentState() {
		if (firstIndex >= 0 && secondIndex >= 0)
			currentState = STATE_ENUM[firstIndex][secondIndex];
		else
			currentState = "NONE";
	}

	private static void processLine(String lineBuffer, int lineNum) {
		ArrayList<String> words = new ArrayList<String>();

		Pattern pattern = Pattern.compile("([0-9]+)?[a-zA-Z]+([0-9]+)?");
		Matcher matcher = pattern.matcher(lineBuffer);
		while (matcher.find()) {
			words.add(matcher.group());
		}

		String line = "";
		String word = "";

		// { LOGIN_STATES, INPUT_STATES, SORT_STATES, SEARCH_STATES };
		if (matchLink(words)) {
			processLink(words, lineNum);
		} else if (matchSubmit(words)) {
			// might be a submit to login, input, search or sort
			switch (firstIndex) {
			case 0:
				processLogin(words, lineNum);
				break;
			case 1:
				processInput(words, lineNum);
				break;
			case 2:
				processSort(words, lineNum);
				break;
			case 3:
				processSearch(words, lineNum);
				break;
			default:
				resetStates();
			}
		} else if (matchSearch(words)) {
			processSearch(words, lineNum);
		} else if (matchSort(words)) {
			processSort(words, lineNum);
		} else if (matchInput(words)) {
			processInput(words, lineNum);
		} else if (matchLogin(words)) {
			processLogin(words, lineNum);
		} else {
			resetStates();
		}
		updateCurrentState();
		if (!alreadyWroteOnThisLine) {
			line = "";
			word = "";
			if (lines.size() > 0)
				for (int i : lines)
					line += i + " ";
			for (String i : words)
				word += i + " ";
			System.out.println(lineNum + ": words:" + word + "|" + currentState
					+ (!line.isEmpty() ? "|lines: " + line : ""));
		} else
			alreadyWroteOnThisLine = false;
	}

	private static void processLink(ArrayList<String> words, int lineNum) {
		// check if trailing sort or search
		if (firstIndex == 2 && secondIndex == 0) {
			// sort without submit is still valid
			patternsFound.put("SORT_" + patternIndex, lines);
			patternIndex++;
		} else if (firstIndex == 3 && secondIndex == 0) {
			// search without submit is still valid
			patternsFound.put("SEARCH_" + patternIndex, lines);
			patternIndex++;
		}

		System.out.println(lineNum + ": CALL");
		ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(lineNum);
		patternsFound.put("CALL_" + patternIndex, a);
		patternIndex++;
		alreadyWroteOnThisLine = true;
		resetStates();
	}

	private static void resetStates() {
		firstIndex = -1;
		secondIndex = -1;
		updateCurrentState();
		lines = new ArrayList<Integer>();
	}

	private static void setStates(int first, int second) {
		firstIndex = first;
		secondIndex = second;
		updateCurrentState();
	}

	private static void processSearch(ArrayList<String> words, int lineNum) {
		// check if trailing sort or search
		if (firstIndex == 2 && secondIndex == 0) {
			// sort without submit is still valid
			patternsFound.put("SORT_" + patternIndex, lines);
			patternIndex++;
		}

		if (matchSubmit(words)) {
			if (firstIndex == 3) {
				if (secondIndex == 0) {
					// full search
					setStates(firstIndex, 1);
					lines.add(lineNum);
					updateCurrentState();

					String line = "";
					for (int i : lines)
						line += i + " ";
					System.out.println(lineNum + ": " + currentState
							+ "|lines: " + line);
					patternsFound.put("SEARCH_" + patternIndex, lines);
					patternIndex++;
					alreadyWroteOnThisLine = true;
					resetStates();
				}
			} else {
				System.out.println(lineNum + "|invalid state: expected 3 got "
						+ firstIndex);
			}
		} else if (matchSearch(words)) {
			lines.add(lineNum);
			setStates(3, 0);
		} else {
			System.out.println("SEARCH:error invalid input");
			resetStates();
		}
	}

	private static void processSort(ArrayList<String> words, int lineNum) {
		// check if trailing sort or search
		if (firstIndex == 3 && secondIndex == 0) {
			// search without submit is still valid
			patternsFound.put("SEARCH_" + patternIndex, lines);
			patternIndex++;
		}

		if (matchSubmit(words)) {
			if (firstIndex == 2) {
				if (secondIndex == 0) {
					// full search
					setStates(firstIndex, 1);
					lines.add(lineNum);
					updateCurrentState();

					String line = "";
					for (int i : lines)
						line += i + " ";
					System.out.println(lineNum + ": " + currentState
							+ "|lines:" + line);
					patternsFound.put("SORT_" + patternIndex, lines);
					patternIndex++;
					alreadyWroteOnThisLine = true;
					resetStates();
				}
			} else {
				System.out.println("SORT: invalid state: expected 2 got "
						+ firstIndex);

			}
		} else if (matchSort(words)) {
			lines.add(lineNum);
			setStates(2, 0);
		} else {
			System.out.println("SORT:error invalid input");
			resetStates();
		}
	}

	private static void processInput(ArrayList<String> words, int lineNum) {
		// check if trailing sort or search
		if (firstIndex == 2 && secondIndex == 0) {
			// sort without submit is still valid
			patternsFound.put("SORT_" + patternIndex, lines);
			patternIndex++;
		} else if (firstIndex == 3 && secondIndex == 0) {
			// search without submit is still valid
			patternsFound.put("SEARCH_" + patternIndex, lines);
			patternIndex++;
		}

		if (matchSubmit(words)) {
			if (firstIndex == 1) {
				if (secondIndex == 0) {
					// full search
					setStates(firstIndex, 1);
					lines.add(lineNum);
					updateCurrentState();

					String line = "";
					for (int i : lines)
						line += i + " ";
					System.out.println(lineNum + ": " + currentState
							+ "|lines: " + line);
					patternsFound.put("INPUT_" + patternIndex, lines);
					patternIndex++;
					alreadyWroteOnThisLine = true;
					resetStates();
				}
			} else {
				System.out.println("invalid state: expected 2 got "
						+ firstIndex);
			}
		} else if (matchInput(words)) {
			lines.add(lineNum);
			setStates(1, 0);
		} else {
			System.out.println("INPUT:error invalid input");
			resetStates();
		}
	}

	private static void processLogin(ArrayList<String> words, int lineNum) {
		// check if trailing sort or search
		if (firstIndex == 2 && secondIndex == 0) {
			// sort without submit is still valid
			patternsFound.put("SORT_" + patternIndex, lines);
			patternIndex++;
		} else if (firstIndex == 3 && secondIndex == 0) {
			// search without submit is still valid
			patternsFound.put("SEARCH_" + patternIndex, lines);
			patternIndex++;
		}

		// "LOGIN","LOGIN_USER","LOGIN_PASS","LOGIN_USER_PASS","LOGIN_USER_PASS_SUBMIT"
		if (matchSubmit(words)) {
			if (firstIndex == 0) {
				// end of login
				if (secondIndex == 3) {
					// valid
					setStates(0, 3);
					lines.add(lineNum);
					updateCurrentState();

					String line = "";
					for (int i : lines)
						line += i + " ";
					System.out.println(lineNum + ": " + currentState
							+ "|lines:" + line);
					patternsFound.put("LOGIN_" + patternIndex, lines);
					patternIndex++;
					alreadyWroteOnThisLine = true;
					resetStates();
				} else {
					System.out.println("LOGIN: premature submit ");
					resetStates();
				}
			} else {
				System.out.println("LOGIN: invalid state: expected 2 got "
						+ firstIndex);
			}
		} else if (words.get(0).toLowerCase().matches(".*login.*")
				|| words.get(0).toLowerCase().matches(".*auth.*")
				|| words.get(0).toLowerCase().matches(".*captcha.*")) {
			if (firstIndex == 0) {
				// doesnt alter states, can go in any state
				lines.add(lineNum);
			} else {
				// starts login
				lines.add(lineNum);
				setStates(0, 0);
			}
		} else if (words.get(0).toLowerCase().matches(".*user.*")
				|| words.get(0).toLowerCase().matches(".*email.*")) {
			if (firstIndex == 0) {
				if (secondIndex == 0) {// curr state LOGIN
					setStates(0, 1);// LOGIN_USER
					lines.add(lineNum);
				} else if (secondIndex == 1) {// curr state LOGIN_USER
					// duplicate writes on user, state keeps the same
					lines.add(lineNum);
				} else if (secondIndex == 2) {// curr state LOGIN_PASS
					// has password, got user, change state
					lines.add(lineNum);
					setStates(0, 3);// LOGIN_USER_PASS
				} else {// curr state LOGIN_USER_PASSWORD
						// keep same state (TODO verify if this causes problems)
					lines.add(lineNum);
				}
			} else {
				// starts login
				lines.add(lineNum);
				setStates(0, 1);
			}
		} else if (words.get(0).toLowerCase().matches(".*password.*")) {
			if (firstIndex == 0) {
				if (secondIndex == 0) {// curr state LOGIN
					lines.add(lineNum);
					setStates(0, 2); // LOGIN_PASS
				} else if (secondIndex == 1) {// curr state LOGIN_USER
					lines.add(lineNum);
					setStates(0, 3);
				} else if (secondIndex == 2) {// curr state LOGIN_PASS
					// duplicate writes on pass, state keeps the same
					lines.add(lineNum);
				} else {// curr state LOGIN_USER_PASS
					lines.add(lineNum);
				}
			} else {
				// starts login
				lines.add(lineNum);
				setStates(0, 2);// LOGIN_PASS
			}
		} else {
			System.out.println("LOGIN:error invalid input");
			resetStates();
		}
	}

	private static boolean matchSubmit(ArrayList<String> words) {
		return (words.size() == 1 ? false : words.get(0).toLowerCase()
				.matches(".*submit.*")
				&& words.get(1).toLowerCase().equals("pagechange"));
	}

	private static boolean matchSearch(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*search.*");
	}

	private static boolean matchSort(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*sort.*");
	}

	private static boolean matchLink(ArrayList<String> words) {
		return (words.size() == 1 ? false : words.get(0).toLowerCase()
				.matches(".*link.*")
				&& words.get(1).toLowerCase().equals("pagechange"));
	}
	
	private static boolean matchLogin(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*login.*")
				|| words.get(0).toLowerCase().matches(".*user.*")
				|| words.get(0).toLowerCase().matches(".*email.*")
				|| words.get(0).toLowerCase().matches(".*password.*")
				|| words.get(0).toLowerCase().matches(".*auth.*")
				|| words.get(0).toLowerCase().matches(".*captcha.*");
	}

	private static boolean matchInput(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*input.*");
	}

	public static void main(String[] args) {
		startInferringProcess();
	}
}
