package inferrer;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import site_accesser.WebsiteExplorer;
import configuration.Configurator;

public class PatternRegister {
    private static Configurator conf = Configurator.getInstance();
    private static WebsiteExplorer we = WebsiteExplorer.getInstance();

    static HashMap<String, HashSet<String>> duplicateControl = new HashMap<String, HashSet<String>>();

    public static boolean isAlreadyWritten(String type, String id) {
        if(type == null || id == null)
            return true;
        
        HashSet<String> set = duplicateControl.get(type);
        if (set == null) {
            set = new HashSet<String>();
            set.add(id);
            duplicateControl.put(type, set);
            return false;
        } else {
            return !duplicateControl.get(type).add(id);
        }
    }

    public static void initializePatternRegister(String baseUrl) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
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
                    + "title=\"" + baseUrl + "\" >\n");
            bw.write("\t<nodes xsi:type=\"Paradigm:Init\" name=\"Init\""
                    + " number=\"0\" outgoingLinks=\"//@relations.0\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void startPattern(String patternType, int number) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());

        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            String pattern = "";
            if (patternType.trim().toLowerCase().equals("sort"))
                pattern = "Sort";
            else if (patternType.trim().toLowerCase().equals("login"))
                pattern = "Login";
            else if (patternType.trim().toLowerCase().equals("masterdetail"))
                pattern = "MasterDetail";
            else if (patternType.trim().toLowerCase().equals("input"))
                pattern = "Input";
            else if (patternType.trim().toLowerCase().equals("search"))
                pattern = "Find";
            else if (patternType.trim().toLowerCase().equals("menu"))
                pattern = "Menu";
            else if (patternType.trim().toLowerCase().equals("call"))
                pattern = "Call";

            bw.write("\t<nodes xsi:type=\"Paradigm:" + pattern + "\" name=\""
                    + pattern + "" + number + "\" number=\"" + number
                    + "\" incomingLinks=\"//@relations." + (number - 1)
                    + "\" outgoingLinks=\"//@relations." + (number) + "\">\n");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closePattern() {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());

        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t</nodes>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeConfigurationLine(String field, String value) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());

        String actionType = "inputs ";

        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t\t\t<" + actionType + " field=\"" + field
                    + "\" value=\"" + value.replaceAll("\"", "") + "\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeConfiguration() {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t\t</configurations>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFields(ArrayList<String> names,
            ArrayList<String> ids, boolean putS) {
        if (names.size() != ids.size())
            return;
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            for (int i = 0; i < names.size(); ++i)
                bw.write("\t\t<field" + (putS ? "s" : "") + " name=\""
                        + names.get(i).replaceAll("\"|&|£|»|«|´|`|º|", "")
                        + "\" id=\""
                        + ids.get(i).replaceAll("\"|&|£|»|«|´|`|º|", "")
                        + "\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFields(HashMap<String, String> ids) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            Iterator<Map.Entry<String, String>> it = ids.entrySet().iterator();
            while (it.hasNext()) {
                // Map.Entry<String, WebElement> pair = it.next();
                Map.Entry<String, String> pair = it.next();
                bw.write("\t\t<fields name=\""
                        + pair.getValue().replaceAll("\"|&|£|»|«|´|`|º|", "")
                        + "\" id=\""
                        + pair.getKey().replaceAll("\"|&|£|»|«|´|`|º|", "")
                        + "\"/>\n");
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void openConfiguration(String check, String validity,
            String position, String result, String master, String mappingURL) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
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
                    + (!(position == null || position.isEmpty()) ? "Position=\""
                            + position + "\" "
                            : "")
                    + (!(result == null || position.isEmpty()) ? "result=\""
                            + result + "\" " : "")
                    + (!(master == null || master.isEmpty()) ? "master=\""
                            + master + "\" " : "")
                    + (!(mappingURL == null || mappingURL.isEmpty()) ? "mappingURL=\""
                            + mappingURL + "\" "
                            : "") + ">\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeClosedConfigurationTag(String value, String check) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t\t<configurations "
                    + (!(value == null || value.isEmpty()) ? "value=\""
                            + value.replaceAll("\"", "") + "\" " : "")
                    + (!(check == null || check.isEmpty()) ? "check=\"" + check
                            + "\" " : "") + "/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enterMenuItemContent(String url, HashSet<String> elements) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));
            bw.write("\t\t<page URL=\"" + url + "\">\n");
            for (String s : elements) {
                bw.write("\t\t\t<item element=\"" + s.replaceAll("\"", "\\\"")
                        + "\"/>\n");
            }
            bw.write("\t\t</page>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void endPatternRegister(int number) {

        File file = new File(we.getFilepath() + conf.getPatternsFilepath());

        Writer bw;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file.getAbsoluteFile(), true), "UTF-8"));

            bw.write("\t<nodes xsi:type=\"Paradigm:End\" name=\"End\""
                    + " number=\"" + number
                    + "\" incomingLinks=\"//@relations." + (number - 1) + "\""
                    + "/>\n\n");
            bw.close();
            writeRelations(number);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeRelations(int number) {
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());

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
        File file = new File(we.getFilepath() + conf.getPatternsFilepath());
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

        String check = "StayOnSamePage", validity = "Invalid";

        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        openConfiguration(check, validity, "", "", "", "");
        for (int i = 0; i < actions.size(); ++i) {
            String field = "field_" + i;
            if (targets.get(i).matches(".*(submit).*"))
                continue;
            else if (targets.get(i).matches(".*(user|@type=email).*"))
                field = "username";
            else if (targets.get(i).matches(".*(@type=password).*"))
                field = "password";

            writeConfigurationLine(field, parameters.get(i));
            names.add(field);
            ids.add(targets.get(i));
        }
        closeConfiguration();

        writeFields(names, ids,true);
    }

    public static void enterMasterDetailContent(Set<String> masters,
            Set<String> details, String mappingURL) {

        String check = "Contains1";

        HashMap<String, String> idNameMap = new HashMap<String, String>();

        int numberMaster = 0, numberDetail = 0;
        for (String master : masters) {
            for (String detail : details) {
                // conf master
                if (!idNameMap.containsKey(master)) {
                    idNameMap.put(master, "master_" + numberMaster);
                    numberMaster++;
                }
                openConfiguration(check, "", "", "", idNameMap.get(master),
                        mappingURL);

                // write detail
                if (!idNameMap.containsKey(detail) && detail != null) {
                    idNameMap.put(detail, "detail_" + numberDetail);
                    numberDetail++;
                }
                if (detail != null)
                    writeDetailLine(idNameMap.get(detail));

                closeConfiguration();
            }
        }

        writeFields(idNameMap);
    }

    public static void enterFindContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        String check = "NumberOfResults_more_than", position = "1", result = "10";

        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        openConfiguration(check, "", position, result, "", "");
        for (int i = 0; i < actions.size(); ++i) {
            if (targets.get(i).matches(".*(submit).*"))
                continue;
            writeConfigurationLine("find_" + i, parameters.get(i));
            names.add("find_" + i);
            ids.add(targets.get(i));
        }
        closeConfiguration();

        writeFields(names, ids, true);
    }

    public static void enterSortContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        writeClosedConfigurationTag("", "");
        for (int i = 0; i < actions.size(); ++i) {
            if (targets.get(i).matches(".*(submit).*"))
                continue;
            writeConfigurationLine("sort_" + i, parameters.get(i));
            names.add("sort_" + i);
            ids.add(targets.get(i));
        }

        writeFields(names, ids,true);
    }

    public static void enterCallContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        String check = "";

        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        writeClosedConfigurationTag("", check);
        for (int i = 0; i < actions.size(); ++i) {
            names.add("call_" + i);
            ids.add(targets.get(i));
        }

        writeFields(names, ids,false);
    }

    public static void enterInputContent(ArrayList<String> actions,
            ArrayList<String> targets, ArrayList<String> parameters) {
        if (actions.size() != targets.size()
                || targets.size() != parameters.size()
                || actions.size() != parameters.size())
            return;

        ArrayList<String> names = new ArrayList<String>(), ids = new ArrayList<String>();

        for (int i = 0; i < actions.size(); ++i) {
            if (targets.get(i).matches(".*(submit).*"))
                continue;
            writeClosedConfigurationTag(parameters.get(i), "");
            names.add("input_" + i);
            ids.add(targets.get(i));
        }

        writeFields(names, ids,false);
    }
}
