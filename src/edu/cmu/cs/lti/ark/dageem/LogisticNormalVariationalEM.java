/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.SeqBlas;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

import java.io.IOException;

/**
 * @author scohen
 * 
 */
public abstract class LogisticNormalVariationalEM {

	public class LNVarEMInformation {
		double avg_niter;
		double total_lhood;
		double converged_pct;
	}

	private LogisticNormalSufficientStatistics ss;
	private LogisticNormalModel model;
	private DoubleMatrix2D[] corpusLambda;
	private DoubleMatrix2D[] corpusNu;
	private double lhood_old;
	private boolean memoryEfficient;

	
	public LogisticNormalVariationalEM(LogisticNormalModel model) {
		this.model = model;
	}
	
	public LogisticNormalModel getModel()
	{
		return model;
	}

	public void run(int t, int dropOutputEvery, String grammarPrefixFile,
			Corpus corpus, LogisticNormalInsideOutsideInterface io,
			boolean memoryEfficient) {

		if (!memoryEfficient) {
			corpusLambda = new DoubleMatrix2D[model.multinomCount()];
			corpusNu = new DoubleMatrix2D[model.multinomCount()];
		}
		this.memoryEfficient = memoryEfficient;

		lhood_old = 0;

		if (!memoryEfficient) {
			for (int i = 0; i < model.multinomCount(); i++) {
				corpusLambda[i] = DoubleFactory2D.sparse.make(corpus.size(),
						model.multinomSize(i));
				corpusNu[i] = DoubleFactory2D.sparse.make(corpus.size(),
						model.multinomSize(i));
			}
		}

		ss = new LogisticNormalSufficientStatistics(model.sizes());

		for (int i = 1; i <= t; i++) {
			System.err.println("Iteration " + i);
			iteration(corpus, io, true);
			if ((t % dropOutputEvery) == 0) {
				saveModel(grammarPrefixFile + i + ".gra");
			}
		}
	}

	public abstract void saveModel(String outputFile);
	
	public void iteration(Corpus corpus,
			LogisticNormalInsideOutsideInterface io, boolean reset_var) {
		LNVarEMInformation info = new LNVarEMInformation();

		int iter = 1;

		while (true) {
			EStep(io, corpus, model, ss, corpusLambda,
					corpusNu, reset_var, info);

			double convergence = (lhood_old - info.total_lhood) / lhood_old;
			if ((convergence < 0) && (lhood_old != 0)) {

				if (memoryEfficient) {
					System.err.println("Variational inference has not converged, but skipping another phase to save memory.");
					break;
				} else {
					LogisticNormalParameters.varMaxIteration += 10;
					LogisticNormalParameters.varConvergence /= 10;
					reset_var = false;
					System.err.println("Continuing E-step [" + iter
							+ "]: did not converge yet.");
					ss.init();
				}
			} else {
				break;
			}
			iter++;
		}

		MStep(model, ss, false);
		lhood_old = info.total_lhood;
		ss.init();
	}

	private void EStep(LogisticNormalInsideOutsideInterface delegator,
			Corpus corpus, LogisticNormalModel model,
			LogisticNormalSufficientStatistics ss,
			DoubleMatrix2D[] corpusLambda, DoubleMatrix2D[] corpusNu,
			boolean reset_var, LNVarEMInformation info) {
		LogisticNormalVariationalInference var;
		Document doc;

		double lhood, total = 0.0;

		IterationMonitor monitor = new IterationMonitor(corpus.size());
		for (int k = 0; k < corpus.size(); k++) {

			monitor.update();

			doc = corpus.get(k);

			if (doc.length() == 0) {
				System.err.println("Warning: skipping empty sentence.");
				continue;
			}

			var = delegator.createVariationalInferenceObject(model, doc);
			if (reset_var) {
				var.initUniformly(delegator);
			} else {
				for (int i = 0; i < var.multinomCount(); i++) {

					if (delegator.multinomialParticipates(doc, i)) {
						// vector lambda = k'th row of corpus_lambda[i]
						// vector nu = kth row of corpus_nu[i]
						DoubleMatrix1D lambda = corpusLambda[i].viewRow(k);
						DoubleMatrix1D nu = corpusNu[i].viewRow(k);

						SeqBlas.seqBlas.dcopy(lambda, var.multinom(i).lambda);
						SeqBlas.seqBlas.dcopy(nu, var.multinom(i).nu);
						var.optZeta(i);
						var.niter = 0;
					}
				}
				var.optPhi();
			}

			lhood = var.inference();
			var.updateExpectedSS(ss);

			total += lhood;
			info.avg_niter = info.avg_niter + var.niter;
			info.converged_pct = info.converged_pct + var.converged;

			if (!memoryEfficient) {
				for (int i = 0; i < var.multinomCount(); i++) {
					if (delegator.multinomialParticipates(doc, i)) {
						DoubleMatrix1D lambda = corpusLambda[i].viewRow(k);
						DoubleMatrix1D nu = corpusNu[i].viewRow(k);

						SeqBlas.seqBlas.dcopy(var.multinom(i).lambda, lambda);
						SeqBlas.seqBlas.dcopy(var.multinom(i).nu, nu);
					}
				}
			}
		}

		info.avg_niter = info.avg_niter / corpus.size();
		info.converged_pct = info.converged_pct / corpus.size();
		info.total_lhood = info.total_lhood + total;

		System.err.println("Likelihood bound = " + total);
	}

	void MStep(LogisticNormalModel model,
			LogisticNormalSufficientStatistics ss, boolean cov_estimate_shrink) {
		double sum;

		// mean maximization

		for (int i = 0; i < model.multinomCount(); i++) {
			for (int j = 0; j < model.multinomSize(i); j++)
				model.mu(i).set(j, ss.muSS(i).get(j) / ss.ndata);

			// covariance maximization

			for (int j = 0; j < model.multinomSize(i); j++) {
				for (int jprime = 0; jprime < model.multinomSize(i); jprime++) {
					model.cov(i).set(
							j,
							jprime,
							(1.0 / ss.ndata)
									* (ss.covSS(i).get(j, jprime) + ss.ndata
											* model.mu(i).get(j)
											* model.mu(i).get(jprime)
											- ss.muSS(i).get(j)
											* model.mu(i).get(jprime) - ss
											.muSS(i).get(jprime)
											* model.mu(i).get(j)));
				}
			}
			if (cov_estimate_shrink) {
				covShrinkage(model.cov(i), ss.ndata, model.cov(i));
			}
		}

		model.computeInverseCovariance();
		model.computeLogDet();
	}

	void covShrinkage(DoubleMatrix2D mle, int n, DoubleMatrix2D result) {
		int p = mle.rows(), i;
		double alpha = 0, tau = 0, log_lambda_s = 0;

		DoubleMatrix1D lambda_star = DoubleFactory1D.dense.make(p);
		DoubleMatrix1D eigen_vals = DoubleFactory1D.dense.make(p);
		DoubleMatrix1D s_eigen_vals = DoubleFactory1D.dense.make(p);

		DoubleMatrix2D d = DoubleFactory2D.dense.make(p, p);
		DoubleMatrix2D eigen_vects = DoubleFactory2D.dense.make(p, p);
		// DoubleMatrix2D s_eigen_vects = DoubleFactory2D.dense.make(p, p);
		DoubleMatrix2D result1 = DoubleFactory2D.dense.make(p, p);

		EigenvalueDecomposition eigen = new EigenvalueDecomposition(mle);

		eigen_vals = eigen.getRealEigenvalues();
		eigen_vects = eigen.getV();

		// get eigen decomposition

		for (i = 0; i < p; i++) {

			// compute shrunken eigenvalues

			alpha = 1.0 / (n + p + 1 - 2 * i);
			lambda_star.set(i, n * alpha * eigen_vals.get(i));
		}

		// get diagonal mle and eigen decomposition

		for (i = 0; i < d.rows(); i++) {
			d.set(i, i, mle.get(i, i));
		}

		EigenvalueDecomposition eigen2 = new EigenvalueDecomposition(d);
		s_eigen_vals = eigen2.getRealEigenvalues();

		// compute tau^2

		for (i = 0; i < p; i++) {
			log_lambda_s += Math.log(s_eigen_vals.get(i));
		}

		log_lambda_s = log_lambda_s / p;
		for (i = 0; i < p; i++)
			tau += Math.pow(Math.log(lambda_star.get(i)) - log_lambda_s, 2)
					/ (p + 4) - 2.0 / n;

		// shrink \lambda* towards the structured eigenvalues

		for (i = 0; i < p; i++)
			lambda_star
					.set(i,
							Math.exp((2.0 / n) / ((2.0 / n) + tau)
									* log_lambda_s + tau / ((2.0 / n) + tau)
									* Math.log(lambda_star.get(i))));

		// put the eigenvalues in a diagonal matrix

		for (i = 0; i < p; i++) {
			d.set(i, i, lambda_star.get(i));
		}

		// reconstruct the covariance matrix

		SeqBlas.seqBlas.dgemm(false, false, 1.0, d, eigen_vects, 0, result1);
		SeqBlas.seqBlas.dgemm(false, false, 1.0, eigen_vects, result1, 0,
				result);

	}
}
