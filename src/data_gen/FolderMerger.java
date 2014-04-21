package data_gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class FolderMerger {
public static void main(String[] args) {
	FileWriter output = null;
		try {
			output = new FileWriter(new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\merge.csv"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		File[] files = new File("H:\\Dropbox\\DISS\\traces\\selenium_traces\\CSVs").listFiles();
		for (File file : files) {
			System.out.println(file.getName());
			BufferedReader in = null;
			String x = "";
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(
						file), /*"UTF8"*/ "ISO-8859-1"));
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
				System.exit(2);
			}
			try {
				while ((x = in.readLine()) != null) {
					output.write(x+"\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		
		try {
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
