package edu.cmu.cs.lti.ark.dageem;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.SeqBlas;

public class LogisticNormalSufficientStatistics {

	public LogisticNormalSufficientStatistics(int[] sizes) {
		N = (int[]) sizes.clone();

		mus = new DoubleMatrix1D[sizes.length];
		covs = new DoubleMatrix2D[sizes.length];

		for (int i = 0; i < sizes.length; i++) {
			mus[i] = DoubleFactory1D.dense.make(sizes[i]);
			covs[i] = DoubleFactory2D.dense.make(sizes[i], sizes[i]);
		}

		init();

		ndata = 0;
	}

	int size(int i)
	{
		return N[i];
	}
	
	void incCovSS(int i, int j, int k, double v) {
		covs[i].set(j, k, covs[i].get(j, k) + v);
	}

	void incMuSS(int i, int j, double v) {
		mus[i].set(j, mus[i].get(j) + v);
	}

	DoubleMatrix1D muSS(int i) {
		return mus[i];
	}

	DoubleMatrix2D covSS(int i) {
		return covs[i];
	}

	void init() {
		for (int i = 0; i < N.length; i++) {
			SeqBlas.seqBlas.dscal(0.0, covSS(i));
			SeqBlas.seqBlas.dscal(0.0, muSS(i));
		}

		ndata = 0;
	}

	int multinomCount() {
		return N.length;
	}

	public int ndata;
	private int[] N;

	private DoubleMatrix1D[] mus;
	private DoubleMatrix2D[] covs;
}
