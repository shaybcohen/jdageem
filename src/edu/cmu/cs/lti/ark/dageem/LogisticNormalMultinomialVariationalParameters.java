/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;


import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;


/**
 * @author scohen
 *
 */
public class LogisticNormalMultinomialVariationalParameters {

	private int tN;
	public double zeta;
	public DoubleMatrix1D nu, lambda, phi, log_phi, C;
	
	public LogisticNormalMultinomialVariationalParameters(int n)
	{
		tN = n;

		DoubleFactory1D F1 = DoubleFactory1D.dense;

		nu = F1.make(n);
		lambda = F1.make(n);
		phi = F1.make(n);
		log_phi = F1.make(n);
		C = F1.make(n);
	}

	public int N()
	{
		return tN;
	}
}
