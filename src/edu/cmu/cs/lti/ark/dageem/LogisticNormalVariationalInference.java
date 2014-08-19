/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.SeqBlas;
import cern.colt.matrix.linalg.Blas;

import org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math.optimization.general.ConjugateGradientFormula;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.util.FastMath;

/**
 * @author scohen
 * 
 */
public class LogisticNormalVariationalInference implements CountInterface {

	private LogisticNormalModel model;
	private LogisticNormalMultinomialVariationalParameters[] multinomial_var_params;
	private LogisticNormalInsideOutsideInterface inside_outside;
	private Blas blas;
	private Document doc;

	public double converged;
	public int niter;

	int K;

	double lhood;

	double Z;

	// starting from here, this is from inference.h

	LogisticNormalVariationalInference(LogisticNormalModel m,
			LogisticNormalInsideOutsideInterface io, Document d) {
		model = m;
		doc = d;

		multinomial_var_params = new LogisticNormalMultinomialVariationalParameters[m
				.multinomCount()];

		for (int i = 0; i < m.multinomCount(); i++) {
			multinomial_var_params[i] = new LogisticNormalMultinomialVariationalParameters(
					m.multinomSize(i));
		}

		K = m.multinomCount();
		Z = 0.0;

		blas = SeqBlas.seqBlas;

		inside_outside = io;

		initTempVectors(model);
	}

	protected double safeLog(double x) {
		if (x == 0)
			return -1000;
		else
			return Math.log(x);
	}

	public int multinomCount() {
		return K;
	}

	LogisticNormalMultinomialVariationalParameters multinom(int i) {
		return multinomial_var_params[i];
	}

	public double getNu(int i, int j) {
		return multinomial_var_params[i].nu.get(j);
	}

	public double getLambda(int i, int j) {
		return multinomial_var_params[i].lambda.get(j);
	}

	public double getPhi(int i, int j) {
		return multinomial_var_params[i].phi.get(j);
	}

	public double getLogPhi(int i, int j) {
		return multinomial_var_params[i].log_phi.get(j);
	}

	public double getCountValue(int i, int j) {
		return getLogPhi(i, j);
	}

	public void setCountValue(int i, int j, double v) {
		setCount(i, j, Math.exp(v));
	}

	public double getCount(int i, int j) {
		return multinomial_var_params[i].C.get(j);
	}

	public double getZeta(int i) {
		return multinomial_var_params[i].zeta;
	}

	public void setZeta(int i, double v) {
		multinomial_var_params[i].zeta = v;
	}

	public void setNu(int i, int j, double v) {
		multinomial_var_params[i].nu.set(j, v);
	}

	public void setLambda(int i, int j, double v) {
		multinomial_var_params[i].lambda.set(j, v);
	}

	public void setPhi(int i, int j, double v) {
		multinomial_var_params[i].phi.set(j, v);
	}

	public void setLogPhi(int i, int j, double v) {
		multinomial_var_params[i].log_phi.set(j, v);
	}

	public void setCount(int i, int j, double v) {
		multinomial_var_params[i].C.set(j, v);
	}

	public double get_logZ() {
		return Z;
	}

	public void set_logZ(double v) {
		Z = v;
	}

	public void updateCounts() {
		inside_outside.getInsideOutside(doc, this);
	}

	// starting from here is from inference.cpp

	public static void initTempVectors(LogisticNormalModel model) {
		temp = new DoubleMatrix1D[model.multinomCount()][];

		for (int i = 0; i < model.multinomCount(); i++) {
			temp[i] = new DoubleMatrix1D[4];
			for (int j = 0; j <= 3; j++) {
				temp[i][j] = DoubleFactory1D.dense.make(model.multinomSize(i));
			}
		}
	}

	private static DoubleMatrix1D[][] temp;

	void df_lambda(int i, DoubleMatrix1D p, DoubleMatrix1D df) {
		DoubleMatrix1D temp0 = temp[i][0];
		DoubleMatrix1D temp1 = temp[i][1];
		DoubleMatrix1D temp3 = temp[i][3];

		// compute \Sigma^{-1}(\mu - \lambda)A
		blas.dscal(0.0, temp0);
		blas.dcopy(model.mu(i), temp1);
		blas.daxpy(-1.0, p, temp1);

		/*
		 * for (int j=0; j<p.size(); j++) {
		 * System.err.println(" e["+i+","+j+"] = "+ p.get(j)); }
		 */

		// gsl_blas_dsymv(CblasLower, 1, mod->inv_cov(i), temp1, 0, temp0);

		blas.dsymv(true, 1.0, model.inverseCovariance(i), temp1, 0, temp0);

		/*
		 * for (int j=0; j<temp1.size(); j++) {
		 * System.err.println("t1["+i+","+j+"]="+temp1.get(j)); }
		 */

		// This function works on a whole lambda vector for each
		// multinomial.
		// This means that "i" is known and is the discussed multinomial

		// params should contain the relevant multinomial

		// compute /dlambda_j = C(j) - [sum_j C(j)] exp(lambda_j + nu^2_j)/zeta

		/*
		 * double v_invariant = 0;
		 * 
		 * for (int jprime = 0; jprime < temp3.size(); jprime++) {
		 * System.err.println("count = "+getCount(i, jprime));
		 * System.err.println("zeta = "+getZeta(i)); System.err.println("nu = "
		 * + getNu(i, jprime)); System.err.println("p = "+p.get(jprime));
		 * v_invariant = (getCount(i, jprime) / getZeta(i)) *
		 * Math.exp(p.get(jprime) + (0.5) * getNu(i, jprime)); }
		 * 
		 * 
		 * for (int j = 0; j < temp3.size(); j++) { double v = getCount(i, j); v
		 * = v - v_invariant;
		 * 
		 * temp3.set(j, v); }
		 */

		double c = 0.0;
		for (int j = 0; j < temp3.size(); j++) {
			c += getCount(i, j);
		}

		c = c / getZeta(i);

		for (int j = 0; j < temp3.size(); j++) {
			temp3.set(
					j,
					getCount(i, j) - c
							* (FastMath.exp(p.get(j) + 0.5 * getNu(i, j))));
		}

		blas.dscal(0.0, df);
		blas.daxpy(-1.0, temp0, df);
		blas.daxpy(-1.0, temp3, df);
	}

	public double f_lambda(int i, DoubleMatrix1D p) {
		// This function works on a whole lambda vector for each
		// multinomial.
		// This means that "i" is known and is the discussed multinomial

		double term2, term3;

		DoubleMatrix1D temp1 = temp[i][1];
		DoubleMatrix1D temp2 = temp[i][2];

		// compute lambda - mu (= temp1)
		blas.dcopy(p, temp1);
		blas.daxpy(-1.0, model.mu(i), temp1);

		blas.dsymv(true, 1, model.inverseCovariance(i), temp1, 0, temp2);

		term2 = blas.ddot(temp2, temp1);
		// gsl_blas_dgemv(CblasNoTrans, 1, mod->inv_cov, temp[1], 0, temp[2]);
		term2 = -0.5 * term2;

		// last term
		term3 = 0;

		double alpha = 0;

		for (int jprime = 0; jprime < multinom(i).N(); jprime++) {
			alpha += FastMath.exp(p.get(jprime) + (0.5) * getNu(i, jprime));
		}

		for (int j = 0; j < multinom(i).N(); j++) {
			term3 += (p.get(j) - alpha / getZeta(i)) * getCount(i, j);
		}

		// System.err.println("f = "+ (term2+term3));
		// negate for minimization
		return (-(term2 + term3));
	}

	public void optZeta(int i) {
		// printf("optimizing zeta_%d\n", i);
		// notice the context i
		setZeta(i, 0.0);
		for (int j = 0; j < multinom(i).N(); j++) {
			setZeta(i,
					getZeta(i)
							+ Math.exp(getLambda(i, j) + (0.5) * getNu(i, j)));
		}
	}

	public void optLambda(int i) {
		NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
				ConjugateGradientFormula.FLETCHER_REEVES);

		double[] startingPoint = new double[model.multinomSize(i)];

		for (int j = 0; j < startingPoint.length; j++) {
			startingPoint[j] = getLambda(i, j);
		}

		/*
		 * for (int j = 0; j < multinom(i).N(); j++) {
		 * System.err.println("i["+i+","+j+"] = "+startingPoint[j]); }
		 */

		try {
			RealPointValuePair pair = optimizer
					.optimize(
							new LogisticNormalLambdaFunction(i, this, model
									.multinomSize(i)), GoalType.MINIMIZE,
							startingPoint);
			double[] x = pair.getPointRef();
			for (int j = 0; j < multinom(i).N(); j++) {
				setLambda(i, j, x[j]);
				// System.err.println("x["+i+","+j+"] = "+x[j]+" old: " +
				// startingPoint[j]);
			}
		}

		catch (OptimizationException e) {
			System.err.println("Warning: caught optimization exception: " + e);
		}

		catch (FunctionEvaluationException e) {
			System.err
					.println("Warning: caught function evaluation exception: "
							+ e);
		}
	}

	public void initUniformly(LogisticNormalInsideOutsideInterface delegator) {

		for (int i = 0; i < model.multinomCount(); i++) {
			for (int j = 0; j < multinom(i).N(); j++) {
				// notice it uses params to decide where to take the logphi
				double logphi = delegator.getInitLogPhi(model, i, j);
				setLogPhi(i, j, logphi);
				setPhi(i, j, Math.exp(logphi));
			}

			setZeta(i, 10);
			int j;
			for (j = 0; j < multinom(i).N(); j++) {
				setNu(i, j, 10.0);
				setLambda(i, j, 0);
			}
			niter = 0;
			lhood = 0;
		}
		updateCounts(doc);
	}

	public void updateCounts(Document doc) {
		inside_outside.getInsideOutside(doc, this);
	}

	public void likelihoodBound() {
		double lhood = 0;
		for (int i = 0; i < model.multinomCount(); i++) {
			// E[log p(\eta | \mu, \Sigma)] + H(q(\eta | \lambda, \nu)

			lhood += (0.5) * model.logDetInverseCovariance(i) + (0.5)
					* (multinom(i).N());
			for (int j = 0; j < multinom(i).N(); j++) {
				double v = -(0.5) * getNu(i, j)
						* model.inverseCovariance(i).get(j, j);
				for (int jprime = 0; jprime < multinom(i).N(); jprime++) {
					v -= (0.5) * (getLambda(i, j) - model.mu(i).get(j))
							* model.inverseCovariance(i).get(j, jprime)
							* (getLambda(i, jprime) - model.mu(i).get(jprime));
				}
				v += (0.5) * Math.log(getNu(i, j));
				lhood += v;
			}

			// H(q(z | phi))

			for (int j = 0; j < multinom(i).N(); j++) {
				double v = getCount(i, j) * getLogPhi(i, j);
				lhood -= v;
			}

			// E[log p(z | \eta)]
			for (int j = 0; j < multinom(i).N(); j++) {
				double v = 0;
				for (int jprime = 0; jprime < multinom(i).N(); jprime++) {
					v += Math.exp(getLambda(i, jprime) + (0.5)
							* getNu(i, jprime));
				}
				v = (-1.0 / getZeta(i)) * v;
				v = v + getLambda(i, j) + 1 - Math.log(getZeta(i));
				v = v * getCount(i, j);
				lhood += v;
			}

		}

		// addendum to H(q(z | phi))
		lhood += get_logZ();

		this.lhood = lhood;
		if (Double.isNaN(lhood)) {
			System.err.println("Warning: likelihood is NaN.");
		}
	}

	double f_nu_ij(double nu_ij, int i, int j, double c) {
		double v;

		v = -(nu_ij * model.inverseCovariance(i).get(j, j) * 0.5);

		v = v - (1.0 / getZeta(i)) * c * Math.exp(getLambda(i, j) + nu_ij / 2);

		v = v + 0.5 * safeLog(nu_ij);

		return v;
	}

	double df_nu_ij(double nu_ij, int i, int j, double c) {
		double v;

		v = -(model.inverseCovariance(i).get(j, j) * 0.5);

		v = v - (0.5 / getZeta(i)) * c * Math.exp(getLambda(i, j) + nu_ij / 2);

		v = v + 0.5 * (1 / nu_ij);

		return v;
	}

	double d2f_nu_ij(double nu_ij, int i, int j, double c) {
		double v;

		v = -(0.25 / getZeta(i)) * c * Math.exp(getLambda(i, j) + nu_ij / 2);

		v = v - 0.5 * (1 / (nu_ij * nu_ij));

		return v;
	}

	void optNu(int i) {
		double c = 0.0;

		for (int jprime = 0; jprime < multinom(i).N(); jprime++) {
			c += getCount(i, jprime);
		}

		// System.err.println("c = " + c);

		for (int j = 0; j < multinom(i).N(); j++) {
			opt_nu_ij(i, j, c);
		}
	}

	double fixed_point_iter_ij(int i, int j) {
		double v;
		double lambda = getLambda(i, j);
		double nu = getNu(i, j);
		double c = 0.0;
		for (int jprime = 0; jprime < multinom(i).N(); jprime++) {
			c += getCount(i, jprime);
		}

		c = (1.0 / getZeta(i)) * c;

		v = model.inverseCovariance(i).get(j, j) + c
				* Math.exp(lambda + nu / 2);

		return v;
	}

	void opt_nu_ij(int i, int j, double c) {
		double init_nu = 10;
		double nu_i = 0, log_nu_i = 0, df = 0, d2f = 0;
		int iter = 0;

		log_nu_i = Math.log(init_nu);
		do {
			iter++;
			nu_i = Math.exp(log_nu_i);
			if (Double.isNaN(nu_i)) {
				init_nu = init_nu * 2;
				System.err
						.println("warning : nu is nan; new init = " + init_nu);
				log_nu_i = safeLog(init_nu);
				nu_i = init_nu;
			}
			df = df_nu_ij(nu_i, i, j, c);
			d2f = d2f_nu_ij(nu_i, i, j, c);
			log_nu_i = log_nu_i - (df * nu_i) / (d2f * nu_i * nu_i + df * nu_i);
		} while ((Math.abs(df) > /* NEWTON_THRESH */1e-8) && (iter < 100));

		setNu(i, j, Math.exp(log_nu_i));
	}

	double inference() {
		double lhood_old = 0;
		double convergence;

		do {
			niter++;

			for (int i = 0; i < multinomCount(); i++) {
				if ((niter == 1)
						|| (inside_outside.multinomialParticipates(doc, i))) {
					optZeta(i);
					optLambda(i);
					optZeta(i);
					optNu(i);
					optZeta(i);
				}
			}
			optPhi();

			lhood_old = lhood;
			likelihoodBound();

			convergence = Math.abs((lhood_old - lhood) / lhood_old);

			// if ((lhood_old - lhood > Math.abs(lhood) * 0.01) && (niter > 1))
			// System.err.println("Warning: iter " + niter + " " + lhood_old
			// + " > " + lhood);
		} while ((convergence > LogisticNormalParameters.varConvergence)
				&& ((LogisticNormalParameters.varMaxIteration < 0) || (niter < LogisticNormalParameters.varMaxIteration)));

		if (convergence > LogisticNormalParameters.varConvergence)
			converged = 0;
		else
			converged = 1;

		return lhood;
	}

	// just set q(z) to the corresponding log-linear model by setting
	// all the "unnormalized multinomials" in var
	void optPhi() {
		// i is the relevant multinomial
		for (int i = 0; i < multinomCount(); i++) {
			// compute zeta_i^-1 * \sum_j exp(lambda_i,j+nu_i,j^2) + (1-log
			// zeta_i)
			double zeta_term = 0;
			for (int j = 0; j < multinom(i).N(); j++) {
				zeta_term = zeta_term
						+ Math.exp(getLambda(i, j) + (0.5) * getNu(i, j));
			}
			zeta_term = zeta_term / getZeta(i) - 1 + Math.log(getZeta(i));

			// compute lambda_i,j - zeta_term for each j and
			// set that to be the phi
			double alpha;
			for (int j = 0; j < multinom(i).N(); j++) {
				alpha = getLambda(i, j) - zeta_term;
				setPhi(i, j, Math.exp(alpha));
				setLogPhi(i, j, alpha);
			}
		}

		updateCounts();
	}

	void updateExpectedSS(int i, LogisticNormalSufficientStatistics ss) {
		double lilj;

		// covariance and mean suff stats
		for (int j = 0; j < ss.size(i); j++) {
			ss.incMuSS(i, j, getLambda(i, j));

			for (int jprime = 0; jprime < ss.size(i); jprime++) {
				lilj = getLambda(i, j) * getLambda(i, jprime);
				if (j == jprime) {
					ss.incCovSS(i, j, jprime, getNu(i, j) + lilj);
				} else {
					ss.incCovSS(i, j, jprime, lilj);
				}
			}
		}
	}

	void updateExpectedSS(LogisticNormalSufficientStatistics ss) {
		for (int i = 0; i < multinomCount(); i++) {
			updateExpectedSS(i, ss);
		}

		// number of data
		ss.ndata++;
	}
}
