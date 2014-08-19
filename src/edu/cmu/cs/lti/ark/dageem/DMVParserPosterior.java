/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 *
 */
public class DMVParserPosterior extends DependencyParser {

	private DMVGrammarEdgePosteriors grammar;

	public DMVParserPosterior(DMVGrammarEdgePosteriors grammar) {
		this.grammar = grammar;
	}


    public int[] parseSentence(SentenceDocument sentence) {
		Chart.semiring.setSemiringLogReal();

		grammar.cleanup();

		EisnerSattaChart chart = new EisnerSattaChart(grammar);
		EisnerSattaAlgorithm alg = new EisnerSattaAlgorithm(grammar, true);

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

		Chart.semiring.setSemiringMax();
		
		CompleteGraphGrammar completeGraph = grammar.createCompleteGraphGrammar(sentence.length(), (EValue)goalTerm.secondaryValue(), true);

		chart = new EisnerSattaChart(completeGraph);
		alg = new EisnerSattaAlgorithm(completeGraph, false);
		
		alg.setChart(chart);
		alg.assertSentence(sentence);
		
		while (alg.agendaIterator()) {
		}
		
		goalTerm = (GoalTerm) (chart.getTerm(GoalTerm.hashcode(0)));
		
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
