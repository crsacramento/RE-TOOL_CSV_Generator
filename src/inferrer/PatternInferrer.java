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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternInferrer {
	private static final String[] LOGIN_STATES = { "LOGIN", "LOGIN_USER",
			"LOGIN_USER_PASS", "LOGIN_USER_PASS_SUBMIT" };
	private static final String[] INPUT_STATES = { "INPUT", "INPUT_SUBMIT" };
	private static final String[] SORT_STATES = { "SORT", "SORT_SUBMIT" };
	private static final String[] SEARCH_STATES = { "SEARCH", "SEARCH_SUBMIT" };
	// private static final String[] CALL_STATES = { "CALL" };
	private static final String[][] STATE_ENUM = { LOGIN_STATES, INPUT_STATES,
			SORT_STATES, SEARCH_STATES };

	private static int firstIndex = -1;
	private static int secondIndex = -1;
	private static String currentState = "NONE";
	private static FileWriter output = null;

	private static ArrayList<Integer> lines = new ArrayList<Integer>();

	public static void startInferringProcess() {
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
		
		File actualOutputFile = new File(file.getName() + ".patterns");

		try {
			output = new FileWriter(actualOutputFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String lineBuffer = "";
		//String line = "";
		int lineNum = 1;
		try {
			while ((lineBuffer = in.readLine()) != null) {
				processLine(lineBuffer, lineNum);
				lineNum++;
				// output.write(lineBuffer + ";" + line + "\n");
				// output.write(line + "\n");
				// updateCurrentState();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// close the streams
		try {
			in.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void updateCurrentState() {
		if (firstIndex > 0 && secondIndex > 0)
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
		}
	}

	private static boolean matchLogin(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*login.*")
				|| words.get(0).toLowerCase().matches(".*user.*")
				|| words.get(0).toLowerCase().matches(".*email.*");
	}

	private static boolean matchInput(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*input.*");
	}

	private static void processLink(ArrayList<String> words, int lineNum) {
		try {
			output.write(lineNum + ": CALL"+"\n");
		} catch (IOException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
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
					try {
						output.write(lineNum + ": " + currentState + "|lines: "
								+ line+"\n");
					} catch (IOException e) {
						//  Auto-generated catch block
						e.printStackTrace();
					}

					resetStates();
				}
			} else {
				try {
					output.write(lineNum +"|invalid state: expected 3 got "
							+ firstIndex+"\n");
				} catch (IOException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (matchSearch(words)) {
			lines.add(lineNum);
			setStates(3, 0);
		} else {
			try {
				output.write(lineNum +" SEARCH:error invalid input"+"\n");
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
			resetStates();
		}
	}

	private static void processSort(ArrayList<String> words, int lineNum) {
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
					try {
						output.write(lineNum + ": " + currentState
								+ "|lines:" + line+"\n");
					} catch (IOException e) {
						//  Auto-generated catch block
						e.printStackTrace();
					}

					resetStates();
				}
			} else {
				try {
					output.write(lineNum +" invalid state: expected 2 got "
							+ firstIndex+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (matchSort(words)) {
			lines.add(lineNum);
			setStates(2, 0);
			try {
				output.write(lineNum + ": " + currentState+"\n");
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				output.write(lineNum +" SORT:error invalid input"+"\n");
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
			resetStates();
		}
	}

	private static void processInput(ArrayList<String> words, int lineNum) {
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
					try {
						output.write(lineNum + ": " + currentState
								+ "|lines: " + line+"\n");
					} catch (IOException e) {
						//  Auto-generated catch block
						e.printStackTrace();
					}

					resetStates();
				}
			} else {
				try {
					output.write(lineNum +" invalid state: expected 2 got "
							+ firstIndex+"\n");
				} catch (IOException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (matchInput(words)) {
			lines.add(lineNum);
			setStates(1, 0);
			try {
				output.write(lineNum + ": " + currentState+"\n");
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				output.write(lineNum +"INPUT:error invalid input"+"\n");
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
			resetStates();
		}
	}

	private static void processLogin(ArrayList<String> words, int lineNum) {
		// { "LOGIN", "LOGIN_USER","LOGIN_USER_PASS", "LOGIN_USER_PASS_SUBMIT"
		// };
		if (matchSubmit(words)) {
			if (firstIndex == 0) {
				// end of login
				if (secondIndex == 2) {
					// valid
					setStates(0, 3);
					lines.add(lineNum);
					updateCurrentState();

					String line = "";
					for (int i : lines)
						line += i + " ";
					try {
						output.write(lineNum + ": " + currentState
								+ "|lines:" + line+"\n");
					} catch (IOException e) {
						//  Auto-generated catch block
						e.printStackTrace();
					}

					resetStates();
				} else {
					try {
						output.write(lineNum +" LOGIN: premature submit"+"\n");
					} catch (IOException e) {
						//  Auto-generated catch block
						e.printStackTrace();
					}
					resetStates();
				}
			} else {
				try {
					output.write(lineNum +" invalid state: expected 2 got "
							+ firstIndex+"\n");
				} catch (IOException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (words.get(0).toLowerCase().matches(".*login.*")) {
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
				if(secondIndex == 0){// curr state LOGIN
					setStates(0, 1);
					lines.add(lineNum);
				}else if(secondIndex == 1){// curr state LOGIN_USER
					// duplicate writes on user, state keeps the same
					lines.add(lineNum);
				}else{// curr state LOGIN_USER_PASSWORD
					// keep same state ( verify if this causes problems)
					lines.add(lineNum);
				}
			}else{
				try {
					output.write(lineNum +" LOGIN: ERROR: reset|"+words.get(0));
				} catch (IOException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
				resetStates();
			}
		} else if (words.get(0).toLowerCase().matches(".*typePassword.*")){
			if (firstIndex == 0) {
				if(secondIndex == 1){// curr state LOGIN_USER
					lines.add(lineNum);
					setStates(0, 2);
				}else{
					try {
						output.write(lineNum + " premature password\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			try {
				output.write(lineNum +" INPUT:error invalid input"+"\n");
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
			resetStates();
		}
	}

	private static boolean matchSubmit(ArrayList<String> words) {
		return (words.size() == 1 ? false : words.get(0).toLowerCase().matches(".*submit.*")
				&& words.get(1).toLowerCase().equals("pagechange"));
	}

	private static boolean matchSearch(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*search.*");
	}

	private static boolean matchSort(ArrayList<String> words) {
		return words.get(0).toLowerCase().matches(".*sort.*");
	}

	private static boolean matchLink(ArrayList<String> words) {
		return (words.size() == 1 ? false : words.get(0).toLowerCase().matches(".*link.*")
				&& words.get(1).toLowerCase().equals("pagechange"));
	}
	
	public static void main(String[] args) {
		startInferringProcess();
	}
}
