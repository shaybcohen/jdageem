/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author scohen
 * 
 */
public class SentenceCorpus extends Corpus {

	private Alphabet a;

	public static final String FORMAT_Plain = "plain";
	public static final String FORMAT_CoNLL2006 = "conll";

	public static final int CoNLLRead_GoldCoarsePOS = 3;
	public static final int CoNLLRead_GoldFineGrainedPOS = 4;
	public static final int CoNLLRead_Word = 1;
	public static final int CoNLLRead_GoldDep = 6;

	public SentenceCorpus(Alphabet alphabet) {
		a = alphabet;
		sentences = new ArrayList<SentenceDocument>();
	}

	public void readCorpus(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String os;

		while ((os = br.readLine()) != null) {
			SentenceDocument d = new SentenceDocument(a, os);
			sentences.add(d);
		}
	}

	public void readCoNLL2006Corpus(String filename, int whatToRead,
			String punctags) throws IOException {

		HashSet<String> punctagsSet = new HashSet<String>();

		if (punctags != null) {
			BufferedReader br = new BufferedReader(new FileReader(punctags));
			String os;

			while ((os = br.readLine()) != null) {
				if (os.equals("")) {
					break;
				}

				punctagsSet.add(os);
			}

		}

		BufferedReader br = new BufferedReader(new FileReader(filename));
		String os;

		int skipped = 0;

		while ((os = br.readLine()) != null) {

			if (os.equals("")) {
				continue;
			}
			ArrayList<String> arr = new ArrayList<String>();
			arr.add(os);

			while (((os = br.readLine()) != null) && (!os.equals(""))) {
				if (os.equals("")) {
					continue;
				}
				arr.add(os);
			}

			String sentence = "";
			boolean printed = false;
			for (int i = 0; i < arr.size(); i++) {

				StringTokenizer st = new StringTokenizer(arr.get(i));
				String[] fields = new String[st.countTokens()];

				int tokens = st.countTokens();

				for (int j = 0; j < tokens; j++) {
					fields[j] = st.nextToken();
				}

				if (punctagsSet.contains(fields[CoNLLRead_GoldCoarsePOS])) {
					skipped++;
				} else {
					if (printed) {
						sentence = sentence + " ";
					}
					sentence = sentence + fields[whatToRead];
					printed = true;
				}
			}

			SentenceDocument d = new SentenceDocument(a, sentence);
			sentences.add(d);
		}

		System.err.println("Skipped " + skipped + " punctuation tokens.");
	}

	protected static int[][] readCoNLL2006Dependencies(String filename)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String os;

		ArrayList<int[]> deps = new ArrayList<int[]>();

		while ((os = br.readLine()) != null) {

			if (os.equals("")) {
				continue;
			}
			ArrayList<String> arr = new ArrayList<String>();
			arr.add(os);

			while (((os = br.readLine()) != null) && (!os.equals(""))) {
				if (os.equals("")) {
					break;
				}
				arr.add(os);
			}

			int[] dep = new int[arr.size()];
			for (int i = 0; i < arr.size(); i++) {

				StringTokenizer st = new StringTokenizer(arr.get(i));
				String[] fields = new String[st.countTokens()];

				int tokens = st.countTokens();

				for (int j = 0; j < tokens; j++) {
					fields[j] = st.nextToken();
				}

				try {
					dep[i] = Integer.parseInt(fields[CoNLLRead_GoldDep]) - 1;
				}

				catch (NumberFormatException e) {
					dep[i] = -2;
				}
			}

			deps.add(dep);
		}

		int[][] depsArray = new int[deps.size()][];
		for (int i = 0; i < deps.size(); i++) {
			depsArray[i] = deps.get(i);
		}

		return depsArray;
	}

	public SentenceDocument get(int i) {
		return sentences.get(i);
	}

	public int size() {
		return sentences.size();
	}

	public void writeDependencies(String filename, int[][] deps,
			String fileFormat) throws IOException {
		if (deps.length != size()) {
			System.err
					.println("Warning: corpus size mismatches number of dependency trees to write. Not writing file.");
		}

		if (fileFormat.equals(FORMAT_Plain)) {
			PrintStream out = new PrintStream(new FileOutputStream(filename));

			for (int i = 0; i < deps.length; i++) {
				out.println(get(i));
				for (int j = 0; j < deps[i].length; j++) {
					if (j > 0) {
						out.print(" ");
					}
					out.print(deps[i][j] + 1);
				}
				out.println("");
				out.println("");
			}
		} else {
			System.err
					.println("Warning: unknown format size. Not writing file.");
		}
	}

	public static int[][] readDependencies(String filename, String fileFormat)
			throws IOException {

		if (fileFormat.equals(FORMAT_Plain)) {
			ArrayList<int[]> deps = new ArrayList<int[]>();
			int[][] myDeps = null;

			BufferedReader br = new BufferedReader(new FileReader(filename));
			String os;

			while ((os = br.readLine()) != null) {

				os = br.readLine();

				if (!os.equals("")) {
					String[] x = os.split(" ");
					int[] sent = new int[x.length];

					for (int i = 0; i < sent.length; i++) {
						try {
							sent[i] = Integer.parseInt(x[i]) - 1;
						} catch (NumberFormatException e) {
							sent[i] = -1;
						}
					}

					deps.add(sent);
				} else {
					deps.add(new int[0]);
				}

				os = br.readLine();

				if (os == null) {
					break;
				}
			}

			myDeps = new int[deps.size()][];
			for (int i = 0; i < myDeps.length; i++) {
				myDeps[i] = deps.get(i);
			}

			return myDeps;
		} else if (fileFormat.equals(FORMAT_CoNLL2006)) {
			return readCoNLL2006Dependencies(filename);
		} else {
			System.err
					.println("Warning: unknown format size. Not reading file.");
			return null;
		}
	}

	private ArrayList<SentenceDocument> sentences;

}
