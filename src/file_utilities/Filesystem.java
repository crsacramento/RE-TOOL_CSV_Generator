package file_utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Filesystem {

	public static void saveToFile(String folder, String filename,
			String content, Boolean append) {
		File file = new File(System.getProperty("user.dir") + "\\HTML" + folder
				+ "\\" + filename + ".txt");
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
			fw = new FileWriter(file.getAbsoluteFile(), append);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			// bw.newLine();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// return true if the word is on the file, false if not
	public static Boolean searchWordInFile(String word, String folder,
			String filename) {
		File file = new File(System.getProperty("user.dir") + "\\HTML" + folder
				+ "\\" + filename + ".txt");

		FileReader fr;

		try {
			fr = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			String s;
			while ((s = br.readLine()) != null) {
				// System.out.println(s);
				if (s.contains(word)) {
					br.close();
					return true;
				}
			}
			fr.close();
			return false;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	// counts number of lines in file
	public static int numberOfLinesInFile(String folder, String filename) {
		int numberOfLines = 0;
		File file = new File(System.getProperty("user.dir") + "\\HTML" + folder
				+ "\\" + filename + ".txt");

		FileReader fr;

		try {
			fr = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			while (br.readLine() != null) {
				// System.out.println(s);
				numberOfLines++;
			}
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return numberOfLines;
	}

	public static String[] getLinesInFile(String filename, int start) {
		return getLinesInFile(filename, start, -1);
	}

	public static String[] getLinesInFile(String filename, int start, int end) {
		if ((end < start && end != -1)|| start < 0)
			return null;
		
		int toRead, numberOfLines = 0;
		if (end == -1)
			toRead = 1;
		else
			toRead = end - start;
		String[] lines = new String[toRead];
		
		
		File file = new File(System.getProperty("user.dir")
				+ File.separatorChar + filename);
		FileReader fr;

		try {
			fr = new FileReader(file.getAbsoluteFile());
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while (br.readLine() != null && numberOfLines < start) {
				// System.out.println(s);
				numberOfLines++;
			}
			if(numberOfLines < start)
				return null;
			for(int i = 0; i < toRead; ++i){
				line = br.readLine();
				if(line == null)
					return lines;
				lines[i] = line;
			}
			
			br.close();
			fr.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lines;

	}

	public static double retrieveNumericParameter(String parameterName,
			String folder, String filename) {
		File file = new File(System.getProperty("user.dir") + "\\HTML" + folder
				+ "\\" + filename + ".txt");
		double parameter = 0;
		FileReader fr;

		try {
			fr = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			String s;
			while ((s = br.readLine()) != null) {
				// System.out.println(s);
				if (s.split(":")[0].equals(parameterName)) {
					parameter = Double.parseDouble(s.split(":")[1]);
					System.out.println(parameter);
				}
			}
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return parameter;
	}

}
