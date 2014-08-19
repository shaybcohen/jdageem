/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * @author scohen
 * 
 */
public class SentenceDocument extends Document {

	public SentenceDocument(Alphabet a) {
		alphabet = a;
		bagOfWords = new HashSet<Integer>();
	}

	public SentenceDocument(Alphabet a, String s) {
		alphabet = a;
		bagOfWords = new HashSet<Integer>();

		fromString(s);
	}

	public Set<Integer> getBagOfWords() {
		return bagOfWords;
	}

	public void fromString(String sentence) {
		strSentence = sentence;
		StringTokenizer st = new StringTokenizer(sentence);

		words = new int[st.countTokens()];

		int tokens = st.countTokens();

		for (int i = 0; i < tokens; i++) {
			words[i] = alphabet.lookupIndex(st.nextToken());
			bagOfWords.add(words[i]);
		}
	}

	public String toString() {
		return strSentence;
	}

	public int wordAt(int i) {
		return words[i];
	}

	public int length() {
		return words.length;
	}

	private Set<Integer> bagOfWords;
	private Alphabet alphabet;
	private int[] words;
	private String strSentence;
}
