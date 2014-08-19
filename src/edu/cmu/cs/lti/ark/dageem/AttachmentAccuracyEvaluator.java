/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 * 
 */
public class AttachmentAccuracyEvaluator implements DependencyEvaluator {

	public static final int EVAL_AccuracyAll = 0;
	public static final int EVAL_AccuracyShorterThan10 = 1;
	public static final int EVAL_AccuracyShorterThan20 = 2;
	public static final int EVAL_AccuracyShorterThan40 = 3;

	public int len10 = 10;
	
	public void setTenLength(int l)
	{
		len10 = l;
	}
	
	public double[] eval(int[][] gold, int[][] parsed) {
		int correct = 0, total = 0, correct10 = 0, total10 = 0, correct20 = 0, total20 = 0, correct40 = 0, total40 = 0;

		if (gold.length != parsed.length) {
			System.err
					.println("Cannot evaluate dependency tree lists of different size.");
			return null;
		}

		for (int j = 0; j < gold.length; j++) {
			int[] h = parsed[j];
			int[] g = gold[j];

			if (h.length != g.length) {
				System.err
						.println("Warning: request for evaluation of sentences of different length. Ignoring this pair.");
			} else {
				for (int i = 0; i < h.length; i++) {
					if (h[i] == g[i]) {
						correct++;
						if (h.length <= len10) {
							correct10++;
						}
						if (h.length <= 20) {
							correct20++;
						}
						if (h.length <= 40) {
							correct40++;
						}
					}
					total++;
					if (h.length <= len10) {
						total10++;
					}
					if (h.length <= 20) {
						total20++;
					}
					if (h.length <= 40) {
						total40++;
					}

				}
			}
		}

		double[] eval = new double[4];
		
		eval[EVAL_AccuracyAll] = ((double)correct) / total;
		eval[EVAL_AccuracyShorterThan10] = ((double)correct10) / total10;
		eval[EVAL_AccuracyShorterThan20] = ((double)correct20) / total20;
		eval[EVAL_AccuracyShorterThan40] = ((double)correct40) / total40;

		return eval;
	}

}
