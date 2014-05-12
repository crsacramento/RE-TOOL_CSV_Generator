package data_gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlphabetGenerator {
    private static HashMap<String, Integer> alphabet = new HashMap<String, Integer>();
    private static HashMap<Integer, String> invertedAlphabet = new HashMap<Integer, String>();
    private static final int NUM_LINES = 10;

    /**
     * Reads CSV file, fills alphabet hashmap with alphabet, writes line in
     * converted file.
     * 
     * @param absolutePath
     *            path to csv file
     */
    
    static void readFile_WriteIndexFile(String absolutePath) {
        // open reading file
        BufferedReader in = null;
        File file = new File(absolutePath);
        try {
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(2);
        }

        // open alphabet file
        FileWriter output = null;
        String dirName = file.getParentFile().toPath().toAbsolutePath()
                .toString();
        File dir = new File(dirName);
        File actualOutputFile = new File(dir, file.getName() + ".num");

        try {
            output = new FileWriter(actualOutputFile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // updates line to write
        String x = "";
        int lineCounter = 1;
        int lastIndex = 1;

        // Regex
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
        //Pattern pattern = Pattern.compile("(?<!-)[0-9a-zA-Z]+");
        Matcher matcher = null;

        try {
            while ((x = in.readLine()) != null) {
                // Find all words
                matcher = pattern.matcher(x);
                ArrayList<String> words = new ArrayList<String>();

                while (matcher.find()) {
                // Get the matching string
                    String s = matcher.group();
                    words.add(s);
                }
                ArrayList<String> words_ = new ArrayList<String>();
                words_.add(words.get(0));
                if(words.size() >= 2)
                    words_.add(words.get(1));
                words=words_;
                String line = "";

                // Processing regex matches
                for (int i = 0; i < words.size(); ++i) {
                    if (!alphabet.containsKey(words.get(i))) {
                        alphabet.put(words.get(i), lastIndex);
                        ++lastIndex;
                    }
                    //line += alphabet.get(words.get(i)) + " ";
                    line += alphabet.get(words.get(i)) + " -1 ";
                }

                if ((lineCounter % NUM_LINES) == 0){
                    //line += "-1 -2 \n";
                    line += "-2 \n";
                }
                output.write(line);
                
                line = "";
                lineCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(lastIndex + " sequences found.");
        
        try {
            output.write("-1 -2 \n");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void buildInvertedAlphabet() {
        Iterator<Entry<String, Integer>> it = alphabet.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Entry<String, Integer>) it
                    .next();
            invertedAlphabet.put(pairs.getValue(), pairs.getKey());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    static void writeAlphabetFile(String absolutePath) {
        File file = new File(absolutePath);
        String dirName = file.getParentFile().toPath().toAbsolutePath()
                .toString();
        File dir = new File(dirName);
        File alphabetFile = new File(dir, file.getName() + ".alphabet");
        FileWriter alphabetFW = null;
        try {
            alphabetFW = new FileWriter(alphabetFile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Iterator<Entry<String, Integer>> it = alphabet.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Entry<String, Integer>) it
                    .next();
            try {
                alphabetFW.write(pairs.getKey() + " => " + pairs.getValue()
                        + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            alphabetFW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void readResultFile_produceTranslatedFile(String absolutePath) {
        BufferedReader in = null;
        File file = new File(absolutePath);
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        try {
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(2);
        }

        // open alphabet file
        FileWriter output = null;
        String dirName = file.getParentFile().toPath().toAbsolutePath()
                .toString();
        File dir = new File(dirName);
        File actualOutputFile = new File(dir, file.getName() + ".translated");

        try {
            output = new FileWriter(actualOutputFile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // updates line to write
        String x = "";

        try {
            while ((x = in.readLine()) != null) {
                // check for #
                if (x.indexOf('#') != -1)
                    x = x.substring(0, x.indexOf("SUP") - 1)
                            + x.substring(x.indexOf("SUP"));
                String support = x.substring(x.indexOf("SUP"));
                x = x.substring(0, x.indexOf("SUP"));

                String[] parcels = x.split(" ");

                String line = "";
                for (int i = 0; i < parcels.length; ++i) {
                    // Sequential rules arrow
                    if (parcels[i].contains("==>"))
                        line += parcels[i] + " ";
                    // Sequential rules comma
                    else if (parcels[i].contains(",")) {
                        String[] numbers = parcels[i].split(",");
                        for (int j = 0; j < numbers.length; ++j) {
                            if (invertedAlphabet.containsKey(Integer
                                    .parseInt(numbers[j])))
                                line += invertedAlphabet.get(Integer
                                        .parseInt(numbers[j]));
                            if (j != numbers.length - 1)
                                line += ", ";
                            else
                                line += " ";
                        }

                    }
                    // Regular number
                    else if (invertedAlphabet.containsKey(Integer
                            .parseInt(parcels[i]))) {
                        line += invertedAlphabet.get(Integer
                                .parseInt(parcels[i])) + " ";
                    }
                }

                line += support + "\n";
                output.write(line);
                line = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("resource")
    static void produceSpaceFilesAndRunAlgorithm(String absolutePath) {
        readFile_WriteIndexFile(absolutePath);
        writeAlphabetFile(absolutePath);

        String[] algorithms = { "PrefixSpan", "GSP", "CM-SPADE", "CM-SPAM",
                "CM-ClaSP", "BIDE+"
        // "CloSpan", 
        // "MaxSP",
        // "VMSP",
        // "TKS",
        // "TSP_nonClosed",
        // "CMRules",
        // "CMDeo",
        // "RuleGrowth"
        };

        String[] results = { ".prefixspan.result", ".gsp.result",
                ".spade.result", ".spam.result",
                ".clasp.result",
                ".bide+.result"
        //".clospan.result",
        // "maxsp.result",
        // ".vmsp.result",
        // ".tks.result",
        // ".tspNonClosed.result",
        // ".cmRules.result",
        // ".cmDeo.result",
        // ".ruleGrowth.result"
        };

        for (int i = 0; i < algorithms.length; ++i) {
            String command = "java -jar "
                    + "C:\\Users\\gekka_000\\workspace\\re-tool_continued\\alphabets\\spmf.jar "
                    + "run "
                    + algorithms[i]
                    + " "
                    + absolutePath + ".num "
                    + absolutePath + results[i] 
                    + " 40% ";

            //if (i == 0 || i == 1 || i == 2 || i == 3 || i == 5)
            if(algorithms[i].equals("PrefixSpan") ||
                    algorithms[i].equals("GSP") ||
                    algorithms[i].equals("CM-SPADE") ||
                    algorithms[i].equals("CM-SPAM"))
                // max sequence length
                command += " 10 ";
            // else if(i == 10) //K for TSP_nonClosed
            // command += " 15 ";
            // else if(i == 7 || i == 8 || i == 9) 
            // min confidence for sequential rules
            // command += " 50% ";

            System.out.println("command: " + command + "\n");
            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec(command);
                InputStream in = proc.getInputStream();
                InputStream err = proc.getErrorStream();
                // print jar out stream
                java.util.Scanner s = new java.util.Scanner(in)
                        .useDelimiter("\\A");
                System.out.println(s.hasNext() ? s.next() : algorithms[i].toUpperCase()+": No out output.\n");
                // print jar err stream
                s = new java.util.Scanner(err).useDelimiter("\\A");
                System.out.println(s.hasNext() ? s.next() : algorithms[i].toUpperCase()+": No err output.\n");
                s.close();
                in.close();
                err.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            buildInvertedAlphabet();
            System.out.println("Inverted alphabet built.");
            readResultFile_produceTranslatedFile(absolutePath + results[i]);
            System.out
                    .println(algorithms[i] + " sequence file built.\n************************************************\n");
        }
    }

    public static void main(String[] args) {
        //File[] files = new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\CSVs").listFiles();
                //"C:\\Users\\gekka_000\\workspace\\re-tool_continued\\alphabets\\tudu_1"
                        //+ ".html"
                        //+ "_merge" 
                        //+ ".csv");
        //for(File file : files){
        File file = new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\merge.csv");
            if(!file.isDirectory() 
                    && !file.getAbsolutePath().matches("(.)*processed(.)*")
                    && !file.getAbsolutePath().matches("(.)*translated(.)*")
                    && !file.getAbsolutePath().matches("(.)*num(.)*")
                    && !file.getAbsolutePath().matches("(.)*result(.)*")
                    && !file.getAbsolutePath().matches("(.)*alphabet(.)*")){
                //System.out.println("FILENAME: "+file.getName());
                //FilePreprocessor.preprocessFile(file.getAbsolutePath());
                //produceSpaceFilesAndRunAlgorithm(file.getAbsolutePath() + ".processed");

                produceSpaceFilesAndRunAlgorithm(file.getAbsolutePath());
            }
        //}
    }

}
