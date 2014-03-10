package data_gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class AlphabetGenerator {
	static HashMap<String, Integer> alphabet = new HashMap<String, Integer>();
	static HashMap<Integer, String> invertedAlphabet = new HashMap<Integer, String>();

	/**
	 * Reads CSV file, fills alphabet hashmap with alphabet, writes line in
	 * converted file.
	 * 
	 * @param absolutePath
	 *            path to csv file
	 */
	static void readCSVFile_WriteSpaceFile(String absolutePath) {
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
		File actualOutputFile = new File(dir, file.getName() + ".space");

		try {
			output = new FileWriter(actualOutputFile, true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// updates line to write
		String x = "";
		int lineCounter = 1, lastIndex = 1;

		try {
			while ((x = in.readLine()) != null) {
				String[] parcels = x.split(",");
				if (parcels.length == 1) {
					System.err.println("ERROR: Line " + lineCounter
							+ " is not comma-separated.");
					output.close();
					System.exit(1);
				}

				// updates alphabet, writes to alphabet file
				String line = "";
				for (int i = 0; i < parcels.length; ++i) {
					if (!alphabet.containsKey(parcels[i])) {
						alphabet.put(parcels[i], lastIndex);
						++lastIndex;
					}
					// line += alphabet.get(parcels[i]) + " -1 ";
					line += alphabet.get(parcels[i]) + " ";

				}

				line = line.substring(0, line.length() - 1);
				// line += " -2 \n";
				line += " -1 -2 \n";
				output.write(line);
				line = "";
				lineCounter++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(lastIndex + " sequences found.");

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
			alphabetFW = new FileWriter(alphabetFile, true);
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
			//it.remove(); // avoids a ConcurrentModificationException
		}

		try {
			alphabetFW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void readResultFile_produceSequenceFile(String absolutePath) {
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
		File actualOutputFile = new File(dir, file.getName() + ".sequence");

		try {
			output = new FileWriter(actualOutputFile, true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// updates line to write
		String x = "";

		try {
			while ((x = in.readLine()) != null) {
				String support = x.substring(x.indexOf('#'));
				x = x.substring(0, x.indexOf('#'));
				String[] parcels = x.split(" ");

				String line = "";
				for (int i = 0; i < parcels.length; ++i) {
					if (invertedAlphabet.containsKey(Integer
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

	static void produceSpaceFilesAndRunAlgorithm(String absolutePath) {
		readCSVFile_WriteSpaceFile(absolutePath);
		writeAlphabetFile(absolutePath);

		String command = "java -jar C:\\Users\\gekka_000\\workspace\\re-tool_continued\\alphabets\\spmf.jar run PrefixSpan "
				+ absolutePath
				+ ".space "
				+ absolutePath
				+ ".space.result 10% 100";
		System.out.println("command: " + command);
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(command);
			InputStream in = proc.getInputStream();
			InputStream err = proc.getErrorStream();
			java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
			System.out.println(s.hasNext() ? s.next() : "");
			s = new java.util.Scanner(err).useDelimiter("\\A");
			System.out.println(s.hasNext() ? s.next() : "");
			s.close();in.close();err.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		buildInvertedAlphabet();
		System.out.println("Inverted alphabet built.");
		readResultFile_produceSequenceFile(absolutePath + ".space.result");
		System.out.println("Sequence file built.");
	}

	public static void main(String[] args) {
		File file = new File(
				"C:\\Users\\gekka_000\\workspace\\re-tool_continued\\alphabets\\amazon_merge.csv");
		System.out.println(file.getName());
		if (!file.isDirectory()) {
			produceSpaceFilesAndRunAlgorithm(file.getAbsolutePath());
		}
	}
}
