package data_gen;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Implementation of Trie data structure for saving a dictionary
 */
public class DicTrie {
	static int MAX_NUM_LETTERS = 26;
	static DicTrieNode root; // keep track of the root
	static DicTrieNode position; // keep track of the position
	public static int[] amountList = new int[MAX_NUM_LETTERS]; // keep track of the
														// amount of each letter

	/**
	 * Default constructor
	 */
	public DicTrie() {
		root = position = null;
	}

	/**
	 * Specifies max num letters
	 */
	public DicTrie(int max) {
		root = position = null;
		MAX_NUM_LETTERS = max;
	}

	/**
	 * Gets rid of spaces/punctuation/numbers in str and converts to lower case
	 * 
	 * @param str
	 * @return
	 */
	public static String fixString(String str) {
		int index = 0;
		str = str.toLowerCase();

		char[] oldStr = str.toCharArray(); // holds the old String
		char[] newStr = new char[str.length()]; // will make up the new String

		// loop until every char in myChars is tested
		for (int x = 0; x < oldStr.length; x++) {
			// accept all alphabetic characters only
			if (oldStr[x] >= 'a' && oldStr[x] <= 'z') {
				newStr[index++] = oldStr[x];
			}
		}

		// return a String consisting of the characters in newChars
		return (new String(newStr));
	}

	/**
	 * receives: a String str inserts the String str into the tree starting at
	 * the end of the word. Each letter is inserted, and based on the other
	 * elements in the list a new child is added to the list, or an existing
	 * child is used.
	 * 
	 * @param str
	 */
	public static void insert(String str) {
		// call fixString to prepare it for insertion
		str = fixString(str);

		// return if str in null
		if (str == null)
			return;

		// keep track of each index in the String
		int index = 0;

		// if root equals null, create an empty node (this will only happen the
		// first time)
		if (root == null) {
			root = new DicTrieNode(' ');
		}

		// declare pointers needed
		DicTrieNode temp, tempBack, pred;
		temp = tempBack = pred = root;

		// loop until every index in str is inserted
		while (index < str.length()) {
			// must make a new branch in the tree because there
			// is not yet a branch starting with the desired letter
			if (temp.down == null) {
				temp.down = new DicTrieNode(str.charAt(index));
				temp.down.right = new DicTrieNode(' ');
				temp.down.right.down = temp;
				temp = temp.down;
			} else {
				// update pred and temp
				pred = temp;
				temp = temp.down;
				// if the letter goes at the start of the list,
				// make an new path at the start of the list
				if (str.charAt(index) < temp.letter) {
					temp = new DicTrieNode(str.charAt(index), temp);
					pred.down = temp;
				}
				// if the letter is goes somewhere else in the list,
				// find the correct spot
				else if (str.charAt(index) > temp.letter) {
					// find the correct spot to insert the letter based on
					// alphabetical order
					while (temp.letter != ' '
							&& str.charAt(index) > temp.letter) {
						tempBack = temp;
						temp = temp.right;
					}
					// if the letter is not present, make a new path
					// otherwise, do nothing, simply follow the correct path
					if (str.charAt(index) != temp.letter) {
						temp = new DicTrieNode(str.charAt(index), temp);
						tempBack.right = temp;
					}
				}
			}
			// keep track of each letter, to easily calculate probabilities
			// later
			if (str.charAt(index) >= 97)
				amountList[str.charAt(index) - 97]++;
			index++;
		}
		// at this point it will always be at the start of a word
		temp.endsWord = true;
	}

	/**
	 * receives: a String str (the name of a file) function builds a tree by
	 * reading the file named fileName one string at a time, and calls insert()
	 * to add that word to the tree.
	 * @param fileName
	 */
	public static void setupTrie(String fileName) {
		String word = new String();

		try {
			FileReader read = new FileReader(fileName);
			BufferedReader in = new BufferedReader(read, 50);

			while (in.ready()) {
				word = in.readLine();
				org.apache.commons.lang3.StringUtils.stripAccents(word);
				insert(word);
			}
			in.close();
		} catch (Exception e) {
			// catch exception cause by reading the file
			e.printStackTrace();
			return;
		}
	}

	/**
	 *  recursively print each node, and under that print each child of the node
	 *  if the node starts a word, a '^' is printed after it. this is used just
	 *  for testing
	 */
	public static void print() {
		DicTrieNode t = root;
		System.out.println(root.letter + " *");
		print(t.down);
	}
	/**
	 * recursive call to print
	 * @param DicTrieNode t
	 */
	static void print(DicTrieNode t) {
		if (t == null)
			return;
		// System.out.println(t.letter + " children");
		DicTrieNode temp = t;
		while (t != null) {
			System.out.print(t.letter + " ");
			if (t.endsWord)
				System.out.print("^");
			t = t.right;
		}
		System.out.println("*");
		t = temp;
		while (t != null) {
			if (t.down != null && t.right != null) {
				System.out.println(t.letter + " children");
				print(t.down);

			}
			t = t.right;
		}
	}
	
	/**
	 * reset the position by pointing it to root
	 */
	public void resetPosition() {
		position = root;
	}

	/**
	 * calls stringInTrie to see if the string is there, and returns true
	 * if the last node in the string ends a word
	 * @param str
	 * @return true if str is found, otherwise false
	 */
	public static boolean find(String str) {
		//only return true if position starts a word, otherwise it's not in the dictionary
		if(stringInTrie(str) && position.endsWord) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * follows the tree until the start until the prefix is found.
	 * it doesn't matter if str is a full word or not.
	 * @param str
	 * @return true if str is found, otherwise false
	 */
	public static boolean stringInTrie(String str) {
		//node to traverse the tree
		DicTrieNode t = root;

		//loop until ever index in str is searched
		for(int index=0;index<str.length();index++) {
			//move t down each time
			if(t.down!=null) {
				t = t.down;
			}
			//if t can go down no more, but str hasn't been found
			//then it is not in the dictionary
			else {
				return false;
			}

			//search the children lists for the correct path to follow
			while(str.charAt(index) != t.letter) {
				//if the correct path hasn't been found,
				//then str is not in the dictionary
				if(t.right==null) {
					return false;
				}
				//move t to the right
				t = t.right;
			};
		}
		position = t;
		return true;
	}
	public static void main(String[] args) {
		setupTrie("C:\\Users\\gekka_000\\workspace\\re-tool_continued\\dictionaries\\dic_EN.txt");
		//print();
		//System.out.println(find("abandons"));
		long start = System.currentTimeMillis();
		int counter = 0;
		try {
			FileReader read = new FileReader("C:\\Users\\gekka_000\\workspace\\re-tool_continued\\dictionaries\\dic_EN.txt");
			BufferedReader in = new BufferedReader(read);

			while (in.ready()) {
				String word = in.readLine();
				System.out.println(word + ": " + find(word));
				counter++;
			}
			in.close();
		} catch (Exception e) {
			// catch exception cause by reading the file
			e.printStackTrace();
			return;
		}
		
		System.out.println((System.currentTimeMillis() - start)/1000.0);
		System.out.println(counter);
	}
}
