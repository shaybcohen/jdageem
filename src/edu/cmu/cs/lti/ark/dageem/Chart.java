/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

//import java.util.HashMap;
import cern.colt.map.OpenIntObjectHashMap;
import org.apache.commons.math.util.FastMath;
import java.util.HashSet;

/**
 * @author scohen
 * 
 */

public abstract class Chart {

	/*
	 * private HashMap<Integer, Term> chart; private HashMap<Integer,
	 * BacktrackChain> backtrack;
	 */
	private OpenIntObjectHashMap chart;
	private OpenIntObjectHashMap backtrack;

	private boolean multBt;

	public static Semiring semiring = new Semiring();

	/*
	public static class Semiring {
		public static final double Zero = java.lang.Double.NEGATIVE_INFINITY;
		public static final double One = 0;
		public static final int Semiring_Max = 1;
		public static final int Semiring_LogReal = 2;
		public static int semiring = Semiring_Max;

		public static int getSemiring()
		{
			return semiring;
		}
		
		public static void setSemiring(int s)
		{
			semiring = s;
		}
		
		public static void setSemiringLogReal() {
			semiring = Semiring_LogReal;
		}

		public static double convertToSemiring(double v) {
			return v;
		}

		public static double convertFromSemiring(double v) {
			return v;
		}

		public static void setSemiringMax() {
			semiring = Semiring_Max;
		}

		public static boolean Idempotent() {
			return (semiring == Semiring_Max);
		}

		public static double Times(double v1, double v2) {
			return (v1 + v2);
		}

		public static double Plus(double v1, double v2) {
			if (semiring == Semiring_Max) {
				if (v1 > v2) {
					return v1;
				} else {
					return v2;
				}
			}

			return LogSum(v1, v2);
		}

		public static double LogMinus(double lx, double ly) {

			if (ly == Zero || lx - ly > 700.0)
				return lx;
			if (lx == Zero || ly - lx > 700.0) {
				System.err
						.println("Warning: LogMinus gives a negative answer: lx="+lx+" ly="+ly);
				return Zero;
			}

			if (lx > ly) {
				return FastMath.log1p(-FastMath.exp(ly - lx)) + lx;
			} else {
				System.err.println("Warning: LogMinus gives a negative answer: lx="+lx+" ly="+ly);
				return Zero;
			}
		}

		public static double LogDivide(double v1, double v2) {
			return (v1 - v2);
		}

		public static double ETimes(double p1, double p2, double r1, double r2) {
			double v = LogSum(Times(p1, r2), Times(p2, r1));

			return v;
		}

		public static double LogSum(double lx, double ly) {
			if (lx == Zero)
				return ly;
			if (ly == Zero)
				return lx;
			double d = lx - ly;
			if (d >= 0) {
				if (d > 745)
					return lx;
				else
					return lx + FastMath.log1p(FastMath.exp(-d));
			} else {
				if (d < -745)
					return ly;
				else
					return ly + FastMath.log1p(FastMath.exp(d));
			}
		}

		public static double exp(double val) {
			final long tmp = (long) (1512775 * val + 1072632447);
			return Double.longBitsToDouble(tmp << 32);
		}
	}*/

	public boolean isPriorityModeNormal()
	{
		return true;
	}
	
	public void clear() {
		chart = new OpenIntObjectHashMap();
		backtrack = new OpenIntObjectHashMap();
	}

	public void setMultipleBacktracks(boolean b) {
		multBt = b;
	}

	public boolean hasMultipleBacktracks() {
		return multBt;
	}

	public Chart() {
		clear();

		multBt = false;
		// clear(true);
		/*
		 * callbacks = NULL; symbols = NULL;
		 * 
		 * setBacktrackSize(1); addToCutsFlag = true; verboseLevel = 0;
		 */
	}

	public double getPriority(Term t, double v, Object ev) {

		/*
		 * if (Chart.Semiring.semiring == Chart.Semiring.Semiring_LogReal){
		 * return ((EValue)ev).p(); }
		 */

		return v;
	}

	protected double getDefaultPriority(Term t, double v, Object ev) {
		return v;
	}

	void removeTerm(Term t) {
		int h = t.hashCode();
		removeTerm(h);
	}

	void removeTerm(int h) {

		Term t = (Term) chart.get(h);

		if (chart.removeKey(h)) {
			removeFromCuts(h, t);
		}
	}

	public abstract void removeFromCuts(int h, Term t);

	public abstract void addToCuts(Term t);

	public void addTerm(Term t) {

		/*
		 * if (chart.containsKey(t.hashcode())) { if
		 * (!t.toString().equals(chart.get(t.hashcode()).toString())) {
		 * System.err
		 * .println("collision between "+t.toString()+" and "+chart.get
		 * (t.hashcode())); } }
		 */
		chart.put(t.hashcode(), t);
		addToCuts(t);
	}

	public Term getTerm(int h) {
		return (Term) chart.get(h);
	}

	public boolean hasTerm(int h) {
		return chart.containsKey(h);
	}

	public double currVal(int h) {
		if (!hasTerm(h)) {
			return Semiring.Zero;
		} else {
			return getTerm(h).value();
		}
	}

	public BacktrackChain getBacktrack(int h) {
		return (BacktrackChain) backtrack.get(h);
	}

	public HashSet<BacktrackChain> getBacktracks(int h) {
		return (HashSet<BacktrackChain>) backtrack.get(h);
	}

	public void addBacktrack(int h, BacktrackChain bt, double value) {
		if (multBt) {
			HashSet<BacktrackChain> set = (HashSet<BacktrackChain>) backtrack
					.get(h);
			if (set == null) {
				set = new HashSet<BacktrackChain>();
				backtrack.put(h, set);
			}

			set.add(bt);
		} else {
			backtrack.put(h, bt);
		}
	}
}
