package prev_work;

import java.io.File;
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

public class BrowserHandler implements NativeKeyListener {

    static WebDriver driver;
    static String pageURL = "http://www.facebook.com";
    // static ArrayList<String> pageSourcesTemp;
    ArrayList<String> pageSources;
    ArrayList<Patch> pageChanges = new ArrayList<Patch>();
    static int HTMLFileIndex = 1;
    // Urls of the pages visited
    static ArrayList<PageInfo> pageInfo = new ArrayList<PageInfo>();
    static int correlationBetweenSeleniumStepAndHtmlFiles = 1;
    static PatternRegister patterns = new PatternRegister();

    public static void main(String[] args) {

        new File(System.getProperty("user.dir") + "\\HTMLtemp" + "\\").mkdirs();
        new File(System.getProperty("user.dir") + "\\HTMLfinal" + "\\")
                .mkdirs();

        // sets first argument of program as pageURL
        if (args.length > 0)
            pageURL = args[0];
        // TODO Auto-generated method stub

        // FirefoxProfile profile = new FirefoxProfile();
        // profile.AddExtension("location xpi of plugin");
        // DesiredCapabilities desiredCapabilities =
        // DesiredCapabilities.firefox();
        // desiredCapabilities.setCapability("firefox_profile", profile);
        // driver = new RemoteWebDriver(desiredCapabilities);

        // fon4dzxw.default

        // the "default" profile is needed because it is the profile where
        // Selenium IDE is installed
        ProfilesIni allProfiles = new ProfilesIni();
        FirefoxProfile profile = allProfiles.getProfile("default");

        // opens Firefox
        driver = new FirefoxDriver(profile);

        // driver.get(pageURL);
        // navigates to given page
        driver.navigate().to(pageURL);

        // pageSourcesTemp=new ArrayList<String>();
        // pageSourcesTemp.add(getPageSource(driver));

        // save initial HTML source
        Filesystem.saveToFile("temp", Integer.toString(HTMLFileIndex),
                getPageSource(driver), false);
        HTMLFileIndex++;
        pageInfo.add(new PageInfo(driver.getCurrentUrl(),
                correlationBetweenSeleniumStepAndHtmlFiles));
        // System.out.println(pageInfo.get(0).getSeleniumStepCorrespondent());

        // create the file to save the patterns in PARADIGM syntax
        patterns.initializePatternRegister();

        // initializes keyboard event handler
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err
                    .println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        // Construct the example object and initialize native hook.
        GlobalScreen.getInstance().addNativeKeyListener(new BrowserHandler());
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        System.out.println("Key Pressed: "
                + NativeKeyEvent.getKeyText(e.getKeyCode()));

        if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
            // when execution trace is finished, list of HTML is processed
            GlobalScreen.unregisterNativeHook();
            ProcessList();
            PatternNonSeleniumFinder.ProcessUrlsAndHTMLSize(pageInfo);
            try {
                ProcessSeleniumActions();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            PatternNonSeleniumFinder.testForMasterDetail();
            patterns.endPatternRegister();

        } else if (e.getKeyCode() == NativeKeyEvent.VK_DEAD_ACUTE) {
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
        } else if (e.getKeyCode() == NativeKeyEvent.VK_CLOSE_BRACKET) {
            correlationBetweenSeleniumStepAndHtmlFiles++;
        }

    }

    private void ProcessSeleniumActions() throws IOException {

        ArrayList<SeleniumIDEElement> actions = new ArrayList<SeleniumIDEElement>();

        try {
            actions = SeleniumHTMLInteraction.parseTableFromSeleniumHTML();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // saves the last indexes of "type" actions in Selenium HTML
        // resets after a clickAndWait action
        // useful for, when a clickAndWait action occurs, to know if there was
        // one or more types before it
        ArrayList<TypeActionHandlers> lastTypeIndexes = new ArrayList<TypeActionHandlers>();

        for (SeleniumIDEElement element : actions) {
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

    @Override
    public void nativeKeyReleased(NativeKeyEvent arg0) {
        // TODO Auto-generated method stub~

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    public static String getPageSource(WebDriver driver) {
        return driver.getPageSource();
    }

    private void ProcessList() {

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

}
