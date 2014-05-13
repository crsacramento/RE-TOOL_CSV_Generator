package inferrer;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;

import site_accesser.GlobalConstants;

public class PatternRegister {
    static int pos = 1;

    public static void initializePatternRegister(String baseUrl) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        // if file doesn't exist, then create it
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Writer bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file.getAbsoluteFile(), false),
                    "UTF-8"));
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bw.write("<Paradigm:Model xmi:version=\"2.0\"\n "
                    + "xmlns:xmi=\"http://www.omg.org/XMI\" "
                    + "xmlns:Paradigm=\"http://www.example.org/Paradigm\"\n"
                    + "title=\"" + baseUrl + "\"/>\n");
            bw.write("\t<node xsi:type=\"Paradigm:Init\" name=\"Init\""
                    + " number=\"0\" outgoingLinks=\"//@relations.0\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void startPattern(String patternType, int number) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);

        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            if (patternType.trim().toLowerCase().equals("sort"))
                bw.write("\t<node xsi:type=\"Paradigm:Sort\" name=\"Sort"
                        + number + "\" number=\"" + number
                        + "\" incomingLinks=\"//@relations." + (number - 1)
                        + "\" outgoingLinks=\"//@relations." + (number)
                        + "\">\n");
            else if (patternType.trim().toLowerCase().equals("login"))
                bw.write("\t<node xsi:type=\"Paradigm:Login\" name=\"Login"
                        + number + "\" number=\"" + number
                        + "\" incomingLinks=\"//@relations." + (number - 1)
                        + "\" outgoingLinks=\"//@relations." + (number)
                        + "\">\n");
            else if (patternType.trim().toLowerCase().equals("masterdetail"))
                bw.write("\t<node xsi:type=\"Paradigm:MasterDetail\" name=\"MasterDetail"
                        + number
                        + "\" number=\""
                        + number
                        + "\" incomingLinks=\"//@relations."
                        + (number - 1)
                        + "\" outgoingLinks=\"//@relations."
                        + (number)
                        + "\">\n");
            else if (patternType.trim().toLowerCase().equals("input"))
                bw.write("\t<node xsi:type=\"Paradigm:Input\" name=\"Input"
                        + number + "\" number=\"" + number
                        + "\" incomingLinks=\"//@relations." + (number - 1)
                        + "\" outgoingLinks=\"//@relations." + (number)
                        + "\">\n");
            else if (patternType.trim().toLowerCase().equals("search"))
                bw.write("\t<node xsi:type=\"Paradigm:Find\" name=\"Find"
                        + number + "\" number=\"" + number
                        + "\" incomingLinks=\"//@relations." + (number - 1)
                        + "\" outgoingLinks=\"//@relations." + (number)
                        + "\">\n");
            else if (patternType.trim().toLowerCase().equals("menu"))
                bw.write("\t<node xsi:type=\"Paradigm:Menu\" name=\"Menu\">\n");
            else if (patternType.trim().toLowerCase().equals("call"))
                bw.write("\t<node xsi:type=\"Paradigm:Call\" name=\"Call"
                        + number + "\" number=\"" + number
                        + "\" incomingLinks=\"//@relations." + (number - 1)
                        + "\" outgoingLinks=\"//@relations." + (number)
                        + "\">\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closePattern() {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);

        Writer bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file.getAbsoluteFile(), true),
                    "UTF-8"));
            bw.write("\t</node>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeConfigurationLine(String actionType, String field,
            String value) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        
        if (actionType.toLowerCase().contains("type")) {
            actionType = "inputs ";
        } else if (actionType.toLowerCase().contains("click")) {
            // TODO ask which one it is
            actionType = "clicks ";
        } else if (actionType.toLowerCase().contains("select")) {
            // TODO ask which one it is
            actionType = "selects";
        }
        Writer bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file.getAbsoluteFile(),true), "UTF-8"));
            bw.write("\t\t\t<" + actionType + " field=\"" + field
                    + "\" value=\"" + value + "\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeConfiguration() {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        Writer bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file.getAbsoluteFile(),true), "UTF-8"));
            bw.write("\t\t</configurations>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFields(ArrayList<String> names,
            ArrayList<String> ids) {
        if (names.size() != ids.size())
            return;
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            for (int i = 0; i < names.size(); ++i)
                bw.write("\t\t<fields name=\"" + names.get(i) + "\" id=\""
                        + ids.get(i) + "\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void openConfiguration(String check, String validity,
            String message, String position, String result, String master) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t\t<configurations "
                    + (!(validity == null || validity.isEmpty()) ? "validity=\""
                            + validity + "\" "
                            : "")
                    + (!(check == null || check.isEmpty()) ? "check=\"" + check
                            + "\" " : "")
                    + (!(message == null || message.isEmpty()) ? "message=\""
                            + message + "\" " : "")
                    + (!(position == null || position.isEmpty()) ? "position="
                            + position + " " : "")
                    + (!(result == null || position.isEmpty()) ? "result=\""
                            + result + "\" " : "")
                    + (!(master == null || master.isEmpty()) ? "master=\""
                            + master + "\" " : "") + ">\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enterMenuItemContent(String element, HashSet<String> urls) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t\t<item element=\"" + element + "\">\n");
            for (String s : urls) {
                bw.write("\t\t\t<page URL=\"" + s + "\"/>\n");
            }
            bw.write("\t\t</item>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void endPatternRegister(int number) {

        File file = new File(GlobalConstants.PATTERNS_FILEPATH);

        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));

            bw.write("\t<node xsi:type=\"Paradigm:End\" name=\"End\""
                    + " number=\"" + number
                    + "\" outgoingLinks=\"//@relations." + (number - 1) + "\""
                    + "/>\n\n");
            bw.close();
            writeRelations(number);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeRelations(int number) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);

        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            // enter relations
            // relations range in [0,number-1]
            for (int i = 0; i < number; ++i)
                bw.write("\t<relations xsi:type=\"Paradigm:Sequence\" label=\">>\""
                        + " source=\"//@nodes."
                        + i
                        + "\" destination=\"//@nodes."
                        + (i + 1)
                        + "\""
                        + "/>\n");
            // enter relations
            // relations range in [0,number-1]
            bw.write("</Paradigm:Model>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeDetailLine(String detail) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t\t\t<detail>" + detail + "</detail>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enterLoginContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        String check = "StayOnSamePage", validity = "Invalid", message = "invalid login", position = "\""
                + pos + "\"", result = "", master = "";
        pos++;
        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        openConfiguration(check, validity, message, position, result, master);
        for (int i = 0; i < actions.size(); ++i) {
            writeConfigurationLine(actions.get(i), "field_" + i,
                    parameters.get(i));
            names.add("field_" + i);
            ids.add(targets.get(i));
        }
        closeConfiguration();

        writeFields(names, ids);
    }

    public static void enterMasterDetailContent(String detail, String master,
            String masterLocator, String detailLocator) {

        String check = "Contains1", validity = "", message = "master detail contain check", position = "\""
                + pos + "\"", result = "";
        pos++;
        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        openConfiguration(check, validity, message, position, result, master);
        writeDetailLine(detail);
        names.add(master);
        ids.add(masterLocator);
        names.add(detail);
        ids.add(detailLocator);
        closeConfiguration();

        writeFields(names, ids);
    }

    public static void enterFindContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        String check = "NumberOfResults_more_than", validity = "", message = "check search results", position = "\""
                + pos + "\"", result = "10", master = "";
        pos++;
        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        openConfiguration(check, validity, message, position, result, master);
        for (int i = 0; i < actions.size(); ++i) {
            writeConfigurationLine(actions.get(i), "field_" + i,
                    parameters.get(i));
            names.add("field_" + i);
            ids.add(targets.get(i));
        }
        closeConfiguration();

        writeFields(names, ids);
    }

    public static void enterSortContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        String check = '\"' + parameters.get(0).replaceAll("(label=|\")", "") + '\"', validity = "Invalid", message = "valid sort", position = "\""
                + pos + "\"", result = "", master = "";
        pos++;
        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        openConfiguration(check, validity, message, position, result, master);
        for (int i = 0; i < actions.size(); ++i) {
            writeConfigurationLine(actions.get(i), "field_" + i,
                    parameters.get(i));
            names.add("field_" + i);
            ids.add(targets.get(i));
        }
        closeConfiguration();

        writeFields(names, ids);
    }

    public static void enterCallContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        String check = "", validity = "", message = "call", position = "\""
                + pos + "\"", result = "", master = "";
        pos++;
        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        openConfiguration(check, validity, message, position, result, master);
        for (int i = 0; i < actions.size(); ++i) {
            writeConfigurationLine(actions.get(i), "field_" + i,
                    parameters.get(i));
            names.add("field_" + i);
            ids.add(targets.get(i));
        }
        closeConfiguration();

        writeFields(names, ids);
    }
}
