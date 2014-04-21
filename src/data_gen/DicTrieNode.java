package data_gen;

/**
 * Implementation of Trie node. Each node has a letter, 
 * connections to the following letters, and a boolean that say 
 * if it is leaf.   
 */
public class DicTrieNode {
	char letter;		        // stores the letter for each node
	boolean endsWord;	    	// true if the letter ends a word
	DicTrieNode down;		    // points to the left most child of the node
	DicTrieNode right;		    // points to the right sibling of the node

	// constructor for node class
	public DicTrieNode(char c) {
		letter = c;
		endsWord = false;
		down = null;
		right = null;
	}
	
	// constructor to associate 'right' node
	public DicTrieNode(char c, DicTrieNode r) {
		letter = c;
		endsWord = false;
		down = null;
		right = r;
	}
}