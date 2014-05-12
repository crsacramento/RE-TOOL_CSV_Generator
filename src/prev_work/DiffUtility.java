package prev_work;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import difflib.*;


public class DiffUtility {

    // Helper method for get the file content
    private static List<String> fileToLines(String filename) {
            List<String> lines = new LinkedList<String>();
            String line = "";
            try {
                    BufferedReader in = new BufferedReader(new FileReader(filename));
                    while ((line = in.readLine()) != null) {
                            lines.add(line);
                    }
                    in.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }
            
            return lines;
    }

    
    public static Patch differenceBetweenFiles(int index1, int index2)
    {
        //System.out.println("I Get here");
        List<String> original = fileToLines(System.getProperty("user.dir")+"\\HTMLtemp\\"+index1+".txt");
        List<String> revised  = fileToLines(System.getProperty("user.dir")+"\\HTMLtemp\\"+index2+".txt");
        //System.out.println("Here too");
        
        return DiffUtils.diff(original, revised);
    }
    
    public static String convertPatchToString(Patch patch)
    {
        String complex="";
        for (Delta delta: patch.getDeltas()) {
            complex+=delta.toString();
            complex+="\n";
        }
        return complex;
    }
    
    public static void main(String[] args) {
            List<String> original = fileToLines(System.getProperty("user.dir")+"\\HTMLtemp\\"+1+".txt");
            List<String> revised  = fileToLines(System.getProperty("user.dir")+"\\HTMLtemp\\"+2+".txt");
            
            // Compute diff. Get the Patch object. Patch is the container for computed deltas.
            Patch patch = DiffUtils.diff(original, revised);

            for (Delta delta: patch.getDeltas()) {
                    System.out.println(delta);
            }
                   
    }

}
