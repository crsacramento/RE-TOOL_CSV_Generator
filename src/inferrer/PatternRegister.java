package inferrer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import site_accesser.GlobalConstants;

public class PatternRegister {
    public static void initializePatternRegister() {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        // if file doesn't exist, then create it
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bw.write("<Paradigm:Model xmi:version=\"2.0\"\n "
                    + "xmlns:xmi=\"http://www.omg.org/XMI\" "
                    + "xmlns:Paradigm=\"http://www.example.org/Paradigm\"\n"
                    + "title=\"patterns\"/>\n");
            bw.write("\t<node xsi:type=\"Paradigm:Init\" name=\"XInit\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void startPattern(String patternType, int number) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);

        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            if (patternType.trim().toLowerCase().equals("sort"))
                bw.write("\t<node xsi:type=\"Paradigm:Sort\" name=\"Sort"
                        + number + "\">\n");
            else if (patternType.trim().toLowerCase().equals("login"))
                bw.write("\t<node xsi:type=\"Paradigm:Login\" name=\"Login"
                        + number + "\">\n");
            else if (patternType.trim().toLowerCase().equals("masterdetail"))
                bw.write("\t<node xsi:type=\"Paradigm:MasterDetail\" name=\"MasterDetail"
                        + number + "\">\n");
            else if (patternType.trim().toLowerCase().equals("input"))
                bw.write("\t<node xsi:type=\"Paradigm:Input\" name=\"Input"
                        + number + "\">\n");
            else if (patternType.trim().toLowerCase().equals("search"))
                bw.write("\t<node xsi:type=\"Paradigm:Find\" name=\"Find"
                        + number + "\">\n");
            else if (patternType.trim().toLowerCase().equals("menu"))
                bw.write("\t<node xsi:type=\"Paradigm:Menu\" name=\"Menu\">\n");
            else if (patternType.trim().toLowerCase().equals("call"))
                bw.write("\t<node xsi:type=\"Paradigm:Call\" name=\"Call"
                        + number + /* "\" number=\"" + number + */"\">\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closePattern() {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);

        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\t</node>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enterPatternContent(String action, String target,
            String parameter) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\t\t<content action=\"" + action + "\" \n\t\ttarget=\""
                    + target);
            if (parameter.toLowerCase().equals("empty"))
                bw.write("\"/>\n");
            else
                bw.write("\" \n\t\tparameter=\"" + parameter.replaceAll("\"", "") + "\"/>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void enterMenuItemContent(String element, HashSet<String> urls) {
        File file = new File(GlobalConstants.PATTERNS_FILEPATH);
        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\t\t<item element=\"" + element + "\">\n");
            for(String s : urls){
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

        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("\t<node xsi:type=\"Paradigm:End\" name=\"End\"/>\n");
            bw.write("</Paradigm:Model>\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
