package data_gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

import difflib.Patch;
import prev_work.DiffUtility;
import prev_work.Filesystem;
import prev_work.PageInfo;
import prev_work.PatternNonSeleniumFinder;
import prev_work.PatternRegister;
import prev_work.SeleniumHTMLInteraction;
import data_gen.ExtendedSeleniumIDEElement;
import prev_work.TypeActionHandlers;

public class ExtendedCSVGenerator implements NativeKeyListener {
	/** Base URL of website to navigate */
	private static String pageURL = "http://www.amazon.com";
	/** Firefox driver */
	private static FirefoxDriver driver;
	/** Registers the index of the current page that is being visited */
	private static int HTMLFileIndex = 1;
	/** URLs of all pages visited */
	static ArrayList<PageInfo> pageInfo = new ArrayList<PageInfo>();
	/** Relates Selenium Steps with the HTML files being processed */
	private static int correlationBetweenSeleniumStepAndHtmlFiles = 1;
	/** Metrics calculated for each page */
	static ArrayList<Patch> pageChanges = new ArrayList<Patch>();
	/** Selenium actions */
	public static ArrayList<ExtendedSeleniumIDEElement> actions;
	/** Inferred patterns */
	static PatternRegister patterns=new PatternRegister();

	/***************************** AUXILIARY METHODS ************************************/

	/**
	 * Returns the URL of the current page the driver is processing
	 * 
	 * @param Firefox
	 *            driver
	 * @return URL source
	 */
	public static String getPageSource(WebDriver driver) {
		return driver.getPageSource();
	}

	/**
	 * Processes the list of HTML files in HTMLtemp, add calculated metrics by
	 * DiffUtility, saves to correspondent file in HTMLfinal folder
	 */
	private static void ProcessList() {

		for (int i = 2; i <= HTMLFileIndex - 1; i++) {
			pageChanges.add(DiffUtility.differenceBetweenFiles(i - 1, i));
		}

		int file = 2;

		for (Patch patch : pageChanges) {
			String changes = DiffUtility.convertPatchToString(patch);
			Filesystem.saveToFile("final", Integer.toString(file), changes,
					false);
			file++;
		}

	}

	/**
	 * Iterates through Selenium actions
	 * 
	 * @throws IOException
	 */
	private static void ProcessSeleniumActions() throws IOException {
		actions = new ArrayList<ExtendedSeleniumIDEElement>();

		try {
			actions = SeleniumHTMLInteraction.parseTableFromSeleniumHTML();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(10);
		}

		// saves the last indexes of "type" actions in Selenium HTML
		// resets after a clickAndWait action
		// useful for, when a clickAndWait action occurs, to know if there was
		// one or more types before it
		ArrayList<TypeActionHandlers> lastTypeIndexes = new ArrayList<TypeActionHandlers>();

		for (ExtendedSeleniumIDEElement element : actions) {

			if (element.getAction().equals("type")) {
				Boolean exists = false;
				for (int i = 0; i != lastTypeIndexes.size(); i++) {
					// if there is already an input in that textbox and it finds
					// another one
					// the Selenium step index is replaced by the most recent
					// in this way, only the most recent string of each text box
					// is analysed
					if (lastTypeIndexes.get(i).getTextBoxId()
							.equals(element.getLink())) {
						lastTypeIndexes.get(i).setIndex(
								actions.indexOf(element) + 1);
						exists = true;
					}
				}

				// if there is no input on that text box
				// plus one so it corresponds to Selenium steps
				if (!exists)
					lastTypeIndexes.add(new TypeActionHandlers(element
							.getLink(), actions.indexOf(element) + 1));

			}

			if (element.getAction().equals("clickAndWait")) {
				if (!lastTypeIndexes.isEmpty()) {
					SeleniumHTMLInteraction.testForPatterns(actions,
							lastTypeIndexes, actions.indexOf(element) + 1,
							pageInfo);
				}

				// reset lastTypeIndexes array
				lastTypeIndexes.clear();
			}

			// at the end of Selenium steps, checks for types that were not
			// analysed for input patterns
			if (actions.get(actions.size() - 1).equals(element)
					&& element.getAction().equals("clickAndWait") != true) {
				SeleniumHTMLInteraction.testForInputs(actions, lastTypeIndexes,
						actions.indexOf(element) + 1, pageInfo);
			}

		}

	}

	/***************************** AUXILIARY METHODS END ********************************/

	public static void main(String[] args) {
		new File(System.getProperty("user.dir") + "\\HTMLtemp" + "\\").mkdirs();
		new File(System.getProperty("user.dir") + "\\HTMLfinal" + "\\")
				.mkdirs();

		// sets first argument of program as pageURL
		if (args.length > 0)
			pageURL = args[0];

		// the "default" profile is needed because it is the profile where
		// Selenium IDE is installed
		ProfilesIni allProfiles = new ProfilesIni();
		FirefoxProfile profile = allProfiles.getProfile("default");
		// opens Firefox
		driver = new FirefoxDriver(profile);
		// driver.get(pageURL);

		// navigates to given page
		driver.navigate().to(pageURL);

		System.out.println(driver.getCurrentUrl());

		// save initial HTML source
		Filesystem.saveToFile("temp", Integer.toString(HTMLFileIndex),
				getPageSource(driver), false);
		HTMLFileIndex++;
		pageInfo.add(new PageInfo(driver.getCurrentUrl(),
				correlationBetweenSeleniumStepAndHtmlFiles));

		//create the file to save the patterns in PARADIGM syntax
		patterns.initializePatternRegister();
		
		// initializes keyboard event handler
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err
					.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			System.exit(2);
		}

		// Construct the example object and initialize native hook.
		GlobalScreen.getInstance().addNativeKeyListener(
				new ExtendedCSVGenerator());

		// Start GUI
		CounterUpdateDialog.createGUI();
	}

	public static void incrementCorrelation() {
		correlationBetweenSeleniumStepAndHtmlFiles++;
	}

	public static void incrementHTML() {
		// obtains HTML Source code
		Filesystem.saveToFile("temp", Integer.toString(HTMLFileIndex),
				getPageSource(driver), false);
		HTMLFileIndex++;
		correlationBetweenSeleniumStepAndHtmlFiles++;
		// System.out.println("correlation: "+correlationBetweenSeleniumStepAndHtmlFiles);
		pageInfo.add(new PageInfo(driver.getCurrentUrl(),
				correlationBetweenSeleniumStepAndHtmlFiles));
		// pageSourcesTemp.add(getPageSource(driver));
		// Calls screenshot function
	}

	public static void escapeProcess() {
		// when execution trace is finished, list of HTML is processed
		GlobalScreen.unregisterNativeHook();
		ProcessList();
		try {
			ProcessSeleniumActions();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(11);
		}
		PatternNonSeleniumFinder.ProcessUrlsAndHTMLSize(pageInfo);

		PatternRegister.endPatternRegister();
		writeFinalCSV();
	}

	public static int getHTMLFileIndex() {
		return HTMLFileIndex;
	}

	public static int getCorrelationBetweenSeleniumStepAndHtmlFiles() {
		return correlationBetweenSeleniumStepAndHtmlFiles;
	}

	public static void quit() {
		driver.close();
		GlobalScreen.unregisterNativeHook();
		System.exit(0);
	}

	static void writeFinalCSV() {
		// first come the column names
		String content = "action,link,parameter,URL,PRESENT_SORT_KEYWORD,RATIOTOTAL,RATIOPREVIOUS,SELENIUMSTEP,POSSIBLE_SEARCH_TEXT_BOX,POSSIBLE_SEARCH_KEYWORD_IN_URL,SEARCH_WORD_IN_URL,NUMBER_OF_KEYWORD_IN_HTML,INPUT_PATTERN_TEXTBOX,INPUT_PATTERN_TEXT,LOGIN_WORD_IN_URL,POSSIBLE_LOGIN_PASS_TEX_BOX,POSSIBLE_MASTER_DETAIL\n";
		for (int i = 0; i < actions.size(); ++i)
			content += actions.get(i).toString();

		//Filesystem.saveToFile("final", "extended.csv", content, false);
		File file = new File(System.getProperty("user.dir")+"\\HTMLfinal\\extended.csv");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile(),false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/********************* OVERRIDEN METHODS *****************/

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		System.out.println("Key Pressed: "
				+ NativeKeyEvent.getKeyText(e.getKeyCode()));

		if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
			escapeProcess();
		} else if (e.getKeyCode() == NativeKeyEvent.VK_DEAD_ACUTE) {
			incrementHTML();
		} else if (e.getKeyCode() == NativeKeyEvent.VK_CLOSE_BRACKET) {
			incrementCorrelation();
		}

	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
	}
}
