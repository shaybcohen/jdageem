/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 * 
 */
public class DMVParserViterbi extends DependencyParser {

	private DMVGrammar grammar;

	public DMVParserViterbi(DMVGrammar grammar) {
		this.grammar = grammar;
	}

	public int[] parseSentence(SentenceDocument sentence) {
		Chart.semiring.setSemiringMax();

		grammar.cleanup();

		EisnerSattaChart chart = new EisnerSattaChart(grammar);
		EisnerSattaAlgorithm alg = new EisnerSattaAlgorithm(grammar, false);

		alg.setChart(chart);

		alg.assertSentence(sentence);

		while (alg.agendaIterator()) {
			//System.err.println(j++);
		}

		GoalTerm goalTerm = (GoalTerm) (chart.getTerm(GoalTerm.hashcode(0)));
		
		if (goalTerm == null) {
			System.err.println("Could not parse sentence: " + sentence);
			int[] dep = new int[sentence.length()];

			for (int i = 0; i < dep.length; i++) {
				dep[i] = -1;
			}
			return dep;
		}
		
		int[] dep = alg.backtrack();

		return dep;
	}

}
