/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction;

/**
 * @author scohen
 * 
 */
public class LogisticNormalLambdaFunction implements
		DifferentiableMultivariateRealFunction {

	private LogisticNormalVariationalInference varInf;
	private int i;

	private FullGradientFunc g1;
	private PartialGradientFunc[] g2;

	public LogisticNormalLambdaFunction(int i_,
			LogisticNormalVariationalInference v, int k) {
		i = i_;

		varInf = v;

		g1 = new FullGradientFunc();
		g2 = new PartialGradientFunc[k];

		for (int j = 0; j < k; j++) {
			g2[j] = new PartialGradientFunc();
			g2[j].k = j;
		}
	}

	/**
	 * Compute the gradient vector.
	 * 
	 * @param evaluationPoint
	 *            point at which the gradient must be evaluated
	 * @return gradient at the specified point
	 * @exception FunctionEvaluationException
	 *                if the function gradient
	 */

	public class FullGradientFunc implements MultivariateVectorialFunction {
		public double[] value(double[] evaluationPoint) {

			DoubleMatrix1D p = DoubleFactory1D.dense.make(evaluationPoint);
			DoubleMatrix1D df = DoubleFactory1D.dense
					.make(evaluationPoint.length);

			//System.err.println("computing gradient");

			varInf.df_lambda(i, p, df);

			/*for (int j=0; j<df.size(); j++) {
				System.err.println(" df["+i+","+j+"] = "+ df.get(j));
			}*/

			return df.toArray();
		}
	}

	public class PartialGradientFunc implements MultivariateRealFunction {

		public int k;

		public double value(double[] evaluationPoint) {

			DoubleMatrix1D p = DoubleFactory1D.dense.make(evaluationPoint);
			DoubleMatrix1D df = DoubleFactory1D.dense
					.make(evaluationPoint.length);

			//System.err.println("computing gradient");
			varInf.df_lambda(i, p, df);


			/*for (int j=k; j<=k; j++) {
				System.err.println(" df["+i+","+j+"] = "+ df.get(j));
			}*/

			return df.get(k);
		}
	}

	public MultivariateRealFunction partialDerivative(int k) {
		// g2.k = k;

		return g2[k];
	}

	public MultivariateVectorialFunction gradient() {
		return g1;
	}

	public double value(double[] evaluationPoint) {

		DoubleMatrix1D p = DoubleFactory1D.dense.make(evaluationPoint);

		//System.err.println("Evaluating function");

		/*for (int j=0; j<evaluationPoint.length; j++) {
			System.err.println("p["+i+","+j+"] = "+ evaluationPoint[j]);
		}*/
		double v = varInf.f_lambda(i, p);

		return v;
	}
}
