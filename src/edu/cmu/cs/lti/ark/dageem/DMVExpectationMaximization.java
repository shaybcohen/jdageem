/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.io.IOException;

/**
 * @author scohen
 * 
 */
public class DMVExpectationMaximization {

	DMVGrammar grammar;
	Alphabet alphabet;

	public DMVExpectationMaximization(Alphabet a) {
		alphabet = a;
		grammar = new DMVGrammar(alphabet);
	}

	public double EStepUpdate(SentenceDocument sentence, DMVGrammar newGrammar) {
		
		if (sentence.length() == 0)
		{
			System.err.println("Warning: skipping empty sentence.");
			return Chart.semiring.Zero;
		}
		
		double goal = 0;

		grammar.cleanup();

		EisnerSattaChart chart = new EisnerSattaChart(grammar);
		EisnerSattaAlgorithm alg = new EisnerSattaAlgorithm(grammar, true);

		alg.setChart(chart);

		alg.assertSentence(sentence);
		while (alg.agendaIterator()) {
		}

		GoalTerm goalTerm = (GoalTerm) (chart.getTerm(GoalTerm.hashcode(0)));

		if (goalTerm == null) {
			System.err.println("Error parsing.");
			return Chart.semiring.Zero;
		}

		goal = goalTerm.value();
		//EValue goalValue = (EValue) goalTerm.secondaryValue();

		// System.err.println("Goal:" + goal + " (" + goal2 + ")");

		grammar.increaseCounts((EValue) chart.getTerm(GoalTerm.hashcode(0))
				.secondaryValue(), newGrammar);

		return goal;
	}

	public void initGrammar(String file, Corpus corpus) throws IOException {
		grammar.readGrammar(file, (SentenceCorpus)corpus);
	}

	private void MStep() {
		grammar.normalizeGrammar();
	}

	private double EStep(SentenceCorpus sentences) {
		DMVGrammar newGrammar = new DMVGrammar(alphabet);
		double ll = 0;

		IterationMonitor monitor = new IterationMonitor(sentences.size());
		int n = 0;
		
		for (int i = 0; i < sentences.size(); i++) {
			monitor.update();

			double goal = EStepUpdate(sentences.get(i), newGrammar);

			if (goal > Chart.semiring.Zero) {
				ll = ll + Chart.semiring.convertFromSemiring(goal);
				n++;
			}
		}

		ll = ll / n;

		System.out.println("");

		grammar = newGrammar;

		return ll;
	}

	private void EMIteration(SentenceCorpus sentences) {
		double ll = EStep(sentences);
		MStep();

		System.err.println("Avg log-likelihood: " + ll);
	}

	public void run(int T, int dropOutputEvery, String grammarPrefixFile,
			SentenceCorpus corpus) throws IOException {

		Chart.semiring.setSemiringLogReal();

		for (int t = 1; t <= T; t++) {
			System.err.println("Iteration " + t);
			EMIteration(corpus);

			if ((t % dropOutputEvery) == 0) {
				getGrammar().writeGrammar(grammarPrefixFile + t + ".gra");
			}
		}
	}

	public DMVGrammar getGrammar() {
		return grammar;
	}
}
