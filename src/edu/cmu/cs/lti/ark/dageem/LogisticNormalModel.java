/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;

/**
 * @author scohen
 * 
 */
public class LogisticNormalModel implements CountInterface {

	private int[] N;
	private DoubleMatrix1D[] mus;
	private DoubleMatrix2D[] inv_covs;
	private DoubleMatrix2D[] covs;
	private Algebra algebra;
	private double[] logdets;

	public LogisticNormalModel(int[] sizes) {
		algebra = new Algebra();

		N = (int[]) sizes.clone();

		DoubleFactory1D F1 = DoubleFactory1D.dense;
		DoubleFactory2D F2 = DoubleFactory2D.dense;

		mus = new DoubleMatrix1D[sizes.length];
		inv_covs = new DoubleMatrix2D[sizes.length];
		covs = new DoubleMatrix2D[sizes.length];

		for (int i = 0; i < N.length; i++) {
			mus[i] = F1.make(sizes[i]);
			inv_covs[i] = F2.make(sizes[i], sizes[i]);
			covs[i] = F2.make(sizes[i], sizes[i]);
		}

		logdets = new double[N.length];

		init(0.0, 1.0, false);

		computeInverseCovariance();
		computeLogDet();
	}
	
	public void setCountValue(int i, int j, double v)
	{
		mu(i).set(j, v);
	}
	
	public double getCountValue(int i, int j)
	{
		return mu(i).get(j);
	}
	
	public int[] sizes()
	{
		return N;
	}
	
	void init()
	{
		init(0.0, 1.0, true);
	}

	void init(double value1, double value2, boolean only_cov) {

		for (int i = 0; i < N.length; i++) {
			for (int j = 0; j < N[i]; j++) {
				if (!only_cov) {
					mus[i].set(j, 0);
				}
				for (int j2 = 0; j2 < N[i]; j2++) {
					if (j == j2) {
						covs[i].set(j, j2, value2);
					} else {
						covs[i].set(j, j2, value1);
					}
				}
			}
		}
	}

	public DoubleMatrix1D mu(int i) {
		return mus[i];
	}

	public DoubleMatrix2D inverseCovariance(int i) {
		return inv_covs[i];
	}

	public DoubleMatrix2D cov(int i) {
		return covs[i];
	}

	public int multinomSize(int i) {
		return N[i];
	}

	public int multinomCount() {
		return N.length;
	}

	public void computeLogDet() {
		for (int i = 0; i < N.length; i++) {
			logdets[i] = Math.log(algebra.det(inv_covs[i]));
		}
	}

	public void computeInverseCovariance() {
		for (int i = 0; i < N.length; i++) {
			inv_covs[i] = algebra.inverse(covs[i]);
		}
	}

	public void computeCovarianceFromInverseCovariance() {
		for (int i = 0; i < N.length; i++) {
			covs[i] = algebra.inverse(inv_covs[i]);
		}
	}

	public double logDetInverseCovariance(int i) {
		return logdets[i];
	}

}
