package prev_work;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PatternRegister {

    /**
     * @param args
     */

    // If it is Login/Sort/MasterDetail/Search/Menu
    String patternType;
    // Number increments with every pattern
    static double number = 1.1;

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    public PatternRegister() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void initializePatternRegister() {
        File file = new File(System.getProperty("user.dir") + "\\HTML"
                + "final" + "\\" + "patterns.paradigm");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.write("<Paradigm:Model xmi:version=\"2.0\" \n"
                    +"xmlns:xmi=\"http://www.omg.org/XMI\" "
                    +"\nxmlns:Paradigm=\"http://www.example.org/Paradigm\" title=\"patterns\"/>\n");
            bw.write("<nodes xsi:type=\"Paradigm:Init\" name=\"XInit\" number=\"1.0\"/>\n");
            // bw.newLine();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void addPattern(String patternType2) {

        File file = new File(System.getProperty("user.dir") + "\\HTML"
                + "final" + "\\" + "patterns.paradigm");

        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            if (patternType2.equals("Sort"))
                bw.write("<nodes xsi:type=\"Paradigm:Sort\" name=\"Sort"
                        + number + "\" number=\"" + number + "\"/>\n");

            if (patternType2.equals("Login"))
                bw.write("<nodes xsi:type=\"Paradigm:Login\" name=\"Login"
                        + number + "\" number=\"" + number + "\"/>\n");

            if (patternType2.equals("MasterDetail"))
                bw.write("<nodes xsi:type=\"Paradigm:MasterDetail\" name=\"MasterDetail"
                        + number + "\" number=\"" + number + "\"/>\n");

            if (patternType2.equals("Input"))
                bw.write("<nodes xsi:type=\"Paradigm:Input\" name=\"Input"
                        + number + "\" number=\"" + number + "\"/>\n");

            if (patternType2.equals("Search"))
                bw.write("<nodes xsi:type=\"Paradigm:Find\" name=\"Find"
                        + number + "\" number=\"" + number + "\"/>\n");

            // bw.write(patternType2);
            // bw.newLine();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        number += 0.1;
    }

    public void endPatternRegister() {

        File file = new File(System.getProperty("user.dir") + "\\HTML"
                + "final" + "\\" + "patterns.paradigm");

        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("<nodes xsi:type=\"Paradigm:End\" name=\"End\" number=\""
                    + number + "\"/>\n");
            bw.write("</Paradigm:Model>\n");

            // bw.write(patternType2);
            // bw.newLine();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        number += 0.1;
    }
}
