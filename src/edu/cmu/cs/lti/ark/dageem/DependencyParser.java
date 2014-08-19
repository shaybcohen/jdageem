/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 *
 */
public abstract class DependencyParser {

	public int[][] parseCorpus(SentenceCorpus corpus) {
		int[][] depTrees = new int[corpus.size()][];

		int dot_print = (corpus.size() / 1000) + 1;

		IterationMonitor monitor = new IterationMonitor(corpus.size());
		for (int k = 0; k < corpus.size(); k++) {
			monitor.update();			
			depTrees[k] = parseSentence(corpus.get(k));
		}

		return depTrees;
	}

	public abstract int[] parseSentence(SentenceDocument document);
}
