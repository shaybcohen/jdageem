/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.Set;

/**
 * @author scohen
 * 
 */
public class LogisticNormalDMVInsideOutside extends
		LogisticNormalInsideOutsideInterface {

	private MultinomialDMVGrammar grammar;
	private EisnerSattaChart chart;
	private EisnerSattaAlgorithm alg;

	public LogisticNormalDMVInsideOutside(MultinomialDMVGrammar g) {
		grammar = g;

		chart = new EisnerSattaChart(grammar);
		alg = new EisnerSattaAlgorithm(grammar, true);

		alg.setChart(chart);
	}

	public LogisticNormalVariationalInference createVariationalInferenceObject(
			LogisticNormalModel model, Document doc) {

		LogisticNormalVariationalInference ln = new LogisticNormalVariationalInference(
				model, this, doc);

		getInsideOutside(doc, ln, false);

		return ln;
	}

	public void getInsideOutside(Document doc,
			LogisticNormalVariationalInference varParams) {

		getInsideOutside(doc, varParams, true);
	}

	public void getInsideOutside(Document doc,
			LogisticNormalVariationalInference varParams, boolean copyPhi) {

		Set<Integer> allowedHeads = doc.getBagOfWords();

		Chart.semiring.setSemiringLogReal();
		
		chart = new EisnerSattaChart(grammar);
		grammar.cleanup();

		alg.setChart(chart);

		if (copyPhi) {
			grammar.copyFromCount(allowedHeads, varParams);
		}

		alg.assertSentence((SentenceDocument) doc);

		while (alg.agendaIterator()) {
		}

		GoalTerm goalTerm = (GoalTerm) (chart.getTerm(GoalTerm.hashcode(0)));

		if (goalTerm == null) {
			System.err
					.println("Error parsing. Is there a sentence with lexical units not appearing in the grammar?");
			return;
		}

		double goal = goalTerm.value();
		EValue goalValue = (EValue) goalTerm.secondaryValue();

		double goal2 = 0;

		if (goalValue != null) {
			goal2 = goalValue.p();
		}

		//System.err.println("Goal:" + goal + " (" + goal2 + ")");

		grammar.setCounts((EValue) chart.getTerm(GoalTerm.hashcode(0))
				.secondaryValue());

		grammar.copyToCount(allowedHeads, varParams);

		varParams.set_logZ(goal);
	}

	public double getInitLogPhi(LogisticNormalModel model, int i, int j) {
		return model.mu(i).get(j);
	}

	public LogisticNormalModel createModel() {
		return createModel(true);
	}

	public boolean multinomialParticipates(Document doc, int i) {
		return ((grammar.getHeadAt(i) == -1) || doc.getBagOfWords().contains(grammar.getHeadAt(i)));
	}

	LogisticNormalModel createModel(boolean init_lambda_from_grammar_chart) {
		LogisticNormalModel model = new LogisticNormalModel(grammar.sizes());

		model.init();

		if (init_lambda_from_grammar_chart) {
			//System.err.println("initializing mu...");
			grammar.copyToCount(null, model);
		}

		model.computeInverseCovariance();
		model.computeLogDet();

		return model;
	}

}
